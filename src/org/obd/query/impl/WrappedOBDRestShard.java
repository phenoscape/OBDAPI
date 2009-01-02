package org.obd.query.impl;

import java.util.Collection;
import java.util.List;

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

/**
 * Implements a Shard by calling a remote or local OBD Rest service layer
 * @author cjm
 *
 */
public class WrappedOBDRestShard extends AbstractShard implements Shard {
	
	String url = "http://www.berkeleybop.org:8192";
	
	public WrappedOBDRestShard() throws  ClassNotFoundException {
	}

	public Collection<Node> getAnnotatedEntitiesBelowNodeSet(Collection<String> ids, EntailmentUse entailment, GraphTranslation gea) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getAnnotatedNodeCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	public Collection<Node> getNodesBelowNodeSet(Collection<String> ids, EntailmentUse entailment, GraphTranslation gea) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesByQuery(QueryTerm queryTerm) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesBySearch(String searchTerm, Operator op, String source, AliasType at) {
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

	public void putGraph(Graph g) {
		// TODO Auto-generated method stub
		
	}

	public Integer getNodeAggregateQueryResults(QueryTerm queryTerm, AggregateType aggType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getLinkAggregateQueryResults(QueryTerm queryTerm, AggregateType aggType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Collection<Node> getLinkStatementSourceNodes() {
		System.err.println("ERROR: getLinkStatementSourceNodes() not yet implemented.");
		return null;
	}

	public Collection<Node> getNodeSourceNodes() {
		System.err.println("ERROR: getNodeSourceNodes() not yet implemented.");
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

	public Collection<Statement> getStatementsWithSearchTerm(String node, 
			String relation, String target, String source,
			Boolean useImplied, Boolean isReified) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesForSearchTermByLabel(String searchTerm, boolean zfinOption, List<String> ontologies) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesForSearchTermBySynonym(String term, boolean zfinOption, List<String> ontologies, boolean searchByName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<Node> getNodesForSearchTermByDefinition(String term, boolean zfinOption, List<String> ontologies) {
		// TODO Auto-generated method stub
		return null;
	}
}

