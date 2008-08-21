#!/usr/bin/perl

$in = 1;
while(<>) {
    $in = 0 if if /BEGIN\s+MATERIALIZE/;
    $in = 1 if if /END\s+MATERIALIZE/;
    print if $in;
}
