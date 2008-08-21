load("obdapi.js");

var nodeId = arguments.shift();

jdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_mouse";

OBDSQLShard obd = new OBDSQLShard();
obd.connect(jdbcPath);
shard = obd;

service = new OBDQueryService();
stmts = service.getAnnotationStatementsForNode(nodeId,null,null);
for (i in stmts) {
  var s = stmts[i];
  print(s);
}
