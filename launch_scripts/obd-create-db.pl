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
while (my $opt = shift @ARGV) {
    if ($opt =~ /^\-/) {
        if ($opt eq '-d' || $opt eq '--database') {
            $db = shift @ARGV;
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
        elsif ($opt eq '--dump') {
            $dump_as = $db . ".pgdump";
        }
        elsif ($opt eq '--dump_as') {
            $dump_as = shift @ARGV;
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

my $args = $dbhost ? "-h $dbhost $db" : $db;
my $dir = `dirname $0`;
chomp $dir;
my $sqldir = "$dir/../sql";
printf STDERR "psql args: $args\n";
run("dropdb $args");
run("createdb $args");
run("psql $args < $sqldir/obd-core-schema.sql ");
#run("psql $args < $sqldir/obd-core-views.sql ");
run("grep -v ^SET $sqldir/obd-core-views.sql | grep -v SCHEMA | psql $args ");

run("psql -c 'CREATE LANGUAGE plpgsql' $args");

run("psql $args < $sqldir/obd-core-functions.plpgsql "); # todo: move
run("psql $args < $sqldir/api/obd-mutable-api.plpgsql ");
run("psql $args < $sqldir/util/realize-relations-as-views.plpgsql ");

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
    run("obd-reasoner.pl $dbiargs");
}

if ($dump_as) {
    run("pg_dump $args > $dump_as && gzip --force $dump_as");
}
print STDERR "Done!\n";
exit 0;

sub run {
    my $cmd = shift;
    print STDERR "CMD: $cmd\n";
    if (system($cmd)) {
        print STDERR "Error in: $cmd\n";
    }
}
