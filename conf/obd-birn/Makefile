JDBC = jdbc:postgresql://localhost:5432

#sao.obo:
#	cd /Users/cjm/cvs/obo/ontology/anatomy/anatomy_xp/ && make sao.obo

#remake-sao.obo:
#	cd /Users/cjm/cvs/obo/ontology/anatomy/anatomy_xp/ && rm sao.obo && make sao.obo

# use pre-Thea library
# leave image and PDSO for now
all.obo:
	blip -u ontol_manifest_metadata_from_nif -import_all -debug load -debug ontol io-convert -f owl -i  http://ccdb.ucsd.edu/PKB/1.0/PKB.owl  -i  http://ccdb.ucsd.edu/NDPO/1.0/NDPO.owl  -to obo -o $@

#all.obo:
#	blip -import_all -debug load -debug ontol io-convert -f owl -i  http://ccdb.ucsd.edu/SAO/DPO/1.0/HumanDPO.owl -i http://ccdb.ucsd.edu/SAO/DPO/1.0/DPO.owl -i http://ccdb.ucsd.edu/SAO/DPO/1.0/ImagePhenotype.owl -i http://ccdb.ucsd.edu/SAO/PDSO/1.0/PDSO.owl -to obo -o $@

# see pkb below
nu-all.obo:
	blip -u ontol_manifest_metadata_from_nif_via_thea -debug load -debug ontol io-convert -f thea2_owl_i -i  http://ccdb.ucsd.edu/PKB/1.0/PKB.owl  -i  http://ccdb.ucsd.edu/NDPO/1.0/NDPO.owl -to obo -o $@

#PATO.obo:
#	cp $(HOME)/cvs/obo/ontology/phenotype/quality.obo $@

ro.obo:
	cp $(HOME)/cvs/obo/ontology/OBO_REL/ro.obo .
ro.owl: ro.obo
	go2owl $< > $@

NIF-Dysfunction.owl:
	wget http://ontology.neuinfo.org/NIF/Dysfunction/NIF-Dysfunction.owl
NIF-GrossAnatomy.owl:
	wget http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-GrossAnatomy.owl
NIF-Organism.owl:
	wget http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Organism.owl
NIF-Cell.owl:
	wget http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Cell.owl
NIF-Molecule.owl:
	wget http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Molecule.owl
NIF-Subcellular.owl:
	wget http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Subcellular.owl
NIF-Quality.owl:
	wget http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Quality.owl
quality.owl:
	wget http://ontology.neuinfo.org/NIF/Backend/quality.owl
PKB.owl:
	wget http://ccdb.ucsd.edu/PKB/1.0/PKB.owl
NDPO.owl:
	wget http://ccdb.ucsd.edu/NDPO/1.0/NDPO.owl
PDSO.owl:
	wget http://ccdb.ucsd.edu/SAO/PDSO/1.0/PDSO.owl
%.owl:
	wget http://purl.org/obo/owl/$@ -O $@

%.owlpl: %.owl remake-owlpl
	thea-owl-to-prolog $< > $@.tmp && mv $@.tmp $@
.PRECIOUS: %.owlpl

PKB_SRCS = PKB PATO NIF-Quality NIF-GrossAnatomy NIF-Organism BIRNLex-OBO-UBO NIF-Dysfunction NDPO NIF-Subcellular NIF-Cell NIF-Molecule PDSO
PKB_OBO = $(patsubst %, %.obo, $(PKB_SRCS))
PKB_OBO_INC = $(patsubst %, -i %.obo,$(PKB_SRCS))
PKB_OWLPL = PKB.owlpl PATO.owlpl NIF-GrossAnatomy.owlpl NIF-Organism.owlpl BIRNLex-OBO-UBO.owlpl NIF-Dysfunction.owlpl
PKB_dlp.pl:  $(PKB_OWLPL)
	thea-owlpl-to-dlp $(PKB_OWLPL) | ./filter-dlp.pl > $@

PKB_all.obo:  $(PKB_OBO)
	echo $(PKB_OBO) && \
	obo-cat.pl $(PKB_OBO) PKB-xp.obo > $@

PKB-xp.obo: PKB.owlpl ro.owlpl BIRNLex-OBO-UBO.owlpl
	./generate-basic-class-expressions  PKB.owlpl ro.owlpl BIRNLex-OBO-UBO.owlpl > $@

PKB_modes.pl: $(PKB_OWLPL)
	./birn-write-progol $(PKB_OWLPL) > $@

run-ilp: PKB_dlp.pl PKB_modes.pl
	./ilp

%.dlp: %.owlpl remake-dlp
	thea-owlpl-to-dlp $< > $@.tmp && grep -v ^% $@.tmp > $@
%.plsyn: %.owl
	thea-owl2plsyn $< > $@.tmp && mv $@.tmp  $@

# TODO: idspace for inheres_in
HumanDPO_xp-basic.obo: all.obo
	obol obol-generate-xps -u xp_pato_birn -i $< -r pato -idspace NDPO_XP: > $@.tmp && mv $@.tmp $@

HumanDPO_xp.obo: HumanDPO_xp-basic.obo
	obol obol-generate-names -debug obol_info -u obol_phenotype_xp_generate -i all.obo -r pato -i $< -query "sub_atom(ID,_,_,_,'NDPO_XP:')" -show_classdef > $@.tmp && cat idspaces.obo $@.tmp > $@

bearers-%:
	psql -q -c "SELECT DISTINCT node_uid(node_id) FROM birn_pdpo.bears" $* -o $@

sim-%: bearers-%
	perl -ne 'print `obd-exec -d $(JDBC)/$* --findsim -x -IC -M 8000 -O 50 $$_` if /BIRN/' $<

build-%: create-%

#create-%: all.obo
create-%: PKB_all.obo
	obd-create-db.pl $* -c birn.conf ;\
	psql $* < ../../sql/obd-birn-views.sql ;\
	./birn-fix-idspaces.sh $(JDBC)/$* ;\
	pg_dump $* > $*-create.dump

xps-%:
	obd-reasoner.pl --inst -d $* ;\
	obd-generate-lca-xps.pl --rematerialize -d $* --pred OBO_REL:towards --range NIF_Subcellular:sao1337158144 --range NIF_Cell:sao1813327414 --range NIF_Molecule:sao500241072 \;
	obd-reasoner.pl --inst -d $* ;\

old-xps-%: 
	psql -c "UPDATE link set combinator='I' where node_id in (SELECT node_id from asserted_is_a_link where object_id=get_node_id('http://purl.org/nbirn/birnlex/ontology/BIRNLex-Investigation.owl#birnlex_2087')) and combinator='' and is_inferred='f'" $* ;\
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

%-view.obo: %-view.owl remake-obo
	blip -u ontol_manifest_metadata_from_nif_via_thea io-convert -f thea2_owl_i -i $< -to obo -o $@

%.obo: %.owl remake-obo
	blip -u ontol_manifest_metadata_from_nif_via_thea io-convert -f thea2_owl -i $< -to obo -o $@
#	blip -i $< io-convert -to obo -u ontol_manifest_metadata_from_nif > $@.tmp && mv $@.tmp $@

remake-%:
	touch $@

align-nifmol-pro.obo: NIF-Molecule.obo
	obol -debug obol -u onto_grep -r protein -i $< onto-exact-align -exclude_xref_strict -exclude_xref -disp 'format(obo)' > $@.tmp && mv $@.tmp $@

align-nifmol-chebi.obo: NIF-Molecule.obo
	obol -debug obol -u onto_grep -r xchebi -i $< onto-exact-align -exclude_xref_strict -exclude_xref -disp 'format(obo)' > $@.tmp && mv $@.tmp $@

publish:
	scp PKB_all.obo sos.lbl.gov:/data/public_ftp/pub/obo/obo/PKB.obo

