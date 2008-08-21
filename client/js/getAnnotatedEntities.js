load("obdapi.js");

var nodeId = arguments.shift();

service = new OBDQueryService();
g = service.getAnnotationGraphAroundNode(nodeId,null,null);
printGraph(g);
