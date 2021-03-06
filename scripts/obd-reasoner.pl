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
my %split;
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
        my $s = shift @ARGV;
        $split{$s} = 1;
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

if ($delete) {
    delete_inferred_links();
}

logmsg("ensuring link type is consistent with relation type");
$dbh->do("UPDATE link SET object_quantifier_some='f', is_metadata='t' WHERE predicate_id IN (SELECT node_id FROM node WHERE node.is_metadata='t')");

logmsg("getting is_a id");
my @is_a_nodes = 
  $dbh->selectrow_array("SELECT node_id FROM node WHERE uid='OBO_REL:is_a'");
if (@is_a_nodes != 1) {
    die "@is_a_nodes";
}
my $is_a = shift @is_a_nodes;
logmsg("is_a[node_id]=$is_a");

if (!@inference_views) {
    my $sql = "SELECT DISTINCT view FROM inference_rule";
    eval {
        @inference_views = $dbh->selectrow_array($sql);
    };
    if ($@) {
        print STDERR "Warning: could not execute: $sql\n";
        print STDERR "Consider upgrading your obd schema to include this table\n";
    }
}

logmsg("getting instance_of id");
my @instance_of_nodes = 
  $dbh->selectrow_array("SELECT node_id FROM node WHERE uid='OBO_REL:instance_of'");
if (@instance_of_nodes != 1) {
    if ($infer_instance_of) {
	die "expected 1 instance_of node. Got: @instance_of_nodes";
    }
}
my $instance_of = shift @instance_of_nodes;
logmsg("instance_of[node_id]=$instance_of");

# used in split
logmsg("getting transitive rels");
my @transitive_relation_node_ids = 
    @{$dbh->selectcol_arrayref("SELECT node_id FROM relation_node WHERE is_transitive='t'")};

# used in split
logmsg("getting inheritable rels");
my @inheritable_relation_node_ids = 
    @{$dbh->selectcol_arrayref("SELECT DISTINCT predicate_id FROM inheritable_link")};
logmsg("got inheritable rels = @inheritable_relation_node_ids");

my $lj=qq[
  LEFT JOIN  link AS existing_link
        ON (x.node_id=existing_link.node_id AND
            x.predicate_id=existing_link.predicate_id AND
            y.object_id=existing_link.object_id)
];
my $lj_cond = "AND existing_link.link_id IS NULL";

# TODO: transitive_over and relation compositions
my @views =
  (
   {id=>'transitivity',
    rule=>"transitive(R), A R B, B R C, => A R C",
    sql=>qq[
 SELECT DISTINCT
  x.node_id             AS node_id,
  x.predicate_id        AS predicate_id,
  y.object_id           AS object_id
 FROM inheritable_link              AS x
  INNER JOIN inheritable_link       AS y 
        ON (x.object_id=y.node_id AND x.predicate_id=y.predicate_id)
  INNER JOIN transitive_relation_node AS r 
        ON (x.predicate_id=r.node_id)
  $lj
 WHERE true
  $lj_cond
],
  },
{id=>'isa1',
    rule=>"A is_a B, B R C => A R C",
    sql=>qq[
 SELECT DISTINCT
  x.node_id             AS node_id,
  y.predicate_id        AS predicate_id,
  y.object_id           AS object_id
 FROM inheritable_link              AS x
  INNER JOIN inheritable_link       AS y ON (x.object_id=y.node_id)
  LEFT JOIN  link AS existing_link
        ON (x.node_id=existing_link.node_id AND
            y.predicate_id=existing_link.predicate_id AND
            y.object_id=existing_link.object_id)
 WHERE  x.predicate_id = $is_a
  AND y.object_quantifier_some='t'
  $lj_cond
],
   },
{id=>'isa2',
    rule=>"A R B, B is_a C => A R C",
    sql=>qq[
 SELECT DISTINCT
  x.node_id             AS node_id,
  x.predicate_id        AS predicate_id,
  y.object_id           AS object_id
 FROM inheritable_link              AS x
  INNER JOIN inheritable_link       AS y ON (x.object_id=y.node_id)
  $lj
 WHERE y.predicate_id = $is_a
  AND y.object_quantifier_some='t'
  $lj_cond
],
   },
   {id=>'isa*',
    rule=>"A is_a A: reflexivity", # we do not implement other reflexive relations for now
    sql=>qq[
 SELECT DISTINCT
  node.node_id          AS node_id,
  $is_a                 AS predicate_id,
  node.node_id          AS object_id
 FROM node
  LEFT JOIN  inheritable_link AS existing_link
        ON (node.node_id=existing_link.node_id AND
            $is_a=existing_link.predicate_id AND
            node.node_id=existing_link.object_id)
 WHERE node.metatype='C'
  $lj_cond
],
    },
   {id=>'subrelations',
    rule=>"A R B, R is_a R2 => A R2 B: reflexivity",
    sql=>qq[
 SELECT DISTINCT
  x.node_id          AS node_id,
  subrel.object_id               AS predicate_id,
  x.object_id        AS object_id
 FROM subrelation_link AS subrel
  INNER JOIN  inheritable_link AS x
        ON (subrel.node_id=x.predicate_id)
  LEFT JOIN  link AS existing_link
        ON (x.node_id=existing_link.node_id AND
            subrel.object_id=existing_link.predicate_id AND
            x.object_id=existing_link.object_id)
 WHERE true
  $lj_cond
],
    }

  );

if (%ruleconf) {
    @views = grep {$ruleconf{$_->{id}}} @views;
}
if (%skip) {
    @views = grep {!$skip{$_->{id}}} @views;
}

if ($split{transitivity}) {
    @views = 
        grep { $_->{id} ne 'transitivity'
    } @views;
    foreach my $rid (@transitive_relation_node_ids) {
        push(@views,
             {id=>"transitivity-$rid",
              rule=>"R=$rid,transitive(R), A R B, B R C, => A R C",
              sql=>"
 SELECT DISTINCT
  x.node_id             AS node_id,
  x.predicate_id        AS predicate_id,
  y.object_id           AS object_id
 FROM inheritable_link              AS x
  INNER JOIN inheritable_link       AS y 
        ON (x.object_id=y.node_id)
  INNER JOIN transitive_relation_node AS r 
        ON (x.predicate_id=r.node_id)
  $lj
 WHERE x.predicate_id=$rid AND y.predicate_id=$rid
  $lj_cond
"
             });
    }

}

if ($split{isa}) {
    @views = 
        grep { $_->{id} ne 'isa1' && $_->{id} ne 'isa2'
    } @views;

    # TODO: use all-some quantifier
    foreach my $rid (@inheritable_relation_node_ids) {
        next if $rid == $is_a; # covered by transitivity

        push(@views,
             {id=>"isa1-$rid",
    rule=>"R=$rid, A is_a B, B R C => A R C",
    sql=>"
 SELECT DISTINCT
  x.node_id             AS node_id,
  y.predicate_id        AS predicate_id,
  y.object_id           AS object_id
 FROM inheritable_link              AS x
  INNER JOIN inheritable_link       AS y ON (x.object_id=y.node_id)
  LEFT JOIN  link AS existing_link
        ON (x.node_id=existing_link.node_id AND
            y.predicate_id=existing_link.predicate_id AND
            y.object_id=existing_link.object_id)
 WHERE  x.predicate_id = $is_a AND y.predicate = $rid
  $lj_cond
"
        },
        {id=>"isa2-$rid",
    rule=>"$=$rid, A R B, B is_a C => A R C",
    sql=>"
 SELECT DISTINCT
  x.node_id             AS node_id,
  x.predicate_id        AS predicate_id,
  y.object_id           AS object_id
 FROM inheritable_link              AS x
  INNER JOIN inheritable_link       AS y ON (x.object_id=y.node_id)
  $lj
 WHERE y.predicate_id = $is_a AND x.predicate_id=$rid
  $lj_cond
"
        }
    );
   }
}

unless ($skip{chain}) {
  # TODO: make this generic - use role chains
    my @relation_chains = 
        (
         ['OBO_REL:inheres_in_part_of','OBO_REL:inheres_in','OBO_REL:part_of'],
         ['OBO_REL:inheres_in_part_of','OBO_REL:inheres_in','part_of'],
        );

   foreach my $rule (@relation_chains) {
       my $rel = shift @$rule;
       my @chain = @$rule;
       if (@chain != 2) {
          die "$rel must have 2 in chain: got: @chain";
       }
      my $r1id = get_or_put_relation(shift @chain);
      my $r2id = get_or_put_relation(shift @chain);
      my $rid = get_or_put_relation($rel);
      my $sql =
        qq[
 SELECT DISTINCT
  x.node_id             AS node_id,
  $rid                  AS predicate_id,
  y.object_id           AS object_id
 FROM inheritable_link              AS x
  INNER JOIN inheritable_link       AS y ON (x.object_id=y.node_id)
  $lj
 WHERE x.predicate_id = $r1id
  AND y.predicate_id = $r2id
  $lj_cond 
];
    push(@views,
       {id=>$rel,
        rule=>"$rel = ".join(' * ',@chain),
        sql=>$sql});
  }
}

if ($skip{rules}) {
    @views = ();
}

foreach my $view (@inference_views) {

      my $sql =
        qq[
 SELECT DISTINCT
  x.node_id     ,
  x.predicate_id,
  x.object_id 
 FROM $view AS x
  LEFT JOIN  link AS existing_link
        ON (x.node_id=existing_link.node_id AND
            x.predicate_id=existing_link.predicate_id AND
            x.object_id=existing_link.object_id)
 WHERE
  existing_link.link_id IS NULL 

];
    push(@views,
       {id=>$view,
        rule=>"$view",
        sql=>$sql});
}

logmsg("preparing stmts");
my $sth_link = $dbh->prepare_cached("SELECT link_id FROM LINK WHERE node_id=? AND predicate_id=? AND object_id=?");
my $sth_store = $dbh->prepare_cached("INSERT INTO link (node_id,predicate_id,object_id,is_inferred) VALUES (?,?,?,'t')");

my $i_by_node_id = get_intersections();
foreach my $node_id (keys %$i_by_node_id) {
    if ($test_intersection && $node_id != $test_intersection) {
        next;
    }
    my $intersection_h = $i_by_node_id->{$node_id};
    my $sql = intersection_to_query($node_id,$intersection_h);
    #print STDERR "$sql\n";
    # we do this at the start - unless new intersections can be added
    unshift(@views,
            {id=>"intersection_for_$node_id",
             sql=>$sql});
    if ($infer_instance_of) {
        my $sql = intersection_to_query($node_id,$intersection_h,1);
        #print STDERR "$sql\n";
        # we do this at the start - unless new intersections can be added
        unshift(@views,
            {id=>"instance_intersection_for_$node_id",
             sql=>$sql});
    }


}

my $done = 0;
my $sweep = 0;
unless ($skip{sweep}) {
    while (!$done) {
        logmsg( "Sweep: $sweep" );
        my $links_added_this_sweep = 0;
        foreach my $view (@views) {
            my $links_added = cache_view($view);
            $links_added_this_sweep += $links_added;
        }
        logmsg( "Sweep: $sweep total_added: $links_added_this_sweep" );
        $done = 1 unless $links_added_this_sweep;
    }
}
unless ($skip{equivalence}) {
    &assert_sameas();
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
            if ($triple[0] == $triple[2] && $view_id ne 'isa*') {
                # TODO: proper reflexivity rules. hardcode OK for is_a for now
                # also: will report cycles for intersections to self, which is normal?
                #
                # this gives us lots of spurious messages for GALEN, since the obo translation
                # uses anonymous IDs and class expression syntax
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
                $sth_store->execute(@triple);
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

sub assert_sameas {
    logmsg("fetching reciprocal subclass links");
    my $eqs =
      $dbh->selectall_arrayref("SELECT DISTINCT x.node_id, x.object_id FROM link AS x INNER JOIN link AS y ON (y.object_id=x.node_id AND x.object_id=y.node_id) WHERE x.predicate_id=$is_a AND y.predicate_id=$is_a AND x.node_id != x.object_id", {Slice=>{}});
    logmsg("got reciprocal subclass links: ".scalar(@$eqs));
    foreach (@$eqs) {
        $dbh->do("INSERT INTO sameas (node_id,object_id,is_inferred) VALUES ($_->{node_id},$_->{object_id},'t')");
        $dbh->do("INSERT INTO sameas (object_id,node_id,is_inferred) VALUES ($_->{node_id},$_->{object_id},'t')");
    }
    logmsg("done sameas");
}

# TODO: unions
sub get_intersections {
    my $i_by_node_id = {};
    unless ($skip{intersections}) {
        my $ilinks = $dbh->selectall_arrayref("SELECT DISTINCT node_id,predicate_id,object_id,combinator FROM link WHERE combinator='I'",{Slice=>{}});
        foreach (@$ilinks) {
            push(@{$i_by_node_id->{$_->{node_id}}}, $_);
        }
    }
    return $i_by_node_id;
}

sub intersection_to_query {
    my $defined_node_id = shift;
    my $i_h = shift;
    my $is_inst = shift;
    my @conds = @$i_h;
    my $linknum=0;
    my @links = ();

    # TODO: remember, is_a is reflexive..
    # TODO: sub-relations
    my $where =
      join(" AND ",
           map {
               $linknum++;
               my $link = "link_".$linknum;
               push(@links,"link AS $link");
               my $q;
               my $pred_id = $_->{predicate_id};
               if ($is_inst) {
                   if ($pred_id == $is_a) {
                       $pred_id = $instance_of;
                       $q = "$link.node_id=subsumed_node.node_id AND $link.predicate_id = $instance_of AND $link.object_id = $_->{object_id} AND $link.combinator!='U'";
                   }
                   else {
                       $q = "$link.node_id=subsumed_node.node_id AND $link.predicate_id = $pred_id AND $link.object_id IN (SELECT node_id FROM instantiation_link WHERE object_id= $_->{object_id}) AND $link.combinator!='U'";
                   }
               }
               else {
                   $q = "$link.node_id=subsumed_node.node_id AND $link.predicate_id = $pred_id AND $link.object_id = $_->{object_id} AND $link.combinator!='U'";
               }
               # TODO: omit negation links
               $q;
           } @conds);
    my $from = join(', ',@links);

    my $inf_pred_id = $is_inst ? $instance_of : $is_a;

    my $sql =
      qq[
 SELECT DISTINCT
  subsumed_node.node_id  AS node_id,
  $inf_pred_id                  AS predicate_id,
  $defined_node_id       AS object_id
 FROM node AS subsumed_node, $from
 WHERE
   $where
  ];
    return $sql;
}

sub delete_inferred_links {
    my $link_ids = $dbh->selectcol_arrayref("SELECT link_id FROM link WHERE is_inferred='t'");
    $dbh->{AutoCommit}=0;
    my $n=0;
    foreach my $link_id (@$link_ids) {
        print STDERR "Deleting $link_id\n";
        $dbh->do("DELETE FROM link WHERE link_id=$link_id");
        $n++;
        if ($n % 1000 == 0) {
            print STDERR "COMMITTING\n";
            $dbh->commit;
        }
    }
    $dbh->commit;
    print STDERR "Deleted all inferred links\n";
}

sub get_or_put_relation {
    my $rel = shift;
    my @nids = 
      $dbh->selectrow_array("SELECT node_id FROM node WHERE uid='$rel'");
    if (@nids == 1) {
        return $nids[0];
    }
    elsif (@nids > 1) {
        die "@nids";
    }
    else {
        $dbh->do("INSERT INTO node (uid,metatype) VALUES ('$rel','R')");
        return get_or_put_relation($rel);
    }
    
}


sub logmsg {
    return unless $verbose;
    my $msg = shift;
    my $t = time;
    print STDERR "LOG $t : $msg\n";
}

sub usage {

    <<EOM;
obd-reasoner.pl -d DBPATH [--view HORN-RULE-VIEW]* [--skip RULE-TO-SKIP]* [-split {transitivity | isa} ]*  [--inst]

Options:

 -d DBPATH

  Either a JDBC or DBI locator or DB name (if localhost)

 --view HORN-RULE-VIEW

  argument should be the name of an SQL view that can be used as a horn rule as part of the forward chaining.
  E.g. for composition chains of length 2:

       x R1 y, y R2 z -> x R z

  Execute this SQL:

  SELECT realize_relation('R1');
  SELECT realize_relation('R2');
  CREATE VIEW R_inf AS 
   SELECT R1.node_id,
          get_node_id('R'),
          R2.object_id
   FROM R1 JOIN R2 ON (R1.object_id=R2.node_id);

   Then do this:

    obd-reasoner -d mydb --view R_inf

 --inst 

   Reason over instances as well as classes (default is just classes)

 --split RULE

  some rules can be expensive to calculate for large databases. This breaks down the rule into smaller more manageable queries.
  Currently the only valid values here are:

     transitivity
     isa

EOM
}
