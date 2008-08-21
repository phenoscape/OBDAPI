obd-sql-docs.html:
	sqlt --from DBI --to HTML --dsn dbi:Pg:database=obd --stylesheet obd-schema.css --title "OBD-Core-Schema" > $@.tmp && mv $@.tmp $@

%-nm.sql: %.sql
	./mk-no-materialize.pl $< > $@
%-mv.sql: %.sql
	./mk-materialize.pl $< > $@

views-noschema.sql: obd-core-views.sql
	grep -v ^SET $< | grep -v SCHEMA > $@
