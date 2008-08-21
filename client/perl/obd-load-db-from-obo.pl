#!/usr/bin/perl

use DBIx::DBStag;
use strict;

my $d;
my $dbhost = '';
my $use_reasoner = $ENV{OBD_USE_OBOEDIT_REASONER} || 0;
my $reasoner = 'oboedit';
my $dump;
my $split;
my $username;
my $password;
my $nodelete = 1;
my @parse_args = ();
while ($ARGV[0] =~ /^\-/) {
    my $opt = shift @ARGV;
    if ($opt eq '-d' || $opt eq '--database') {
        $d = shift @ARGV;
    }
    elsif ($opt eq '--noreasoner') {
        $use_reasoner = 0;
    }
    elsif ($opt eq '--reasoner') {
        $use_reasoner = 1;
    }
    elsif ($opt eq '-h' || $opt eq '--host') {
        $dbhost = shift @ARGV;
    }
    elsif ($opt eq '--dump') {
        $dump = 1;
    }
    elsif ($opt eq '--nodelete') {
        $nodelete = 1;
    }
    elsif ($opt eq '--delete') {
        $nodelete = 0;
    }
    elsif ($opt eq '--split') {
        $split = shift @ARGV;
    }
    elsif ($opt eq '--parg' || $opt eq '-p') {
        push(@parse_args, shift @ARGV);
    }
	elsif ($opt eq '-u' || $opt eq '--user' || $opt eq '--username') {
		$username = shift @ARGV;
	}
	elsif ($opt eq '-p' || $opt eq '--pass' || $opt eq '--password') {
		$password = shift @ARGV;
	}
    else {
        die $opt;
    }
}
if (!$d) {
    $d = shift @ARGV;
}
my $dbname = $d;
if ($d !~ /^jdbc:/) {
    if ($dbhost) {
        $d = "jdbc:postgresql://$dbhost:5432/$d";
     }
    else {
        $d = "jdbc:postgresql://localhost:5432/$d";
    }
}

my $OBO2DATABASE = "obo2database";
if (system("which $OBO2DATABASE")) {
    my $dir = `dirname $0`;
    chomp $dir;
    my $scriptdir = "$dir/../launch_scripts";
    if (! (-d $scriptdir)) {
        $scriptdir = "$dir/../../launch_scripts";
    }
    $OBO2DATABASE = "$scriptdir/obo2database";
}

my @files = @ARGV;
print STDERR "Loading\n";

foreach my $file (@files) {
    # only deletes if asked..
    unless ($nodelete) {
        clear_previous($file);
    }
    print STDERR "Converting File: $file\n";
    my $is_tmp = 0;
    if ($file =~ /^http:.*\/(.*)$/) {
        my $nu = $1;
        run("wget -O $nu $file");
        $file=$nu;
        $is_tmp=1;
    }
    elsif ($file =~ /^file:(.*)$/) {
        $file = $1;
    }
    my $ifile = "$file.implied.obo";
    if ($use_reasoner) {
        if ($reasoner eq 'oboedit') {
            run("obo2obo -o -saveallimpliedlinks $ifile $file");
        }
    }
    else {
        $ifile = $file;
    }
    print STDERR "Loading File: $ifile\n";
    if ($split) {
        run("obo-split.pl -s $split -x go2fmt.pl -x 'oboxml_to_obd_prestore - | stag-storenode.pl -p xml -cache node=1 -d $d -' \\; $ifile");
    }
    else {
        #run("go2fmt.pl @parse_args -x oboxml_to_obd_prestore $ifile | stag-storenode.pl -p xml -cache node=1 -d $d -");
        #run("obo2database  @parse_args -allowdangling -o $d $ifile");
		$d .= " -u $username" if $username;
		$d .= " -p $password" if $password;
		run("$OBO2DATABASE  @parse_args -allowdangling -o $d $ifile");
    }
    if ($is_tmp) {
        unlink($file);
    }
    print STDERR "Loaded File: $ifile\n"; 

    if ($use_reasoner && $reasoner eq 'perl') {
        run("obd-reasoner.pl --host $dbhost -d $dbname");
    }
   
}
if ($dump) {
    my $dumpfile = "$d-obd-sql.sql";
    print STDERR "Dumping $dumpfile\n";
    run('pg_dump `stag-connect-parameters.pl '.$d.'` > '.$dumpfile." && gzip --force $dumpfile");
}
exit 0;

sub run {
    print STDERR "Running: @_\n";
    #system("@_");
	if (system("@_")) {
		system("./" . "@_");
	}
}

sub clear_previous {
    my $f = shift;
    print STDERR "Clearing: $f\n";
    my %ns = ();
    my $dbh = DBIx::DBStag->connect($d);
    open(F,$f);
    while(<F>) {
        chomp;
        if (/^namespace:\s*(\S+)/) {
            $ns{$1} = 1;
        }
        if (/^default-namespace:\s*(\S+)/) {
            $ns{$1} = 1;
        }
    }
    close(F);
    foreach my $source (keys %ns) {
        print STDERR "source: $source\n";
        my ($source_id) = $dbh->selectrow_array("SELECT node_id FROM node WHERE uid='$source'");
        return unless $source_id;
        foreach my $table (qw(link sameas tagval alias description)) {
            my $sql = "DELETE FROM $table WHERE source_id=$source_id";
            print STDERR "SQL: $sql\n";
            $dbh->do($sql);
        }
        foreach (qw(node_id predicate_id object_id)) {
            $dbh->do("DELETE FROM link WHERE is_inferred='t' AND node_id IN (SELECT node_id FROM node WHERE source_id=$source)")
        }
    }
}
