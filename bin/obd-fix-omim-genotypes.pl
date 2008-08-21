#!/usr/bin/perl -w

use DBIx::DBStag;

my $sdbh = DBIx::DBStag->connect(shift);

my $dbh = $sdbh->dbh;

my $rows = $dbh->selectall_arrayref("SELECT node_id,uid FROM node WHERE uid like 'OMIM:%'");

foreach my $row (@$rows) {
    my ($id,$uid) = @$row;
    print STDERR "$uid\n";
    my $ref;
    my $gtnum;
    if ($uid =~ /OMIM:(\d+)\.(\d+)/) {
        $ref = "MIM:$1-$2";
        $gtnum = $2;
    }
    elsif ($uid =~ /OMIM:(\d+)/) {
        $ref = "MIM:$1";
    }
    if (!$gtnum) {
        $gtnum = $uid;
        $gtnum =~ s/OMIM://;
        $gtnum =~ s/.*\.//;
    }
    my $refrows = $dbh->selectall_arrayref("SELECT node_id,uid FROM node WHERE uid = '$ref'");
    foreach my $refrow (@$refrows) {
        print STDERR "  @$refrow\n";
        $dbh->do("SELECT store_node_dbxref_i($id,'$ref')");
    }
    my $generows = $dbh->selectall_arrayref("SELECT object_id,object_uid,object_label FROM node_link_node_with_pred WHERE node_uid = '$uid' and pred_uid='OBO_REL:variant_of'");
    foreach my $grow (@$generows) {
        my $label = sprintf("%s OMIM genotype %s",$grow->[2],"$gtnum");
        print STDERR "  G:@$grow :: $label\n";
        
        $dbh->do("UPDATE node SET label = '$label' WHERE node_id=$id");
    }
}
