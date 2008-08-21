package org.obd.ws;

import java.sql.SQLException;
import java.util.Collection;

import org.bbop.dataadapter.DataAdapterException;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.stats.AggregateStatisticCollection;
import org.obd.model.stats.ScoredNode;
import org.obd.query.QueryTerm;
import org.obd.query.Shard.EntailmentUse;
import org.obd.query.Shard.GraphExpansionAlgorithm;
import org.obd.query.impl.MultiShard;
import org.obd.query.impl.MutableOBOSessionShard;
import org.obd.query.impl.OBDSQLShard;

/**
 * Axis2 startup class
 * @deprecated
 * @author cjm
 *
 */
public class OBDQueryService {
	
	String defaultJdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_mouse";
	MultiShard shard;

	public OBDQueryService() throws ClassNotFoundException, DataAdapterException {
		shard = new MultiShard();
		
		OBDSQLShard obd;
		try {
			obd = new OBDSQLShard();
			obd.connect(defaultJdbcPath);
			shard.addShard(obd);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		MutableOBOSessionShard moss = new MutableOBOSessionShard();
		moss.loadLocal("cell");
		shard.addShard(moss);
	}
	
	

	public MultiShard getShard() {
		return shard;
	}


	public String getID() {
		return "OBD-Query-Service"; // TODO: introspect metadata
	}
	
	public Node getNode(String id) {
		System.out.println("getNode: "+id);
		return shard.getNode(id);
	}
	
	// TODO - tidy this - make delegate calls match signature

	public Node[] getNodes() {
		return (Node[])shard.getNodes().toArray(new Node[0]);
	}
	
	public Node[]  getSourceNodes() {
		return (Node[] )shard.getSourceNodes().toArray(new Node[0]);
	}
	
	
	public Node[] getNodesBySource(String sourceId) {
		return (Node[])shard.getNodesBySource(sourceId).toArray(new Node[0]);
	}
	public Node[] getNodesBySearch(String searchTerm) throws Exception {
		return (Node[])shard.getNodesBySearch(searchTerm).toArray(new Node[0]);
	}
	
	public Node[] getNodesBelowNodeSet(Collection<String> ids,
			EntailmentUse entailment,
			GraphExpansionAlgorithm gea) {
		return (Node[])shard.getNodesBelowNodeSet(ids, entailment, gea).toArray(new Node[0]);		
	}

	public Graph getGraph() {
		return null;
	}


	
	public Graph getAnnotationGraphAroundNode(String id, EntailmentUse entailment, GraphExpansionAlgorithm gea) {
		return shard.getAnnotationGraphAroundNode(id, entailment, gea);
	}
	
	

	public Statement[] getAnnotationStatementsForAnnotatedEntity(String id, EntailmentUse entailment, GraphExpansionAlgorithm strategy) {
		return (Statement[] ) shard.
		getAnnotationStatementsForAnnotatedEntity(id, entailment, strategy).
		toArray(new Statement[0]);
	}
	public Statement[] getAnnotationStatementsForNode(String id, EntailmentUse entailment, GraphExpansionAlgorithm strategy) {
		return (Statement[] ) shard.
		getAnnotationStatementsForNode(id, entailment, strategy).
		toArray(new Statement[0]);
	}
	
	public Node[] getAnnotatedEntitiesBelowNodeSet(Collection<String> ids,
			EntailmentUse entailment,
			GraphExpansionAlgorithm gea) {
		return (Node[])shard.getAnnotatedEntitiesBelowNodeSet(ids, entailment, gea).toArray(new Node[0]);		
	}
	
	

	public Graph getGraphByQuery(QueryTerm queryTerm, EntailmentUse entailment, GraphExpansionAlgorithm gea) {
		return shard.getGraphByQuery(queryTerm, entailment, gea);
	}

	public Node[] getNodesByQuery(QueryTerm queryTerm) {
		return (Node[])shard.getNodesByQuery(queryTerm).toArray(new Node[0]);
	}

	public Statement[] getStatementsByQuery(QueryTerm queryTerm) {
		return (Statement[])shard.getStatementsByQuery(queryTerm).toArray(new Statement[0]);
	}

	public Statement getStatement(String id) {
		//return shard.getStatements().iterator().next();
		return null;
	}
	public Statement[] getStatementsWithSource(String sourceId) {
		return (Statement[])shard.getStatements(sourceId).toArray(new Statement[0]);
	}


	public Statement[] getStatementsForTarget(String id) {
		return (Statement[])shard.getStatementsForTarget(id).toArray(new Statement[0]);
	}
	public Statement[] getStatementsForTargetWithSource(String id, String sourceId) throws Exception {
		return (Statement[])shard.getStatementsForTargetWithSource(id, sourceId).toArray(new Statement[0]);
	}

	
	public  Statement[] getStatementsForNode(String id) {
		return (Statement[])shard.getStatementsForNode(id).toArray(new Statement[0]);
	}
	public Statement[] getStatementsForNodeWithSource(String id, String sourceId) {
		System.out.println("parentsFrom="+id);
		return (Statement[])shard.getStatementsForNodeWithSource(id, sourceId).toArray(new Statement[0]);
	}



	public AggregateStatisticCollection getSummaryStatistics() {
		return shard.getSummaryStatistics();
	}



	public ScoredNode[] getSimilarNodes(String nodeId) {
		return (ScoredNode[])shard.getSimilarNodes(nodeId).toArray(new ScoredNode[0]);
	}	
	
}
