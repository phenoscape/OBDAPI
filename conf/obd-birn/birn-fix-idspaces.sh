#!/bin/bash
DB=$1
obd-exec  -d $DB --rename-idspace http://purl.org/obo/owl/obo# OBO_REL:
obd-exec  -d $DB --rename-idspace http://www.obofoundry.org/ro/ro.owl# OBO_REL:
obd-exec  -d $DB --rename-idspace http://www.ifomis.org/bfo/1.1/snap# snap:
obd-exec  -d $DB --rename-idspace http://www.ifomis.org/bfo/1.1/span# span:
obd-exec  -d $DB --rename-idspace http://www.ifomis.org/bfo/1.1# bfo
obd-exec  -d $DB --rename-idspace __file:///Users/cjm/.blip/data_cache/http:::ccdb.ucsd.edu:SAO:DPO:1.0:HumanDPO.owl# _SAO_HumanDPO:

# TODO:

