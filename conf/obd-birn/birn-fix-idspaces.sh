#!/bin/bash
DB=$1
obd-exec  -d $DB --rename-idspace http://purl.org/obo/owl/obo# OBO_REL:
obd-exec  -d $DB --rename-idspace http://www.obofoundry.org/ro/ro.owl# OBO_REL:
obd-exec  -d $DB --rename-idspace http://www.ifomis.org/bfo/1.1/snap# snap:
obd-exec  -d $DB --rename-idspace http://www.ifomis.org/bfo/1.1/span# span:
obd-exec  -d $DB --rename-idspace http://www.ifomis.org/bfo/1.1# bfo
obd-exec  -d $DB --rename-idspace __file:///Users/cjm/.blip/data_cache/http:::ccdb.ucsd.edu:SAO:DPO:1.0:HumanDPO.owl# _SAO_HumanDPO:

obd-exec  -d $DB --rename-idspace BIRN_PDPO:has_Quality BIRN_PDPO:has_quality
obd-exec  -d $DB --rename-idspace BIRN_PDPO:has_part OBO_REL:has_part
obd-exec  -d $DB --rename-idspace http://www.owl-ontologies.com/unnamed.owl#has_quality BIRN_PDPO:has_quality
obd-exec  -d $DB --rename-idspace BIRN_PDPO:is_part_of OBO_REL:part_of
obd-exec  -d $DB --rename-idspace __file:///Users/cjm/.blip/data_cache/ _:
#obd-exec  -d $DB --rename-idspace birnlex_ubo:birnlex_17 BIRN_PDPO:has_quality

obd-exec  -d $DB --rename-idspace http://www.owl-ontologies.com/unnamed.owl#sao ImagePhenotype:sao

# mapping new (todo: change local idspace)
obd-exec  -d $DB --rename-idspace http://ccdb.ucsd.edu/SAO/DPO/2.0/DPO.owl# BIRN_PDPO:
obd-exec  -d $DB --rename-idspace http://ccdb.ucsd.edu/SAO/DPO/2.0/HumanDPO.owl# SAO_HumanDPO:
obd-exec  -d $DB --rename-idspace _:http:::ccdb.ucsd.edu:SAO:DPO:2.0:HumanDPO.owl# _:SAO_Human_DPO_

obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/SAO-CORE_properties.owl# NIF_core:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-GrossAnatomy.owl# birnlex_anatomy:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/Backend/BIRNLex-OBO-UBO.owl# birnlex_ubo:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/DigitalEntities/NIF-Investigation.owl# NIF_investigation:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Subcellular.owl# sao:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Cell.owl# NIF_cell:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Molecule.owl# NIF_molecule:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Quality.owl# NIF_quality:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/Dysfunction/NIF-Dysfunction.owl# birnlex_disease:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Organism.owl# birnlex_tax:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/Backend/BIRNLex-OBI-proxy.owl# NIF_OBI:

obd-exec  -d $DB --rename-idspace NIF_core:sao1239937685 OBO_REL:regional_part_of

# TODO:

