package org.obd.test;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.obd.model.Graph;
import org.obd.model.Statement;
import org.obd.query.LinkQueryTerm;
import org.obd.query.impl.MutableOBOSessionShard;

/**
 * tests ability to write to wrap an OBOSession rather than a database
 * @author cjm
 *
 */
public class OBOSessionWrapTest extends AbstractOBDTest {

	MutableOBOSessionShard shard;
	
	public OBOSessionWrapTest(String n) {
		super(n);
	}
	
	public static void addTests(TestSuite suite) {
		suite.addTest(new OBOSessionWrapTest("getDescriptionsTest"));
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		addTests(suite);
		return suite;
	}
	
	public void setUp() throws Exception {
		System.out.println("Setting up: " + this);
		shard = new MutableOBOSessionShard();
		String path = getResourcePath()+"/so.obo";
		shard.loadFile(path);
	}
	
	public void getDescriptionsTest() {
		LinkQueryTerm qt = new LinkQueryTerm();
		qt.setDescriptionLink(true);
		Collection<Statement> stmts = shard.getStatementsByQuery(qt);
		boolean ok = true;
		for (Statement s : stmts) {
			System.out.println(s);
			if (!s.isIntersectionSemantics())
				ok = false;
		}
		assertTrue(ok);
		assertTrue(stmts.size() > 0);
		
		// now try with Graph
		// actual matching statements will be nested under graph
		// other node statements under node
		// TODO
		Graph g = shard.getGraphByQuery(qt, null, null);
		printGraph(g);
		
		//
		for (Statement s : g.getStatements()) {
			System.out.println(s);
			if (!s.isIntersectionSemantics())
				ok = false;
		}
		assertTrue(ok);
		assertTrue(g.getStatements().length > 0);

		assertTrue(true);
	}
	
	protected String getResourcePath() {
		return "test_resources";
	}



}
