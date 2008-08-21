#!/usr/bin/perl

use DBIx::DBStag;
use strict;

while ($ARGV[0] =~ /^\-/) {
    my $opt = shift @ARGV;
    if ($opt eq '-d' || $opt eq '--database') {
        $d = shift @ARGV;
    }
    else {
        die $opt;
    }
}
if (!$d) {
    $d = shift @ARGV;
}
my $dbh = DBIx::DBStag->connect($d);
my @files = @ARGV;
print STDERR "Loading\n";

foreach my $file (@files) {
    print STDERR "Converting File: $file\n";
    open(F,$file) || die $file;
    while (<F>) {
        my ($hid,$taxid,$geneid,$symbol,$ref) = split(/\t/,$_);
        $hid = "NCBI_Homologene:$hid";
        $taxid = "NCBITaxon:$taxid";
        $geneid = "EntrezGene:$geneid";
        my $hid_i = store_class({uid=>$hid});
        #my $taxid_i = store_class({uid=>$taxid});
        my $geneid_i = store_class({uid=>$geneid});
        store_link({node_id=>$geneid_i,object_id=>$hid_i,predicate=>"x"});
    }
    close(F);
    print STDERR "Loaded File: $ifile\n";
}
exit 0;

