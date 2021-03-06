#!/bin/sh
DB=$1
make all.obo
obd-create-db.pl -d $DB all.obo --reasoner perl
psql $DB < ../sql/obd-birn-views.sql
./birn-fix-idspaces.sh $DB
psql $DB -c "SELECT realize_all_relations()"
obd-load-db-from-obo.pl -d $DB generic-annotation.obo 
psql $DB -c "SELECT reify_links_by_predicate('BIRN_PDPO:bears','BIRN:generic_annotation');"
psql $DB -c "SELECT create_birn_xps(NULL)";
psql $DB -c "SELECT create_birn_xps(NULL)"; # 2nd time, to create p^inh(x^has_part(y))
obd-reasoner.pl --inst $DB 
