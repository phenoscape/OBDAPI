package org.obd.query.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.bridge.OBOBridge;
import org.obd.model.stats.AggregateStatisticCollection;
import org.obd.model.stats.MeasuredEntity;
import org.obd.model.stats.AggregateStatistic.AggregateType;
import org.obd.model.bridge.OBOBridge;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.exception.ShardExecutionException;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.datamodel.Dbxref;
import org.obo.datamodel.DbxrefedObject;
import org.obo.datamodel.DefinedObject;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOSession;
import org.obo.filters.ContainsComparison;
import org.obo.filters.EqualsComparison;
import org.obo.filters.Filter;
import org.obo.filters.LinkFilter;
import org.obo.filters.LinkFilterFactory;
import org.obo.filters.NameSynonymSearchCriterion;
import org.obo.filters.ObjectFilter;
import org.obo.filters.ObjectFilterFactory;
import org.obo.filters.RegexpComparison;
import org.obo.filters.SearchComparison;
import org.obo.filters.StartsWithComparison;


/**
 * Implements a Shard by wrapping an in-memory OBOSession object.
 * Use this shard for interoperating with obo files and OBO-Edit
 * @author cjm
 *
 */
public class OBOSessionShard extends AbstractShard implements Shard {
	OBOFileAdapter adapter = new OBOFileAdapter();
	
	protected OBOSession session;
	

	public OBOSession getSession() {
		return session;
	}

	public void setSession(OBOSession session) {
		this.session = session;
	}

	//TODO: reasoned link database
	public LinkDatabase getLinkDatabase() {
		return session.getLinkDatabase();
	}
	
	@Override
	public Node getNode(String id) {
		IdentifiedObject obj = session.getObject(id);
		Node node = null;
		if (obj == null) {
			Namespace ns = session.getNamespace(id);
			if (ns != null) {
				node = OBOBridge.obj2node(ns);				
			}
			else {
				
			}
		}
		else {
			node = OBOBridge.obj2node(obj);
		}
		//node.serial
		return node;
	}
	
	@Override
	public Collection<Node> getNodes() {
		HashSet<Node> nodes = new HashSet<Node>();
		for (IdentifiedObject io : session.getObjects()) {
			if (io.isBuiltIn())
				continue;
			Node node = OBOBridge.obj2node(io);
			nodes.add(node);
		}
		return nodes;
	}
	
	public Collection<Node> getNodesBySource(String sourceId) {
		HashSet<Node> nodes = new HashSet<Node>();
		for (IdentifiedObject io : session.getObjects()) {
			if (io.isBuiltIn())
				continue;
			if (io.getNamespace() != null && io.getNamespace().getID().equals(sourceId))
				nodes.add(OBOBridge.obj2node(io));
		}
		return nodes;
	}
	

	@Override
	public Collection<Node> getNodesBySearch(String searchTerm, Operator op) throws Exception {
		HashSet<Node> nodes = new HashSet<Node>();

		ObjectFilterFactory off = new ObjectFilterFactory();
		ObjectFilter filter = (ObjectFilter)off.createNewFilter();

		SearchComparison comparison;
		if (op.equals(Operator.EQUAL_TO)) {
			comparison = new EqualsComparison();
		}
		else if (op.equals(Operator.CONTAINS)) {
			comparison = new ContainsComparison();
		}
		else if (op.equals(Operator.STARTS_WITH)) {
			comparison = new StartsWithComparison();
		}
		else if (op.equals(Operator.MATCHES)) {
			comparison = new RegexpComparison();
		}
		else
			throw new Exception("not implemented"); // TODO: Exception types

		filter.setCriterion(new NameSynonymSearchCriterion());
		filter.setComparison(comparison);
		filter.setValue(searchTerm); // TODO: escape

		for (IdentifiedObject io : session.getObjects()) {
			if (filter.satisfies(io))
				nodes.add(OBOBridge.obj2node(io));
		}
		//System.out.println(filter+" N_matches: "+matches.size());
		return nodes;
	}
	
	
	public Collection<Node> getNodesBelowNodeSet(Collection<String> ids, EntailmentUse entailment, GraphTranslation gea) {
		// TODO Auto-generated method stub
		return new HashSet<Node>();
	}

	
	// simple types
	public Collection<String> getNodeIDs() {
		HashSet<String> ids = new HashSet<String>();
		for (IdentifiedObject io : session.getObjects()) {
			if (io.isBuiltIn())
				continue;
			ids.add(io.getID());
		}
		return ids;
	}		

	public Collection<Statement> getStatements() {
		HashSet<Statement> stmts = new HashSet<Statement>();
		for (IdentifiedObject io : session.getObjects()) {
			if (io.isBuiltIn())
				continue;
			for (Link link : ((LinkedObject)io).getParents())
				stmts.add(OBOBridge.link2statement(link));
		}
		return stmts;
	}
	

	@Override
	public Collection<Statement> getStatementsByNode(String id) {
		HashSet<Statement> statements = new HashSet<Statement>();
		LinkedObject obj = (LinkedObject)session.getObject(id);
		System.out.println("obj="+obj);
		if (obj != null ) {
			for (Link link : obj.getParents()) {
				Logger.getLogger("org.obd").fine("p2s"+link);
				statements.add(OBOBridge.link2statement(link));
			}
			DbxrefedObject xo = (DbxrefedObject)obj;
			for (Dbxref x : xo.getDbxrefs()) {
				statements.add(OBOBridge.xref2statement(obj,x));
			}
		}
		return statements;
	}
	

	@Override
	public Collection<Statement> getStatementsForNodeWithSource(String id, String sourceId) {
		HashSet<Statement> statements = new HashSet<Statement>();
		LinkedObject obj = (LinkedObject)session.getObject(id);
		for (Link link : obj.getParents()) {
			Statement s = OBOBridge.link2statement(link);
			if (s.getSourceId().equals(sourceId))
				statements.add(s);
		}
		return statements;
	}


	@Override
	public Collection<Statement> getStatementsForTarget(String id) {
		HashSet<Statement> statements = new HashSet<Statement>();
		LinkedObject obj = (LinkedObject)session.getObject(id);
		if (obj != null) {
			for (Link link : obj.getChildren()) {
				statements.add(OBOBridge.link2statement(link));
			}
		}
		return statements;
	}
	
	@Override
	public Collection<Statement> getStatementsForTargetWithSource(String id, String sourceId) {
		HashSet<Statement> statements = new HashSet<Statement>();
		LinkedObject obj = (LinkedObject)session.getObject(id);
		if (obj != null) {
			for (Link link : obj.getChildren()) {
				Statement s = OBOBridge.link2statement(link);
				if (s.getSourceId().equals(sourceId))
					statements.add(s);
			}
		}
		return statements;
	}

	// simple types
	public String getNodeName(String id) {
		return session.getObject(id).getName();
	}
	
	public String getNodeDef(String id) {
		return ((DefinedObject)session.getObject(id)).getDefinition();
	}

	public Collection<Node> getSourceNodes() {
		Collection<Namespace> nsl = session.getNamespaces();
		HashSet<Node> nodes = new HashSet<Node>();
		for (Namespace ns : nsl)
			nodes.add(OBOBridge.namespace2srcNode(ns));
		return nodes;
	}
	
	public AggregateStatisticCollection getSummaryStatistics() {
		AggregateStatisticCollection stats = new AggregateStatisticCollection();
		for (IdentifiedObject io : session.getObjects()) {
			if (io.isBuiltIn())
				continue;
			String src = OBOBridge.namespace2src(io.getNamespace());
			MeasuredEntity entity = stats.getMeasuredEntity("node",src);
			stats.incrementCount(entity);
			if (io instanceof LinkedObject) {
				for (Link link : ((LinkedObject)io).getParents()) {
					stats.incrementCount("link", src);
				}
			}
		}
		return stats;
	}

	public Collection<Statement> getStatements(String nodeId, String relationId, String targetId, String sourceId, Boolean useImplied, Boolean isReified) {
		return new LinkedList<Statement>();
	}


	
	public Collection<Node> getAnnotatedEntitiesBelowNodeSet(Collection<String> ids,
			EntailmentUse entailment,
			GraphTranslation gea){
		return new LinkedList<Node>(); // TODO
	}

	public Collection<Node> getNodesByQuery(QueryTerm queryTerm) {
		// TODO Auto-generated method stub
		return new HashSet<Node>();
	}

	@Override
	public Graph getGraphByQuery(QueryTerm queryTerm, EntailmentUse entailment, GraphTranslation gea) {
		Graph g = new Graph();
		LinkFilter filter = (LinkFilter)OBOBridge.query2filter(queryTerm);
		for (IdentifiedObject io : session.getObjects()) {
			if (io instanceof LinkedObject) {
				LinkedObject lo = (LinkedObject)io;
				g.addNode(OBOBridge.obj2node(lo));
				for (Link link : lo.getParents()) {
					if (filter.satisfies(link))
						g.addStatement(OBOBridge.link2statement(link));
				}
			}
		}
		return g;
	}

	public Collection<Statement> getStatementsByQuery(QueryTerm queryTerm) {
		Collection<Statement>stmts = new LinkedList<Statement>();
		LinkFilter filter = (LinkFilter)OBOBridge.query2filter(queryTerm);
		for (IdentifiedObject io : session.getObjects()) {
			if (io instanceof LinkedObject) {
				LinkedObject lo = (LinkedObject)io;
				//g.addNode(OBOBridge.obj2nodeBasic(lo));
				for (Link link : lo.getParents()) {
					if (filter.satisfies(link))
						stmts.add(OBOBridge.link2statement(link));
				}
			}
		}

		return stmts;
	}

	public int getAnnotatedNodeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public CompositionalDescription getCompositionalDescription(String id, boolean traverseNamedClasses) {
	  	LinkQueryTerm dqt = new LinkQueryTerm();
    	dqt.setDescriptionLink(true);
    	Collection<Statement> statements = getStatementsByQuery(dqt);
    	// TODO: expose option for full traversal
    	Graph g = new Graph(statements);
    	return g.getCompositionalDescription(id);
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
		// TODO
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
