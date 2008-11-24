#!/usr/bin/perl -w

#author Cartik1.0
#date 10/23/2008


my $db = '';
my $dbhost = '';
my $conf_file;
my $reasoner;
my $dirFile;
my $drop;
my @files;

if (!@ARGV) {
    printusage();
    exit(0);
}

while (my $opt = shift @ARGV) {
    if ($opt =~ /^\-/) {
        if ($opt eq '-d' || $opt eq '--database') {
            $db = shift @ARGV;
        }
        elsif ($opt eq '--help') {
            printusage();
            exit(0);
        }
        elsif ($opt eq '-h' || $opt eq '--host' || $opt eq '--dbhost') {
            $dbhost = shift @ARGV;
        }
        elsif ($opt eq '-c' || $opt eq '--conf') {
            $conf_file = shift @ARGV;
        }
        elsif ($opt eq '--reasoner') {
            $reasoner = shift @ARGV;
        }
        elsif ($opt eq '--drop') {
            $drop = 1;
        }
	elsif ($opt eq '--dataDir'){
	    $dirFile = shift @ARGV;	
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

if (!$db) {
    printusage();
    exit(0);
}

my $deleteDBargs = $dbhost ? "-h $dbhost $db" : $db;
my $createDBargs = "$deleteDBargs -c $conf_file"; 

#hard coded classpath
my $classpath = '../lib/runlibs/phenex.jar';

if(!$conf_file){
	print STDERR "Usage: Please specify a configuration file location\n";
	exit(1);
}

if(!$dirFile){
	print STDERR "Usage: Please specify a file location containing the datafile directories at Phenoscape Sourceforge\n";
	exit(2);
}

#download the data files from Phenoscape sourceforge
run("perl checkout-datafiles.pl --dataDir ../conf/datafile-locations.conf");

#drop the database
run("dropdb $deleteDBargs"); 

#call the Perl script which creates the database and also loads the ontology terms into the DB
run("perl obd-create-db.pl $createDBargs");

print STDERR "Finished creating database\n";

#call the Java class that loads the Zfin data files into the newly created database
run("sh ../launch_scripts/zfin2database");

#call the Java class that loads all the data files into the newly created database
run("sh ../launch_scripts/nexml2database");

#run the reasoner
run("perl obd-reasoner.pl -d $deleteDBargs");

#commit the problem log to the SVN site
#run("svn commit");

sub run {
    my $cmd = shift;
    print STDERR "CMD: $cmd\n";
    if (system($cmd)) {
        print STDERR "Error in: $cmd\n";
        exit(2);
    }
}

