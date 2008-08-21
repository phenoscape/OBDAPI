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
        elsif ($opt eq '-h' || $opt eq '--host') {
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
my %idh = ();
my $id;
my $type;
while (my $f = shift @files) {
    open(F,$f) || die $f;
    while (<F>) {
        chomp;
        if (/^id\t(\S+)/) {
            $id=$1;
            $idh{$id}=1;
        }
        if (/^type\t(\S+)/) {
            $idh{$id} = $1;
        }
    
    }
    close(F);
}

foreach my $id (keys %idh) {
    print STDERR "id: $id\n";
    my $this_db = $db."_".$id;
    my $cmd = "obd-create-db.pl ";
    if ($dbhost) {
        $cmd .= "--dbhost $dbhost ";
    }
    $cmd .= " -d $this_db --reasoner perl --dump ";
    my $f = "obo-all/$id/$id";
    if ($idh{$id} eq 'mapping' || $idh{$id} eq 'logical_definitions') {
        $f .= ".imports";
    }
    $f .= ".obo";
    $cmd .= $f;
    run($cmd);
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
