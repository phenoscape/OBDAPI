load("obdapi.js");

var nodeId = arguments.shift();

//jdbcPath = "jdbc:postgresql://spade.lbl.gov:5432/obd_phenotype_200805";
jdbcPath = "jdbc:postgresql://localhost:9999/obd_phenotype_200805";

obd = new OBDSQLShard();
obd.connect(jdbcPath);
shard = obd;
aq = new AnnotationLinkQueryTerm(nodeId);
stmts = obd.getStatementsByQuery(aq).toArray();
for (i in stmts) {
  var s = stmts[i];
  print(s);
}
