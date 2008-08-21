#!/usr/bin/perl -w

use strict;

my $use_pgschema = 0;

print "\nThis script will help you install the OBD RDBMS schema for PostgreSQL. You must have the postgresql psql, createlang, & createdb binaries in your path.\n\nPlease note that you must have postgres superuser privileges to \n1) create a new database and \n2) create the plpgsql procedural language that are used for OBD views. \n\nAt the time of writing, we highly suggest using PostgreSQL 8.3\n\n";

print "Please enter your OBD database name: ";

my $db_name = <STDIN>;
chomp($db_name);

if ($db_name eq ""){
	print "\nERROR: Database name is required.\n";
	exit;
}

print "Database username: ";

my $db_username = <STDIN>;
chomp($db_username);

if ($db_username eq ""){
	print "\nERROR: Database username is required.\n";
}

print "Database port (optional): ";

my $db_port = <STDIN>;
chomp($db_port);

print "Database host (optional): ";
my $db_host = <STDIN>;
chomp($db_host);


my $dir = `dirname $0`;
chomp $dir;
my $sqldir = "$dir/../sql";

my $dbcs = " -U " . $db_username;

if ($db_host ne ""){
	$dbcs .= " -h " . $db_host;
}
if ($db_port ne ""){
	$dbcs .= " -p " . $db_port;
}
$dbcs .= " ";

my $createdb_command = "createdb -e " . $dbcs . $db_name;

run($createdb_command);

my $createl_command  = "createlang -e " . $dbcs . " plpgsql " . $db_name;
run($createl_command);


run ("psql $dbcs $db_name < $sqldir/obd-core-schema.sql");
useddl("$sqldir/obd-core-views.sql");
useddl("$sqldir/obd-simple-views.sql");
useddl("$sqldir/obd-util-funcs.sql");

useddl("$sqldir/obd-core-functions.plpgsql"); # todo: move
useddl("$sqldir/api/obd-mutable-api.plpgsql");
useddl("$sqldir/util/realize-relations-as-views.plpgsql");

print "\n\nOBD Schema Install Complete.\n\n";
sub useddl {
    my $ddlf = shift;
    if ($use_pgschema) {
        # warning - position sensitive!
        run("perl -npe 's/^.. CREATE SCHEMA/CREATE SCHEMA/;s/^.. SET search_path/SET search_path/'  $ddlf | psql $dbcs $db_name ");
    }
    else {
        run("cat $ddlf | psql $dbcs $db_name ");
    }
}

sub run {
    my $cmd = shift;
    print STDERR "CMD: $cmd\n";
    if (system($cmd)) {
        print STDERR "Error in: $cmd\n";
		exit;
    }
}