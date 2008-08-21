load("obdapi.js");

var q = arguments.shift();
print("q="+q);

if (q == null) {
  print("./jsobd query.js 'new AnnotationLinkQueryTerm(\"MP:0003674\")'");
 }

//jdbcPath = "jdbc:postgresql://spade.lbl.gov:5432/obd_phenotype_200805";
jdbcPath = "jdbc:postgresql://localhost:9999/obd_phenotype_200805";

obd = new OBDSQLShard();
obd.connect(jdbcPath);
shard = obd;
aq = eval(q);
stmts = obd.getStatementsByQuery(aq).toArray();
for (i in stmts) {
  var s = stmts[i];
  print(s);
}
