#!/usr/bin/perl

use DBIx::DBStag;
use strict;

my @dbs = ();
while ($ARGV[0] =~ /^\-/) {
    my $opt = shift @ARGV;
    if ($opt eq '-d' || $opt eq '--database') {
        @dbs = (shift @ARGV);
    }
    else {
        die $opt;
    }
}
unshift(@dbs,@ARGV);

die unless @dbs > 1;

my @dbhs = map {DBIx::DBStag->connect($_)} @dbs;
my $target_db = pop @dbs;
my $target_dbh = pop @dbhs;
print STDERR "Copying @dbs => $target_db\n";

foreach my $dbh (@dbhs) {
    copy_db($dbh,$target_dbh);
}
print STDERR "Done!\n";
exit 0;

sub copy_db {
    my $source_dbh = shift;
    my $target_dbh = shift;

    my %node_id_map_source_to_target = ();
    my $sth = $source_dbh->prepare("SELECT * FROM node");
    while (my $rh = $sth->fetchrow_hashref) {
        my $node_id = $rh->{node_id};
        delete $rh->{node_id};
        $node_id_map_source_to_target{$rh->{node_id}} = 
    }
}
