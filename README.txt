OBDAPI
======

REST Service
------------

Quick start:

you should have the required jars from svn - use ant eclipse to rebuild if the src is changed

FROM ECLIPSE:

  Run as application: org.obd.ws.OBDRestApplication
  
  arguments as below:

FROM THE COMMAND LINE:

  launch_scripts/run-obd-server -d jdbc:postgresql://spitz.lbl.gov:5432/obd_phenotype_all

Minor hack may be required if running straight from jars:

  cd OBDAPI
  rm -rf bin
  ln -s src bin

(TODO: fix this)

Once started, point a browser at:

	http://localhost.8182
	
	and start following links

Tomcat
------

Or can be run from within tomcat - instructions to follow	

TODO

DEVELOPER DOCS
==============

doc/ directory

xml/obd-rest.rnc -- core OBD model


PROJECT DIRECTORY ORGANIZATION
==============================

sql/	-- DDL Schema for OBDSQL

launch_scripts/
		-- various scripts for loading db and launching server
		   perl scripts to be converted to java TODO
		  
src/	-- java source

	org.obd.
			ws -- REST layer (and deprecated SOAP layer)
			query -- Main API and QueryTerm objects
			model -- Data transfer object model
			parser -- for various external formats
