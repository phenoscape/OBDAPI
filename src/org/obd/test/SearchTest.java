package org.obd.test;

import java.util.Collection;

import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.LiteralQueryTerm;
import org.obd.query.ComparisonQueryTerm.Operator;

/**
 * tests search capabilities for entities of interest
 * @author cjm
 *
 */
public class SearchTest extends AbstractOBDTest {

	
	public SearchTest(String n) {
		super(n);
	}
	
	public void testFetchLiteral() {
		String T_CELL = "CL:0000084";
		LiteralQueryTerm qt = new LiteralQueryTerm();
		qt.setNode(T_CELL);
		qt.setAlias(true);
		Collection<LiteralStatement> stmts = shard.getLiteralStatementsByQuery(qt);
		showStatements(stmts, 3);
	}

	/**
	 * Requirement: if genes have names like ABC1, find these using queries
	 * such as AB*
	 * @throws Exception
	 */
	public void testSearchQuery() throws Exception {
		
		showNodes(shard.getNodesBySearch("AB", Operator.STARTS_WITH),5);
		showNodes(shard.getNodesBySearch("apoptosis"), 5);
		
	}
	
	public void showNodes(Collection<Node> nodes, int min) {
		for (Node n : nodes) {
			System.out.println(n);
			for (Statement s : n.getStatements()) {
				System.out.println("    "+s);
			}	
			
		}
		assertTrue(nodes.size() > 0);
	}
	
	public void showStatements(Collection<? extends Statement> statements, int min) {
		for (Statement n : statements) {
			System.out.println(n);
				
		}
		assertTrue(statements.size() > 0);
	}
	

}
