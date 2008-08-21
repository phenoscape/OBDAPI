package org.obd.test;

import java.util.Collection;

import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Statement;
import org.obd.model.bridge.OWLBridge;
import org.obd.query.LinkQueryTerm;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.DefaultOntologyFormat;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyFormat;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 * tests bridge capabilities for exporting annotations as OWL
 * @author cjm
 *
 */
public class ExportDescriptionsInOWLTest extends AbstractOBDTest {
	
	
	public ExportDescriptionsInOWLTest(String n) {
		super(n);
	}

	public void testWriteOWL() {
		
		// warning: whole database!
	  	LinkQueryTerm dqt = new LinkQueryTerm();
    	dqt.setDescriptionLink(true);
    	//dqt.setNode("MP:0005240");
    	//dqt.setTarget("PATO:0000644");
    	//dqt.setTarget("hyperplastic");
    	Collection<LinkStatement> statements = shard.getLinkStatementsByQuery(dqt);
		Graph graph = new Graph();
		for (Statement s : statements)
			graph.addStatement(s);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology oo = OWLBridge.graph2owlOntology(graph, manager, null);
		OWLOntologyFormat fmt = new DefaultOntologyFormat();
		OWLBridge.writeOwlOntology(oo, manager, "file:///tmp/foo.owl",fmt);

	}

}
