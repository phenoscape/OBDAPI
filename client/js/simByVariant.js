load("obdapi.js");

/*



*/

var nodeId = arguments.shift();

jdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_mouse";

obd = new OBDSQLShard();
obd.connect(jdbcPath);
shard = obd;

nq = new LinkQueryTerm();
nq.setRelation("OBO_REL:variant_of");
nq.setAspect(QueryTerm.Aspect.TARGET);


q = new LinkQueryTerm("OBO_REL:variant_of", nodeId);

sns = shard.getSimilarNodes(q).toArray();
for (i in sns) {
  nq.setNode(sns[i].getNodeId());
  nodes = shard.getNodesByQuery(nq).toArray();
  nid="?";
  nn="?";
  for (j in nodes) {
    //print(nodes[j]);
    nid=nodes[j].getId();
    nn=nodes[j].getLabel();
  }
  print(nid+" "+nn+" "+sns[i]);
  
}
