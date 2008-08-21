package org.obd.test;

import java.util.Collections;
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
public class GraphSeedTest extends AbstractOBDTest {

	public GraphSeedTest(String n) {
		super(n);
	}

	public void txxestSeed() {
		String id = "MP:0001008"; // abnormal sympathetic ganglion morphology
		Set<String> seeds = Collections.singleton(id);
		Graph g = shard.getGraphFromSeeds(seeds, null);
		String tid = "MA:0002406"; // ganglion [note currently SG not is_a G in MA]
		String xid = "MA:0000226"; // sympathetic ganglion
		
		boolean hasNode = true;
		boolean ok = true;
		for (LinkStatement s : g.getLinkStatements()) {
			if (s.getTargetId().equals(tid))
				ok = true;
			if (g.getNode(s.getNodeId()) == null) {
				hasNode = false;
			}
		}
		assertTrue(ok);
		assertTrue(hasNode);

		boolean found = false;
		for (LinkStatement s : g.getAllLinkStatementsForNode(id)) {
			if (s.getTargetId().equals(tid))
				found = true;
		}
		assertTrue(!found); // may change as MA changes

		found = false;
		for (String x : g.simpleClosure(id, 2)) {
			if (x.equals(tid))
				found = true;
		}
		assertTrue(found);
		
		found = false;
		for (LinkStatement s : g.getAllLinkStatementsForNode(xid)) {
			//System.out.println(g.getNode(s.getTargetId()));
			if (s.getTargetId().equals(tid))
				found = true;
		}
		assertTrue(found);

		g.storeSimpleClosure(2);
		found = false;
		for (LinkStatement s : g.getAllLinkStatementsForNode(id)) {
			System.out.println(g.getNode(s.getTargetId()));
			if (s.getTargetId().equals(tid))
				found = true;
		}
		assertTrue(found); // may change as MA changes
		
	}		

	public void testSimpleClosure() {
		HomologyView hv = new HomologyView(shard);
		String id = "ZFIN:ZDB-GENO-980202-1196";
		Graph g = hv.fetchAnnotationGraph(id);
		System.out.println("closure of "+id);
		for (Statement s : g.getAllStatements())
			System.out.println(s);
		//shard.simpleClosureOver(g, id, hv.)
		g.storeSimpleClosure(2);
		boolean ok = false;
		for (Statement s : g.getAllStatementsForNode(id)) {
			System.out.println(s);
			if (s.getTargetId() != null && s.getTargetId().equals("NCBITaxon:7955")) {
				ok = true;
				System.out.println(g.getNode(s.getTargetId()));
			}
		}
		assertTrue(ok);
	}


}
