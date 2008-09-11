package org.obd.test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obd.model.Graph;
import org.obd.model.HomologyView;
import org.obd.model.LinkStatement;
import org.obd.model.Statement;

/**
 * Tests annotation metadata requirements are fulfilled
 * 
 * Annotation metadata is modeled as sub-statements on a statement.
 * See {@link Statement#getSubStatements}
 * 
 * @author cjm
 *
 */
public class GraphQueryTest extends AbstractOBDTest {

	public GraphQueryTest(String n) {
		super(n);
	}



	public void testGraphQuery() {
		Collection<String> qnids = new HashSet<String>();
		qnids.add("GO:0006915");
		qnids.add("GO:0007067 ");
		
		Graph g = shard.getGraphByNodes(qnids, null);
		printGraph(g);
		Map<String, Set<String>> cmap = g.getClosureMap();
		assertTrue(cmap.get("GO:0006915").contains("GO:0008150"));
		assertTrue(true);
	}


}
