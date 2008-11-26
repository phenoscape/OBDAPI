package org.obd.query.impl;

import java.util.Collection;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.stats.AggregateStatisticCollection;
import org.obd.model.stats.AggregateStatistic.AggregateType;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.exception.ShardExecutionException;

public class InMemoryShard extends AbstractShard implements Shard {
	
	
	protected Graph graph;
		
	// node fetching
	@Override
	public Node getNode(String id) {
		return graph.getNode(id);
	}

	public Collection<Node> getAnnotatedEntitiesBelowNodeSet(Collection<String> ids, EntailmentUse entailment, GraphTranslation gea) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getAnnotatedNodeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public Integer getLinkAggregateQueryResults(QueryTerm queryTerm, AggregateType aggType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getNodeAggregateQueryResults(QueryTerm queryTerm, AggregateType aggType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesBelowNodeSet(Collection<String> ids, EntailmentUse entailment, GraphTranslation gea) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesByQuery(QueryTerm queryTerm) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Collection<Node> getNodesBySource(String sourceId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getSourceNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getStatements(String nodeId, String relationId, String targetId, String sourceId, Boolean useImplied, Boolean isReified) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Statement> getStatementsByQuery(QueryTerm queryTerm) {
		// TODO Auto-generated method stub
		return null;
	}


	public Collection<Statement> getSubjectStatements(String subjectId, String subjectRelationId, String targetRelationId, String nodeId, String targetId, String subjectSourceId, String nodeSourceId, Boolean useImplied, Boolean isReified) {
		// TODO Auto-generated method stub
		return null;
	}

	public AggregateStatisticCollection getSummaryStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getLinkStatementSourceNodes() {
		// TODO Auto-generated method stub
		System.err.println("ERROR: Not Yet Implemented.");
		return null;
	}

	public Collection<Node> getNodeSourceNodes() {
		// TODO Auto-generated method stub
		System.err.println("ERROR: Not Yet Implemented.");
		return null;
	}

	public void putNode(Node n) {
		// TODO Auto-generated method stub
		
	}

	public void putStatement(Statement s) {
		// TODO Auto-generated method stub
		
	}

	public void removeNode(String nid) throws ShardExecutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Statement> getStatementsWithSearchTerm(String node, 
			String relation, String target, String source,
			Boolean useImplied, Boolean isReified) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Node> getNodesForSearchTermByLabel(String searchTerm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Node> getNodesForSearchTermBySynonym(String term) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Node> getNodesForSearchTermByDefinition(String term) {
		// TODO Auto-generated method stub
		return null;
	}




	
	
	


}
