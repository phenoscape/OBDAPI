package org.obd.test;

import java.util.Collection;

import org.obd.model.Graph;
import org.obd.model.Statement;
import org.obd.model.bridge.OWLBridge;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

/**
 * tests bridge capabilities for exporting annotations as OWL
 * @author cjm
 *
 */
public class OWLGraphTest extends AbstractOBDTest {
	
	
	public OWLGraphTest(String n) {
		super(n);
	}

	public void testWriteOWL() {
		
		//Graph graph = shard.getAnnotationGraphAroundNode("CL:0000148", null, null);
		Collection<Statement> stmts = shard.getStatementsForNode("CL:0000148");
		Graph graph = new Graph(stmts);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology oo = OWLBridge.graph2owlOntology(graph, manager, null);
		OWLBridge.writeOwlOntology(oo, manager, "file:///tmp/foo.owl");

	}

}
