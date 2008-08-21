load("obdapi.js");

jdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_all";
//jdbcPath = "jdbc:postgresql://localhost:5432/obdtest";

obd = new OBDSQLShard();
obd.connect(jdbcPath);
shard = obd;
obd.unsetLinkAppliesToAllForMetadataRelations();
