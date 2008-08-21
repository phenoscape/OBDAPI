load("obdapi.js");
importPackage(Packages.org.semanticweb.owl.apibinding);
importPackage(Packages.org.semanticweb.owl.io);
importPackage(Packages.org.semanticweb.owl.model);

jdbcPath = arguments.shift();
shard = getShard(jdbcPath);

qt = new LinkQueryTerm();
qt.setInferred(false);
qt.setPositedBy(new ExistentialQueryTerm());
qt.setNode("omim");

statements = shard.getLinkStatementsByQuery(qt);
graph = new Graph();
graph.addStatements(statements);
manager = OWLManager.createOWLOntologyManager();
oo = OWLBridge.graph2owlOntology(graph, manager, null);
fmt = new DefaultOntologyFormat();
OWLBridge.writeOwlOntology(oo, manager, "file:///tmp/omim.owl",fmt);

