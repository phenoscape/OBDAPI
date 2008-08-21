load("obdapi.js");

var nodeId = arguments.shift();

service = new OBDQueryService();
qstr = arguments.shift;
query = eval(qstr);
print(query);
print(query());
print(query.toString());
print(query().toString());
stmts = service.getStatementsByQuery(query);
for (i in stmts) {
  var s = stmts[i];
  print(s);
}
