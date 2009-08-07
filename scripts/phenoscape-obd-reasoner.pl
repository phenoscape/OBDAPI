#!/usr/bin/perl -w
use strict;
use DBI;
use FileHandle;

my $d;
my $dbhost = '';
my $uid;
my $pwd;
my $port;
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
while (@ARGV && $ARGV[0] =~ /^\-/) {
    my $opt = shift @ARGV;
    if ($opt eq '-d' || $opt eq '--database') {
        $d = shift @ARGV;
    }
    elsif ($opt eq '--uid' || $opt eq '-U') {
        $uid = shift @ARGV;
    }
    elsif ($opt eq '--pwd') {
        $pwd = shift @ARGV;
    }
    elsif ($opt eq '--port') {
        $port = shift @ARGV;
    }
    elsif ($opt eq '--noreasoner') {
        $use_reasoner = 0;
    }
    elsif ($opt eq '--dump') {
        $dump = 1;
    }
    elsif ($opt eq '--delete') {
        $delete = 1;
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
    elsif ($opt eq '--split') {
        $split = shift @ARGV;
    }
    elsif ($opt eq '--test-intersection') {
        $test_intersection = shift @ARGV;
    }
    elsif ($opt eq '--source') {
        $source = shift @ARGV;
    }
    elsif ($opt eq '--skip') {
        $skip{shift @ARGV}=1;
    }
    elsif ($opt eq '--rule') {
        $ruleconf{shift @ARGV}=1;
    }
    elsif ($opt eq '--view') {
        push(@inference_views, shift @ARGV);
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
    die "No database specified\n";
    exit(1);
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
    $dbh = DBI->connect("dbi:Pg:dbname=$d;host=$dbhost",$uid,$pwd);
}

if ($delete) {
    delete_inferred_links();
}

my @views;

{
my @quality_ontology_nodes = $dbh->selectrow_array("SELECT node_id FROM node WHERE uid = 'quality'");
my @character_slim_ontology_nodes = $dbh->selectrow_array("SELECT node_id FROM node WHERE uid = 'character_slims'");

if(@quality_ontology_nodes != 1){
	die "No quality ontology node found";
}

if(@character_slim_ontology_nodes != 1){
	die "No character slim ontology node found";
}

my $quality_ontology_id = shift @quality_ontology_nodes;
my $character_slim_ontology_id = shift @character_slim_ontology_nodes;

my @value_for_nodes =   $dbh->selectrow_array("SELECT node_id FROM node WHERE uid='PHENOSCAPE:value_for'");
if (@value_for_nodes != 1) {
    die "@value_for_nodes";
}
my $value_for = shift @value_for_nodes;

my $sql = qq[
		SELECT 
		node_id,
		$value_for AS predicate_id,
		getAttributeForQuality(node_id) AS object_id
		FROM node WHERE source_id IN ($quality_ontology_id, $character_slim_ontology_id)
	];	

#print STDERR "Creating state to character mapping with SQL: \n%\n", $sql;

push(@views,
	{id => 'value_for',
	 rule => "insubset(X, value_slim), is_a(X, Y), insubset(Y, character_slim) => value_for(X, Y)",
	 sql => $sql
	 });	
}
{
my @exhibits_nodes = $dbh->selectrow_array("SELECT node_id FROM node WHERE uid ='PHENOSCAPE:exhibits'");

if(@exhibits_nodes != 1){
	die "No exhibits node found";
}

my $exhibits = shift @exhibits_nodes;

my @isa_nodes = $dbh->selectrow_array("SELECT node_id FROM node WHERE uid ='OBO_REL:is_a'");
if(@isa_nodes != 1){
	die "@isa_nodes";
}

my $isa = shift @isa_nodes;

my $exhibits_sql = qq[
	SELECT DISTINCT
		suptaxon_node.node_id AS node_id, 
		exhibits_link.predicate_id AS predicate_id,
		phenotype_node.node_id AS object_id
	FROM 
		node AS taxon_node 
		JOIN link AS exhibits_link ON (exhibits_link.node_id = taxon_node.node_id) 
		JOIN node AS phenotype_node ON (exhibits_link.object_id = phenotype_node.node_id)
		JOIN link AS is_a_link ON (is_a_link.node_id = taxon_node.node_id)
		JOIN node AS suptaxon_node ON (is_a_link.object_id = suptaxon_node.node_id)
	WHERE
		is_a_link.predicate_id = $isa AND
		exhibits_link.predicate_id = $exhibits
	];
	
push(@views,
	{id => 'Balhoff rule',
	 rule => "is_a (A, B), exhibits (A, P) => exhibits(B, P)",
	 sql => $exhibits_sql
	 });	
}

my $sth_link = $dbh->prepare_cached("SELECT link_id FROM LINK WHERE node_id=? AND predicate_id=? AND object_id=?");
my $sth_store = $dbh->prepare_cached("INSERT INTO link (node_id,predicate_id,object_id,is_inferred) VALUES (?,?,?,'t')");

foreach my $view (@views) {
	my $links_added = cache_view($view);
}

my $time_finished = time;

printf STDERR "Started: %d Finished: %d Duration: %d\n", $time_started, $time_finished, $time_finished - $time_started;

exit 0;

# TODO: insert and select in same step; or temp table
sub cache_view {
    my $view = shift;
    my $view_links_added = 0;
    my $offset = 0;

    my $view_id = $view->{id};
    logmsg( "  View: $view_id" );
    my $done_with_view;
    while (!$done_with_view) {

        my $sql = $view->{sql};
        #$sql.= "ORDER BY x.link_id,y.link_id";
        #$sql.= " LIMIT $limit OFFSET $offset";
        my $sth = 
          $dbh->prepare_cached($sql);

        #logmsg( "    Executing [$offset,$limit]" );
        logmsg( "    Executing $sql" );
		#print STDERR "    Executing $sql \n";
        $sth->execute;
        logmsg( "    EXECUTED" );
        my $links_added = 0;
        my $links_in_db = 0;
        my $n_rows = 0;
        while (my $link = $sth->fetchrow_hashref) {
            $n_rows++;
            my @triple =
              ($link->{node_id},
               $link->{predicate_id},
               $link->{object_id});
            
            if (($triple[0] == $triple[2]) && ($view_id ne 'value_for')) {
                # TODO: proper reflexivity rules. hardcode OK for is_a for now
                # also: will report cycles for intersections to self, which is normal?
                #
                # Changed this to add reflexive links with 'value_for' predicate between characters: Cartik 05/08/09
                logmsg("    Cycle detected for node: $triple[0] pred: $triple[1]");
                next;
            }
            my $rv = $sth_link->execute(@triple);
            if ($n_rows % 1000 == 0) {
                logmsg("    Checked $n_rows links. Current: @triple");
            }
            if ($sth_link->fetchrow_array) {
                $links_in_db++;
            }
            else {
                #print STDERR "NEW @triple\n";
                $sth_store->execute( @triple);
                $links_added++;
            }
        }
        #$offset += $limit;
        $done_with_view=1 unless $links_added;
        $view_links_added += $links_added;
        logmsg( "    Links added: $links_added [in_view: $view_links_added] already_there: $links_in_db" );
    }
    return $view_links_added;
}

sub logmsg {
    return unless $verbose;
    my $msg = shift;
    my $t = time;
    print STDERR "LOG $t : $msg\n";
}
