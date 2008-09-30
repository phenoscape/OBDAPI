#!/usr/bin/perl

print "-- SELECT drop_all_matviews();\n\n";
$in = 0;
while(<>) {
    $in = 0 if /END\s+MATERIALIZE/;
    if ($in) {
        s/^\-\-\s+//;
    }
    $in = 1 if /BEGIN\s+MATERIALIZE/;
    print;
}
print "VACUUM FULL ANALYZE;\n\n";
