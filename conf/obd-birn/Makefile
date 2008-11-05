sao.obo:
	cd /Users/cjm/cvs/obo/ontology/anatomy/anatomy_xp/ && make sao.obo

remake-sao.obo:
	cd /Users/cjm/cvs/obo/ontology/anatomy/anatomy_xp/ && rm sao.obo && make sao.obo

all.obo:
	blip -import_all -debug load -debug ontol io-convert -f owl -i  http://ccdb.ucsd.edu/SAO/DPO/1.0/HumanDPO.owl -i http://ccdb.ucsd.edu/SAO/DPO/1.0/DPO.owl -i http://ccdb.ucsd.edu/SAO/DPO/1.0/ImagePhenotype.owl -i http://ccdb.ucsd.edu/SAO/PDSO/1.0/PDSO.owl -to obo -o $@