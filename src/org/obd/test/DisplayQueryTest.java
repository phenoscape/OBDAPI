package org.obd.test;

import java.sql.SQLException;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.QueryTerm;

public class DisplayQueryTest extends QueryTest {

	public DisplayQueryTest(String n) {
		super(n);
		// TODO Auto-generated constructor stub
	}
	
	public Graph runNodeQuery(QueryTerm qt, String nid) throws SQLException, ClassNotFoundException {
		System.out.println("exec Q: "+qt.toString());
		Collection<Node> nodes = shard.getNodesByQuery(qt);
		boolean matchFound = false;
		for (Node n : nodes) {
			System.out.println(n);
			if (nid == null || n.getId().equals(nid))
				matchFound = true;
		}
		assertTrue(nodes.size() > 0);
		assertTrue(matchFound);
		Graph g = new Graph();
		g.setNodes(nodes);
		return g;
	}
	
	public Graph old__runNodeQuery(QueryTerm qt) throws SQLException, ClassNotFoundException {
		System.out.println("exec Q: "+qt.toString());
		Graph g = shard.getGraphByQuery(qt, null, null);
		g.nestStatementsUnderNodes();
		for (Node n : g.getNodes()) {
			System.out.println(n);
			for (Statement s : n.getStatements()) {
				System.out.println("    "+s);
			}	
		}
		assertTrue(g.getNodes().length > 0);
		return g;
	}
	

	
	public Graph runLinkQuery(QueryTerm qt, Statement matchStmt) throws SQLException, ClassNotFoundException {
		System.out.println("exec Q: "+qt.toString());
		 Collection<Statement> stmts = shard.getStatementsByQuery(qt);
		 boolean matchFound = false;
		for (Statement s : stmts) {
			System.out.println(" "+s);
			if (matchStmt == null || s.equals(matchStmt) )
				matchFound = true;
		}
		assertTrue(stmts.size() > 0);
		assertTrue(matchFound);
		return new Graph(stmts);
	}

	public Graph runLiteralQuery(QueryTerm qt) throws SQLException, ClassNotFoundException {
		System.out.println("exec Q: "+qt.toString());
		Collection<Statement> stmts = shard.getStatementsByQuery(qt);
		for (Statement s : stmts)
			System.out.println(" "+s);
		assertTrue(stmts.size() > 0);
		return new Graph(stmts);
	}

	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		addTests(suite);
		return suite;
	}


	public static void addTests(TestSuite suite) {
		suite.addTest(new DisplayQueryTest("testQuery"));
	}


}
