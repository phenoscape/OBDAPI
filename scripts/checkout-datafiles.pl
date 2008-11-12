#!/usr/bin/perl -w

#Author: Cartik1.0
#Date: 11/04/2008

my $dirFile;
my $opt = shift @ARGV;
my @dirs;

if($opt eq '--dataDir'){ #look for file containing all the directories from which to download
  $dirFile = shift @ARGV;
  @dirs = ();
  
  open(F, $dirFile); #open the file containing all the directories
  while(<F>){
    chomp;
    push(@dirs, $_); #create the svn checkout URLs abd 
													# load them into the array
  }
  close(F);
}
else{
  print STDERR "File containing data directories not specified\n";
  exit(0);	
}

foreach my $dir (@dirs){
 run("svn co http://phenoscape.svn.sourceforge.net/svnroot/phenoscape/trunk/data/phenex-files/$dir ../data/$dir");			#checkout data files fromevery directory
}

sub run {
    my $cmd = shift;
    print STDERR "CMD: $cmd\n";
    if (system($cmd)) {
        print STDERR "Error in: $cmd\n";
        exit(2);
    }
}

