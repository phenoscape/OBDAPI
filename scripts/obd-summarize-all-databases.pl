#!/usr/bin/perl
use DBI;
foreach my $s (DBI->data_sources("Pg")) {
    my $dbh = DBI->connect($s);
    print "SOURCE: $s\n";
    my $sth = $dbh->prepare("SELECT * FROM source_summary");
    $sth->execute;
    while (my $h = $sth->fetchrow_hashref) {
        print "--\n";
        foreach (keys %$h) {
            printf "%30s : %s\n", $_, $h->{$_},
        }
        print "\n";
    }
    print "\n";
    print "\n";
}
