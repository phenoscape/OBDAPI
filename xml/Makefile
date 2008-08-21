TRANG = java -jar $(HOME)/Java/trang/trang.jar

specs: xmlspec/obd-core.rng xmlspec/obd-core.xsd xmlspec/obd-core-xsd.html

xmlspec/%.rng: xmlspec/%.rnc
	$(TRANG) $< $@
.PRECIOUS: xmlspec/%.rng

xmlspec/%.xsd: xmlspec/%.rng
	$(TRANG) $< $@
.PRECIOUS: xmlspec/%.xsd

xmlspec/%-xsd.html: xmlspec/%.xsd
	xsltproc xsl/xsd2html.xsl $< > $@

jars/%.jar: xmlspec/%.xsd
	scomp -out $@ $<
