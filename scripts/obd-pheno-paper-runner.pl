#!/usr/bin/perl -w

my $jdbc = "jdbc:postgresql://localhost:9999/obdp";

my @genes = qw(NCBI_Gene:2035 NCBI_Gene:2132 NCBI_Gene:2138 NCBI_Gene:2235 NCBI_Gene:487 NCBI_Gene:5076 NCBI_Gene:6469 NCBI_Gene:6662 NCBI_Gene:6663 NCBI_Gene:7139 NCBI_Gene:7273 MGI:98297 ZFIN:ZDB-GENE-980526-166);

my @taxa = qw(NCBITaxon:7955 NCBITaxon:9606 NCBITaxon:10090);

foreach my $gene (@genes) {
    foreach my $tax (@taxa) {
        run($gene,$tax);
    }
}
print STDERR "Done!\n";

sub run {
    my $g = shift;
    my $t = shift;
    my $outf = "$g-vs-$t-IC.sim";
    if (-f $outf) {
        print STDERR "Already have: $outf\n";
        return 0;
    }
    else {
        my $time = time;
        my $cmd = "(obd-exec -d $jdbc --findsim -x --ic -M 8000 -O 50 --org $t $g > $outf) >& $outf.err";
        print STDERR "Cmd: $cmd\n";
        #print `$cmd`;
        my $err = system($cmd);
        print STDERR "STATUS: $err\n";
        my $t2 = time;
        my $td = $t2-$time;
        print STDERR "Completed in: $td\n";
        return 1;
    }
    
}

