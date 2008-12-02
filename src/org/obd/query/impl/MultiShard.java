package org.obd.query.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.stats.AggregateStatistic;
import org.obd.model.stats.AggregateStatisticCollection;
import org.obd.model.stats.ScoredNode;
import org.obd.model.stats.AggregateStatistic.AggregateType;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.AnalysisCapableRepository.SimilaritySearchParameters;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.exception.ShardExecutionException;

/**
 * A MultiShard implementation can wrap multiple Shards. Queries to MultiShards
 * are mapped to each sub-shard and then reduced / combined before being
 * returned to the API caller
 * 
 * TODO: add attributes for policies / strategies. E.g. parallel vs serial
 * @author cjm
 *
 */
public class MultiShard extends AbstractShard implements Shard {

	Collection<Shard> shards = new HashSet<Shard>();
	
	public Collection<Shard> getShards() {
		return shards;
	}
	
	public Shard getWriteShard() {
		return shards.iterator().next();
	}

	public void setShards(Collection<Shard> shards) {
		this.shards = shards;
	}
	
	public void addShard(Shard shard) {
		if (shards == null)
			shards = new HashSet<Shard>();
		shards.add(shard);
	}

	public Node getNode(String id) {
		for (Shard s : shards) {
			Node n = s.getNode(id);
			//System.out.println(s+" "+n);
			if (n != null)
				return n;
		}
		return null;
	}
	
	public Collection<Node> getNodes() {
		HashSet<Node> nodes = new HashSet<Node>();
		for (Shard s : shards) 
			nodes.addAll(s.getNodes());
		return nodes;
	}
	
	public Collection<Node> getNodesBySource(String sourceId) {
		HashSet<Node> nodes = new HashSet<Node>();
		for (Shard s : shards) 
			nodes.addAll(s.getNodesBySource(sourceId));
		return nodes;
	}
	
	public Collection<Node> getNodesBySearch(String searchTerm,	ComparisonQueryTerm.Operator op, String source, AliasType at) throws Exception {
		HashSet<Node> nodes = new HashSet<Node>();
		for (Shard s : shards) 
			nodes.addAll(s.getNodesBySearch(searchTerm,op,source,at));
		return nodes;
	}
	
	public Collection<Node> getNodesBelowNodeSet(Collection<String> ids, EntailmentUse entailment, GraphTranslation gea) {
		HashSet<Node> nodes = new HashSet<Node>();
		for (Shard s : shards) 
			nodes.addAll(s.getNodesBelowNodeSet(ids, entailment, gea));
		return nodes;
	}
	
	public Collection<Node> getRootNodes(String sourceId, String rel) {
		HashSet<Node> nodes = new HashSet<Node>();
		for (Shard s : shards) 
			nodes.addAll(s.getRootNodes(sourceId, rel));
		return nodes;
	}
	public Collection<Node> getRootNodes(String sourceId) {
		return getRootNodes(sourceId, null);
	}


	public Collection<Statement> getStatements(String sourceId) {
		HashSet<Statement> statements = new HashSet<Statement>();
		for (Shard s : shards) 
			statements.addAll(s.getStatements(sourceId));
		return statements;
	}

	public Collection<Statement> getStatementsForTarget(String id)  {
		HashSet<Statement> statements = new HashSet<Statement>();
		for (Shard s : shards) 
			statements.addAll(s.getStatementsForTarget(id));
		return statements;
	}
	public Collection<Statement> getStatementsForTargetWithSource(String id, String sourceId) {
		HashSet<Statement> statements = new HashSet<Statement>();
		for (Shard s : shards) 
			statements.addAll(s.getStatementsForTargetWithSource(id, sourceId));
		return statements;
	}

	public Collection<Statement> getStatementsByNode(String id) {
		HashSet<Statement> statements = new HashSet<Statement>();
		System.out.println("getting parents for="+id);
		for (Shard s : shards) 
			statements.addAll(s.getStatementsByNode(id));
		return statements;
	}

	public Collection<Statement> getStatementsForNodeWithSource(String id, String sourceId) {
		HashSet<Statement> statements = new HashSet<Statement>();
		for (Shard s : shards) 
			statements.addAll(s.getStatementsForNodeWithSource(id, sourceId));
		return statements;
	}


	public Collection<Statement> getStatements(String nodeId, String relationId, String targetId, String sourceId, Boolean useImplied, Boolean isReified) {
		HashSet<Statement> statements = new HashSet<Statement>();
		for (Shard s : shards) 
			statements.addAll(s.getStatements(nodeId, 
					relationId,  targetId,  sourceId,  useImplied,  isReified));
		return statements;
	}



	public Collection<Statement> getAnnotationStatementsForAnnotatedEntity(String id, EntailmentUse entailment, GraphTranslation gea) {
		HashSet<Statement> statements = new HashSet<Statement>();
		for (Shard s : shards) 
			statements.addAll(s.getAnnotationStatementsForAnnotatedEntity( id,
					entailment, gea));

		return statements;
	}
	
	public Collection<Statement> getAnnotationStatementsForNode(String id, EntailmentUse entailment, GraphTranslation gea) {
		HashSet<Statement> statements = new HashSet<Statement>();
		for (Shard s : shards) 
			statements.addAll(s.getAnnotationStatementsForNode( id,
					entailment, gea));

		return statements;
	}

	public Graph getAnnotationGraphAroundNode(String id, EntailmentUse entailment, GraphTranslation gea) {
		Graph g = new Graph();
		for (Shard s : shards) 
			g.merge(s.getAnnotationGraphAroundNode(id,  entailment,  gea));
		return g;	
		
	}
	
	public Collection<Node> getAnnotatedEntitiesBelowNodeSet(Collection<String> ids, EntailmentUse entailment, GraphTranslation gea) {
		HashSet<Node> nodes = new HashSet<Node>();
		for (Shard s : shards) 
			nodes.addAll(s.getAnnotatedEntitiesBelowNodeSet(ids, entailment, gea));
		return nodes;
	}


	public Collection<Node> getSourceNodes() {
		HashSet<Node> nodes = new HashSet<Node>();
		for (Shard s : shards) 
			nodes.addAll(s.getSourceNodes());
		return nodes;	
	}

	public AggregateStatisticCollection getSummaryStatistics() {
		AggregateStatisticCollection combinedStats = new AggregateStatisticCollection();
		for (Shard s : shards) {
			AggregateStatisticCollection sc = s.getSummaryStatistics();
			Collection<AggregateStatistic> stats;
			if (sc != null) {
				stats = sc.getStats();
				combinedStats.getStats().addAll(stats);
			}
		}
		return combinedStats;
	}
	
	public List<ScoredNode> getSimilarNodes(SimilaritySearchParameters params, String nodeId) {
		if (shards.size() == 1)
			return shards.iterator().next().getSimilarNodes(params, nodeId);
		List<ScoredNode> sns = new ArrayList<ScoredNode>();
		for (Shard s : shards) {
			sns.addAll(s.getSimilarNodes(params, nodeId));
			Collections.sort(sns);
		}		
		return sns;
	}

	public Collection<Node> getNodesByQuery(QueryTerm queryTerm) {
		HashSet<Node> nodes = new HashSet<Node>();
		for (Shard s : shards) 
			nodes.addAll(s.getNodesByQuery(queryTerm));
		return nodes;
	}

	public Graph getGraphByQuery(QueryTerm queryTerm, EntailmentUse entailment, GraphTranslation gea) {
		Graph g = new Graph();
		for (Shard s : shards) 
			g.merge(s.getGraphByQuery(queryTerm,  entailment,  gea));
		return g;	
	}

	public Collection<Statement> getStatementsByQuery(QueryTerm queryTerm) {
		HashSet<Statement> statements = new HashSet<Statement>();
		for (Shard s : shards) 
			statements.addAll(s.getStatementsByQuery(queryTerm));

		return statements;
	}
	
	public Collection<LinkStatement> getLinkStatementsByQuery(QueryTerm queryTerm) {
		HashSet<LinkStatement> statements = new HashSet<LinkStatement>();
		for (Shard s : shards) 
			statements.addAll(s.getLinkStatementsByQuery(queryTerm));

		return statements;
	}
	
	public Collection<LiteralStatement> getLiteralStatementsByQuery(QueryTerm queryTerm) {
		HashSet<LiteralStatement> statements = new HashSet<LiteralStatement>();
		for (Shard s : shards) 
			statements.addAll(s.getLiteralStatementsByQuery(queryTerm));

		return statements;
	}
	public Collection<LiteralStatement> getLiteralStatementsByNode(String nodeID){
		return getLiteralStatementsByNode(nodeID,null);
	}
	public Collection<LiteralStatement> getLiteralStatementsByNode(String nodeID, String relationID){
		HashSet<LiteralStatement> statements = new HashSet<LiteralStatement>();
		for (Shard s : shards){
			statements.addAll(s.getLiteralStatementsByNode(nodeID, relationID));
		}
		return statements;
	}

	public int getAnnotatedNodeCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public CompositionalDescription getCompositionalDescription(String id, boolean traverseNamedClasses) {

		// TODO: conflict resolution?
		for (Shard s : shards) {
			CompositionalDescription d = 
				s.getCompositionalDescription(id, traverseNamedClasses);
			if (d != null)
				return d;
		}
		return null;
	}
	

	public List<ScoredNode> getSimilarNodes(String nodeId) {
		List<ScoredNode> scoredNodes = new LinkedList<ScoredNode>();
		for (Shard s : shards) {
			// TODO: combine p-vals
			scoredNodes.addAll(s.getSimilarNodes(nodeId));
		}
		return scoredNodes;
	}

	public List<ScoredNode> getSimilarNodes(QueryTerm nodeQueryTerm) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void putGraph(Graph g) {
		for (Shard s : shards) {
			// TODO: designate shard to be written to
			s.putGraph(g);
		}
	}

	public Integer getNodeAggregateQueryResults(QueryTerm queryTerm, AggregateType aggType) throws Exception {
		Integer num;
		if (shards.size() > 1) {
			throw new Exception("cannot aggregate aggregate queries!");
		}
		return 
		shards.iterator().next().getNodeAggregateQueryResults(queryTerm, aggType);
	}
	public Integer getLinkAggregateQueryResults(QueryTerm queryTerm, AggregateType aggType) throws Exception {
		Integer num;
		if (shards.size() > 1) {
			throw new Exception("cannot aggregate aggregate queries!");
		}
		return 
		shards.iterator().next().getLinkAggregateQueryResults(queryTerm, aggType);
	}

	public void transferFrom(Shard shard) {
		// TODO - make this configurable
		// e.g. one shard can be designated the mutable shard
		// for now default is to use as a new sub-shard
		addShard(shard);
	}

	public Graph getGraph() {
		Graph g = new Graph();
		g.addNodes(getNodes());
		return g;
	}

	public Boolean includesEntailedStatements() {
		boolean includes = false;
		for (Shard shard: shards) {
			includes |= shard.includesEntailedStatements();
		}
		return includes;
	}


	public void disconnect() {
		for (Shard shard: shards) {
			shard.disconnect();
		}
	}

	public Set<String> mapNodeToSubset(String id, String subsetId) {
		HashSet<String> nids = new HashSet<String>();
		for (Shard s : shards) 
			nids.addAll(s.mapNodeToSubset(id, subsetId));
		return nids;
	}

	public Map<Node, Integer> getAnnotatedEntityCountBySubset(AnnotationLinkQueryTerm qt, String subsetId) throws ShardExecutionException {
		Map<Node,Integer> map = new HashMap<Node,Integer>();
		for (Shard s : shards) {
			Map<Node, Integer> smap = s.getAnnotatedEntityCountBySubset(qt, subsetId);
			if (smap == null)
				continue;
			for (Node key : smap.keySet()) {
				// TODO: check for inconsistencies
				map.put(key, smap.get(key));
			}
		}
		return map;
	}

	public Collection<Node> getLinkStatementSourceNodes() {
		// TODO Auto-generated method stub
		List<Node> nodes = new LinkedList<Node>();
		for (Shard s : this.shards){
			for (Node n : s.getLinkStatementSourceNodes()){
				if (!nodes.contains(n)){
					nodes.add(n);
				}
			}
		}
		return nodes;
		
	}

	public Collection<Node> getNodeSourceNodes() {
		// TODO Auto-generated method stub
		List<Node> nodes = new LinkedList<Node>();
		for (Shard s : this.shards){
			for (Node n : s.getNodeSourceNodes()){
				if (!nodes.contains(n)){
					nodes.add(n);
				}
			}
		}
		return nodes;
	}
	
	@Override
	public Double getInformationContentByAnnotations(String classNodeId) {
		// Warn if more than one shard can return a value for shard.getInformationContentByAnnotations;
		for (Shard s : this.shards){
			Double val = s.getInformationContentByAnnotations(classNodeId);
			if (val != null){
				return val;
			}
		}
		return null;
	}

	public void removeSource(String srcId) {
		for (Shard s : this.shards){
			s.removeSource(srcId);
		}
	}
	
	public void mergeIdentifierByIDSpaces(String fromIdSpace, String toIdSpace) throws ShardExecutionException {
		for (Shard s : this.shards){
			s.mergeIdentifierByIDSpaces(fromIdSpace, toIdSpace);
		}
		
	}


	public void putNode(Node n) {
		getWriteShard().putNode(n);	
	}

	public void putStatement(Statement s) {
		getWriteShard().putStatement(s);
	}

	public void removeNode(String nid) throws ShardExecutionException {
		for (Shard s : this.shards){
			s.removeNode(nid);
		}
		
	}

	public Collection<Statement> getStatementsWithSearchTerm(String node, 
			String relation, String target, String source,
			Boolean useImplied, Boolean isReified) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesForSearchTermByLabel(String searchTerm) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesForSearchTermBySynonym(String term) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesForSearchTermByDefinition(String term) {
		// TODO Auto-generated method stub
		return null;
	}

}
