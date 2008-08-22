package org.obd.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.obd.model.CompositionalDescription.Predicate;
import org.obd.query.BasicRepository;

/**
 * A collection of nodes and statements
 * <p>
 * 
 *  A graph is simply a holder for Nodes and Statements.
 * Nodes represent instances, types or relations, and the
 * edges of the graph are statements concerning these Nodes.
 *
 * Statement elements can be nested under the Graph element
 * or under a Node element. Semantically there is no difference
 * between these alternate structures.
 * <p>
 * See:
 * {@link <a href="doc-files/obd-xml-xsd.html#element-type-Graph">Graph</a>}

 * @author cjm
 *
 */
public class Graph implements BasicRepository, Serializable {

	protected Map<String,Node> nodeMap = new HashMap<String,Node> ();
	protected Map<String,Collection<Statement>> statementsByNodeMap = new HashMap<String,Collection<Statement>> ();
	protected Collection<Statement> statements;

	public Graph() {
		super();
		clearNodes();
		clearStatements();
	}

	public Graph(Collection<Statement> statements) {
		super();
		setStatements(statements);
	}


	public Graph(Collection<Node> nodes, Collection<Statement> statements) {
		super();
		setNodes(nodes);
		setStatements(statements);
	}

	public Collection<Node> getNodes() {
		//return  (Node[]) nodeMap.values().toArray(new Node[0]);
		return nodeMap.values();
	}

	public Node getNode(String id) {
		return nodeMap.containsKey(id) ? nodeMap.get(id) : null;
	}

	public Node removeNode(String id) {
		Node n = null;
		if (nodeMap.containsKey(id)) {
			n = nodeMap.get(id);
			nodeMap.remove(id);
		}
		return n;
	}



	public Collection<String> getReferencedNodeIds() {
		HashSet<String> nids = new HashSet<String>();
		for (Statement s : getAllStatements()) {
			if (s.getNodeId() != null)
				nids.add(s.getNodeId());
			if (s instanceof LinkStatement && s.getTargetId() != null)
				nids.add(s.getTargetId());
			if (s.getRelationId() != null)
				nids.add(s.getRelationId());
			// exclude posited (we typically already have these)
			//if (s.getPositedByNodeId() != null)
			//	nids.add(s.getPositedByNodeId());
		}
		//return (String[]) nids.toArray(new String[0]);
		return nids;
	}

	public Collection<String> getSubjectIds() {
		HashSet<String> nids = new HashSet<String>();
		for (Statement s : getAllStatements()) {
			if (s.getNodeId() != null)
				nids.add(s.getNodeId());
		}
		//return (String[]) nids.toArray(new String[0]);
		return nids;
	}

	public Collection<String> getTargetIds() {
		HashSet<String> nids = new HashSet<String>();
		for (Statement s : getAllStatements()) {
			if (s.getTargetId() != null)
				nids.add(s.getTargetId());
		}
		//return (String[]) nids.toArray(new String[0]);
		return nids;
	}

	public void addNode(Node n) {
		if (n != null)
			nodeMap.put(n.getId(), n);
	}
	public void addNodes(Collection<Node> nodes) {
		for (Node n : nodes)
			nodeMap.put(n.getId(), n);
	}
	public void addNodes(Node[] nodeArr) {
		HashSet<Node> nodes = new HashSet<Node>();
		nodes.addAll(Arrays.asList(nodeArr));
		addNodes(nodes);
	}
	public void setNodes(Collection<Node> nodes) {
		clearNodes();
		for (Node n : nodes)
			nodeMap.put(n.getId(), n);
	}

	public void clearNodes() {
		nodeMap = new HashMap<String,Node> ();
	}

	/**
	 * all statements not nested under nodes
	 */
	//public Statement[] getStatements() {
	//	return (Statement[])statements.toArray(new Statement[0]);
	//}
	public Collection<Statement> getStatements() {
		return statements;
	}

	/**
	 * all statements not nested under nodes that are LinkStatements
	 */
	public Collection<LinkStatement> getLinkStatements() {
		Collection<LinkStatement> stmts = new LinkedList<LinkStatement>();
		for (Statement s : statements)
			if (s instanceof LinkStatement)
				stmts.add((LinkStatement)s);
		//return (LinkStatement[])stmts.toArray(new LinkStatement[0]);
		return stmts;
	}

	/**
	 * all statements not nested under nodes that are LiteralStatements
	 */
	public LiteralStatement[] getLiteralStatementsAsArr() {
		Collection<LiteralStatement> stmts = new LinkedList<LiteralStatement>();
		for (Statement s : statements)
			if (s instanceof LiteralStatement)
				stmts.add((LiteralStatement)s);
		return (LiteralStatement[])stmts.toArray(new LiteralStatement[0]);
	}
	
	/**
	 * all statements not nested under nodes that are LiteralStatements
	 */
	public Collection<LiteralStatement> getLiteralStatements() {
		Collection<LiteralStatement> stmts = new LinkedList<LiteralStatement>();
		for (Statement s : statements)
			if (s instanceof LiteralStatement)
				stmts.add((LiteralStatement)s);
		return stmts;
	}

	/**
	 * all statements in the graph, whether or not they are nested under nodes
	 * or not
	 */
	public Collection<Statement> getAllStatements() {
		HashSet<Statement> statements = new HashSet<Statement>();
		statements.addAll(getStatements());
		for (Node n : getNodes()) {
			statements.addAll(Arrays.asList(n.getStatements()));
		}
		return statements;
	}

	public Collection<Statement> getStatementsByNode(String id) {
		return getAllStatementsForNode(id);
	}
	public Collection<Statement> getAllStatementsForNode(String id) {
		/*
		HashSet<Statement> statements = new HashSet<Statement>();
		// TODO: index?
		for (Statement s : getAllStatements()) {
			if (s.getNodeId().equals(id))
				statements.add(s);
		}

		return Statement.toUniqueArray(statements);
		 */
		Collection<Statement> statements = new HashSet<Statement>();
		if (statementsByNodeMap.containsKey(id))
			statements = statementsByNodeMap.get(id);
		Node n = getNode(id);
		if (n != null)
			for (Statement s : n.getStatements())
				statements.add(s);
		return statements;
	}
	public Collection<LinkStatement> getAllLinkStatementsForNode(String id) {
		Collection<LinkStatement> statements = new HashSet<LinkStatement>();
		for (Statement s : getAllStatementsForNode(id)) {
			if (s instanceof LinkStatement)
				statements.add((LinkStatement)s);
		}
		return statements;
		//return (LinkStatement[])statements.toArray(new LinkStatement[0]);
		//return LinkStatement.toUniqueArray(statements);
	}
	public Collection<Statement> getStatementsForTarget(String id) {
		Collection<Statement> statements = new HashSet<Statement>();
		// TODO - index
		for (Statement s : getAllStatements()) {
			if (s instanceof LinkStatement && s.getTargetId().equals(id))
				statements.add((LinkStatement)s);
		}
		return statements;
		//return (LinkStatement[])statements.toArray(new LinkStatement[0]);
		//return LinkStatement.toUniqueArray(statements);
	}

	public Collection<LinkStatement> getAllLinkStatementsForTarget(String id) {
		Collection<LinkStatement> statements = new HashSet<LinkStatement>();
		// TODO - index
		for (Statement s : getAllStatements()) {
			if (s instanceof LinkStatement && s.getTargetId().equals(id))
				statements.add((LinkStatement)s);
		}
		return statements;
		//return (LinkStatement[])statements.toArray(new LinkStatement[0]);
		//return LinkStatement.toUniqueArray(statements);
	}
	public Collection<LiteralStatement> getAllLiteralStatementsForNode(String id) {
		Collection<LiteralStatement> statements = new HashSet<LiteralStatement>();
		// TODO: index?
		for (Statement s : getAllStatementsForNode(id)) {
			if (s instanceof LiteralStatement)
				statements.add((LiteralStatement)s);
		}
		return statements;
		//return (LiteralStatement[])statements.toArray(new LiteralStatement[0]);
	}

	private void indexStatement(Statement s) {
		String nid = s.getNodeId();
		if (!statementsByNodeMap.containsKey(nid)) {
			statementsByNodeMap.put(nid, new HashSet<Statement>());
		}
		statementsByNodeMap.get(nid).add(s);	
	}

	private void indexStatements(Collection<Statement> stmts) {
		for (Statement s : stmts) {
			indexStatement(s);
		}
	}

	public void setStatements(Collection<Statement> statements) {
		clearStatements();
		this.statements.addAll(statements);
		indexStatements(statements);
	}



	public void addStatement(Statement statement) {
		if (statements == null)
			statements = new HashSet<Statement>();
		statements.add(statement);
		indexStatement(statement);
	}

	public void addStatements(Collection<Statement> newStatements) {
		if (statements == null)
			statements = new HashSet<Statement>();
		statements.addAll(newStatements);
		indexStatements(statements);
	}

	public void addStatements(Statement[] statements) {
		addStatements(Arrays.asList(statements));
		indexStatements(Arrays.asList(statements));

	}


	/**
	 * removes a statement (must be nested under the graph)
	 * statements nested under nodes are hidden
	 * @param statement
	 * @return
	 */
	public Statement removeStatement(Statement s) {
		statements.remove(s);
		String nid = s.getNodeId();
		statementsByNodeMap.get(nid).remove(s);
		// considered identical if values identical
		return s;
	}

	/**
	 * statements nested under nodes are hidden
	 * @param s
	 * @return
	 */
	public boolean hasStatement(Statement s) {
		return statements.contains(s);
	}



	public void clearStatements() {
		statements = new HashSet<Statement>();
		statementsByNodeMap = new HashMap<String,Collection<Statement>>();
	}

	// shift to frame-style
	public void nestStatementsUnderNodes() {
		for (Statement s : getStatements()) {
			Node n = nodeMap.get(s.getNodeId());
			if (n==null) {
				n = new Node();
				n.setId(s.getNodeId());
				addNode(n);
			}
			n.addStatement(s);
		}
		clearStatements();
	}

	// shift to axiom-style
	public void nestStatementsUnderGraph() {
		for (Node n : getNodes()) {
			for (Statement s : n.getStatements()) {
				addStatement(s);
			}
			n.clearStatements();
		}
	}

	public CompositionalDescription getCompositionalDescription(String id) {
		CompositionalDescription d = new CompositionalDescription();
		for (Statement s : getAllStatementsForNode(id)) {
			if (s instanceof LinkStatement) {
				LinkStatement ls = (LinkStatement)s;
				if (ls.intersectionSemantics ||
						ls.unionSemantics) {
					if (ls.intersectionSemantics)
						d.setPredicate(Predicate.INTERSECTION);
					else
						d.setPredicate(Predicate.UNION);
					String targetId = ls.getTargetId();
					CompositionalDescription subd = getCompositionalDescription(targetId);
					if (ls.isSubClassSemantics()) {
						d.addArgument(subd);
					}
					else {
						CompositionalDescription r = new CompositionalDescription(Predicate.RESTRICTION);
						r.addArgument(subd);
						r.setRestriction(ls);
						d.addArgument(r);
					}
				}
			}
		}
		if (d.getPredicate() == null)
			d.setPredicate(Predicate.ATOM);
		if (!d.isValid())
			return null;
		d.setNodeId(id);
		return d;
	}

	public void merge(Graph g2) {
		if (g2==null)
			return;
		addNodes(g2.getNodes());
		addStatements(g2.getStatements());
	}

	public Graph clone() {
		Graph g = new Graph();
		g.merge(this);
		return g;
	}

	public Graph mutableDiff(Graph subtrahendGraph) {
		Graph igraph = new Graph(); // intersection graph
		Graph uniqueToSubtrahendGraph = new Graph();

		this.nestStatementsUnderGraph();
		subtrahendGraph.nestStatementsUnderGraph();

		for (Node n2 : subtrahendGraph.getNodes()) {
			String n2id = n2.getId();
			Node nd = removeNode(n2id);
			if (nd == null)
				uniqueToSubtrahendGraph.addNode(n2);
			else
				igraph.addNode(nd);
		}
		for (Statement s2 : subtrahendGraph.getStatements()) {
			Statement sd = removeStatement(s2);
			if (sd == null)
				uniqueToSubtrahendGraph.addStatement(s2);
			else
				igraph.addStatement(sd);
		}
		return igraph;
	}



	public void addStatements(CompositionalDescription desc) {
		if (desc.isAtomic())
			return;
		if (desc.getId() == null)
			desc.setId(desc.generateId());
		String id = desc.getId();
		for (CompositionalDescription d : desc.getArguments()) {
			LinkStatement s = new LinkStatement();
			if (desc.getSourceId() != null)
				s.setSourceId(desc.getSourceId());
			s.setNodeId(id);
			// TODO - src
			if (d.getPredicate().equals(Predicate.RESTRICTION)) {
				s.setRelationId(d.getRelationId());
				CompositionalDescription target = d.getFirstArgument();
				s.setTargetId(target.generateId());
				addStatements(target);
			}
			else {
				s.setRelationId("OBO_REL:is_a");
				s.setTargetId(d.generateId());
				addStatements(d);
			}
			if (d.getPredicate().equals(Predicate.UNION))
				s.setUnionSemantics(true);
			else
				s.setIntersectionSemantics(true);
			addStatement(s);
		}

	}

	public Collection<String> simpleClosure(String seedId, int numSteps) {
		Collection<String> tids = new HashSet<String>();
		Collection<String> seeds = new HashSet<String>();
		Collection<String> doneIds = new HashSet<String>();
		seeds.add(seedId);
		while (numSteps > 0 && seeds.size() > 0) {
			numSteps--;
			Collection<String> newIds = new HashSet<String>();
			for (String id : seeds) {

				for (LinkStatement s : getAllLinkStatementsForNode(id)) {
					String tid = s.getTargetId();
					newIds.add(tid);
				}
			}
			tids.addAll(newIds);
			if (numSteps > 0) {
				seeds.addAll(newIds);
				seeds.removeAll(doneIds);
			}
			doneIds.addAll(seeds);

		}
		return tids;
	}

	public void storeSimpleClosure(int numSteps) {
		Collection<String> tids = new HashSet<String>();
		Collection<String> seeds = new HashSet<String>();
		//Map<String,Collection<LinkStatement>> sByNodeId = new HashMap<String,Collection<LinkStatement>>();
		for (Statement s : getAllStatements()) {
			if (s instanceof LinkStatement) {
				LinkStatement ls = (LinkStatement) s;
				String nid = ls.getNodeId();
				String tid = ls.getTargetId();
				seeds.add(nid);
				seeds.add(tid);
				/*
				if (!sByNodeId.containsKey(nid))
					sByNodeId.put(nid, new HashSet<LinkStatement>());
				sByNodeId.get(nid).add(ls);
				*/
			}
		}
		//System.out.println("keys:"+sByNodeId.keySet().size());
		while (numSteps > 1 && seeds.size() > 0) {
			numSteps--;
			//for (String id : sByNodeId.keySet()) {
			for (String id : seeds) {

						
				Collection<String> doneIds = new HashSet<String>();

				Collection<LinkStatement> newStmts =
					new HashSet<LinkStatement>();

				for (LinkStatement ls : getAllLinkStatementsForNode(id)) {
					String tid = ls.getTargetId();
					
					// have we done this target on this iteration?
					if (doneIds.contains(tid))
						continue;
					doneIds.add(tid);

					//if (!sByNodeId.containsKey(tid))
					//	continue;
					for (LinkStatement ls2 : getAllLinkStatementsForNode(tid)) {
						String t2id = ls2.getTargetId();
						if (doneIds.contains(t2id))
							continue;
						String relId = ls.getRelationId() + "*" + ls2.getRelationId();
						LinkStatement newStmt = 
							new LinkStatement(id,relId,t2id);
						newStmts.add(newStmt);
						//System.out.println("newStmt="+newStmt);
						//doneIds.add(t2id);
						seeds.add(t2id);
					}
				}
				for (Statement s : newStmts)
					addStatement(s);
				//addStatements(newStmts);
				//statements.addAll(newStmts);
				//sByNodeId.get(id).addAll(newStmts);
			}
		}
	}
}
