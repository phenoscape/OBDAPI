load("obdapi.js");

var from = arguments.shift();
var to = arguments.shift();

jdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_all";

obd = new OBDSQLShard();
obd.connect(jdbcPath);
shard = obd;
obd.switchRelationIdGlobally(from,to);
