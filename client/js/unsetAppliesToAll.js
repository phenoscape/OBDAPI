load("obdapi.js");

var rel = arguments.shift();

jdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_all";

obd = new OBDSQLShard();
obd.connect(jdbcPath);
shard = obd;
obd.unsetLinkAppliesToAllForRelation(from,to);
