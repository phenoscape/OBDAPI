package org.obd.test;

import java.util.HashSet;

import junit.framework.TestCase;

import org.obd.model.Graph;
import org.obd.model.Statement;

public class MiniTest extends TestCase {


	
	
	public MiniTest(String n) {
		super(n);
	}


		
	public void testQuery() {
		Graph g= new Graph();
		g.addStatement(testStatement());
		g.addStatement(testStatement());
		System.out.println("Len="+g.getStatements().size());
		assertTrue(g.getStatements().size()  == 1);
		HashSet<Statement> set = new HashSet<Statement>();
		set.add(testStatement());
		set.add(testStatement());
		System.out.println("Set Len="+set.size());
		assertTrue(set.size() == 1);

	}
	
	public Statement testStatement() {
		Statement s = new Statement();
		s.setNodeId("x");
		s.setRelationId("y");
		s.setTargetId("z");
		System.out.println(s);
		return s;
	}
	
}
