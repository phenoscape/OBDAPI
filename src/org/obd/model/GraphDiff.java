package org.obd.model;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Collection of deltas between two {@link Graph}s
 * @author cjm
 *
 */
public class GraphDiff {
	
	protected boolean isDiffed = false;
	protected Graph graph1;
	protected Graph graph2;

	protected Graph intersectionGraph;
	protected Graph uniqueGraph1;
	protected Graph uniqueGraph2;

	public GraphDiff() {
		super();
	}
	public GraphDiff(Graph minuendGraph, Graph subtrahendGraph) {
		super();
		this.graph1 = minuendGraph;
		this.graph2 = subtrahendGraph;
		diff();
	}
	public Graph getGraph1() {
		return graph1;
	}
	public void setGraph1(Graph minuendGraph) {
		this.graph1 = minuendGraph;
		isDiffed = false;
	}
	public Graph getUniqueGraph2() {
		return uniqueGraph2;
	}
	public void setUniqueGraph2(Graph notSubtractedGraph) {
		this.uniqueGraph2 = notSubtractedGraph;
	}
	public Graph getIntersectionGraph() {
		return intersectionGraph;
	}
	public void setIntersectionGraph(Graph outputGraph) {
		this.intersectionGraph = outputGraph;
	}
	public Graph getUniqueGraph1() {
		return uniqueGraph1;
	}
	public void setUniqueGraph1(Graph subtractedGraph) {
		this.uniqueGraph1 = subtractedGraph;
	}
	public Graph getGraph2() {
		return graph2;
	}
	public void setGraph2(Graph subtrahendGraph) {
		this.graph2 = subtrahendGraph;
		isDiffed = false;
	}

	public void diff() {
		if (isDiffed)
			return;
		this.uniqueGraph1 = (Graph) graph1.clone();
		this.intersectionGraph = new Graph(); // intersection graph
		this.uniqueGraph2 = new Graph();

		graph1.nestStatementsUnderGraph();
		graph2.nestStatementsUnderGraph();
		
		// ug1(n) unless n in g2
		// ig(n) if n in g1 and g2
		// ug2(n) if n in g2 and not in g1
		for (Node n2 : graph2.getNodes()) {
			String n2id = n2.getId();
			Node nd = graph1.getNode(n2id);
			if (nd == null)
				uniqueGraph2.addNode(n2);
			else {
				uniqueGraph1.removeNode(n2id);
				intersectionGraph.addNode(nd);
			}
		}
		for (Statement s2 : graph2.getStatements()) {
			if (graph1.hasStatement(s2))
				uniqueGraph2.addStatement(s2);
			else {
				uniqueGraph1.removeStatement(s2);
				intersectionGraph.addStatement(s2);
			}
		}
		isDiffed = true;
	}
	
	public Collection<String> diffLines() {
		diff();
		Collection<String> diffs = new LinkedList<String>();
		diffs.addAll(uniqLines(graph1, "unique to graph1"));
		diffs.addAll(uniqLines(graph2, "unique to graph2"));
		return diffs;
	}
	
	public Collection<String> uniqLines(Graph g, String prefix) {
		Collection<String> diffs = new LinkedList<String>();
		for (Node n : g.getNodes()) {
			diffs.add(prefix+": "+n);
		}
		for (Statement s : g.getStatements()) {
			diffs.add(prefix+": "+s);
		}
		return diffs;
	}
	
	

}
