#!/usr/bin/perl -w

use strict;
my $db = '';
my $dbhost = '';
my @files = ();
my $dump_as;
my $conf_file;
my $metadata_file;
my $user = $ENV{USER};
my $reasoner;
my $notes;
my $use_pgschema = 0;
my $drop;
if (!@ARGV) {
    printusage();
    exit(0);
}
while (my $opt = shift @ARGV) {
    if ($opt =~ /^\-/) {
        if ($opt eq '-d' || $opt eq '--database') {
            $db = shift @ARGV;
        }
        elsif ($opt eq '--help') {
            printusage();
            exit(0);
        }
        elsif ($opt eq '-h' || $opt eq '--host' || $opt eq '--dbhost') {
            $dbhost = shift @ARGV;
        }
        elsif ($opt eq '-c' || $opt eq '--conf') {
            $conf_file = shift @ARGV;
        }
        elsif ($opt eq '--metadata') {
            $metadata_file = shift @ARGV;
        }
        elsif ($opt eq '--reasoner') {
            $reasoner = shift @ARGV;
        }
        elsif ($opt eq '--notes') {
            $notes = shift @ARGV;
        }
        elsif ($opt eq '--dump') {
            $dump_as = $db . ".pgdump";
        }
        elsif ($opt eq '--dump_as') {
            $dump_as = shift @ARGV;
        }
        elsif ($opt eq '--drop') {
            $drop = 1;
        }
        elsif ($opt eq '--use_pgschema') {
            $use_pgschema = 1;
        }
        else {
            die "unknown option: $opt";
        }
    }
    else {
        if (!$db) {
            $db = $opt;
        }
        else {
            push(@files, $opt);
        }
    }

}

if (!$db) {
    printusage();
    exit(0);
}

my $args = $dbhost ? "-h $dbhost $db" : $db;
my $dir = `dirname $0`;
chomp $dir;
my $sqldir = "$dir/../sql";
printf STDERR "psql args: $args\n";
run("dropdb $args", 
    sub { print STDERR "DID NOT DROP BECAUSE $db DID NOT EXIST\n" }) 
    if $drop;
run("createdb $args");
run("psql -c 'CREATE LANGUAGE plpgsql' $args", sub {print STDERR "Error creating language plpgsql: may not be fatal if language already exists\n";}); #don't exit
run("psql $args < $sqldir/obd-core-schema.sql ");
#run("psql $args < $sqldir/obd-core-views.sql ");
useddl("$sqldir/obd-core-views.sql");
useddl("$sqldir/obd-simple-views.sql");
useddl("$sqldir/obd-util-funcs.sql");
useddl("$sqldir/obd-matview-funcs.sql");

run("psql -c 'INSERT INTO obd_schema_metadata (notes) VALUES(\"$notes\")' $args")
  if $notes;

#run("psql $args < $sqldir/obd-core-functions.plpgsql "); # todo: move
useddl("$sqldir/obd-core-functions.plpgsql"); # todo: move
useddl("$sqldir/api/obd-mutable-api.plpgsql");
useddl("$sqldir/util/realize-relations-as-views.plpgsql");

run("echo 'ALTER USER $user SET search_path TO public,obd_statistical_view,obd_aggregate_view,obd_annotation_view,obd_prejoins_view, obd_obo_metamodel_view,obd_core_view,obd_denormalized;' | psql $args");

if ($conf_file) {
    my $dir = `dirname $conf_file`;
    chomp $dir;
    open(F,$conf_file) || die $conf_file;
    while(<F>) {
        chomp;
        if (/^http:/ || /^file:/) {
            if (/^file:\.\/(.*)/) {
                $_ = "file:$dir/$1";
            }
            push(@files, $_);
        }
        else {
            push(@files,"http://purl.org/obo/obo-all/$_/$_.obo");
        }
    }
    close(F);
}

my $dbiargs = $dbhost ? "-h $dbhost -d $db" : "-d $db";
foreach my $file (@files) {
    run("obd-load-db-from-obo.pl $dbiargs $file");
}

if ($reasoner && $reasoner eq 'perl') {
    run("psql $dbiargs -c 'VACUUM FULL ANALYZE'");
    run("obd-reasoner.pl $dbiargs");
}

if ($dump_as) {
    run("pg_dump $args > $dump_as && gzip --force $dump_as");
}
print STDERR "Done!\n";
exit 0;

sub useddl {
    my $ddlf = shift;
    if ($use_pgschema) {
        # warning - position sensitive!
        run("perl -npe 's/^.. CREATE SCHEMA/CREATE SCHEMA/;s/^.. SET search_path/SET search_path/'  $ddlf | psql $args ");
    }
    else {
        run("cat $ddlf | psql $args ");
    }
}

sub run {
    my $cmd = shift;
    my $onfail = shift;
    print STDERR "CMD: $cmd\n";
    my $err = system($cmd);
    if ($err) {
        if ($onfail) {
            $onfail->($err);
        }
        else {
            print STDERR "Error $err in: $cmd\n";
            exit(1);
        }
    }
}

sub printusage {
    print <<EOM
obd-create-db.pl [-h <HOST>] -d <DBNAME> [-c <CONF-FILE>] [--reasoner perl] [OBO_FILE...]

Creates a Pg database with the OBD schema, views and functions
Optionally loads additional sources.

The conf file consits of newline separated resource IDs. You dont need to specify obo files
on the command line if you use a conf file. See the conf/ directory for examples

EOM
;
    
}
