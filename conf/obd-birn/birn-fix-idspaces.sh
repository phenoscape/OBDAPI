#!/bin/bash
DB=$1
obd-exec  -d $DB --rename-idspace towards OBO_REL:towards
obd-exec  -d $DB --rename-idspace inheres_in OBO_REL:inheres_in
obd-exec  -d $DB --rename-idspace http://purl.org/obo/owl/obo# OBO_REL:
obd-exec  -d $DB --rename-idspace http://purl.org/obo/owl/OBO_REL# OBO_REL:
obd-exec  -d $DB --rename-idspace http://purl.org/obo/owl/PATO#PATO_ PATO:
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
obd-exec  -d $DB --rename-idspace http://ccdb.ucsd.edu/SAO/Disease/1.0/NDPO.owl# NDPO:
obd-exec  -d $DB --rename-idspace http://ccdb.ucsd.edu/SAO/DPO/2.0/DPO.owl# PKB:
obd-exec  -d $DB --rename-idspace http://ccdb.ucsd.edu/PKB/1.0/PKB.owl# PKB:
obd-exec  -d $DB --rename-idspace http://ccdb.ucsd.edu/SAO/DPO/2.0/HumanDPO.owl# NDPO:
obd-exec  -d $DB --rename-idspace _:http:::ccdb.ucsd.edu:SAO:DPO:2.0:HumanDPO.owl# _:NDPO_
obd-exec  -d $DB --rename-idspace _:http:::ccdb.ucsd.edu:SAO:Disease:1.0:NDPO.owl# _:NDPO_
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/SAO-CORE_properties.owl# NIF_core:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-GrossAnatomy.owl# NIF_GrossAnatomy:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/Backend/BIRNLex-OBO-UBO.owl# NIF_UBO:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/DigitalEntities/NIF-Investigation.owl# NIF_Investigation:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Subcellular.owl# NIF_Subcellular:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Cell.owl# NIF_Cell:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Molecule.owl# NIF_Molecule:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Quality.owl# NIF_Quality:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/Dysfunction/NIF-Dysfunction.owl# NIF_Dysfunction:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/BiomaterialEntities/NIF-Organism.owl# NIF_Organism:
obd-exec  -d $DB --rename-idspace http://ontology.neuinfo.org/NIF/Backend/BIRNLex-OBI-proxy.owl# NIF_OBI:
obd-exec  -d $DB --rename-idspace file:/Users/cjm/Eclipse/workspace/OBDAPI/conf/obd-birn/all.obo PKB

obd-exec  -d $DB --rename-idspace NIF_core:sao1239937685 OBO_REL:regional_part_of


# TODO:

