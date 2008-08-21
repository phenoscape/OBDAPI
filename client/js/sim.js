load("obdapi.js");

var ae = arguments.shift();

service = new OBDQueryService();
sns = service.getSimilarNodes(ae);
//sns = service.getSimilarNodes("Eya1<tm1Rilm>/Eya1<tm1Rilm>");
print(sns.length);
for (i in sns) {
  var sn = sns[i];
  print(sn.getNodeId()+" "+sn.getScore());
}
		
