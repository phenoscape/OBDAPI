#!/usr/bin/perl -w
use strict;
use DBI;
use FileHandle;

# TODO: replace inferred links when ontology changes (just get rid of inferences with any dependencies on updated ontology?)
# TODO: keep other properties in inference; eg reiflink??

# TODO: more robust w.r.t cycles; eg MP-XP-towards, FMA-has_part

my $d;
my $dbhost = '';
my $use_reasoner = 1;
my $dump;
my $split;
my $delete;
my $limit = 50000;
my $source;
my %skip = ();
my $verbose = 0;
my %ruleconf = ();
my $test_intersection;
my $infer_instance_of;
my @inference_views = ();
my $pred;
while (@ARGV && $ARGV[0] =~ /^\-/) {
    my $opt = shift @ARGV;
    if ($opt eq '-d' || $opt eq '--database') {
        $d = shift @ARGV;
    }
    elsif ($opt eq '-v' || $opt eq '--verbose') {
        $verbose = 1;
    }
    elsif ($opt eq '-h' || $opt eq '--host') {
        $dbhost = shift @ARGV;
    }
    elsif ($opt eq '-?' || $opt eq '--help') {
        print usage();
        exit 0;
    }
    elsif ($opt eq '-p' || $opt eq '--predicate') {
        $pred = shift @ARGV;
    }
    elsif ($opt eq '--inst') {
        $infer_instance_of = 1;
    }
    else {
        die $opt;
    }
}
if (!$d) {
    $d = shift @ARGV;
}
if (!$d) {
    print usage();
    exit(1);
}
if ($dbhost) {
    $d  = "$d\@$dbhost";
}

my $time_started = time;

my $dbh;
if ($d =~ /^dbi:/) {
    $dbh = DBI->connect($d);
}
elsif ($d =~ /^jdbc:postgresql:\/\/(.*):(\d+)/) {
    $dbh = DBI->connect("dbi:Pg:dbname=$d;host=$1;port=$2");
}
elsif ($d =~ /\@/) {
    require 'DBIx::DBStag';
    $dbh = DBIx::DBStag->connect($d);
}
else {
    $dbh = DBI->connect("dbi:Pg:dbname=$d");
}

printf STDERR "Finding candidates\n";
my $candidate_sql = "SELECT DISTINCT node_class_id,predicate_id,object_class_id FROM instance_link_class_pair";
if ($pred) {
    $candidate_sql .= " WHERE predicate_id IN (SELECT node_id FROM node WHERE uid='$pred')";
}
my $candidates =
  $dbh->selectall_arrayref($candidate_sql);
my $num = scalar(@$candidates);
printf STDERR "Found $num candidates\n";

my $i=0;
foreach my $candidate (@$candidates) {
    $i++;
    if ($i % 1000 == 0) {
        printf STDERR "Checked $i (%d % done)\n", ($i/num) * 100;
    }
    my ($nc_id,$pred_id,$oc_id) = @$candidate;
    my $xps = $dbh->selectall_arrayref("SELECT * FROM instance_LCA_tuple WHERE node_class_id=$nc_id AND object_class_id=$oc_id AND predicate_id=$pred_id");
    foreach my $xp (@$xps) {
        #printf STDERR "xp: @$xps\n";
    }
    if (@$xps) {
        my $nc = info($nc_id);
        my $oc = info($oc_id);
        my $p = info($pred_id);
        my $xp_ids = $dbh->selectcol_arrayref("SELECT store_genus_differentium($nc_id,$pred_id,$oc_id, get_node_id('BIRN:generic_annotation'))");
        print STDERR "OK: $nc --[$p]--> $oc (IDS: @$candidate ) xp_ids: @$xp_ids\n";
    }
    else {
        #print STDERR "REDUNDANT: ";
    }
        
}

exit 0;

sub info {
    my @info = $dbh->selectrow_array("SELECT uid,label FROM node WHERE node_id=@_");
    "@info";
}
