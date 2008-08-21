load("obdapi.js");

var shard = getShardFromArgs(arguments);
var nid = arguments.shift();
var stmts;

var node = shard.getNode(nid);
print("NODE: "+node);

print("Statements about this node:");
stmts = shard.getStatementsForNode(nid,false).toArray();
printStatements(stmts.filter(function(s){return !s.isAnnotation()}));

print("Statements to this node:");
stmts = shard.getStatementsForTarget(nid,false).toArray();
printStatements(stmts.filter(function(s){return !s.isAnnotation()}));

print("ANNOTATIONS");
print("Annotations (to this node):");
stmts = shard.getAnnotationStatementsForNode(nid,null,null).toArray();
printStatements(stmts);

print("Annotations (about this node):");
stmts = shard.getAnnotationStatementsForAnnotatedEntity(nid,null,null).toArray();
printStatements(stmts);

