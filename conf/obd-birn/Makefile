JDBC = jdbc:postgresql://localhost:5432

sao.obo:
	cd /Users/cjm/cvs/obo/ontology/anatomy/anatomy_xp/ && make sao.obo

remake-sao.obo:
	cd /Users/cjm/cvs/obo/ontology/anatomy/anatomy_xp/ && rm sao.obo && make sao.obo

all.obo:
	blip -import_all -debug load -debug ontol io-convert -f owl -i  http://ccdb.ucsd.edu/SAO/DPO/1.0/HumanDPO.owl -i http://ccdb.ucsd.edu/SAO/DPO/1.0/DPO.owl -i http://ccdb.ucsd.edu/SAO/DPO/1.0/ImagePhenotype.owl -i http://ccdb.ucsd.edu/SAO/PDSO/1.0/PDSO.owl -to obo -o $@

# leave image and PDSO for now
all2.obo:
	blip -u ontol_manifest_metadata_from_nif -import_all -debug load -debug ontol io-convert -f owl -i  http://ccdb.ucsd.edu/SAO/DPO/2.0/HumanDPO.owl -i http://ccdb.ucsd.edu/SAO/DPO/2.0/DPO.owl -to obo -o $@

bearers-%:
	psql -q -c "SELECT DISTINCT node_uid(node_id) FROM birn_pdpo.bears" $* -o $@

sim-%: bearers-%
	perl -ne 'print `obd-exec -d $(JDBC)/$* --findsim -x -IC -M 8000 -O 50 $$_` if /BIRN/' $<

build-%: create-%

create-%: all.obo
	obd-create-db.pl $* -c birn.conf ;\
	psql $* < ../../sql/obd-birn-views.sql ;\
	./birn-fix-idspaces.sh $(JDBC)/$* ;\

xps-%: 
	psql -c "UPDATE link set combinator='I' where node_id in (SELECT node_id from asserted_is_a_link where object_id=get_node_id('http://purl.org/nbirn/birnlex/ontology/BIRNLex-Investigation.owl#birnlex_2087')) and combinator='' and is_inferred='f'" $* \;
	psql -c "SELECT reify_links_by_predicate('BIRN_PDPO:has_quality','BIRN:generic_annotation')" $* ;\
	psql -c "SELECT select migrate_links_to_alt_id('quality')" $* ;\
	psql -c "SELECT create_xps_by_relation(get_node_id('OBO_REL:inheres_in'),NULL)" $* ;\
	psql -c "SELECT create_xps_by_relation(get_node_id('OBO_REL:part_of'),NULL)" $* ;\
	psql -c "SELECT create_xps_by_relation(get_node_id('sao:sao7905250655'),NULL)" $* ;\
	psql -c "SELECT create_xps_by_relation(get_node_id('BIRN_PDPO:has_quality'),NULL)" $* ;\
	psql -c "SELECT reify_links_by_predicate('BIRN_PDPO:bears','BIRN:generic_annotation')" $* ;\
	obd-reasoner.pl --skip intersections --view inheres_in_link_from_has_quality --view has_population_of --rule none -d $* ;\
	psql -c "SELECT create_xps_by_relation(get_node_id('OBO_REL:has_population_of'),NULL)" $* ;\
	obd-reasoner.pl --view has_population_of --inst -d $* ;\
	psql -c "SELECT create_xps_by_relation_between('OBO_REL:has_part','sao:sao2254405550','snap:IndependentContinuant',NULL)" $* ;\
	obd-reasoner.pl --inst --rule none -d $* ;\
	psql -c "SELECT create_xps_by_relation_to_xp(get_node_id('OBO_REL:inheres_in'),NULL)" $* ;\
	obd-reasoner.pl --inst --rule none -d $* ;\
	obd-reasoner.pl --inst --view eq_instance_of_link --view eq_instance_of_link_basic --view eq_instance_of_link_more_basic --view eq_instance_of_invq_link -d $* ;\
	psql -c "UPDATE node set label = name_from_intersections_full(node_id) where uid like '%^%'" $* ;\
	psql -c "INSERT INTO link (node_id,predicate_id,object_id) SELECT node_id,predicate_id,object_id FROM nr_inferred_exemplifies" $* ;\
	psql -c "SELECT reify_links_by_predicate('OBO_REL:exemplifies','BIRN:generic_annotation')" $* ;\
	echo done
#experimental:
#SELECT create_nested_xps_by_relation_chain(get_node_id('OBO_REL:inheres_in'),get_node_id('OBO_REL:part_of'),NULL);
#select create_xps_by_relation_with_asserted_instances(get_node_id('OBO_REL:inheres_in'),NULL);
#	psql -c "SELECT create_xps_by_relation(get_node_id('BIRN_PDPO:has_diagnosis'),NULL)" $* ;\

