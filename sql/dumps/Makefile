OBDURL = http://spade.lbl.gov:8182
DB=obd_phenotype_test
all: all_reports_txt all_reports_html

sync:
	rsync -avz * sos.lbl.gov:cvs/obo-database/dumps

REPORTS = source_summary link_count_by_metatype_and_source link_count_by_relation_and_node_source link_count_by_node_and_object_source\
omim_annotator_global_congruence_by_gene \
omim_gene_annotation_summary \


all_reports_txt: $(patsubst %,reports/$(DB)-%.txt,$(REPORTS))
all_reports_html: $(patsubst %,reports/$(DB)-%.html,$(REPORTS))

reports/$(DB)-%.txt:
	psql -x -e -c "SELECT * FROM $*" $(DB) $(DBARGS) > $@.tmp && mv $@.tmp $@
reports/$(DB)-%.html:
	psql -H -e -c "SELECT * FROM $*" $(DB) $(DBARGS) > $@.tmp && mv $@.tmp $@

dump-%.obdxml:
	wget -O $@ '$(OBDURL)/obdxml/nodes/$*/statements/annotations'

exports/descriptions.%:
	wget -O $@ '$(OBDURL)/$*/descriptions'
