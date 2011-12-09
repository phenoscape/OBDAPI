all: obd-d2rq-entailed.n3

%-entailed.n3: %.n3
	grep -v is_inferred $< > $@
