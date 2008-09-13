package org.obd.query.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.NodeSet;
import org.obd.model.Statement;
import org.obd.model.Node.Metatype;
import org.obd.model.rule.InferenceRule;
import org.obd.model.stats.SimilarityPair;
import org.obd.model.stats.ScoredNode;
import org.obd.model.stats.AggregateStatistic.AggregateType;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.ExistentialQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.LiteralQueryTerm;
import org.obd.query.NodeSetQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.RootQueryTerm;
import org.obd.query.Shard;
import org.obd.query.SourceQueryTerm;
import org.obd.query.SubsetQueryTerm;
import org.obd.query.AnalysisCapableRepository.SimilaritySearchParameters;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.QueryTerm.Aspect;
import org.obd.query.Shard.EntailmentUse;
import org.obd.query.Shard.GraphTranslation;
import org.obd.query.exception.ShardExecutionException;

/**
 * base class for Shard implementations
 * @author cjm
 *
 */
public abstract class AbstractShard implements Shard {

	public class CongruentPair {
		private Node baseNode;
		private Node targetNode;
		private int totalNodesInCommon;
		private int totalNodes;
		private double congruence;

		public CongruentPair() {
			super();
			// TODO Auto-generated constructor stub
		}

		public Node getBaseNode() {
			return baseNode;
		}

		public void setBaseNode(Node baseNode) {
			this.baseNode = baseNode;
		}

		public double getCongruence() {
			return congruence;
		}

		public void setCongruence(double congruence) {
			this.congruence = congruence;
		}

		public Node getTargetNode() {
			return targetNode;
		}

		public void setTargetNode(Node targetNode) {
			this.targetNode = targetNode;
		}

		public int getTotalNodes() {
			return totalNodes;
		}

		public void setTotalNodes(int totalNodes) {
			this.totalNodes = totalNodes;
		}

		public int getTotalNodesInCommon() {
			return totalNodesInCommon;
		}

		public void setTotalNodesInCommon(int totalNodesInCommon) {
			this.totalNodesInCommon = totalNodesInCommon;
		}

		public String toString() {
			return baseNode + " <-> " + targetNode + " total:" + totalNodes
			+ " in_common:" + totalNodesInCommon + " congruence:"
			+ congruence;
		}

	}

	public Node getNode(String id) {
		Collection<Node> nodes = getNodesByQuery(new LabelQueryTerm(
				AliasType.ID, id));
		if (nodes.size() == 0)
			return null;
		else if (nodes.size() == 1)
			return (Node) nodes.toArray()[0];
		else
			return null; // TODO exception; this should never happen
	}

	public Collection<Node> getNodesBySearch(String searchTerm) throws Exception {
		return getNodesBySearch(searchTerm, Operator.MATCHES);
	}



	public Collection<Node> getNodesBySearch(String searchTerm, ComparisonQueryTerm.Operator op) throws Exception {
		if (op==null){
			op = Operator.MATCHES;
		}
		return getNodesBySearch(searchTerm,op,null,null);
	}

	public Collection<Node> getNodes() {
		return getNodesBySource(null);
	}

	public Collection<Node> getNodesBySource(String srcId) {
		SourceQueryTerm qt = new SourceQueryTerm(srcId);
		return getNodesByQuery(qt);
	}

	public Collection<Node> getNodesBySearch(String searchTerm,	ComparisonQueryTerm.Operator op, String source, AliasType at) throws Exception {
		ComparisonQueryTerm cqt = new ComparisonQueryTerm(op, searchTerm);
		if (op == null){
			op = Operator.CONTAINS_ALL;
		}
		if (at == null){
			at = AliasType.ANY_LABEL;

		} 	
		LabelQueryTerm q = new LabelQueryTerm(at, cqt);	
		if (source != null){
			q.setNodeSource(source);
		}	
		return getNodesByQuery(q);
	}


	public Collection<Node> getRootNodes(String sourceId, String relationId) {
		RootQueryTerm q = new RootQueryTerm();
		q.setRelation(relationId);
		q.setRootSource(sourceId);
		return getNodesByQuery(q);
	}

	public Collection<Node> getRootNodes(String sourceId) {
		return getRootNodes(sourceId, null);
	}

	public Collection<Statement> getStatements(String ns) {
		return getStatementsByQuery(new SourceQueryTerm(ns));
	}

	public Collection<Statement> getStatements() {
		return getStatementsByQuery(new LinkQueryTerm());
	}

	public Collection<Statement> getStatementsByNode(String id) {
		LinkQueryTerm qt = new LinkQueryTerm();
		qt.setNode(id);
		return getStatementsByQuery(qt);
	}

	public Collection<Statement> getStatementsForNodeWithSource(String id,
			String ns) {
		QueryTerm qt = new LinkQueryTerm(id);
		qt.setSource(ns);
		return getStatementsByQuery(qt);
	}
	public Collection<Statement> getStatementsForNode(String nodeId, Boolean isInferred) {
		QueryTerm qt = new LinkQueryTerm(nodeId,null,null);
		qt.setInferred(isInferred);
		return getStatementsByQuery(qt);
	}

	public Collection<Statement> getStatementsForTarget(String nodeId) {
		return getStatementsForTarget(nodeId, null);
	}
	public Collection<Statement> getStatementsForTarget(String nodeId, Boolean isInferred) {
		QueryTerm qt = new LinkQueryTerm(null,null,nodeId);
		qt.setInferred(isInferred);
		return getStatementsByQuery(qt);
	}
	public Collection<Statement> getStatementsForTargetWithSource(String nodeId, String srcId) {
		QueryTerm qt = new LinkQueryTerm(null,null,nodeId);
		qt.setSource(srcId);
		return getStatementsByQuery(qt);
	}

	/**
	 * @param id
	 * @return All statements whose target is subsumed by id
	 * @throws Exception
	 */
	public Collection<Statement> getStatementsAround(String id) throws Exception {
		return getStatementsByQuery(new LinkQueryTerm(new LinkQueryTerm(id)));
	}

	public Collection<Statement> getAnnotationStatementsForNode(String id, EntailmentUse entailment, GraphTranslation strategy) {
		Collection<Statement> stmts = new LinkedList<Statement>();
		stmts.addAll( getLinkStatementsByQuery(new AnnotationLinkQueryTerm(id)) );
		return stmts;
	}

	public Collection<Statement> getAnnotationStatementsForAnnotatedEntity(
			String id, EntailmentUse entailment,
			GraphTranslation strategy) {

		LinkQueryTerm qt = new LinkQueryTerm();
		// we do not want inference up the ontology graph;
		// but we may want other inferences; eg promotion from genotype to gene.
		// in general the former should not be realized in the deductive closure
		if (entailment == null)
			qt.setInferred(null);
		else
			qt.setInferred(false);
		qt.setPositedBy(new ExistentialQueryTerm());

		qt.setNode(id);
		return getStatementsByQuery(qt);
	}

	public Collection<LinkStatement> getAnnotationLinkStatementsForAnnotatedEntity(
			String id, EntailmentUse entailment,
			GraphTranslation strategy) {

		LinkQueryTerm qt = new LinkQueryTerm();
		qt.setInferred(false);
		qt.setPositedBy(new ExistentialQueryTerm());

		qt.setNode(id);
		return getLinkStatementsByQuery(qt);
	}

	/**
	 * for a COI, this builds the following graph:
	 * 
	 * R(A,X) where R'(E,AC),R''(AC,COI)
	 * union
	 * R(A,COI)
	 * 
	 * this is typically large: for all annotated entities in the graph, we
	 * fetch all the surrounding relations, not just those in the path to COI
	 */
	public Graph getAnnotationGraphAroundNode(String id, EntailmentUse entailment, GraphTranslation gea) {

		// find graph around node, outwards
		Collection<Statement> stmts;
		Logger.getLogger("org.obd").info("fetching subgraph: "+id);

		if (gea == null || gea.isIncludeSubgraph()) {
			Graph sg = getGraphAroundNode(id, entailment, gea);
			stmts = sg.getStatements();
		}
		else
			stmts = new LinkedList<Statement>(); // TODO: minimal info?

		Logger.getLogger("org.obd").fine("fetching annotation statements: "+id);
		Collection<Statement> annots = 
			getAnnotationStatementsForNode(id, entailment, null);
		Graph g = new Graph(annots);
		Collection<String> referencedNodeIds = g.getReferencedNodeIds();
		Logger.getLogger("org.obd").fine("fetching referenced nodes: "+id+" num="+
				referencedNodeIds.size());
		for (String nid : referencedNodeIds) {
			Node n = getNode(nid);
			g.addNode(n);
			Collection<Statement> mdLinks = getStatementsForNode(nid,false);
			g.addStatements(mdLinks);
		}
		g.addStatements(stmts);
		g.nestStatementsUnderNodes();
		return g;

	}

	public Collection<LinkStatement> getLinkStatementsByQuery(QueryTerm queryTerm) {
		Collection<LinkStatement> stmts = new LinkedList<LinkStatement>();
		for (Statement s : getStatementsByQuery(queryTerm))
			if (s instanceof LinkStatement)
				stmts.add((LinkStatement)s);
		return stmts;
	}

	public Collection<LiteralStatement> getLiteralStatementsByNode(String nodeID){
		return getLiteralStatementsByNode(nodeID,null);
	}

	public Collection<LiteralStatement> getLiteralStatementsByNode(String nodeID,String relationID){
		LiteralQueryTerm lqt = new LiteralQueryTerm();
		lqt.setNode(nodeID);
		if (relationID != null){
			lqt.setRelation(relationID);
		}
		return getLiteralStatementsByQuery(lqt);
	}

	public Collection<LiteralStatement> getLiteralStatementsByQuery(QueryTerm queryTerm) {
		Collection<LiteralStatement> stmts = new LinkedList<LiteralStatement>();
		for (Statement s : getLiteralStatementsByQuery(queryTerm))
			if (s instanceof LiteralStatement)
				stmts.add((LiteralStatement)s);
		return stmts;
	}


	public List<ScoredNode> getSimilarNodes(String nodeId) {
		return getSimilarNodes(nodeId, null);
	}
	
	public List<ScoredNode> getSimilarNodes(String nodeId, String src) {
		SimilaritySearchParameters params = new SimilaritySearchParameters();
		params.ontologySourceId = src;
		return getSimilarNodes(params, nodeId);
	}

	
	public List<ScoredNode> getSimilarNodes(String nodeId, String ontologySourceId, QueryTerm hitNodeFilter) {
		// use defaults
		return
		getSimilarNodes(new SimilaritySearchParameters(), nodeId, ontologySourceId, hitNodeFilter);
	}


	public List<ScoredNode> getSimilarNodes(SimilaritySearchParameters params, String nodeId, String ontologySrcId, QueryTerm hitNodeFilter) {
		params.ontologySourceId = ontologySrcId;
		params.hitNodeFilter = hitNodeFilter;
		return getSimilarNodes(params, nodeId);
	}
	public List<ScoredNode> getSimilarNodes(SimilaritySearchParameters params, String nodeId) {
		// TODO: use ontologySrcId
		// find annotations for this node, then use this as basis
		Collection<Statement> stmts = 
			getAnnotationStatementsForAnnotatedEntity(nodeId, null, null);
		return getSimilarNodes(params, stmts);
	}
	
	public List<ScoredNode> getSimilarNodes(SimilaritySearchParameters params, QueryTerm nodeQueryTerm) {
		Collection<Node> nodes = this.getNodesByQuery(nodeQueryTerm);
		Collection<Statement> stmts = new LinkedList<Statement>();
		for (Node node : nodes) 
			stmts.addAll(
					getAnnotationStatementsForAnnotatedEntity(node.getId(), null, null));
		return getSimilarNodes(params, stmts);
	}


	public List<ScoredNode> getSimilarNodes(QueryTerm nodeQueryTerm) {
		// TODO Auto-generated method stub
		return getSimilarNodes(new SimilaritySearchParameters(), nodeQueryTerm);
	}

	/**
	 * helper method for getSimilarNodes
	 * 
	 * given a list of attributes (i.e. statements), find nodes with
	 * similar attributes
	 * 
	 * @param stmts
	 * @return
	 */
	private List<ScoredNode> getSimilarNodes(SimilaritySearchParameters params, Collection<Statement> stmts) {

		// TODO: use ontologySrcId and other params
		// find annotations for this node, then use this as basis

		int totalNodes = this.getAnnotatedNodeCount();
		List<ScoredNode> scoredNodes = new LinkedList<ScoredNode>();
		HashMap<String, Double> nodePvalMap = new HashMap<String,Double>();
		HashSet<String> annotClassIds = new HashSet<String>();
		for (Statement stmt : stmts) {
			// TODO : don't count redundant ones
			// E.g. annot to Eye and Organ should only count for Organ
			annotClassIds.add(stmt.getTargetId());
		}

		/*
		 * multiply all probabilities together
		 * note: this biases things in favour of multiple annotations
		 * 
		 * also in favour of more specific annotations; if the focus node is
		 * annotated to "left kidney" annotations to "kidney" will be invisible;
		 * difficult to address this and remain efficient.
		 * 
		 * we can do the reciprocal hits, but this is not guaranteed to catch all
		 * of them
		 */
		for (String annotClassId : annotClassIds) {
			/*
			 * find all nodes annotated to this class: these are candidate
			 * similar nodes. We also use the node count to calculate the frequency
			 * of annotations to this class/attribute
			 */
			Collection<Node> nodes = 
				getAnnotatedEntitiesBelowNodeSet(Collections.singleton(annotClassId)
						, null, null);
			/*
			 * p(class) = numNodes(class)/ totalNodes
			 */
			double p = ((float)nodes.size()) / totalNodes;

			/*
			 * calculate cumulative probability
			 * p(c1) * p(c2) * .. p(cn)
			 */
			for (Node n : nodes) {
				String nid = n.getId();
				if (nodePvalMap.containsKey(nid)) {
					nodePvalMap.put(nid, p * nodePvalMap.get(nid));
				}
				else {
					nodePvalMap.put(nid, p);
				}
			}

		}
		for (String nid : nodePvalMap.keySet())
			scoredNodes.add(new ScoredNode(nid,nodePvalMap.get(nid)));
		Collections.sort(scoredNodes);
		return scoredNodes;
	}


	public Collection<ScoredNode> getCoAnnotatedClasses(QueryTerm qt)
	throws ShardExecutionException {
		Collection<Node> nodes = getNodesByQuery(qt);
		Collection<ScoredNode> sns = new LinkedList<ScoredNode>();
		for (Node node : nodes) {
			sns.addAll(getCoAnnotatedClasses(node.getId()));
		}
		return sns;
	}


	protected static double pvalToScore(double p) {
		return Math.log(p)/Math.log(2);
	}

	public void transferFrom(Shard inShard) {
		Graph g = new Graph();
		g.addNodes(inShard.getNodes());
		putGraph(g);
	}

	public Graph getGraph() {
		Graph g = new Graph();
		g.addNodes(getNodes());
		return g;
	}

	public Graph getGraphAroundNode(String nodeId, EntailmentUse entailment,
			GraphTranslation gea) {

		LinkQueryTerm linksFromSelectedNodeQuery = new LinkQueryTerm();
		linksFromSelectedNodeQuery.setNode(nodeId);
		// we invert the inner query
		// - this means we plug the target variable back in as the main
		//   node variable in the asserted link query
		linksFromSelectedNodeQuery.setAspect(Aspect.TARGET); // all parents of nodeId
		LinkQueryTerm assertedLinkQuery = 
			new LinkQueryTerm(linksFromSelectedNodeQuery,null,null);
		assertedLinkQuery.setInferred(false);
		assertedLinkQuery.setNode(linksFromSelectedNodeQuery); // redundant?
		Logger.getLogger("org.obd").fine("subgraph q="+assertedLinkQuery.toString());

		Collection<Statement> stmts = getStatementsByQuery(assertedLinkQuery);
		Graph g = new Graph();
		Collection<String> nids = new HashSet<String>();
		for (Statement s : stmts) {
			nids.add(s.getNodeId());
			nids.add(s.getRelationId()); // incl relation hierarchy
			g.addStatement(s);
		}
		for (String nid : nids) {
			g.addNode(getNode(nid));
		}
		return g;
	}

	public Graph getGraphByQuery(QueryTerm queryTerm, EntailmentUse entailment, GraphTranslation gea) {

		Collection<Statement> stmts = 
			getStatementsByQuery(queryTerm);
		Graph g = new Graph(stmts);
		for (String nid : g.getReferencedNodeIds()) {
			Node n = getNode(nid);
			g.addNode(n);
			//Collection<Statement> mdLinks = getStatementsForNode(nid,false);
			//g.addStatements(mdLinks);
		}
		g.addStatements(stmts);
		g.nestStatementsUnderNodes();
		return g;
	}

	public Graph getGraphByNodes(Collection<String> qnids, QueryTerm extQt) {

		/*
		 * relies on some kind of reflexive relation for qnids has been set..
		 * 
		 */
		NodeSetQueryTerm nsqt = new NodeSetQueryTerm(qnids);
		LinkQueryTerm lqt = new LinkQueryTerm();
		lqt.setNode(nsqt);
		lqt.setAspect(Aspect.TARGET);
		LinkQueryTerm assertedLinkQuery = 
			new LinkQueryTerm(lqt,null,null);
		//assertedLinkQuery.setInferred(false);
		Logger.getLogger("org.obd").fine("subgraph q="+assertedLinkQuery.toString());
		//System.err.println("subgraph q="+assertedLinkQuery.toString());

		Collection<Statement> stmts = getStatementsByQuery(assertedLinkQuery);
		stmts.addAll(this.getStatementsByQuery(new LinkQueryTerm(nsqt,null,null)));
		Graph g = new Graph();
		Collection<String> nids = new HashSet<String>();
		for (Statement s : stmts) {
			nids.add(s.getNodeId());
			nids.add(s.getRelationId()); // incl relation hierarchy
			g.addStatement(s);
		}
		g.trim();
		for (String nid : nids) {
			g.addNode(getNode(nid));
		}

		return g;
	}


	public Graph getGraphFromSeeds(Collection<String> nodeIds, Collection<String> relationIds) {

		Graph g = new Graph();
		Collection<String> idsToCheck = new HashSet<String>(nodeIds);
		Collection<String> checkedIds = new HashSet<String>();

		// loop until we have completed the closure
		while (idsToCheck.size() > 0) {
			Collection<String> newIds = new HashSet<String>();
			for (String id : idsToCheck) {

				// two parts - get the node itself, then statements. TODO - combine?
				Node node = getNode(id);
				g.addNode(node);

				// find all links that subsume by this node; i.e.
				// R(x,y) where R'(id,x)
				// this should include the reflexive case, e.g. x=y
				// note: this is more efficient than getting parents
				// and adding them to the newIds list
				LinkQueryTerm iqt = new LinkQueryTerm();
				iqt.setNode(id);
				iqt.setAspect(Aspect.TARGET);
				LinkQueryTerm qt = new LinkQueryTerm(iqt,null,null);
				//qt.setInferred(false);
				Collection<LinkStatement> stmts = this.getLinkStatementsByQuery(qt);
				for (LinkStatement s : stmts) {
					g.addStatement(s);
					String nid = s.getNodeId();
					newIds.add(s.getTargetId());
					checkedIds.add(nid);
					Logger.getLogger("org.obd").fine("added: "+nid);
					if (g.getNode(nid) == null) {
						Node n = getNode(nid);
						g.addNode(n);
						Logger.getLogger("org.obd").fine("fetched: "+n);
					}
					if (g.getNode(nid) == null) {
						Logger.getLogger("org.obd").fine("problem:"+nid);
					}					
				}
				if (false) {
					// TODO: make configurable
					// slight performance penalty in fetching all literals
					Collection<LiteralStatement> litstmts = this.getLiteralStatementsByQuery(qt);
					for (LiteralStatement s : litstmts) {
						g.addStatement(s);
					}
				}

				checkedIds.add(id);
			}
			idsToCheck = newIds;
			idsToCheck.removeAll(checkedIds);
		}
		return g;
	}


	public CompositionalDescription getCompositionalDescription(String id, boolean traverseNamedClasses) {

		Graph graph = new Graph();
		buildDescription(id, traverseNamedClasses, graph, 0);
		CompositionalDescription desc = graph.getCompositionalDescription(id);
		desc.setSourceGraph(graph);

		return desc;
	}

	public void buildDescription(String id, boolean traverseNamedClasses,
			Graph graph, int depth) {

		Node node = graph.getNode(id);
		if (graph.getNode(id) == null) {
			// we have not encountered this node already
			node = getNode(id);
			if (node == null) {
				System.err.println("cannot find node: "+id);
			}
			if (!node.isAnonymous() && !traverseNamedClasses && depth > 0)
				return;
			graph.addNode(node);

			LinkQueryTerm q = new LinkQueryTerm();
			q.setNode(id);
			q.setInferred(false);
			q.setDescriptionLink(true);
			Collection<Statement> stmts = getStatementsByQuery(q);
			node.setStatements(stmts);


			for (Statement s : stmts) {
				if (!(s instanceof LinkStatement))
					continue;
				//System.out.println("recursing for desc of "+id+" s="+s);
				String nextId = s.getTargetId();
				if (nextId != null)
					buildDescription(nextId, traverseNamedClasses, graph, depth+1);
			}
		}

	}




	public void simpleClosureOver(Graph graph, String seedId, Collection<String> relationIds, Collection<String> reverseRelationIds) {
		if (graph.getNode(seedId) != null) 
			return;
		// cycles should not be possible beyond this point
		graph.addNode(getNode(seedId));
		if (relationIds == null && reverseRelationIds == null) {
			for (Statement s : getStatementsByNode(seedId)) {
				graph.addStatement(s);
				if (s instanceof LinkStatement)
					simpleClosureOver(graph, s.getTargetId(), relationIds, reverseRelationIds);
			}
			for (Statement s : getStatementsForTarget(seedId)) {
				graph.addStatement(s);
				simpleClosureOver(graph, s.getNodeId(), relationIds, reverseRelationIds);
			}
		}
		else {
			for (String relId : relationIds) {
				for (Statement s : getStatementsByQuery(new LinkQueryTerm(seedId, relId, null))) {
					graph.addStatement(s);
					if (s instanceof LinkStatement)
						simpleClosureOver(graph, s.getTargetId(), relationIds, reverseRelationIds);
				}
			}
			for (String relId : reverseRelationIds) {
				for (Statement s : getLinkStatementsByQuery(new LinkQueryTerm(null, relId, seedId))) {
					graph.addStatement(s);
					simpleClosureOver(graph, s.getNodeId(), relationIds, reverseRelationIds);
				}						
			}
		}
	}



	public Set<String> mapNode(String id, QueryTerm qt) {
		return mapNode(id,qt,false);
	}
	public Set<String> mapNode(String id, QueryTerm qt, boolean countMode) {
		Set<String> mappedIds = new HashSet<String>();
		for (Node n : getNodesByQuery(qt)) {
			mappedIds.add(n.getId());
		}
		if (!countMode) {
			Set<String> excludeIds = new HashSet<String>();
			for (String mappedId : mappedIds) {
				Collection<Statement> stmts = getStatementsByNode(mappedId);
				for (Statement stmt : stmts) {
					String tid = stmt.getTargetId();
					if (tid == null)
						continue;
					if (tid.equals(mappedId))
						continue;
					excludeIds.add(tid); // more general
				}
			}
			mappedIds.removeAll(excludeIds);
		}
		return mappedIds;
	}

	public Map<String,Collection<String>> mapNodes(Collection<String> id, String subsetId) {
		Map<String,Collection<String>> nodeMap = new HashMap<String,Collection<String>>();
		return nodeMap;
	}

	public Set<String> mapNodeToSubset(String id, String subsetId) {
		LinkQueryTerm qt = new LinkQueryTerm();
		qt.setQueryAlias("nodemap");
		qt.setNode(id);
		SubsetQueryTerm ssqt = new SubsetQueryTerm(subsetId);
		qt.setTarget(ssqt);
		ssqt.setQueryAlias("in_subset");
		qt.setAspect(Aspect.TARGET);
		return mapNode(id,qt);
	}

	public Map<String,Collection<LinkStatement>> getAnnotationStatementMapByQuery(QueryTerm queryTerm) {
		return null;
	}

	/**
	 * Given a collection of statements linking nodes in a pairwise
	 * fashion, create a collection of nodesets such that connected
	 * nodes are always in the same nodeset
	 * 
	 * @param stmts
	 * @return
	 */
	public Collection<NodeSet> createNodeSets(Collection<LinkStatement> stmts) {
		return createNodeSetMap(stmts).values();
	}

	public Map<String,NodeSet> createNodeSetMap(Collection<LinkStatement> stmts) {

		Set<NodeSet> nodeSets = new HashSet<NodeSet>();
		Map<String,NodeSet> nodeSetByNodeId = new HashMap<String,NodeSet>();
		Set<String> nids = new HashSet<String>();

		// get all unique nodes
		for (LinkStatement s : stmts) {
			nids.add(s.getNodeId());
			nids.add(s.getTargetId());
		}

		// start off with singleton nodesets
		for (String nid : nids) {
			NodeSet ns = new NodeSet();
			ns.addNode(getNode(nid));
			nodeSetByNodeId.put(nid, ns);
		}

		// merge nodesets if they are connected
		for (LinkStatement s : stmts) {
			String nid = s.getNodeId();
			String tid = s.getTargetId();
			NodeSet nns = nodeSetByNodeId.get(nid);
			NodeSet tns = nodeSetByNodeId.get(tid);
			if (nns != tns) {
				// move everything from target nodeset to subject nodeset
				nns.addNodes(tns.getNodes());
				for (Node n : tns.getNodes())
					nodeSetByNodeId.put(n.getId(), nns);
				nodeSetByNodeId.put(tid, nns);
				// we should now have no way of getting to tns
			}
		}

		return nodeSetByNodeId;
	}



	public Map<Node, Integer> getAnnotatedEntityCountBySubset(AnnotationLinkQueryTerm qt, String subsetId) throws ShardExecutionException {
		if (qt == null)
			qt = new AnnotationLinkQueryTerm();

		SubsetQueryTerm sqt = new SubsetQueryTerm(subsetId);
		qt.setTarget(sqt);
		Collection<Statement> annots = getStatementsByQuery(qt);
		Map<String,Collection<String>> entitiesById = 
			new HashMap<String,Collection<String>>();
		for (Statement annot : annots) {
			String tid = annot.getTargetId();
			Set<String> mappedIds = mapNodeToSubset(tid, subsetId);
			for (String mappedId : mappedIds) {
				if (!entitiesById.containsKey(mappedId))
					entitiesById.put(mappedId,new HashSet<String>());
				entitiesById.get(mappedId).add(annot.getNodeId());
			}
		}
		Map<Node,Integer> countByNode = new HashMap<Node,Integer>();
		for (String nid : entitiesById.keySet()) {
			Node n = getNode(nid);
			countByNode.put(n, entitiesById.get(nid).size());
		}

		return countByNode; // TODO
	}

	public void putCompositionalDescription(CompositionalDescription desc) {
		Graph g = new Graph();
		g.addStatements(desc);
		Node n = new Node(desc.getId());
		n.setMetatype(Metatype.CLASS);
		g.addNode(n);
		putGraph(g);
	}


	public Double getInformationContentByAnnotations(String classNodeId) {
		// TODO
		return null;
	}

	public Collection<ScoredNode> getCoAnnotatedClasses(String classNodeId) throws ShardExecutionException {
		return null;
	}

	public SimilarityPair compareAnnotationsBySourcePair(String aeid, String src1, String src2) {
		LinkQueryTerm extQt = new LinkQueryTerm();
		return compareAnnotationsBySourcePair(aeid, src1, src2,extQt);
	}
	public SimilarityPair compareAnnotationsBySourcePair(String aeid, String src1, String src2, LinkQueryTerm closureQueryTerm) {
		LinkQueryTerm qt = new LinkQueryTerm();
		qt.setNode(aeid);
		qt.setIsAnnotation(true);
		qt.setSource(src1);
		Collection<LinkStatement> stmts1 = getLinkStatementsByQuery(qt);
		qt.setSource(src2);
		Collection<LinkStatement> stmts2 = getLinkStatementsByQuery(qt);

		SimilarityPair sp = compareAnnotationSetPair(stmts1, stmts2, closureQueryTerm);
		sp.setId1(src1);
		sp.setId2(src2);
		return sp;
	}

	public SimilarityPair compareAnnotationsByAnnotatedEntityPair(String aeid1, String aeid2) {
		LinkQueryTerm extQt = new LinkQueryTerm(); // by default,follow all links
		LinkQueryTerm aqt = new LinkQueryTerm(); // by default, do not filter annotations
		return compareAnnotationsByAnnotatedEntityPair(aeid1,aeid2,extQt,aqt);
	}
	public SimilarityPair compareAnnotationsByAnnotatedEntityPair(String aeid1, String aeid2, LinkQueryTerm closureQueryTerm, LinkQueryTerm annotationQueryTerm) {
		annotationQueryTerm.setIsAnnotation(true);

		//		annotationQueryTerm.setNode(aeid1);
		//		Collection<LinkStatement> stmts1 = getLinkStatementsByQuery(annotationQueryTerm); // TODO - optimize - do not need full stmt
		//		annotationQueryTerm.setNode(aeid2);
		//		Collection<LinkStatement> stmts2 = getLinkStatementsByQuery(annotationQueryTerm);	
		// TODO - do the extension at query time; we also can drop the annotation metadata
		// e.g. G inf (P R P2) - fetch the (...)
		annotationQueryTerm.setNode(aeid1);
		Collection<LinkStatement> stmts1 = getLinkStatementsByQuery(annotationQueryTerm); // TODO - optimize - do not need full stmt
		annotationQueryTerm.setNode(aeid2);
		Collection<LinkStatement> stmts2 = getLinkStatementsByQuery(annotationQueryTerm);	

		SimilarityPair sp = compareAnnotationSetPair(stmts1, stmts2, closureQueryTerm);
		sp.setId1(aeid1);
		sp.setId2(aeid2);
		return sp;

	}

	/**
	 * given two sets of annotations, return a SimilarityPair indicating target nodes in common
	 * 
	 * if extQt is not null, then the annotations are extended using this query term
	 * For example, given a set of asserted/direct annotations, we may want to extend these to be
	 * implied annotations
	 * 
	 * @param stmts1
	 * @param stmts2
	 * @param extQt - 
	 */
	protected SimilarityPair compareAnnotationSetPair(Collection<LinkStatement> stmts1, Collection<LinkStatement> stmts2, 
			LinkQueryTerm extQt) {

		SimilarityPair sp = new SimilarityPair();
		if (extQt != null) {
			Collection<LinkStatement> allStmts = new HashSet<LinkStatement>();
			allStmts.addAll(stmts1);
			allStmts.addAll(stmts2);
			Map<String,Set<String>> closureMap = new HashMap<String,Set<String>>();
			Map<String,Set<LinkStatement>> sMap = new HashMap<String,Set<LinkStatement>>();
			stmts1 = extendLinksWithCache(stmts1, extQt, sMap);
			stmts2 = extendLinksWithCache(stmts2, extQt, sMap);
			Collection<String> seedIds = new HashSet<String>();
			for (LinkStatement s : allStmts) {
				seedIds.add(s.getTargetId());
			}
			for (String nid : sMap.keySet()) {
				for (LinkStatement s : sMap.get(nid)) {
					if (!closureMap.containsKey(nid))
						closureMap.put(nid, new HashSet<String>());
					closureMap.get(nid).add(s.getTargetId());
				}
			}
			sp.setClosureMap(closureMap);

			/*

			//System.err.println("seeds: "+seedIds);
			Graph g = this.getGraphByNodes(seedIds, extQt);
			sp.setGraph(g);
			Map<String,Set<String>> closureMap = g.getClosureMap();
			//System.err.println("cmap: "+closureMap);
			System.err.println("annots1: "+stmts1.size());
			System.err.println("annots2: "+stmts2.size());

			//stmts1 = extendLinks(stmts1, extQt, closureMap);
			//stmts2 = extendLinks(stmts2, extQt, closureMap);
			stmts1 = extendLinks(stmts1, closureMap);
			stmts2 = extendLinks(stmts2, closureMap);
			System.err.println("annots1+: "+stmts1.size());
			System.err.println("annots2+: "+stmts2.size());
			 */

			// TODO: put this somewhere reusable
			/*
			for (LinkStatement s : allStmts) {
				if (!closureMap.containsKey(s.getNodeId()))
					closureMap.put(s.getNodeId(), new HashSet<String>());
				closureMap.get(s.getNodeId()).add(s.getTargetId());
				}
			 */

		}
		Set<String> nodesInSet1 = new HashSet<String>();
		Set<String> nodesInSet2 = new HashSet<String>();
		for (LinkStatement s : stmts1) {
			String tid = s.getTargetId();
			nodesInSet1.add(tid);
		}
		for (LinkStatement s : stmts2) {
			String tid = s.getTargetId();
			nodesInSet2.add(tid);
		}
		sp.setFromNodeSetPair(nodesInSet1, nodesInSet2);

		return sp;
	}


	// TODO - make this more efficient
	public void calculateInformationContentMetrics(SimilarityPair sp) {
		double maxIC = 0;
		double sumICinCommon = 0;
		double sumICinUnion = 0;
		String nodeWithMaxIC = null;
		Map<String,Double> nodeEntropyMap = new HashMap<String,Double>();
		// we only calculate for NR nodes
		for (String nid : sp.getPostPartitionedNonRedundantNodesInUnion()) {
			Double ic = getInformationContentByAnnotations(nid);
			sp.setInformationContent(nid, ic);
			sumICinUnion += ic;
			//System.out.println(nid + " IC:"+ic);
		}	
		for (String nid : sp.getNonRedundantNodesInCommon()) {
			Double ic = sp.getInformationContent(nid);
			if (ic > maxIC) {
				maxIC = ic;
				nodeWithMaxIC = nid;
			}
			sumICinCommon += ic;
		}
		double avgIC = sumICinCommon / sp.getTotalNodesInCommon();
		double simGIC = sumICinCommon / sumICinUnion;
		sp.setMaximimumInformationContentForNodesInCommon(maxIC);
		sp.setNodeWithMaximumInformationContent(nodeWithMaxIC);
		sp.setInformationContentRatio(simGIC);

	}

	// implements |x AND y| / 
	public void calculateInformationContentMetricsForAll(SimilarityPair sp) {
		double maxIC = 0;
		double sumICinCommon = 0;
		double sumICinUnion = 0;
		String nodeWithMaxIC = null;
		Map<String,Double> nodeEntropyMap = new HashMap<String,Double>();
		for (String nid : sp.getNodesInUnion()) {
			Double ic = getInformationContentByAnnotations(nid);

			nodeEntropyMap.put(nid, ic);

			sumICinUnion += ic;
			//System.out.println(nid + " IC:"+ic);
		}	
		for (String nid : sp.getNodesInCommon()) {
			Double ic = nodeEntropyMap.get(nid);
			if (ic > maxIC) {
				maxIC = ic;
				nodeWithMaxIC = nid;
			}
			sumICinCommon += ic;
		}
		double avgIC = sumICinCommon / sp.getTotalNodesInCommon();
		double simGIC = sumICinCommon / sumICinUnion;
		sp.setMaximimumInformationContentForNodesInCommon(maxIC);
		sp.setNodeWithMaximumInformationContent(nodeWithMaxIC);
		sp.setInformationContentRatio(simGIC);

	}

	/**
	 * given a collection of links, 
	 * @param linkStatements
	 * @param qt
	 * @return
	 */
	public Collection<LinkStatement> extendLinks(Collection<LinkStatement> stmts, QueryTerm qt) {
		return extendLinks(stmts,  qt, null);
	}
	public Collection<LinkStatement> extendLinksWithCache(Collection<LinkStatement> stmts, QueryTerm extQt, Map<String,Set<LinkStatement>> sMap) {
		for (LinkStatement s : stmts) {
			if (!sMap.containsKey(s.getNodeId()))
				sMap.put(s.getNodeId(), new HashSet<LinkStatement>());
			sMap.get(s.getNodeId()).add(s);
		}
		Collection<LinkStatement> elinks = new LinkedList<LinkStatement>();
		Set<String> seedIds = new HashSet<String>();
		Set<String> newSeedIds = new HashSet<String>();
		for (LinkStatement s : stmts) {
			elinks.add(s);
			seedIds.add(s.getTargetId());
		}
		newSeedIds.addAll(seedIds);
		for (String nid : sMap.keySet()) {
			newSeedIds.remove(nid); // we've seen this
		}
		NodeSetQueryTerm nsqt = new NodeSetQueryTerm(newSeedIds);
		Set<String> extSeedIds = new HashSet<String>();
		Collection<LinkStatement> nlinks = getLinkStatementsByQuery(new LinkQueryTerm(nsqt,null,null));
		for (LinkStatement s : nlinks) {
			if (!sMap.containsKey(s.getNodeId()))
				sMap.put(s.getNodeId(), new HashSet<LinkStatement>());
			sMap.get(s.getNodeId()).add(s);
			if (!sMap.containsKey(s.getTargetId()))
				extSeedIds.add(s.getTargetId());
		}
		Collection<LinkStatement> xlinks = getLinkStatementsByQuery(new LinkQueryTerm(new NodeSetQueryTerm(extSeedIds),null,null));
		for (LinkStatement s : xlinks) {
			if (!sMap.containsKey(s.getNodeId()))
				sMap.put(s.getNodeId(), new HashSet<LinkStatement>());
			sMap.get(s.getNodeId()).add(s);
		}

		for (String nid : seedIds) {
			elinks.addAll(sMap.get(nid));
		}
		return elinks;
	}

	public Collection<LinkStatement> extendLinks(Collection<LinkStatement> stmts, QueryTerm extQt, Map<String,Set<String>> closureMap) {
		Collection<LinkStatement> nu = new LinkedList<LinkStatement>();
		Set<String> seedIds = new HashSet<String>();
		for (LinkStatement s : stmts) {
			nu.add(s);
			seedIds.add(s.getTargetId());
		}
		if (closureMap == null) {
			// caller does not care about the map: do this the most efficient way
			stmts = getClosure(seedIds, extQt);
		}
		else {
			// caller wants their map extended
			stmts = getClosure(seedIds, extQt, closureMap);
			/*
			stmts = getClosure(seedIds, extQt);
			for (LinkStatement s : stmts) {
				if (!closureMap.containsKey(s.getNodeId()))
					closureMap.put(s.getNodeId(), new HashSet<String>());
				closureMap.get(s.getNodeId()).add(s.getTargetId());
			}
			 */
		}
		for (LinkStatement s : stmts) {
			nu.add(s);
		}
		return nu;
	}

	@Deprecated
	public Collection<LinkStatement> extendLinks(Collection<LinkStatement> stmts, Map<String,Set<String>> closureMap) {
		Collection<LinkStatement> elinks = new LinkedList<LinkStatement>();
		Set<String> nids = new HashSet<String>();
		for (LinkStatement s : stmts) {
			elinks.add(s); // reflexive
			if (!closureMap.containsKey(s.getNodeId()))
				continue;
			for (String tid : closureMap.get(s.getNodeId())) {
				LinkStatement elink = new LinkStatement(s);
				elink.setTargetId(tid);
				elinks.add(elink);
			}
		}
		return elinks;
	}

	/* (non-Javadoc)
	 * @see org.obd.query.Shard#getClosure(java.util.Collection, java.lang.String)
	 */
	public Collection<LinkStatement> getClosure(Collection<String> ids, String relId) {
		LinkQueryTerm extQt = new LinkQueryTerm();
		extQt.setRelation(relId);
		return getClosure(ids, extQt);
	}

	/**
	 * given node ids {n : n1,n2,...}
	 * find all statements s: [n r t] such that s matches extQt
	 * @param ids - node identifiers
	 * @param extQt - closure extension query
	 * @return
	 */
	public Collection<LinkStatement> getClosure(Collection<String> ids, QueryTerm extQt) {
		extQt.setNode(new NodeSetQueryTerm(ids));
		return getLinkStatementsByQuery(extQt);
	}

	/**
	 * as above, but augments a map
	 * @param ids
	 * @param extQt
	 * @param map
	 * @return
	 */
	public Collection<LinkStatement> getClosure(Collection<String> ids, QueryTerm extQt, Map<String,Set<String>> map) {
		// calculating the closure for a set of IDs in a database is expensive: if we have some IDs
		// in the closure map already, we don't need to recompute
		// this assumes the closureMap is invariant w.r.t extQt - but we can't check this, 
		// up to the caller to be safe
		HashSet<String> newIds = new HashSet<String>();
		newIds.addAll(ids);
		Collection<LinkStatement> cachedStmts = new HashSet<LinkStatement>();
		for (String id : map.keySet()) {
			newIds.remove(id);
			for (String tid : map.get(id))
				cachedStmts.add(new LinkStatement(id,null,tid));
		}
		extQt.setNode(new NodeSetQueryTerm(newIds));
		Collection<LinkStatement> stmts = getLinkStatementsByQuery(extQt);

		// ids for a further iteration to get the inter-set closure; see below
		Set<String> nextIds = new HashSet<String>();
		// augment the map
		for (Statement s : stmts) {
			String nid = s.getNodeId();
			String tid = s.getTargetId();
			if (!map.containsKey(nid)) {
				map.put(nid, new HashSet<String>());
			}
			map.get(nid).add(tid);
			// see below
			if (!newIds.contains(tid))
				nextIds.add(tid);
		}

		// there may be a more efficient way to do this...
		// we have the closure map for the initial input set, but not the
		// inter-set closure map;
		// e.g. if the initial set is {a, b, c}
		// and the closure contained a->d, b->e
		// we don't know anything about the relation between d and e
		extQt.setNode(new NodeSetQueryTerm(nextIds));
		Collection<LinkStatement> nextStmts = getLinkStatementsByQuery(extQt);
		for (Statement s : nextStmts) {
			String nid = s.getNodeId();
			if (!map.containsKey(nid)) {
				map.put(nid, new HashSet<String>());
			}
			map.get(nid).add(s.getTargetId());
		}
		stmts.addAll(cachedStmts);

		return stmts;
	}

	public Map<String,Set<String>> getClosureMap(Collection<String> ids, String relId) {
		QueryTerm extQt = new LinkQueryTerm();
		extQt.setRelation(relId);
		return getClosureMap(ids, extQt);
	}
	public Map<String,Set<String>> getClosureMap(Collection<String> ids, QueryTerm extQt) {
		Map<String,Set<String>> map = new HashMap<String,Set<String>>();
		return getClosureMap(ids, extQt, map);
	}
	/**
	 * This extends the existing closureMap. If ids contains s1,s2,...,sn
	 * and the shard has s1->s1a,s1b,... etc
	 * these will be added to the map
	 * 
	 * passing in an existing closureMap will both extend that map, and also use the map
	 * to efficiently query the shard. This means the extQt must be invariant over multiple calls.
	 * It is up to the caller to be safe here
	 * @param ids
	 * @param extQt
	 * @param map - new keys and values will be added
	 * @return
	 */
	public Map<String,Set<String>> getClosureMap(Collection<String> ids, QueryTerm extQt, Map<String,Set<String>> map) {
		// calculating the closure for a set of IDs in a database is expensive: if we have some IDs
		// in the closure map already, we don't need to recompute
		// this assumes the closureMap is invariant w.r.t extQt - but we can't check this, 
		// up to the caller to be safe
		HashSet<String> newIds = new HashSet<String>();
		newIds.addAll(ids);
		for (String id : map.keySet())
			newIds.remove(id);
		Collection<LinkStatement> stmts = getClosure(newIds, extQt);
		for (LinkStatement s : stmts) {
			String nid = s.getNodeId();
			if (!map.containsKey(nid))
				map.put(nid, new HashSet<String>());

			map.get(nid).add(s.getTargetId());
		}

		return map;
	}


	public Collection<Graph> mapGraph(Graph g, QueryTerm mapQt) {
		Collection<Graph> mappedGraphs = new LinkedList<Graph>();
		Map<String, String> nodeIdMap = new HashMap<String, String>();
		Collection<String> nids = g.getReferencedNodeIds();
		for (String nid : nids) {
			// TODO
		}
		return mappedGraphs;
	}

	public CompositionalDescription mapCompositionalDescription(
			CompositionalDescription d, QueryTerm mapQt) {
		CompositionalDescription mappedDesc = new CompositionalDescription();
		if (d.isAtomic()) {
			String nid = d.getNodeId();
			mapQt.setNode(nid);
			Collection<Node> mappedNodes = getNodesByQuery(mapQt);
			for (Node n : mappedNodes) {
				// substitute in place or ...?
			}
		}
		for (CompositionalDescription arg : d.getArguments()) {
			CompositionalDescription mappedArg = mapCompositionalDescription(
					arg, mapQt);
			mappedDesc.addArgument(mappedArg);
		}
		return mappedDesc;
	}

	public QueryTerm mapQueryTerm(QueryTerm qt, QueryTerm mapQt) {
		QueryTerm mappedQt = null;
		return mappedQt;
	}


	public Boolean includesEntailedStatements() {
		return false;
	}

	public void removeMatchingStatements(String su, String rel, String ob) throws ShardExecutionException {
		// TODO
	}


	public void disconnect() {

	}

	public void removeSource(String srcId) {
		// TODO
	}

	public void realizeRules(Collection<InferenceRule> rules) {
		for (InferenceRule rule : rules) {
			realizeRule(rule);
		}
	}

	public void realizeRule(InferenceRule rule) {
	}

	public void mergeIdentifierByIDSpaces(String fromIdSpace, String toIdSpace) throws ShardExecutionException {
	}

	public void putGraph(Graph graph) {
		for (Node n : graph.getNodes()) {
			putNode(n);
		}
		for (Statement s : graph.getStatements()) {
			putStatement(s);
		}
	}


}
