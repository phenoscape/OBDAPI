TRANG = java -jar $(HOME)/Java/trang/trang.jar

all: src/org/obd/model/doc-files/obd-xml-xsd.html

%.rng: %.rnc
	$(TRANG) $< $@
.PRECIOUS: %.rng

%.xsd: %.rng
	$(TRANG) $< $@
.PRECIOUS: %.xsd

%-xsd.html: %.xsd
	xsltproc xml/xsd2html.xsl $< > $@

publish:
	rsync -avz -e ssh docs lsc.lbl.gov:/local/berkeleybop.org_80/www/htdocs/obd/
