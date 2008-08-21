importPackage(java.io);
importPackage(Packages.org.obd.util);
importPackage(Packages.org.obd.query);
importPackage(Packages.org.obd.query);
importPackage(Packages.org.obd.query.impl);
importPackage(Packages.org.obd.ws);
importPackage(Packages.org.obd.model);
importPackage(Packages.org.obd.model.bridge);
importPackage(Packages.org.obd.model.impl);

function getShard(jdbcPath) {

  var obd = new OBDSQLShard();
  obd.connect(jdbcPath);
  return obd;
}

function getShardFromArgs(args) {
  var shard;
  if (args[0] == '-d') {
    args.shift();
    shard = getShard(args.shift());
  }
  return shard;
}

function serializeNamespace(x) {
  return " [" + x.getNamespace() + "]";
}

function printGraph(g) {
  nodes = g.getNodes();
  for (i in nodes) {
    var n = nodes[i];
    print(n);
    stmts = n.getStatements();
    for (j in stmts) {
      print("  "+stmts[j]);
    }
  }	
}

/**
 * Query Builder
 */	

function lq() {
  a = arguments;
  l = a.length;
  target = a[l-1];
  rel = a[l-2];
  node = a[l-3];
  return new LinkQueryTerm(node, rel,target);
}

function labelq(alias,term,op) {
  if (!alias) { alias = "PRIMARY_NAME" }
  var aliasType = eval("LabelQueryTerm.AliasType."+alias);

  if (!op) { op = "EQUAL_TO" }
  var opType = eval("ComparisonQueryTerm.Operator."+op);

  //var cq = new ComparisonQueryTerm("ComparisonQueryTerm.Operator."+op, term);
  var q = new LabelQueryTerm(aliasType, term, opType);
  //q.setAliasType(aliasType);
  //q.setOperator(opType);
  return q;
}

function testarg() {
  return new LinkQueryTerm(arguments);
}

function getCongruence(shard,gene) {
  conn = shard.getConnection();
  nodes = shard.getNodesByQuery(labelq("",gene,"")).toArray();
  print(nodes);
  for (i in nodes) {
    var n = nodes[i];
    print(n);
    print(n.getId() + " :: " +n.getLabel());
    cps = shard.fetchAnnotationSourceCongruenceForAnnotatedEntity(n.getId()).toArray();
    for (j in cps) {
      var cp = cps[j];
      print("  "+cp);
    }
  }
  return nodes;
}

function printtab(tab,msg) {
  print("  ",msg);
}

function printStatement(s) {
  var tab = 1;
  print("[");
  printtab(tab,s.getNodeId());
  printtab(tab,s.getRelationId());
  printtab(tab,s.getTargetId());
  printtab(tab,s.isInferred() ? "[IMPLIED]" :"");
  print("]");
}
function printStatements(stmts) {
  for (i in stmts) {
    var s = stmts[i];
    printStatement(s);
  }
}
