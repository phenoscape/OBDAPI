package org.obd.query.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bbop.rdbms.GroupByClause;
import org.bbop.rdbms.RelationalQuery;
import org.bbop.rdbms.SelectClause;
import org.bbop.rdbms.WhereClause;
import org.bbop.rdbms.impl.SqlQueryImpl;
import org.bbop.rdbms.impl.SqlSelectClauseImpl;
import org.bbop.rdbms.impl.SqlWhereClauseImpl;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.NodeAlias;
import org.obd.model.Statement;
import org.obd.model.bridge.OBOBridge;
import org.obd.model.rule.InferenceRule;
import org.obd.model.rule.RelationCompositionRule;
import org.obd.model.stats.AggregateStatisticCollection;
import org.obd.model.stats.ScoredNode;
import org.obd.model.stats.AggregateStatistic.AggregateType;
import org.obd.model.vocabulary.TermVocabulary;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.AtomicQueryTerm;
import org.obd.query.BooleanQueryTerm;
import org.obd.query.CoAnnotatedQueryTerm;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.CompositionalDescriptionQueryTerm;
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
import org.obd.query.BooleanQueryTerm.BooleanOperator;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.QueryTerm.Aspect;
import org.obd.query.exception.ShardExecutionException;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.OBOSessionImpl;
import org.purl.obo.vocab.RelationVocabulary;

/**
 * Implements a Shard by wrapping a relational database using the OBD-SQL
 * Schema. <a
 * href="http://www.bioontology.org/wiki/index.php/OBD:OBD-SQL-Schema">Schema
 * docs</a>
 * 
 * @author cjm
 * 
 */
public class OBDSQLShard extends AbstractSQLShard implements Shard {


	Logger logger = Logger.getLogger("org.obd.shard.OBDSQLShard");

	// TODO - enums/constants for DDL
	protected String NODE_TABLE = "node";
	protected String LINK_TABLE = "link";
	protected String LITERAL_TABLE = "node_literal_with_pred";
	protected String NODE_INTERNAL_ID_COLUMN = "node_id";
	protected String NODE_EXPOSED_ID_COLUMN = "uid";
	protected String NODE_SOURCE_INTERNAL_ID_COLUMN = "source_id";
	protected String NODE_SOURCE_EXPOSED_ID_COLUMN = "source_uid";

	protected String LITERAL_VALUE_COLUMN = "val";

	protected String LINK_NODE_EXPOSED_ID_COLUMN = "node_uid";
	protected String LINK_TARGET_EXPOSED_ID_COLUMN = "object_uid";
	protected String LINK_RELATION_EXPOSED_ID_COLUMN = "pred_uid";
	protected String LINK_SOURCE_EXPOSED_ID_COLUMN = "source_uid";
	protected String LINK_REIF_EXPOSED_ID_COLUMN = "reiflink_node_uid";

	protected String LINK_NODE_INTERNAL_ID_COLUMN = "node_id";
	protected String LINK_TARGET_INTERNAL_ID_COLUMN = "object_id";
	protected String LINK_RELATION_INTERNAL_ID_COLUMN = "predicate_id";
	protected String LINK_SOURCE_INTERNAL_ID_COLUMN = "source_id";
	protected String LINK_REIF_INTERNAL_ID_COLUMN = "reiflink_node_id";

	//added to address a discrepancy between column names (pred_uid and predicate_uid)
	// this must be addressed in the DDL statements creating the tables and views: Cartik
	protected String LINK_RELATION_EXPOSED_ID_COLUMN_FULL = "predicate_uid";

	protected String IS_A_LINK_TABLE = "is_a_link";
	protected String REIFIED_LINK_TABLE = "reified_link";

	protected String APPLIES_TO_ALL = "applies_to_all";
	protected String SUBJECT_NODE_ALIAS = "snode";
	protected String TARGET_NODE_ALIAS = "tnode";
	protected String RELATION_NODE_ALIAS = "rnode";
	protected String REIF_NODE_ALIAS = "anode";
	protected String SOURCE_NODE_ALIAS = "srcnode";
	protected String IMPLIED_ANNOTATION_LINK_ALIAS = "implied_annotation_link";

	private RelationVocabulary rvocab = new RelationVocabulary();
	private TermVocabulary tvocab = new TermVocabulary();

	protected OBOSession session = new OBOSessionImpl(); // TODO: remove? need
	// it for tracking
	// namespaces etc

	HashMap<Integer, String> iid2nodeId = new HashMap<Integer, String>();

	public OBDSQLShard() throws SQLException, ClassNotFoundException {
	}



	public String getID() {
		return "OBD-Query-Service"; // TODO: introspect metadata
	}

	public Integer getNodeInternalId(String id) {
		RelationalQuery rq = translateQueryForNode(new LabelQueryTerm(
				AliasType.ID, id));
		try {
			ResultSet rs = execute(rq);
			if (rs.next()) {
				return rs.getInt(NODE_INTERNAL_ID_COLUMN);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}

	public Collection<Node> getNodesBelowNodeSet(Collection<String> ids,
			EntailmentUse entailment, GraphTranslation gea) {
		return getNodesBelowNodeSet(ids, entailment, gea, "link_to_node");
	}

	public Collection<Node> getNodesBelowNodeSet(Collection<String> ids,
			EntailmentUse entailment, GraphTranslation gea,
			String linkTable) {
		RelationalQuery q = new SqlQueryImpl();
		WhereClause whereClause = new SqlWhereClauseImpl();
		q.addTable("node_with_source", NODE_TABLE);
		int num = 0;
		for (String id : ids) {
			String alias = LINK_TABLE + num;
			q.addTable(linkTable, alias);
			num++;
			whereClause.addEqualityConstraint(tblCol(alias, LINK_TARGET_EXPOSED_ID_COLUMN), id);
			whereClause.addConstraint(tblCol(alias, LINK_NODE_EXPOSED_ID_COLUMN) + " = " + tblCol(NODE_TABLE, NODE_INTERNAL_ID_COLUMN));
			q.setWhereClause(whereClause);
		}
		q.getSelectClause().addColumn(tblCol(NODE_TABLE, "*"));
		q.getSelectClause().setDistinct(true);
		return getNodesByQuery(q);
	}


	public Collection<Node> getSourceNodes() {
		Collection<Node> nodes = new LinkedList<Node>();
		RelationalQuery q = new SqlQueryImpl();
		q.addTable("source_node");
		q.setSelectClause("*, NULL as "+LINK_SOURCE_EXPOSED_ID_COLUMN);
		return getNodesByQuery(q);
	}

	public Collection<Node> getLinkStatementSourceNodes() {
		Collection<Node> nodes = new LinkedList<Node>();
		RelationalQuery subQuery = new SqlQueryImpl();
		subQuery.addTable(LINK_TABLE);
		subQuery.setSelectClause("distinct "+LINK_SOURCE_INTERNAL_ID_COLUMN);

		RelationalQuery nodeQuery = new SqlQueryImpl();
		nodeQuery.addTable(NODE_TABLE);
		nodeQuery.setSelectClause("*, NULL as "+LINK_SOURCE_EXPOSED_ID_COLUMN);

		WhereClause wc = new SqlWhereClauseImpl();
		wc.addInConstraint(NODE_INTERNAL_ID_COLUMN, subQuery);

		nodeQuery.setWhereClause(wc);

		try {
			ResultSet rs = execute(nodeQuery);
			while (rs.next()) {
				IdentifiedObject io = obd.fetchObject(session, rs);
				nodes.add(OBOBridge.obj2node(io));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return nodes;
	}

	public Collection<Node> getNodeSourceNodes() {
		Collection<Node> nodes = new LinkedList<Node>();
		RelationalQuery subQuery = new SqlQueryImpl();
		subQuery.addTable(NODE_TABLE);
		subQuery.setSelectClause("distinct "+NODE_SOURCE_INTERNAL_ID_COLUMN);

		RelationalQuery nodeQuery = new SqlQueryImpl();
		nodeQuery.addTable(NODE_TABLE);
		nodeQuery.setSelectClause("*, NULL AS "+NODE_SOURCE_EXPOSED_ID_COLUMN);

		WhereClause wc = new SqlWhereClauseImpl();
		wc.addInConstraint(NODE_INTERNAL_ID_COLUMN, subQuery);

		nodeQuery.setWhereClause(wc);

		try {
			ResultSet rs = execute(nodeQuery);
			while (rs.next()) {
				IdentifiedObject io = obd.fetchObject(session, rs);
				nodes.add(OBOBridge.obj2node(io));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return nodes;
	}

	public Collection<Statement> getStatements(WhereClause whereClause) {
		HashSet<Statement> statements = new HashSet<Statement>();
		try {
			RelationalQuery q = new SqlQueryImpl();
			q.addTable("node_link_node_with_pred_and_source");
			q.setWhereClause(whereClause);
			ResultSet rs = execute(q);

			OBOSession tempSession = new OBOSessionImpl();
			HashMap<String, Link> positsMap = new HashMap<String, Link>();
			while (rs.next()) {
				obd.includeLinkResultSetInSession(tempSession, rs, positsMap);
			}
			HashSet<Link> links = new HashSet<Link>();
			for (IdentifiedObject io : tempSession.getObjects()) {
				if (io instanceof LinkedObject && !io.isBuiltIn())
					links.addAll(((LinkedObject) io).getParents());
			}
			HashMap<String, Statement> statementMap = new HashMap<String, Statement>();
			for (Link link : links) {
				Statement s = OBOBridge.link2statement(link);
				statements.add(s);
				statementMap.put(s.toString(), s);
			}
			// slightly convoluted way of making reverse links, from
			// posited link to positing (annotation) node
			for (IdentifiedObject io : tempSession.getObjects()) {
				String nodeId = io.getID();
				if (positsMap.containsKey(nodeId)) {
					Statement sKey = OBOBridge.link2statement(positsMap.get(io
							.getID()));
					Statement s = statementMap.get(sKey.toString());
					if (s != null)
						s.setPositedByNodeId(nodeId);
					else
						System.err.println("no stmt: " + sKey);
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statements;

	}


	public Collection<Statement> getStatements(String nodeId,
			String relationId, String targetId, String sourceId,
			Boolean useImplied, Boolean isReified) {
		WhereClause whereClause = new SqlWhereClauseImpl();
		if (nodeId != null)
			whereClause.addEqualityConstraint(LINK_NODE_EXPOSED_ID_COLUMN, nodeId);
		if (relationId != null)
			whereClause.addEqualityConstraint(LINK_RELATION_EXPOSED_ID_COLUMN, relationId);
		if (targetId != null)
			whereClause.addEqualityConstraint(LINK_TARGET_EXPOSED_ID_COLUMN, targetId);
		if (sourceId != null)
			whereClause.addEqualityConstraint(LINK_SOURCE_EXPOSED_ID_COLUMN, sourceId);
		if (useImplied != null)
			whereClause.addEqualityConstraint("is_inferred", useImplied);
		if (isReified != null) {
			whereClause.addConstraint(LINK_REIF_INTERNAL_ID_COLUMN+ (isReified ? " IS NOT NULL" : " IS NULL"));
		}
		return getStatements(whereClause);
	}

	/**
	 * @author cartik
	 * This method has been defined to look for wildcard matches in statements using the 'LIKE' SQL keyword
	 * 
	 */

	public Collection<Statement> getStatementsWithSearchTerm(String node, String relation, String target, 
			String source, Boolean useImplied, Boolean isReified) {
		WhereClause whereClause = new SqlWhereClauseImpl();
		if(node != null){
			whereClause.addLikeConstraint(LINK_NODE_EXPOSED_ID_COLUMN, node);
		}
		if(relation != null){
			whereClause.addLikeConstraint(LINK_RELATION_EXPOSED_ID_COLUMN, relation);
		}
		if(target != null){
			whereClause.addLikeConstraint(LINK_TARGET_EXPOSED_ID_COLUMN, target);
		}
		if(source != null){
			whereClause.addEqualityConstraint(LINK_SOURCE_EXPOSED_ID_COLUMN, source);
		}
		if(useImplied != null){
			whereClause.addEqualityConstraint("is_inferred", useImplied);
		}
		if (isReified != null) {
			whereClause.addConstraint(LINK_REIF_INTERNAL_ID_COLUMN+ (isReified ? " IS NOT NULL" : " IS NULL"));
		}
		return getStatements(whereClause);
	}

	
	/**
	 * @author cartik
	 * @PURPOSE: This method retrieves matches for a search term, by label, by synonym, and by definition
	 * @PROCEDURE: A query is constructed for searching for a match on term labels. If the synonym and definition
	 * options are specified, UNIONS of other queries are added to the original query. Finally, if the ZFIN option
	 * is specified, a last query to find ZFIN GENEs is UNIONed to the main query. The assembled query is now executed
	 * over the Shard and the results are processed and packaged into a returned collection of nodes
	 * @param searchTerm - the term to be searched for (if TRUE)
	 * @param zfinOption - look for matches on GENE names (if TRUE)
	 * @param synOption - look for synonyms containing search term (if TRUE)
	 * @param defOption - look for definitions containing search term (if TRUE)
	 * @param ontologyList - list of ontologies to filter by. by default, we look through all ontologies as set up in the
	 * autocomplete resource. specified lists make the search scope more specific
	 */
	/** 
	 * This is an experimental method that may be implemented in the future for better query execution 5/08/09
	 */
	public Collection<Node> getAutoCompletions(String searchTerm, boolean zfinOption, boolean synOption,
								boolean defOption, List<String> ontologyList) throws SQLException{
		Collection<Node> results = new LinkedList<Node>();
		RelationalQuery labelQ = new SqlQueryImpl(), zfinQ = null, synQ = null, defQ = null;
		SelectClause labelSC = new SqlSelectClauseImpl(), zfinSC, synSC, defSC;
		WhereClause labelWC = new SqlWhereClauseImpl(), zfinWC, synWC, defWC;
		RelationalQuery query4Ontologies = null;
		
		Node resultNode; //watch this node. this is what every returned row is packaged into
				
		/*
		 * This is the default search, looking for matches on term labels
		 */
		labelSC.setDistinct(true);
		labelSC.addColumn("label_node.uid", "uid");
		labelSC.addColumn("label_node.label", "label");
		labelSC.addColumn("NULL", "synonym");
		labelSC.addColumn("NULL", "definition");
		//synSC.setDistinct(true);
		//defSC.setDistinct(true);
		labelWC.addCaseInsensitiveRegexConstraint("lower(label_node.label)", searchTerm);

		if(ontologyList != null && ontologyList.size() > 0){
			query4Ontologies = new SqlQueryImpl();
			query4Ontologies.addTable(NODE_TABLE, "n1");
			query4Ontologies.setSelectClause("n1.node_id AS nodeId");
			WhereClause subWc = new SqlWhereClauseImpl();
			subWc.addInConstraint("n1.uid", ontologyList);
			query4Ontologies.setWhereClause(subWc);
			labelWC.addInConstraint("label_node.source_id", query4Ontologies);
		}
		labelQ.addTable(NODE_TABLE, "label_node");
		labelQ.setSelectClause(labelSC);
		labelQ.setWhereClause(labelWC);
		
		/*
		 * We begin to assemble the final query here
		 */
		String finalQuery = labelQ.toSQL();
		
		/*
		 * If ZFIN option is specified, ZFIN nodes come from text files and not from ontologies. A
		 * separate query goes in here
		 */
		if(zfinOption){
			zfinQ = new SqlQueryImpl();
			
			zfinSC = new SqlSelectClauseImpl();
			zfinSC.setDistinct(true);
			zfinSC.addColumn("zfin_node.uid", "uid");
			zfinSC.addColumn("zfin_node.label", "label");
			zfinSC.addColumn("NULL", "synonym");
			zfinSC.addColumn("NULL", "definition");
			
			zfinWC = new SqlWhereClauseImpl();
			zfinWC.addCaseInsensitiveRegexConstraint("zfin_node.uid", "ZDB-GENE");
			zfinWC.addCaseInsensitiveRegexConstraint("lower(zfin_node.label)", searchTerm);
			
			zfinQ.addTable(NODE_TABLE, "zfin_node");
			zfinQ.setWhereClause(zfinWC);
			zfinQ.setSelectClause(zfinSC);
			
			finalQuery += " UNION " + zfinQ.toSQL();
		}
		
		/*
		 * Synonym option is used, so we use a table join between NODE and ALIAS tables 
		 */
		if(synOption){
			synQ = new SqlQueryImpl();
			
			synSC = new SqlSelectClauseImpl();
			synSC.setDistinct(true);
			synSC.addColumn("main_node.uid", "uid");
			synSC.addColumn("main_node.label", "label");
			synSC.addColumn("alias_node.label", "synonym");
			synSC.addColumn("NULL", "definition");
			
			synWC = new SqlWhereClauseImpl();
			synWC.addJoinConstraint("main_node.node_id", "alias_node.node_id");
			synWC.addCaseInsensitiveRegexConstraint("lower(alias_node.label)", searchTerm);
			
			synQ.addTable(NODE_TABLE, "main_node");
			synQ.addTable("alias", "alias_node");
			synQ.setSelectClause(synSC);
			synQ.setWhereClause(synWC);
			
			finalQuery += " UNION " + synQ.toSQL();
		}
		/*
		 * Definition option is used, so we join NODE and DESCRIPTION tables
		 */
		if(defOption){
			defQ = new SqlQueryImpl();
			
			defSC = new SqlSelectClauseImpl();
			defSC.setDistinct(true);
			defSC.addColumn("main_node.uid", "uid");
			defSC.addColumn("main_node.label", "label");
			defSC.addColumn("NULL", "synonym");
			defSC.addColumn("desc_node", "definition");
			
			defWC = new SqlWhereClauseImpl();
			defWC.addJoinConstraint("main_node.node_id", "desc_node.node_id");
			defWC.addCaseInsensitiveRegexConstraint("lower(desc_node.label)", searchTerm);
			
			defQ.addTable(NODE_TABLE, "main_node");
			defQ.addTable("description", "desc_node");
			defQ.setSelectClause(defSC);
			defQ.setWhereClause(defWC);
			
			finalQuery += " UNION " + defQ.toSQL();
		}
		 
		Connection conn = this.getConnection();
		PreparedStatement ps = conn.prepareStatement(finalQuery);
		
		try{
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				String uid = rs.getString(1);
				String label = rs.getString(2);
				String syn = rs.getString(3);
				String def = rs.getString(4);
				resultNode = new Node(uid);
			}
		}
		catch(SQLException sqle){
			logger.severe("SqlException");
			throw sqle;
			
		}
		
		
		return results;
	}
	
	/**
	 * @author cartik
	 * @param searchTerm
	 * @return
	 * This has been added to search for nodes by labels in the node table
	 */

	public Collection<Node> getNodesForSearchTermByLabel(String searchTerm, boolean zfinOption, List<String> ontologies)
	throws SQLException{

		Collection<Node> results = new LinkedList<Node>();
		RelationalQuery rq, zq = null;
		
		rq = new SqlQueryImpl();
		rq.addTable(NODE_TABLE, "n");
		rq.setSelectClause("n.uid AS uid");

		
		WhereClause wc = new SqlWhereClauseImpl();
		wc.addCaseInsensitiveRegexConstraint("lower(n.label)", searchTerm);
		
		if(ontologies != null){
			RelationalQuery sq = new SqlQueryImpl();
			sq.addTable(NODE_TABLE, "n1");
			sq.setSelectClause("n1.node_id AS nodeId");
			WhereClause subWc = new SqlWhereClauseImpl();
			subWc.addInConstraint("n1.uid", ontologies);
			sq.setWhereClause(subWc);
			wc.addInConstraint("n.source_id", sq);
		}
		rq.setWhereClause(wc);
//		System.out.println(rq.toSQL());
		if(zfinOption){
			zq = new SqlQueryImpl();
			zq.addTable(NODE_TABLE, "n2");
			zq.setSelectClause("n2.uid AS uid");
			
			WhereClause wcz = new SqlWhereClauseImpl();
			wcz.addCaseInsensitiveRegexConstraint("n2.uid", "ZDB-GENE");
			wcz.addCaseInsensitiveRegexConstraint("lower(n2.label)", searchTerm);
			zq.setWhereClause(wcz);
			logger.fine(zq.toString());
		}

		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				results.add(getNode(rs.getString(1)));
			}
			if(zq != null){
				ResultSet rs2 = execute(zq);
				while(rs2.next())
					results.add(getNode(rs2.getString(1)));
			}
			
		} catch (SQLException e) {
			System.err.println("Error fetching nodes: "
					+ e.getMessage());
			throw e;
		}

		return results;
	}

	/**
	 * This method is used to find synonyms for the TermResource
	 */
	public Collection<Node> getSynonymsForTerm(String searchTerm) throws SQLException{
		
		Collection<Node> results = new ArrayList<Node>();
		
		RelationalQuery rq = new SqlQueryImpl();
		rq.addTable("alias", "a");
		rq.addTable(NODE_TABLE, "n");
		rq.setSelectClause("a.label as synonym");

		WhereClause wc = new SqlWhereClauseImpl();
		wc.addJoinConstraint("a.node_id", "n.node_id");
		wc.addEqualityConstraint("n.label", searchTerm);
		
		rq.setWhereClause(wc);
		//	System.out.println(rq.toSQL());
		/*
		 * A set to weed out duplicate synonyms
		 */
		Set<String> synonymSet = new HashSet<String>();
		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				String synonym = rs.getString(1);
				synonymSet.add(synonym);
			}
			
			int j = 0;
			for(String synonym : synonymSet){
			  	Node node = new Node("Synonym#" + ++j);
				LiteralStatement s = new LiteralStatement();
				s.setNodeId(node.getId());
				s.setRelationId("hasSynonym");
				s.setValue(synonym);
				node.addStatement(s);
				results.add(node);
			}
				
		} catch (SQLException e) {
			System.err.println("Error fetching nodes: "
					+ e.getMessage());
			e.printStackTrace();
			throw e;
		}
		
		return results;
		
	}
	
	public Collection<Node> getNodesForSearchTermBySynonym(String searchTerm, boolean zfinOption, List<String> ontologies, boolean searchByName) throws SQLException{

		Collection<Node> results = new ArrayList<Node>();

		RelationalQuery rq = new SqlQueryImpl();
		rq.addTable("alias", "a");
		rq.addTable(NODE_TABLE, "n");
		rq.setSelectClause("n.uid AS uid, a.label as synonym");

		WhereClause wc = new SqlWhereClauseImpl();
		wc.addJoinConstraint("a.node_id", "n.node_id");
		if(searchByName){
			wc.addCaseInsensitiveRegexConstraint("lower(a.label)", searchTerm);
		}
		else{
			wc.addCaseInsensitiveRegexConstraint("lower(n.label)", searchTerm);
		}
		if(ontologies != null){
			RelationalQuery sq = new SqlQueryImpl();
			sq.addTable(NODE_TABLE, "n1");
			sq.setSelectClause("n1.node_id AS nodeId");
			WhereClause subWc = new SqlWhereClauseImpl();
			subWc.addInConstraint("n1.uid", ontologies);
			sq.setWhereClause(subWc);
			wc.addInConstraint("n.source_id", sq);
		}
		rq.setWhereClause(wc);
		//	System.out.println(rq.toSQL());
		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				String nodeId = rs.getString(1);
				Node node = getNode(nodeId);
				LiteralStatement s = new LiteralStatement();
				s.setNodeId(nodeId);
				s.setRelationId("hasSynonym");
				s.setValue(rs.getString(2));
				node.addStatement(s);
				results.add(node);
			}
		} catch (SQLException e) {
			System.err.println("Error fetching nodes: "
					+ e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return results;

	}

	public Collection<Node> getNodesForSearchTermByDefinition(String searchTerm, boolean zfinOption, List<String> ontologies) throws SQLException{

		Collection<Node> results = new HashSet<Node>();

		RelationalQuery rq = new SqlQueryImpl();
		rq.addTable("description", "d");
		rq.addTable(NODE_TABLE, "n");
		rq.setSelectClause("n.uid AS uid, d.label as definition");

		WhereClause wc = new SqlWhereClauseImpl();
		wc.addJoinConstraint("d.node_id", "n.node_id");
		wc.addCaseInsensitiveRegexConstraint("lower(d.label)", searchTerm);
		
		if(ontologies != null){
			RelationalQuery sq = new SqlQueryImpl();
			sq.addTable(NODE_TABLE, "n1");
			sq.setSelectClause("n1.node_id AS nodeId");
			WhereClause subWc = new SqlWhereClauseImpl();
			subWc.addInConstraint("n1.uid", ontologies);
			sq.setWhereClause(subWc);
			wc.addInConstraint("n.source_id", sq);
		}
		rq.setWhereClause(wc);
		//	System.out.println(rq.toSQL());
		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				String nodeId = rs.getString(1);
				Node node = getNode(nodeId);
				LiteralStatement s = new LiteralStatement();
				s.setNodeId(nodeId);
				s.setRelationId("hasDefinition");
				s.setValue(rs.getString(2));
				node.addStatement(s);
				results.add(node);
			}
		} catch (SQLException e) {
			System.err.println("Error fetching nodes: "
					+ e.getMessage());
			e.printStackTrace();
			throw e;
		}

		return results;

	}


	public Collection<Node> getAnnotatedEntitiesBelowNodeSet(
			Collection<String> ids, EntailmentUse entailment,
			GraphTranslation gea) {
		return getNodesBelowNodeSet(ids, entailment, gea,
		"implied_annotation_link_to_node");
	}


	public AggregateStatisticCollection getSummaryStatistics() {
		AggregateStatisticCollection sc = new AggregateStatisticCollection();

		RelationalQuery q = new SqlQueryImpl();
		q.addTable("source_summary");
		ResultSet rs;
		try {
			rs = execute(q);
			while (rs.next()) {
				String src = rs.getString(NODE_EXPOSED_ID_COLUMN);
				Integer nc = rs.getInt("node_count");
				sc.setCount(NODE_TABLE, src, nc);
				Integer lc = rs.getInt("link_count");
				sc.setCount(LINK_TABLE, src, lc);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sc;
	}

	private Double getBasicSimilarityScore(int iid1, int iid2) {
		try {
			CallableStatement cs = executeSqlFunc("get_basic_similarity_score",
					Types.REAL, iid1, iid2);
			float f = cs.getFloat(1);
			return (double) f;
			//return (float) this.callSqlFunc("get_basic_similarity_score", iid1, iid2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Double getBasicSimilarityScore(String aeid1, String aeid2) {
		int iid1 = this.getNodeInternalId(aeid1);
		int iid2 = this.getNodeInternalId(aeid2);
		return getBasicSimilarityScore(iid1, iid2);
	}

	public void cacheSimilarityScores(QueryTerm qt) {
		cacheSimilarityScores(qt,null);
	}
	public void cacheSimilarityScores(QueryTerm qt, QueryTerm qt2) {
		Collection<Integer> iids = new ArrayList<Integer>();
		Collection<Integer> iids2 = new ArrayList<Integer>();
		String alias = "query_node";
		ResultSet rs;
		try {
			RelationalQuery rq;
			if (qt == null) {
				// all annotations
				rq = new SqlQueryImpl();
				rq.addTable(REIFIED_LINK_TABLE);
				alias = REIFIED_LINK_TABLE;
			}
			else {
				rq = translateQueryForNode(qt);
			}
			rq.setSelectClause(tblCol(alias,NODE_INTERNAL_ID_COLUMN));
			rq.getSelectClause().setDistinct(true);
			System.out.println(rq.toString());
			rs = this.execute(rq);
			while (rs.next()) {
				iids.add(rs.getInt(NODE_INTERNAL_ID_COLUMN));
			}
			if (qt2 == null) {
				iids2 = iids;
			}
			else {
				RelationalQuery rq2 = translateQueryForNode(qt2);
				rq2.setSelectClause(tblCol(alias,NODE_INTERNAL_ID_COLUMN));
				rq.getSelectClause().setDistinct(true);
				System.out.println(rq2.toString());
				rs = this.execute(rq2);
				while (rs.next()) {
					iids2.add(rs.getInt(NODE_INTERNAL_ID_COLUMN));
				}
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int iid1 : iids) {
			System.out.println(" caching Q:"+iid1);
			for (int iid2 : iids2) {
				double bss = 1;
				bss = getBasicSimilarityScore(iid1, iid2);
			}
		}
	}


	@Override
	public Double getInformationContentByAnnotations(String classNodeId) {
		// String sql =
		// "SELECT shannon_information FROM class_node_entropy_by_evidence WHERE node_id = "
		// +getNodeInternalId(classNodeId);
		try {
			// caching is implemented in the database
			// we need to use the REAL type with Pg, not FLOAT
			CallableStatement cs = executeSqlFunc("get_information_content",
					Types.REAL, getNodeInternalId(classNodeId));
			float f = cs.getFloat(1);
			return (double) f;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}


	// exhaustive search - compares node with all matching targets,
	// calls getBasicSimilarityScore - mapped to SQL function, which caches results
	private List<ScoredNode> getSimilarNodesExhaustive(SimilaritySearchParameters params, String nodeId) {
		String ontologySourceId = params.ontologySourceId;

		/*
		 * what kind of thing is nodeId? We want to compare like with like; eg gene with gene,
		 * genotype with genotype.
		 * At this time this query is hardcoded to use the asserted is_a. It doesn't take things
		 * such as the SO hierarchy into account for example. Also it will not work for instances, just
		 * classes
		 */
		LinkQueryTerm isaQt = new LinkQueryTerm(nodeId, rvocab.is_a(), null);
		isaQt.setInferred(false);
		isaQt.setAspect(Aspect.TARGET);
		Collection<Node> isas = this.getNodesByQuery(isaQt);
		Integer isaIid = null;
		for (Node isa : isas) {
			isaIid = this.getNodeInternalId(isa.getId());
			break;
		}
		Integer sourceIid = null;
		if (ontologySourceId != null) {
			sourceIid = this.getNodeInternalId(ontologySourceId);
		}

		RelationalQuery fetchRq = new SqlQueryImpl();
		//fetchRq.addTable(IMPLIED_ANNOTATION_LINK_ALIAS,ialAlias);
		String nodeAlias = "hit_node";
		fetchRq.addTable("node", nodeAlias);
		WhereClause wc = fetchRq.getWhereClause();
		if (isaIid != null) {
			String isaLinkAlias = "isa_link";
			// return AEs of the same type: e.g. if query is a gene, return genes
			fetchRq.addTable(IS_A_LINK_TABLE,isaLinkAlias);
			wc.addJoinConstraint(tblCol(nodeAlias,NODE_INTERNAL_ID_COLUMN),
					tblCol(isaLinkAlias,NODE_INTERNAL_ID_COLUMN));
			wc.addEqualityConstraint(tblCol(isaLinkAlias,LINK_TARGET_INTERNAL_ID_COLUMN), isaIid);	
			wc.addConstraint(isaLinkAlias+".is_inferred = 'f'");
		}

		// must have at least 1 annotation
		fetchRq.addTable(REIFIED_LINK_TABLE);
		wc.addJoinConstraint(tblCol(nodeAlias,NODE_INTERNAL_ID_COLUMN),
				tblCol(REIFIED_LINK_TABLE,NODE_INTERNAL_ID_COLUMN));

		if (params.in_organism != null) {
			params.hitNodeFilter = new LinkQueryTerm("OBO_REL:in_organism",params.in_organism);
			params.hitNodeFilter.setQueryAlias("in_organism_link");
		}
		if (params.hitNodeFilter != null) {
			this.translateQuery(params.hitNodeFilter, fetchRq, tblCol(nodeAlias,"node_id"));
		}

		fetchRq.setSelectClause(tblCol(nodeAlias,NODE_INTERNAL_ID_COLUMN));
		fetchRq.getSelectClause().setDistinct(true);

		int iid1 = this.getNodeInternalId(nodeId); // query node

		Collection<Integer> iids = new ArrayList<Integer>();
		HashMap<Integer,Double> iid2score = new HashMap<Integer,Double>();
		try {
			System.err.println("  exhaustive hit list q: "+fetchRq.toString());
			ResultSet rs = this.execute(fetchRq);
			while (rs.next()) {
				int iid2 = rs.getInt(NODE_INTERNAL_ID_COLUMN);
				iids.add(iid2);
				Double bss = this.getBasicSimilarityScore(iid1, iid2);
				iid2score.put(iid2, bss);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("got hit list; size="+iids.size());
		LinkedHashMap<Integer,Double> shm = sortHashMapByValuesD(iid2score, true);
		System.err.println("shm; size="+shm.size());
		List<ScoredNode> sns = new LinkedList<ScoredNode>();
		Connection conn = obd.getConnection();
		int rowNum = 0;
		try {
			PreparedStatement nodePS = conn.prepareStatement("SELECT uid FROM node WHERE node_id=?");

			for (int iid2 : shm.keySet()) {

				if (rowNum > params.max_candidate_hits) {
					System.err.println("got "+rowNum+" hits");
					break;
				}
				rowNum++;
				double score = shm.get(iid2);
				nodePS.setInt(1, iid2);
				ResultSet nodeRS =  nodePS.executeQuery();
				nodeRS.next();
				String uid = nodeRS.getString(NODE_EXPOSED_ID_COLUMN);
				ScoredNode sn = new ScoredNode(uid, score);
				sns.add(sn);
				System.err.println(" ::"+sn);
			}
		}
		catch (SQLException e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Collections.sort(sns);
		return sns;
	}

	@Override
	public List<ScoredNode> getSimilarNodes(SimilaritySearchParameters params, String nodeId) {

		if (params.isExhaustive) {
			return getSimilarNodesExhaustive(params, nodeId);
		}
		String ontologySourceId = params.ontologySourceId;

		/*
		 * what kind of thing is nodeId? We want to compare like with like; eg gene with gene,
		 * genotype with genotype.
		 * At this time this query is hardcoded to use the asserted is_a. It doesn't take things
		 * such as the SO hierarchy into account for example. Also it will not work for instances, just
		 * classes
		 */
		LinkQueryTerm isaQt = new LinkQueryTerm(nodeId, rvocab.is_a(), null);
		isaQt.setInferred(false);
		isaQt.setAspect(Aspect.TARGET);
		Collection<Node> isas = this.getNodesByQuery(isaQt);
		Integer isaIid = null;
		for (Node isa : isas) {
			isaIid = this.getNodeInternalId(isa.getId());
			break;
		}
		Integer sourceIid = null;
		if (ontologySourceId != null) {
			sourceIid = this.getNodeInternalId(ontologySourceId);
		}


		/*
		 * first of all find some bait with which to hook comparable node ids -
		 * we must take the inferred graph into account. we want to find similar
		 * nodes that share inferred annotations, but all nodes will share
		 * annotations to the root. as an approximation, we only include
		 * inferred annotations to reasonably informative nodes: nodes near the
		 * root will have high numbers of annotations
		 */
		Collection<Integer> baitIds = new HashSet<Integer>();
		Map<Integer,Integer> iid2srcIid = new HashMap<Integer,Integer>();
		Map<Integer,Integer> iid2total = new HashMap<Integer,Integer>();

		int iid = this.getNodeInternalId(nodeId); // query node

		String ialAlias = "ial";
		WhereClause  wc;

		RelationalQuery baitRq = new SqlQueryImpl();
		baitRq.addTable("implied_annotation_link_with_total", ialAlias); // may be materialized
		wc = baitRq.getWhereClause();
		String srcAlias = "c";
		baitRq.addTable(NODE_TABLE, srcAlias);
		wc.addJoinConstraint(tblCol(srcAlias,NODE_INTERNAL_ID_COLUMN), 
				tblCol(ialAlias,LINK_TARGET_INTERNAL_ID_COLUMN));
		if (sourceIid != null)
			wc.addEqualityConstraint(tblCol(srcAlias,NODE_SOURCE_INTERNAL_ID_COLUMN), sourceIid);

		wc.addEqualityConstraint(tblCol(ialAlias,LINK_NODE_INTERNAL_ID_COLUMN), iid);
		wc.addConstraint("total < " + params.search_profile_max_annotated_entities_per_class); // only choose informative nodes


		baitRq.setOrderByClause("total"); // most informative first
		//baitRq.setSelectClause("DISTINCT *");
		baitRq.setSelectClause("DISTINCT "+srcAlias+".source_id,total,"+LINK_TARGET_INTERNAL_ID_COLUMN);
		System.err.println(baitRq.toSQL());
		// find annotations for this node, then use this to fetch scores
		Map<Integer,Integer> srcIid2numSelected = new HashMap<Integer,Integer>();
		ResultSet rs;
		int i = 0;
		try {
			rs = this.execute(baitRq);
			while (rs.next()) {
				int siid = rs.getInt("source_id");
				int tiid = rs.getInt(LINK_TARGET_INTERNAL_ID_COLUMN);
				iid2srcIid.put(tiid, siid);
				int numSelectedInSrc = 0;
				if (srcIid2numSelected.containsKey(siid)) {
					numSelectedInSrc = srcIid2numSelected.get(siid);
				}
				if (numSelectedInSrc < params.search_profile_max_classes_per_source) {
					numSelectedInSrc++;
					srcIid2numSelected.put(siid,numSelectedInSrc);

					baitIds.add(tiid);
				}
				else {
				}

				iid2total.put(tiid, rs.getInt("total"));
				i++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("num bait ids = "+baitIds.size());
		System.err.println("srcIid2numSelected = "+srcIid2numSelected);

		/*
		Integer MIN_TOTAL_ANNOTS = 1;
		Integer MAX_TOTAL_ANNOTS = 100;
		int MAX_HOOKS = 200;
		while (baitIds.size() < MAX_HOOKS && MAX_TOTAL_ANNOTS < 200) {
			System.err.println("MAX: "+MAX_TOTAL_ANNOTS);
			System.err.println("hooks: "+baitIds.size());

			baitIds.addAll(fetchQueryBaitIds(iid,sourceIid,hitNodeFilter, MIN_TOTAL_ANNOTS, MAX_TOTAL_ANNOTS, MAX_HOOKS));
			MIN_TOTAL_ANNOTS = MAX_TOTAL_ANNOTS;
			MAX_TOTAL_ANNOTS *= 2;
		}
		String ialAlias = "ial";
		 */

		/*
		 * 
		 */
		RelationalQuery fetchRq = new SqlQueryImpl();
		//fetchRq.addTable(IMPLIED_ANNOTATION_LINK_ALIAS,ialAlias);
		fetchRq.addTable("implied_annotation_link_with_prob",ialAlias);
		wc = fetchRq.getWhereClause();
		wc.addInConstraint("ial.object_id", baitIds);
		if (isaIid != null) {
			// return AEs of the same type: e.g. if query is a gene, return genes
			fetchRq.addTable(IS_A_LINK_TABLE);
			wc.addJoinConstraint("ial.node_id", IS_A_LINK_TABLE+".node_id");
			wc.addEqualityConstraint(IS_A_LINK_TABLE+".object_id", isaIid);	
			wc.addConstraint(IS_A_LINK_TABLE+".is_inferred = 'f'");
		}
		//fetchRq.setSelectClause("ial.node_id, COUNT(DISTINCT ial.object_id) AS ovlp");
		fetchRq.setSelectClause("ial.node_id, SUM(-log(p)) AS ovlp");
		//fetchRq.setSelectClause("ial.node_id, ial.object_id");
		fetchRq.setGroupByClause("ial.node_id");
		fetchRq.setOrderByClause("ovlp DESC");

		if (params.in_organism != null) {
			params.hitNodeFilter = new LinkQueryTerm("OBO_REL:in_organism",params.in_organism);
		}
		if (params.hitNodeFilter != null) {
			System.err.println(baitRq.toSQL());
			this.translateQuery(params.hitNodeFilter, fetchRq, tblCol(ialAlias,"node_id"));
		}

		System.err.println(fetchRq.toSQL());
		return getSimilarNodes(fetchRq,iid,params);
	}


	/**
	 * given a relational query to pull nodes, executes query and iterates through results
	 * building up a ScoredNode for each
	 * 
	 * @param fetchRq
	 * @param iid
	 * @param params
	 * @return
	 */
	private List<ScoredNode> getSimilarNodes(RelationalQuery fetchRq, Integer iid, SimilaritySearchParameters params) {

		List<ScoredNode> sns = new LinkedList<ScoredNode>();

		Connection conn = obd.getConnection();
		int rowNum = 0;
		try {
			String metricCol = "basic_score";
			//String metricCol = "total_nodes_in_intersection";
			String scoreSQL = "SELECT basic_score FROM node_pair_annotation_similarity_score WHERE node1_id= " + iid + " AND node2_id=?";
			//String scoreSQL = "SELECT total_nodes_in_intersection FROM node_pair_annotation_intersection_count WHERE node1_id= " + iid + " AND node2_id=?";
			System.err.println(scoreSQL);
			PreparedStatement scorePS = conn.prepareStatement(scoreSQL);
			PreparedStatement nodePS = conn.prepareStatement("SELECT uid FROM node WHERE node_id=?");
			ResultSet rs = this.execute(fetchRq);
			while (rs.next() && rowNum < params.max_candidate_hits) {
				//int ovlp = rs.getInt("ovlp");
				float ovlp = rs.getFloat("ovlp");
				// get the uid
				nodePS.setInt(1, rs.getInt(NODE_INTERNAL_ID_COLUMN));
				ResultSet nodeRS =  nodePS.executeQuery();
				nodeRS.next();
				String uid = nodeRS.getString(NODE_EXPOSED_ID_COLUMN);
				// get the overlap score
				scorePS.setInt(1, rs.getInt(NODE_INTERNAL_ID_COLUMN));
				ResultSet scoreRS =  scorePS.executeQuery();
				scoreRS.next();
				ScoredNode sn = new ScoredNode(uid, - scoreRS.getFloat(metricCol));
				sns.add(sn);
				System.err.println(":: "+sn+" ovlp: "+ovlp);
				rowNum++;
			}
		} catch (SQLException e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collections.sort(sns);

		return sns;

	}

	@Deprecated
	private Collection<Integer> fetchQueryBaitIds(int iid, Integer sourceIid, QueryTerm hitNodeFilter, Integer MIN_TOTAL_ANNOTS, Integer MAX_TOTAL_ANNOTS, int MAX_HOOKS) {
		Collection<Integer> baitIds = new HashSet<Integer>();

		RelationalQuery baitRq = new SqlQueryImpl();
		String ialAlias = "ial";
		baitRq.addTable("implied_annotation_link_with_total", ialAlias); // may be materialized
		WhereClause wc = baitRq.getWhereClause();
		if (sourceIid != null) {
			String srcAlias = "src";
			baitRq.addTable(NODE_TABLE, srcAlias);
			wc.addJoinConstraint(tblCol(srcAlias,NODE_INTERNAL_ID_COLUMN), 
					tblCol(ialAlias,LINK_TARGET_INTERNAL_ID_COLUMN));
			wc.addEqualityConstraint(tblCol(srcAlias,NODE_SOURCE_INTERNAL_ID_COLUMN), sourceIid);
		}
		wc.addEqualityConstraint(tblCol(ialAlias,LINK_NODE_INTERNAL_ID_COLUMN), iid);
		wc.addConstraint("total < " + MAX_TOTAL_ANNOTS); // only choose informative nodes
		wc.addConstraint("total > " + MIN_TOTAL_ANNOTS); // only choose informative nodes

		if (hitNodeFilter != null) {
			this.translateQuery(hitNodeFilter, baitRq, ialAlias);
		}

		baitRq.setOrderByClause("total"); // most informative first
		System.err.println(baitRq.toSQL());
		// find annotations for this node, then use this to fetch scores
		ResultSet rs;
		int i = 0;
		try {
			rs = this.execute(baitRq);
			while (rs.next()) {
				baitIds.add(rs.getInt(LINK_TARGET_INTERNAL_ID_COLUMN));
				i++;
				if (i > MAX_HOOKS)
					break;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baitIds;
	}

	public Collection<ScoredNode> getCoAnnotatedClasses(String n1id)
	throws ShardExecutionException {
		AnnotationLinkQueryTerm aqt = new AnnotationLinkQueryTerm(n1id);
		Integer numEntities1 = this.getNodeAggregateQueryResults(aqt, null);
		int iid = getNodeInternalId(n1id);
		SqlQueryImpl rq = new SqlQueryImpl();
		rq.addTable("co_annotated_to_pair_with_score");
		WhereClause wc = rq.getWhereClause();
		wc.addEqualityConstraint("object1_id", iid);
		rq.getOrderByClause().addColumn("object2_score DESC");
		ResultSet rs;
		List<ScoredNode> sns = new LinkedList<ScoredNode>();
		try {
			rs = execute(rq);
			int sampleSize = 10000;
			while (rs.next()) {
				int n2iid = rs.getInt("object2_id");
				Node n2 = getNodeByInternalId(n2iid);
				ScoredNode sn = new ScoredNode();
				sns.add(sn);
				sn.setNodeId(n2.getId());
				int numBoth = rs.getInt("total_entities_annotated_to_both");
				sn.setCount(numBoth);
				int numEntities2 = rs.getInt("annotated_entity_count");
				double pBoth = (numEntities1 / (double) sampleSize)
				* (numEntities2 / (double) sampleSize);
				double qBoth = 1 - pBoth;
				double score = 1 - Math.pow(qBoth, numEntities1);
				double expectedBoth = (numEntities1 * numEntities2)
				/ (double) sampleSize;
				sn.setScore(score);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Collections.sort(sns);
		return sns;
	}



	public Collection<CongruentPair> getAnnotationSourceCongruenceForAnnotatedEntity(
			String nid) throws SQLException {
		SqlQueryImpl rq = new SqlQueryImpl();
		int iid = this.getNodeInternalId(nid);
		String tbl = "annotated_entity_congruence_between_annotsrc_pair";
		rq.addTable(tbl);
		WhereClause wc = rq.getWhereClause();
		wc.addEqualityConstraint("annotated_entity_id", iid);
		ResultSet rs = this.execute(rq);
		Collection<CongruentPair> cps = new LinkedList<CongruentPair>();
		while (rs.next()) {
			CongruentPair cp = new CongruentPair();
			cp.setBaseNode(getNodeByInternalId(rs.getInt("base_source_id")));
			cp
			.setTargetNode(getNodeByInternalId(rs
					.getInt("target_source_id")));
			if (cp.getBaseNode().equals(cp.getTargetNode()))
				continue;
			cp.setTotalNodes(rs.getInt("total_annotation_nodes"));
			cp.setTotalNodesInCommon(rs.getInt("total_nodes_in_common"));
			cp.setCongruence(rs.getDouble("congruence"));
			cps.add(cp);
		}
		return cps;
	}


	@Override
	public Map<Node, Integer> getAnnotatedEntityCountBySubset(
			AnnotationLinkQueryTerm aqt, String subsetId)
			throws ShardExecutionException {
		// Create a query object that filters class nodes based on
		// membership of a subset. This will later by used in conjunction
		// with the annotation query to find all matching annotations to
		// subset classes
		SubsetQueryTerm sqt = new SubsetQueryTerm(subsetId);
		sqt.setQueryAlias("in_subset");
		return getAnnotatedEntityCountByMapping(aqt, sqt);
	}

	public Map<Node, Integer> getAnnotatedEntityCountByMapping(
			AnnotationLinkQueryTerm aqt, QueryTerm targetQt)
			throws ShardExecutionException {

		// if no query specified, create an empty annotation query object,
		// this will perform the count for all annotations in
		// the database
		if (aqt == null)
			aqt = new AnnotationLinkQueryTerm();

		Map<Node, Integer> countByNode = new HashMap<Node, Integer>();

		// constrain the annotation query
		aqt.setTarget(targetQt);

		// Translate the annotation query to SQL
		RelationalQuery rq = new SqlQueryImpl();
		aqt.setQueryAlias("annot");
		translateQuery(aqt, rq, null);

		// The default query will retrieve the surrogate database IDs;
		// we need to do an extra join on the node table to be able to
		// populate a Node object
		WhereClause wc = rq.getWhereClause();
		rq.addTable(NODE_TABLE, SUBJECT_NODE_ALIAS);
		wc.addConstraint("snode.node_id = in_subset.node_id");
		rq
		.setSelectClause("snode.uid AS uid, snode.label AS label, COUNT(DISTINCT annot.node_id) AS c");
		rq.setGroupByClause("snode.uid, snode.label");

		ResultSet rs;
		try {
			rs = execute(rq);
			while (rs.next()) {
				String id = rs.getString(NODE_EXPOSED_ID_COLUMN); // TODO: use enums
				Node n = new Node(id);
				n.setLabel(rs.getString("label"));
				countByNode.put(n, rs.getInt("c"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ShardExecutionException("Error executing: " + rq.toSQL());
		}
		return countByNode;
	}


	/**
	 * perform an aggregate query; e.g. count all nodes; avg all nodes;
	 * optionally partitioned; eg by source of node
	 * 
	 * @param queryTerm
	 * @return
	 */
	public Integer getNodeAggregateQueryResults(QueryTerm queryTerm,
			AggregateType aggType) {
		RelationalQuery rq = new SqlQueryImpl();
		String tbl = translateQuery(queryTerm, rq, null);
		rq.setSelectClause(aggType + "(DISTINCT " + tbl + ".node_id) AS agg");

		Integer num = null;
		try {
			ResultSet rs = execute(rq);
			if (rs.next()) {
				num = rs.getInt("agg");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return num;
	}

	/**
	 * perform an aggregate query; e.g. count all nodes; avg all nodes;
	 * optionally partitioned; eg by source of node
	 * 
	 * Example: gnaqr(null, COUNT, {LinkQueryTerm()/SOURCE}
	 * 
	 * @param queryTerm
	 * @return
	 */
	public Integer getNodeAggregateQueryResults(QueryTerm queryTerm,
			AggregateType aggType, Collection<QueryTerm> groupByQueryTerms) {
		RelationalQuery rq = new SqlQueryImpl();
		String tbl = translateQuery(queryTerm, rq, null);

		// GROUP BY a,b,c
		// group by term can be a link eg to source
		GroupByClause groupByClause = rq.getGroupByClause();
		for (QueryTerm groupBy : groupByQueryTerms) {
			String groupByTbl = translateQuery(groupBy, rq, tbl + ".node_id");
			groupByClause.addColumn(groupByTbl + ".uid");
		}

		if (aggType.equals(AggregateType.COUNT))
			rq.setSelectClause("COUNT(DISTINCT " + tbl + ".node_id) AS agg");
		else if (aggType.equals(AggregateType.AVERAGE))
			rq.setSelectClause("AVG(" + tbl + ".node_id) AS agg");
		else
			rq.setSelectClause(aggType + "(" + tbl + ".node_id) AS agg");

		Integer num = null;
		try {
			ResultSet rs = execute(rq);
			if (rs.next()) {
				num = rs.getInt("agg");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return num;
	}

	/**
	 * perform an aggregate query; e.g. count all nodes; avg all nodes;
	 * optionally partitioned; eg by source of node
	 * 
	 * @param queryTerm
	 * @return
	 */
	public Integer getLinkAggregateQueryResults(QueryTerm queryTerm,
			AggregateType aggType) {
		// TODO: DRY
		if (!(queryTerm instanceof LinkQueryTerm)) {
			// it only makes sense to return links for link queries;
			// a query to for example LiteralQuery("apoptosis") should
			// be wrapped such that links with this node are returned
			LinkQueryTerm outerq = new LinkQueryTerm();
			outerq.setNode(queryTerm);
			return getLinkAggregateQueryResults(outerq, aggType);
		}

		RelationalQuery rq = new SqlQueryImpl();
		String tbl = translateQuery(queryTerm, rq, null);
		rq.setSelectClause(aggType + "(DISTINCT " + tbl + ".link_id) AS agg");

		Integer num = null;
		try {
			ResultSet rs = execute(rq);
			if (rs.next()) {
				num = rs.getInt("agg");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return num;
	}

	public Collection<Statement> getStatementsByQuery(QueryTerm queryTerm) {
		Collection<Statement> stmts = new LinkedList<Statement>();

		if (queryTerm instanceof LinkQueryTerm) {
			stmts.addAll(getLinkStatementsByQuery(queryTerm));
		} else if (queryTerm instanceof LiteralQueryTerm) {
			stmts.addAll(getLiteralStatementsByQuery(queryTerm));
		} else {
			stmts.addAll(getLiteralStatementsByQuery(queryTerm));
			stmts.addAll(getLinkStatementsByQuery(queryTerm));
		}
		return stmts;
	}

	public Map<String, Collection<LinkStatement>> getAnnotationStatementMapByQuery(
			QueryTerm queryTerm) {
		AnnotationLinkQueryTerm aqt;
		if (queryTerm instanceof AnnotationLinkQueryTerm)
			aqt = (AnnotationLinkQueryTerm) queryTerm;
		else
			aqt = new AnnotationLinkQueryTerm(queryTerm);
		aqt.setQueryAlias("annot");
		RelationalQuery rq = translateQueryForLinkStatement(aqt);
		String inodeTblAlias = rq.addAutoAliasedTable(NODE_TABLE);
		rq.getWhereClause().addJoinConstraint(
				tblCol(inodeTblAlias, NODE_INTERNAL_ID_COLUMN), tblCol(IMPLIED_ANNOTATION_LINK_ALIAS,
						LINK_TARGET_INTERNAL_ID_COLUMN));
		rq.getSelectClause().addColumn(inodeTblAlias + ".uid AS inode_uid");
		rq.getSelectClause().addColumn(inodeTblAlias + ".label AS inode_label");
		Collection<LinkStatement> statements = new LinkedList<LinkStatement>();
		Map<String, Collection<LinkStatement>> annotsByMappedId = new HashMap<String, Collection<LinkStatement>>();
		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				LinkStatement s = createLinkStatementFromResultSet(rs);
				statements.add(s);
				String inodeId = rs.getString("inode_uid");
				if (!annotsByMappedId.containsKey(inodeId))
					annotsByMappedId.put(inodeId, new HashSet<LinkStatement>());
				annotsByMappedId.get(inodeId).add(s);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return annotsByMappedId;
	}

	public Node createNodeFromResultSet(ResultSet rs) throws SQLException {
		Node n = new Node(rs.getString(NODE_EXPOSED_ID_COLUMN));
		n.setMetatype(rs.getString("metatype"));
		n.setLabel(rs.getString("label"));
		setSourceId(n, rs.getInt(NODE_SOURCE_INTERNAL_ID_COLUMN));
		return n;
	}

	public LinkStatement createLinkStatementFromResultSet(ResultSet rs)
	throws SQLException {
		LinkStatement s = new LinkStatement();
		s.setNodeId(rs.getString(LINK_NODE_EXPOSED_ID_COLUMN));
		s.setRelationId(rs.getString(LINK_RELATION_EXPOSED_ID_COLUMN));
		s.setTargetId(rs.getString(LINK_TARGET_EXPOSED_ID_COLUMN));
		String positedById = rs.getString(LINK_REIF_EXPOSED_ID_COLUMN);
		if (positedById != null) {
			// TODO: make more efficient, use internal IDs.
			s.setPositedByNodeId(positedById);
			Collection<Statement> subStatements = getStatementsByNode(positedById);
			s.setSubStatements(subStatements);
		}

		// if this is a reified link (annotation), fetch all metadata
		// attached to the link; e.g. provenance, evidence etc
		Integer reiflinkNodeInternalId = rs.getInt(LINK_REIF_INTERNAL_ID_COLUMN);
		if (reiflinkNodeInternalId != null && reiflinkNodeInternalId != 0) {
			// TODO: redundant with the above
			// s.setPositedByNodeId(positedById);

			// TODO - make this more efficient. Currently we assume the
			// only metadata is links and tagval literals
			// TODO - we don't need node.uid
			LabelQueryTerm linkMetadataDataQt = new LabelQueryTerm(
					AliasType.INTERNAL_ID, reiflinkNodeInternalId);
			for (Statement ss : getLinkStatementsByQuery(linkMetadataDataQt))
				s.addSubStatement(ss);
			//for (Statement ss : getLiteralStatementsByQuery(linkMetadataDataQt,"tagval"))
			for (Statement ss : getLiteralStatementsByQuery(linkMetadataDataQt))
				s.addSubStatement(ss);
		}
		s.setAppliesToAllInstancesOf(rs.getBoolean(APPLIES_TO_ALL));
		s.setExistential(rs.getBoolean("object_quantifier_some"));
		s.setUniversal(rs.getBoolean("object_quantifier_only"));
		s.setInferred(rs.getBoolean("is_inferred"));
		setSourceId(s, rs.getInt(LINK_SOURCE_INTERNAL_ID_COLUMN)); // may invoke another query
		s.setExistential(rs.getBoolean("object_quantifier_some"));
		s.setUniversal(rs.getBoolean("object_quantifier_only"));

		String comb = rs.getString("combinator");
		if (comb.equals("I"))
			s.setIntersectionSemantics(true);
		else if (comb.equals("U"))
			s.setUnionSemantics(true);
		return s;
	}

	public LiteralStatement createLiteralStatementFromResultSet(ResultSet rs)
	throws SQLException {

		LiteralStatement s = new LiteralStatement();
		s.setNodeId(rs.getString(LINK_NODE_EXPOSED_ID_COLUMN));
		s.setRelationId(rs.getString(LINK_RELATION_EXPOSED_ID_COLUMN));
		String valCol = "val";
		s.setValue(rs.getString(valCol));
		// TODO setSourceId(s,rs.getInt(LINK_SOURCE_INTERNAL_ID_COLUMN));
		return s;
	}


	public LiteralStatement createLiteralStatementFromResultSet(String tbl, ResultSet rs)
	throws SQLException {

		LiteralStatement s = new LiteralStatement();
		s.setNodeId(rs.getString(LINK_NODE_EXPOSED_ID_COLUMN));
		s.setRelationId(rs.getString(LINK_RELATION_EXPOSED_ID_COLUMN));
		String valCol = "val";
		if (!tbl.equals("tagval")) {
			valCol = "label";
		}
		s.setValue(rs.getString(valCol));
		// TODO setSourceId(s,rs.getInt(LINK_SOURCE_INTERNAL_ID_COLUMN));
		return s;
	}

	public Collection<LinkStatement> getLinkStatementsByQuery(
			QueryTerm queryTerm) {

		RelationalQuery rq = translateQueryForLinkStatement(queryTerm);
		Collection<LinkStatement> statements = new LinkedList<LinkStatement>();
		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				LinkStatement s = createLinkStatementFromResultSet(rs);
				statements.add(s);
			}
		} catch (SQLException e) {
			System.err.println("Error in SQL: " + rq.toSQL());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statements;
	}

	public Collection<LiteralStatement> getLiteralStatementsByQuery(
			QueryTerm queryTerm) {

		RelationalQuery rq = translateQueryForLiteral(queryTerm);
		Collection<LiteralStatement> statements = new LinkedList<LiteralStatement>();
		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				LiteralStatement s = createLiteralStatementFromResultSet(rs);
				statements.add(s);
			}
		} catch (SQLException e) {
			System.err.println("Error in SQL: " + rq.toSQL());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statements;
	}


	public Collection<LinkStatement> getImpliedLinkStatementsByQuery(
			QueryTerm queryTerm, String rel, String innerLinkAlias) {

		RelationalQuery rq = translateQueryForLinkStatement(queryTerm,
				innerLinkAlias);
		Collection<LinkStatement> statements = new LinkedList<LinkStatement>();
		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				LinkStatement s = new LinkStatement();
				s.setNodeId(rs.getString(LINK_NODE_EXPOSED_ID_COLUMN));
				s.setRelationId(rs.getString(rel));
				s.setTargetId(rs.getString(innerLinkAlias + ".object_uid"));

				statements.add(s);
			}
		} catch (SQLException e) {
			System.err.println("uh-oh");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statements;
	}

	/*
	public Collection<LiteralStatement> getLiteralStatementsByQuery(
			QueryTerm queryTerm) {
		if (queryTerm instanceof LiteralQueryTerm
				&& ((LiteralQueryTerm) queryTerm).isAlias()) {
			// optimization: only need to query one table if we want aliases
			return getLiteralStatementsByQuery(queryTerm, "alias");
		} else {
			Collection<LiteralStatement> statements = getLiteralStatementsByQuery(
					queryTerm, "tagval");
			statements.addAll(getLiteralStatementsByQuery(queryTerm, "alias"));
			statements.addAll(getLiteralStatementsByQuery(queryTerm,
			"description"));
			return statements;
		}
	}
	 */

	@Override
	public Collection<LiteralStatement> getLiteralStatementsByNode(
			String nodeID, String relationID) {

		System.err
		.println("WARNING: getLiteralStatementsByNode is hacky and fully implemeneted.");

		Collection<LiteralStatement> statements = new LinkedList<LiteralStatement>();

		RelationalQuery rq = new SqlQueryImpl();
		rq.addTable("node_literal", "nl");
		rq.addTable(NODE_TABLE, "n");
		rq.addTable(NODE_TABLE, "p");

		rq
		.setSelectClause("n.uid as node_uid,p.uid as predicate_uid,nl.val as val");

		WhereClause wc = new SqlWhereClauseImpl();
		wc.addJoinConstraint("nl.node_id", "n.node_id");
		wc.addJoinConstraint("nl.predicate_id", "p.node_id");
		wc.addEqualityConstraint("n.uid", nodeID);
		if (relationID != null) {
			wc.addEqualityConstraint("p.uid", relationID);
		}

		rq.setWhereClause(wc);

		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				LiteralStatement s = new LiteralStatement();
				s.setNodeId(rs.getString(LINK_NODE_EXPOSED_ID_COLUMN));
				s.setRelationId(rs.getString(LINK_RELATION_EXPOSED_ID_COLUMN_FULL));
				s.setValue(rs.getString("val"));
				statements.add(s);
			}
		} catch (SQLException e) {
			System.err.println("Error fetching literal statements: "
					+ e.getMessage());
			e.printStackTrace();
		}

		return statements;

	}


	public RelationalQuery translateQueryForNode(QueryTerm qt) {
		return translateQueryForNode(qt,"query_node");

	}
	public RelationalQuery translateQueryForNode(QueryTerm qt, String nodeAlias) {
		RelationalQuery rq = new SqlQueryImpl();
		String rel = rq.addAutoAliasedTable(NODE_TABLE, nodeAlias);

		translateQuery(qt, rq, tblCol(rel, NODE_INTERNAL_ID_COLUMN));

		if (qt instanceof LinkQueryTerm) {
			// rq.addTable(NODE_TABLE);
			// rq.getWhereClause().addJoinConstraint("node.node_id",
			// rel+".node_id"); // TODO - aspect
			// rel = NODE_TABLE;
		}

		rq.setSelectClause(rel + ".*");

		rq.getSelectClause().setDistinct(true);
		return rq;
	}

	protected Collection<LiteralStatement> old___getLiteralStatementsByQuery(
			QueryTerm queryTerm, String tbl) {

		Collection<LiteralStatement> statements = new LinkedList<LiteralStatement>();
		if (queryTerm instanceof LinkQueryTerm) {
			// cannot return anything by definition
			return statements;
		}
		boolean join = tbl.equals("tagval");
		RelationalQuery rq = old___translateQueryForLiteral(queryTerm, tbl, join);
		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				LiteralStatement s = createLiteralStatementFromResultSet(tbl, rs);
				// TODO setSourceId(s,rs.getInt(LINK_SOURCE_INTERNAL_ID_COLUMN));

				statements.add(s);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statements;
	}


	protected RelationalQuery old___translateQueryForLiteral(QueryTerm qt, String tbl,
			boolean joinRelationNode) {
		RelationalQuery rq = new SqlQueryImpl();
		WhereClause wc = rq.getWhereClause();

		rq.addTable(tbl);
		translateQuery(qt, rq, tblCol(tbl, LINK_NODE_INTERNAL_ID_COLUMN));

		rq.addTable(NODE_TABLE, SUBJECT_NODE_ALIAS);
		wc.addJoinConstraint(tblCol(SUBJECT_NODE_ALIAS,NODE_INTERNAL_ID_COLUMN),
				tblCol(tbl,NODE_INTERNAL_ID_COLUMN));
		SelectClause selectClause = rq.getSelectClause();
		selectClause.setDistinct(true);
		selectClause.addColumn(tbl + ".*");
		selectClause.addColumn(tblCol(SUBJECT_NODE_ALIAS,NODE_EXPOSED_ID_COLUMN), 
				LINK_NODE_EXPOSED_ID_COLUMN);

		if (joinRelationNode) {
			rq.addTable(NODE_TABLE, RELATION_NODE_ALIAS);
			wc.addJoinConstraint(tblCol(RELATION_NODE_ALIAS,NODE_INTERNAL_ID_COLUMN), 
					tblCol(tbl, "tag_id"));
			selectClause.addColumn(tblCol(RELATION_NODE_ALIAS, NODE_EXPOSED_ID_COLUMN),
					LINK_RELATION_EXPOSED_ID_COLUMN);
		} else {
			String pred = tvocab.HAS_SYNONYM();
			if (tbl.equals("description"))
				pred = tvocab.HAS_DEFINITION();
			selectClause.addColumn("CAST('" + pred
					+ "' AS VARCHAR) AS predicate_uid");
		}
		return rq;
	}

	public RelationalQuery old___translateQueryForLiteral(QueryTerm qt) {
		RelationalQuery rq = new SqlQueryImpl();
		WhereClause wc = rq.getWhereClause();

		rq.addTable(LITERAL_TABLE);
		translateQuery(qt, rq, tblCol(LITERAL_TABLE,LINK_NODE_INTERNAL_ID_COLUMN));
		rq.addTable(NODE_TABLE, "snode");
		wc.addJoinConstraint(tblCol("snode",LINK_NODE_INTERNAL_ID_COLUMN), tblCol(LITERAL_TABLE,LINK_NODE_INTERNAL_ID_COLUMN));
		SelectClause selectClause = rq.getSelectClause();
		selectClause.setDistinct(true);
		selectClause.addColumn(tblCol(LITERAL_TABLE,"*"));
		selectClause.addColumn(tblCol("snode", NODE_EXPOSED_ID_COLUMN), LINK_NODE_EXPOSED_ID_COLUMN );
		return rq;
	}

	public RelationalQuery translateQueryForLinkStatement(QueryTerm qt) {
		return translateQueryForLinkStatement(qt, null);
	}

	public RelationalQuery translateQueryForLinkStatement(QueryTerm qt,
			String targetLinkAlias) {
		RelationalQuery rq = new SqlQueryImpl();

		if (!(qt instanceof LinkQueryTerm)) {
			// it only makes sense to return links for link queries;
			// a query to for example LiteralQuery("apoptosis") should
			// be wrapped such that links with this node are returned
			LinkQueryTerm outerq = new LinkQueryTerm();
			outerq.setNode(qt);
			return translateQueryForLinkStatement(outerq, targetLinkAlias);
		}

		// translate the link query to an SQL term. This SQL term will lack
		// select clauses
		String linkTableAlias = translateQuery(qt, rq, null);
		SelectClause selectClause = rq.getSelectClause();
		selectClause.setDistinct(true);
		selectClause.addColumn(linkTableAlias + ".*");
		WhereClause wc = rq.getWhereClause();

		// The basic query translation leaves us surrogate keys hanging off
		// link.
		// we can join using an extra node table alias;
		// however, the appropriate table may already have been joined.

		String subjectJoinCol = tblCol(linkTableAlias, NODE_INTERNAL_ID_COLUMN);
		String subjectNodeTable = rq.getTableAliasReferencedInJoin(
				subjectJoinCol, NODE_TABLE);
		if (subjectNodeTable == null) {
			// add a new alias
			rq.addTable(NODE_TABLE, SUBJECT_NODE_ALIAS);
			wc.addJoinConstraint(tblCol(SUBJECT_NODE_ALIAS,
					NODE_INTERNAL_ID_COLUMN), subjectJoinCol);
			selectClause.addColumn(tblCol(SUBJECT_NODE_ALIAS,
					NODE_EXPOSED_ID_COLUMN), LINK_NODE_EXPOSED_ID_COLUMN);
		} else {
			// we have already joined the subject node table. Use the alias
			// given
			selectClause.addColumn(tblCol(subjectNodeTable,
					NODE_EXPOSED_ID_COLUMN), LINK_NODE_EXPOSED_ID_COLUMN);

		}
		String relationJoinCol = tblCol(linkTableAlias, LINK_RELATION_INTERNAL_ID_COLUMN);
		String relationNodeTable = rq.getTableAliasReferencedInJoin(
				relationJoinCol, NODE_TABLE);
		if (relationNodeTable == null) {
			// add a new alias
			rq.addTable(NODE_TABLE, RELATION_NODE_ALIAS);
			wc.addJoinConstraint(tblCol(RELATION_NODE_ALIAS,
					NODE_INTERNAL_ID_COLUMN), relationJoinCol);
			selectClause.addColumn(tblCol(RELATION_NODE_ALIAS,
					NODE_EXPOSED_ID_COLUMN), LINK_RELATION_EXPOSED_ID_COLUMN);
		} else {
			// we have already joined the relation node table. Use the alias
			// given
			selectClause.addColumn(tblCol(relationNodeTable,
					NODE_EXPOSED_ID_COLUMN), LINK_RELATION_EXPOSED_ID_COLUMN);

		}

		if (targetLinkAlias == null) {
			targetLinkAlias = linkTableAlias;
		}

		String targetJoinCol = tblCol(targetLinkAlias, LINK_TARGET_INTERNAL_ID_COLUMN);
		String targetNodeTable = rq.getTableAliasReferencedInJoin(
				targetJoinCol, NODE_TABLE);
		if (targetNodeTable == null) {
			// add a new alias
			rq.addTable(NODE_TABLE, TARGET_NODE_ALIAS);
			wc.addJoinConstraint(tblCol(TARGET_NODE_ALIAS,
					NODE_INTERNAL_ID_COLUMN), targetJoinCol);
			selectClause.addColumn(tblCol(TARGET_NODE_ALIAS,
					NODE_EXPOSED_ID_COLUMN), LINK_TARGET_EXPOSED_ID_COLUMN);
		} else {
			// we have already joined the target node table. Use the alias given
			selectClause.addColumn(tblCol(targetNodeTable,
					NODE_EXPOSED_ID_COLUMN), LINK_TARGET_EXPOSED_ID_COLUMN);
		}

		selectClause.addColumn(tblCol(linkTableAlias,LINK_REIF_INTERNAL_ID_COLUMN), LINK_REIF_INTERNAL_ID_COLUMN);
		// TODO: do this for all link queries
		if (qt instanceof AnnotationLinkQueryTerm) {
			rq.addTable(NODE_TABLE, REIF_NODE_ALIAS);
			wc.addJoinConstraint(tblCol(REIF_NODE_ALIAS, NODE_INTERNAL_ID_COLUMN),
					tblCol(linkTableAlias,LINK_REIF_INTERNAL_ID_COLUMN));
			selectClause.addColumn(tblCol(REIF_NODE_ALIAS, NODE_EXPOSED_ID_COLUMN), LINK_REIF_EXPOSED_ID_COLUMN);
		} else {
			selectClause.addColumn("NULL", LINK_REIF_EXPOSED_ID_COLUMN);
		}
		// rq.addTable(NODE_TABLE);
		return rq;
	}

	public RelationalQuery translateQueryForLiteral(QueryTerm qt) {
		return translateQueryForLiteral(qt, null);
	}
	public RelationalQuery translateQueryForLiteral(QueryTerm qt,
			String targetLiteralAlias) {
		RelationalQuery rq = new SqlQueryImpl();

		if (!(qt instanceof LiteralQueryTerm)) {
			// it only makes sense to return links for link queries;
			// a query to for example LiteralQuery("apoptosis") should
			// be wrapped such that links with this node are returned
			LiteralQueryTerm outerq = new LiteralQueryTerm();
			outerq.setNode(qt);
			return translateQueryForLiteral(outerq, targetLiteralAlias);
		}

		// translate the link query to an SQL term. This SQL term will lack
		// select clauses
		String linkTableAlias = translateQuery(qt, rq, null);
		SelectClause selectClause = rq.getSelectClause();
		selectClause.setDistinct(true);
		selectClause.addColumn(linkTableAlias + ".*");
		WhereClause wc = rq.getWhereClause();

		// The basic query translation leaves us surrogate keys hanging off
		// link.
		// we can join using an extra node table alias;
		// however, the appropriate table may already have been joined.

		String subjectJoinCol = tblCol(linkTableAlias, NODE_INTERNAL_ID_COLUMN);
		String subjectNodeTable = rq.getTableAliasReferencedInJoin(
				subjectJoinCol, NODE_TABLE);
		if (subjectNodeTable == null) {
			// add a new alias
			rq.addTable(NODE_TABLE, SUBJECT_NODE_ALIAS);
			wc.addJoinConstraint(tblCol(SUBJECT_NODE_ALIAS,
					NODE_INTERNAL_ID_COLUMN), subjectJoinCol);
			selectClause.addColumn(tblCol(SUBJECT_NODE_ALIAS,
					NODE_EXPOSED_ID_COLUMN), LINK_NODE_EXPOSED_ID_COLUMN);
		} else {
			// we have already joined the subject node table. Use the alias
			// given
			selectClause.addColumn(tblCol(subjectNodeTable,
					NODE_EXPOSED_ID_COLUMN), LINK_NODE_EXPOSED_ID_COLUMN);

		}
		String relationJoinCol = tblCol(linkTableAlias, LINK_RELATION_INTERNAL_ID_COLUMN);
		String relationNodeTable = rq.getTableAliasReferencedInJoin(
				relationJoinCol, NODE_TABLE);
		if (relationNodeTable == null) {
			// add a new alias
			rq.addTable(NODE_TABLE, RELATION_NODE_ALIAS);
			wc.addJoinConstraint(tblCol(RELATION_NODE_ALIAS,
					NODE_INTERNAL_ID_COLUMN), relationJoinCol);
			selectClause.addColumn(tblCol(RELATION_NODE_ALIAS,
					NODE_EXPOSED_ID_COLUMN), LINK_RELATION_EXPOSED_ID_COLUMN);
		} else {
			// we have already joined the relation node table. Use the alias
			// given
			selectClause.addColumn(tblCol(relationNodeTable,
					NODE_EXPOSED_ID_COLUMN), LINK_RELATION_EXPOSED_ID_COLUMN);

		}

		return rq;
	}

	/**
	 * @param qt
	 *            - OBD Query to be translated
	 * @param rq
	 *            - constructed Relation SQL Query
	 * @param subjCol
	 *            - column to use in where constraint. Can be null
	 * @return tableName
	 * 
	 *         Recursively translates a query or part of a query to SQL.
	 * 
	 *         Examples:
	 * 
	 *         Link(parent) => link(X,R,Y) where Y=parent Link(rel,parent) =>
	 *         link(X,R,Y) where Y=parent, R=rel Link(Link(gparent)) =>
	 *         link(X,R1,Z),link(Z,R2,Y) where Y=gparent Link(node=child) =>
	 *         link(X,R,Y) where X=child
	 * 
	 *         Link(Label(ID,Equals(parent))) => link(X,R,Y) where Y=parent
	 *         Link(Label(LABEL,Equals(pl))) => link(X,R,Y),label(Y,L) where
	 *         L=pl Link(Label(SYNONYM,StartsWith(match))) =>
	 *         link(X,R,Y),synonym(Y,L) where L like match%
	 * 
	 *         As this method recurses into the QueryTerm tree, a forward record
	 *         is kept of what column should be used in the match/join
	 *         (subjCol). For example, in Link(Link(gp)) we start off with the
	 *         outer Link, generate a FROM clause involving the link table:
	 *         link(X,R1,Y) X is used in the SELECT. When we traverse to the
	 *         inner link, passing Y as the value for subjCol. we generate a new
	 *         link in the FROM clause link(A,R2,B) And then use the subjCol to
	 *         join on Y(subjCol)=A We can simplify the relational notation by
	 *         unifying variable and writing: link(X,R1,Y),link(Y,R2,B)
	 * 
	 *         The same principle applies to other QueryTerm clauses; eg
	 *         Link(Label(StartsWith(LABEL,label)))
	 * 
	 */
	public String translateQuery(QueryTerm qt, RelationalQuery rq,
			String subjCol) {
		WhereClause wc = rq.getWhereClause();

		String aspectCol = null;
		if (subjCol != null && qt != null) {
			aspectCol = NODE_INTERNAL_ID_COLUMN;
			Aspect aspect = qt.getAspect();
			if (aspect.equals(Aspect.TARGET))
				aspectCol = LINK_TARGET_INTERNAL_ID_COLUMN;
			else if (aspect.equals(Aspect.RELATION))
				aspectCol = LINK_RELATION_INTERNAL_ID_COLUMN;
			else if (aspect.equals(Aspect.SOURCE))
				aspectCol = LINK_SOURCE_INTERNAL_ID_COLUMN;
			else if (aspect.equals(Aspect.POSITED_BY))
				aspectCol = LINK_REIF_INTERNAL_ID_COLUMN;
		}

		if (qt instanceof AtomicQueryTerm) { // DEPRECATED: Atomicity is tested
			// in container
			String val = ((AtomicQueryTerm) qt).getSValue();
			if (subjCol == null) {
			} else {
				Class<?> dt = ((AtomicQueryTerm) qt).getDatatype();
				if (dt.equals(Integer.class)) {
					// TODO: this is a temp hack for dealing with integer IDs
					wc.addConstraint("subjCol=" + val);
				} else {
					setNodeIdEqualityClause(wc, subjCol, val);
				}
				// wc.addEqualityConstraint(subjCol,val);
			}
			// note: this is the only time we return a value and not a table
			// should use generics here?
			return val;
		} else if (qt instanceof ExistentialQueryTerm) {
			if (subjCol == null) {
			} else {
				wc.addConstraint(subjCol + " IS NOT NULL");
			}
			return subjCol;
		} else if (qt instanceof NodeSetQueryTerm) {
			NodeSetQueryTerm cqt = (NodeSetQueryTerm) qt;
			SqlQueryImpl subRq = new SqlQueryImpl();
			subRq.addTable(NODE_TABLE);
			String inCol = NODE_EXPOSED_ID_COLUMN;
			if (cqt.isInternalIdentifiers())
				inCol = NODE_INTERNAL_ID_COLUMN;
			subRq.getWhereClause().addInConstraint(inCol, cqt.getNodeIds());
			subRq.setSelectClause(NODE_INTERNAL_ID_COLUMN);
			wc.addInConstraint(subjCol, subRq);
			return subjCol;
		} else if (qt instanceof BooleanQueryTerm) {
			BooleanQueryTerm cqt = (BooleanQueryTerm) qt;
			WhereClause booleanWhereClause = wc;
			String rel = "";
			// if (subjCol == null) {
			// rq.addTable(NODE_TABLE);
			// rel = NODE_TABLE;
			// subjCol = "node.node_id";
			// }

			// constraints are conjunctive by default.
			// if a disjunctive query is required we add a sub-clause
			if (cqt.getOperator().equals(BooleanOperator.OR)) {
				booleanWhereClause = new SqlWhereClauseImpl();
			}
			for (QueryTerm subquery : cqt.getQueryTerms()) {
				RelationalQuery subrq = new SqlQueryImpl();
				// translateQuery(subquery, subrq, "link.node_id");
				String subTbl = translateQuery(subquery, subrq, null);
				subrq.setSelectClause(subTbl + ".node_id");
				booleanWhereClause.addInConstraint(subjCol, subrq);
			}

			// make sure placeholders go in the right order..
			if (cqt.getOperator().equals(BooleanOperator.OR)) {
				wc.addDisjunctiveConstraints(booleanWhereClause);
			}
			return rel;
		} else if (qt instanceof ComparisonQueryTerm) {
			ComparisonQueryTerm cqt = (ComparisonQueryTerm) qt;
			Operator op = cqt.getOperator();
			QueryTerm comparedToQt = cqt.getValue();
			if (comparedToQt instanceof AtomicQueryTerm) {
				AtomicQueryTerm atomQt = (AtomicQueryTerm) comparedToQt;
				Class datatypeClass = atomQt.getDatatype();
				Object atom = atomQt.getValue();

				if (subjCol != null) {
					if (op.equals(Operator.STARTS_WITH)) {
						wc.addOperatorConstraint("LIKE", subjCol, atom + "%");
					} else if (op.equals(Operator.CONTAINS)) {
						wc.addOperatorConstraint("LIKE", subjCol, "%" + atom
								+ "%");
					} else if (op.equals(Operator.MATCHES)) {
						wc.addOperatorConstraint("~", subjCol, atom);
					} else if (op.equals(Operator.EQUAL_TO)) {
						wc.addOperatorConstraint("=", subjCol, atom);
					} else if (op.equals(Operator.CONTAINS_ALL)) {
						wc.addContainsAllConstraint(subjCol, atom.toString());
					} else if (op.equals(Operator.CONTAINS_ANY)) {
						wc.addContainsAnyConstraint(subjCol, atom.toString());
					} else {
						wc.addOperatorConstraint(cqt.getOperator().toString(),
								subjCol, atom);
					}
				}
			} else {

			}
			return "";
		} else if (qt instanceof LiteralQueryTerm
				&& false
				&& ((LiteralQueryTerm) qt).isAlias()) { // OLD
			LiteralQueryTerm cqt = (LiteralQueryTerm) qt;
			String tbl = "node_literal";
			String tblAlias = rq.addAutoAliasedTable(tbl);
			if (subjCol != null)
				wc.addJoinConstraint(tblCol(tblAlias,LINK_NODE_INTERNAL_ID_COLUMN), subjCol);
			if (cqt.isInferred() != null)
				wc.addEqualityConstraint(tblAlias + ".is_inferred", cqt
						.isInferred());
			translateQuery(cqt.getValue(), rq, "lower(" + tblCol(tblAlias,"val")+")");
			translateQuery(cqt.getNode(), rq, tblCol(tblAlias, LINK_NODE_INTERNAL_ID_COLUMN));
			translateQuery(cqt.getRelation(), rq, tblCol(tblAlias, LINK_RELATION_INTERNAL_ID_COLUMN));
			// String valTbl = translateQuery(cqt.getValue()), rq,
			// tblAlias+".object_id");
			translateQuery(cqt.getPositedBy(), rq, tblCol(tblAlias, LINK_REIF_INTERNAL_ID_COLUMN));
			// returns the name of the link table alias used in this query
			return tblAlias;
		} else if (qt instanceof LabelQueryTerm) { // TODO: merge with Literal?
			// Example: Label(NAME,Comparison(CONTAINS,"x")) => label like
			// '%x%';
			// Example: Label(SYNONYM,Comparison(STARTS_WITH,"x")) => label =
			// 'x%';
			// Example: Label(ID,Comparison(EQUALS_TO,"x")) => uid = 'x';
			LabelQueryTerm cqt = (LabelQueryTerm) qt;
			AliasType alias = cqt.getAliasType();

			// Special case for restricting by the source of the node
			QueryTerm nsq = qt.getNodeSource();
			String rel = null;
			if (nsq != null) {
				// note: this may turn out to be wasteful if we are at the root
				// of the QueryTerm tree
				// and are querying for nodes
				String srel = rq.addAutoAliasedTable(NODE_TABLE, SOURCE_NODE_ALIAS);
				if (subjCol != null)
					wc.addJoinConstraint(tblCol(srel,aspectCol),subjCol);
				translateQuery(nsq, rq, tblCol(srel,NODE_SOURCE_INTERNAL_ID_COLUMN));
			}
			if (subjCol != null && subjCol.equals(tblCol("query_node",NODE_INTERNAL_ID_COLUMN)) 
					&& aspectCol.equals(NODE_INTERNAL_ID_COLUMN)) {
				// OPT: if this is the top query, no need to self-join
				// TODO: generalize
				rel = "query_node";
			}
			/*
			 * we may have to join an additional table, depending on what is
			 * being compared
			 */
			if (alias.equals(AliasType.ID)) {
				if (rel == null)
					rel = rq.addAutoAliasedTable(NODE_TABLE);
				translateQuery(cqt.getValue(), rq, tblCol(rel , NODE_EXPOSED_ID_COLUMN));
				translateQuery(qt.getNodeSource(), rq, tblCol(rel , NODE_SOURCE_INTERNAL_ID_COLUMN));
				if (subjCol != null)
					wc.addJoinConstraint(tblCol(rel,aspectCol),subjCol);
				return rel;
			} else if (alias.equals(AliasType.INTERNAL_ID)) {
				if (rel == null)
					rel = rq.addAutoAliasedTable(NODE_TABLE); // required? just use
				// subjCol?
				translateQuery(cqt.getValue(), rq, rel + ".node_id");
				if (subjCol != null)
					wc.addConstraint(rel + "." + aspectCol + " = " + subjCol);
				return rel;
			} else if (alias.equals(AliasType.ANY_LABEL)) {
				String tbl = "node_label"; // TODO: table alias
				// TODO: node.label
				rel = rq.addAutoAliasedTable(tbl);
				if (subjCol != null)
					wc.addJoinConstraint(tblCol(rel , LINK_NODE_INTERNAL_ID_COLUMN), subjCol);
				// e.g. Contains("x") => tbl.label like 'x%'
				translateQuery(cqt.getValue(), rq, rel + ".label");
				return rel;
			} else if (alias.equals(AliasType.ANY_LITERAL)) {
				// note: so far this does NOT search labels!
				String tbl = "node_literal"; // TODO: table alias
				// TODO: node.label
				rel = rq.addAutoAliasedTable(tbl);
				if (subjCol != null)
					wc.addJoinConstraint(tblCol(rel , LINK_NODE_INTERNAL_ID_COLUMN), subjCol);
				// e.g. Contains("x") => tbl.label like 'x%'
				translateQuery(cqt.getValue(), rq, rel + ".val");
				return rel;
			} else if (alias.equals(AliasType.ALTERNATE_LABEL)) {
				String tbl = "alias"; // TODO: table alias
				// TODO: node.label
				rel = rq.addAutoAliasedTable(tbl);
				if (subjCol != null)
					wc.addJoinConstraint(tblCol(rel , LINK_NODE_INTERNAL_ID_COLUMN), subjCol);
				// e.g. Contains("x") => tbl.label like 'x%'
				translateQuery(cqt.getValue(), rq, rel + ".label");
				return rel;
			} else { // primarily PRIMARY_NAME - this is the default
				if (rel == null)
					rel = rq.addAutoAliasedTable(NODE_TABLE);
				translateQuery(cqt.getValue(), rq, rel + ".label");
				if (subjCol != null)
					wc.addJoinConstraint(tblCol(rel , LINK_NODE_INTERNAL_ID_COLUMN), subjCol);
				return rel;
			}
		} else if (qt instanceof AnnotationLinkQueryTerm) {
			AnnotationLinkQueryTerm origAnnotQt = (AnnotationLinkQueryTerm) qt;

			/*
			 * We want to return annotations of R(X,Y) if R'(Y,Z), where Z is
			 * the class of interest and X is the annotated entity. R(X,Y) is
			 * asserted. We do this with a nested link query newqt =
			 * Link(inf=f,positedBy=NOT_NULL,target=Link(target=COI))
			 */
			LinkQueryTerm ontolQt = new LinkQueryTerm(origAnnotQt.getTarget());
			LinkQueryTerm transformedAnnotQt = new LinkQueryTerm(ontolQt);

			transformedAnnotQt.setSource(origAnnotQt.getSource());
			transformedAnnotQt.setNode(origAnnotQt.getNode());
			transformedAnnotQt.setRelation(origAnnotQt.getRelation()); // R
			ontolQt.setRelation(origAnnotQt.getOntologyRelation()); // R'

			ontolQt.setQueryAlias(IMPLIED_ANNOTATION_LINK_ALIAS);
			transformedAnnotQt.setQueryAlias(origAnnotQt.getQueryAlias());
			transformedAnnotQt.setPositedBy(new ExistentialQueryTerm());
			// transformedAnnotQt.setInferred(false);
			Logger.getLogger("org.obd").fine(
					"annotq=" + transformedAnnotQt.toString());
			return translateQuery(transformedAnnotQt, rq, subjCol);
		} else if (qt instanceof CoAnnotatedQueryTerm) {
			CoAnnotatedQueryTerm cqt = (CoAnnotatedQueryTerm) qt;
			// TODO: allow inner link query to have a relation
			String tbl = "co_annotated_to_pair";
			if (cqt.isInferred() != null && !cqt.isInferred())
				tbl = "co_annotated_to_pair_asserted";
			String tblAlias = rq.addAutoAliasedTable(tbl);
			rq.getSelectClause().addColumn(tblAlias + ".node_count");
			if (subjCol != null)
				wc.addConstraint(tblAlias + ".node_id = " + subjCol);
			String nodeTbl = translateQuery(cqt.getNode(), rq, tblAlias
					+ ".object1_id");
			String targetTbl = translateQuery(cqt.getTarget(), rq, tblAlias
					+ ".object2_id");
			// returns the name of the link table alias used in this query
			return tblAlias;
		} else if (qt instanceof LinkQueryTerm) {

			// Example: LQ(partOf,x) => link * node[r]{po} * node[p]{x}
			LinkQueryTerm cqt = (LinkQueryTerm) qt;
			String tbl = LINK_TABLE;
			String tblAlias = rq.addAutoAliasedTable(tbl, qt.getQueryAlias());
			if (subjCol != null) {
				wc.addConstraint(tblAlias + "." + aspectCol + " = " + subjCol);
			}

			if (cqt.isInferred() != null)
				wc.addEqualityConstraint(tblAlias + ".is_inferred", cqt
						.isInferred());
			if (cqt.isDescriptionLink())
				wc.addConstraint(tblAlias + ".combinator != ''");
			translateQuery(cqt.getNode(), rq, tblCol(tblAlias, LINK_NODE_INTERNAL_ID_COLUMN));
			translateQuery(cqt.getRelation(), rq, tblCol(tblAlias, LINK_RELATION_INTERNAL_ID_COLUMN));
			translateQuery(cqt.getTarget(), rq, tblCol(tblAlias, LINK_TARGET_INTERNAL_ID_COLUMN));
			translateQuery(cqt.getSource(), rq, tblCol(tblAlias, LINK_SOURCE_INTERNAL_ID_COLUMN));
			translateQuery(cqt.getPositedBy(), rq, tblCol(tblAlias, LINK_REIF_INTERNAL_ID_COLUMN));
			// returns the name of the link table alias used in this query
			if (qt.getIsAnnotation() != null) {
				String isNullConstr = qt.getIsAnnotation() ? " IS NOT NULL"
						: " IS NULL";
				wc.addConstraint(tblCol(tblAlias, LINK_REIF_INTERNAL_ID_COLUMN)
						+ isNullConstr);
			}
			return tblAlias;
		} else if (qt instanceof LiteralQueryTerm) {

			// Example: LitQ(alias,x) => node_literal * node[r]{alias} 
			LiteralQueryTerm cqt = (LiteralQueryTerm) qt;
			String tbl = LITERAL_TABLE;
			String tblAlias = rq.addAutoAliasedTable(tbl, qt.getQueryAlias());
			if (subjCol != null) {
				wc.addConstraint(tblAlias + "." + aspectCol + " = " + subjCol);
			}

			translateQuery(cqt.getNode(), rq, tblCol(tblAlias, LINK_NODE_INTERNAL_ID_COLUMN));
			translateQuery(cqt.getRelation(), rq, tblCol(tblAlias, LINK_RELATION_INTERNAL_ID_COLUMN));
			translateQuery(cqt.getValue(), rq, tblCol(tblAlias, "val")); 
			translateQuery(cqt.getSource(), rq, tblCol(tblAlias, LINK_SOURCE_INTERNAL_ID_COLUMN));
			return tblAlias;
		} else if (qt instanceof RootQueryTerm) { // TODO: DRY
			RootQueryTerm rqt = (RootQueryTerm) qt;
			SourceQueryTerm sq = new SourceQueryTerm(rqt.getRootSource());
			String tbl = translateQuery(sq, rq, subjCol);
			if (subjCol != null) {
				SqlQueryImpl subrq = new SqlQueryImpl();
				subrq.addTable(LINK_TABLE);
				subrq.getWhereClause().addConstraint("is_inferred='f'");
				subrq.setSelectClause(NODE_INTERNAL_ID_COLUMN);
				String relTbl = translateQuery(rqt.getRelation(), subrq,
				"link.predicate_id");
				wc.addNotInConstraint(subjCol, subrq);
				// String tblAlias = rq.addAutoAliasedTable(NODE_TABLE,
				// "source_node");
				// translateQuery(sq,rq,tblAlias+".node_id");
			}
			return tbl;
		}
		/*
		 * else if (qt instanceof RootQueryTerm) { // TODO: DRY RootQueryTerm
		 * rqt = (RootQueryTerm)qt; String tbl = "graph_root_id_by_relation";
		 * String tblAlias = rq.addAutoAliasedTable(tbl); if (subjCol != null)
		 * wc.addConstraint(tblAlias+".node_id = " + subjCol); String relTbl =
		 * translateQuery(rqt.getRelation(), rq, tblAlias+".predicate_id");
		 * String rootSrcTbl = translateQuery(rqt.getRootSource(), rq,
		 * tblAlias+".node_id"); return tblAlias; }
		 */
		else if (qt instanceof SourceQueryTerm) { // Source Query
			SourceQueryTerm cqt = (SourceQueryTerm) qt;

			String tblAlias = rq.addAutoAliasedTable(NODE_TABLE, "source_node");
			if (subjCol != null)
				wc.addJoinConstraint(tblCol(tblAlias, NODE_INTERNAL_ID_COLUMN),subjCol);

			String nodeTbl = translateQuery(cqt.getNode(), rq, tblAlias
					+ ".node_id");
			String targetTbl = translateQuery(cqt.getTarget(), rq, tblAlias
					+ ".source_id");
			return tblAlias;
		} else if (qt instanceof CompositionalDescriptionQueryTerm) { // Source
			// Query
			CompositionalDescriptionQueryTerm cqt = (CompositionalDescriptionQueryTerm) qt;
			try {
				return translateQuery(cqt.translateToQueryTerm(), rq, subjCol);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else
			return null; // TODO _ throw
	}


	public int getAnnotatedNodeCount() {
		RelationalQuery q = new SqlQueryImpl();
		q.addTable("annotated_entity");
		q.setSelectClause("count(node_id) AS c");
		ResultSet rs;
		int n = 0;
		try {
			rs = execute(q);
			if (rs.next()) {
				n = rs.getInt("c");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n;
	}

	/*
	 * DATA MANIPULATION 
	 */

	public void removeMatchingStatements(String su, String rel, String ob)
	throws ShardExecutionException {
		System.err.println("OBDSQL shard; removing " + su + " " + rel + " "
				+ ob);
		int sui = this.getNodeInternalId(su);
		int reli = this.getNodeInternalId(rel);
		int obi = this.getNodeInternalId(ob);

		String sql = "DELETE FROM link WHERE node_id = " + sui
		+ " AND predicate_id = " + reli + " AND object_id = " + obi;
		System.err.println("SQL: " + sql);
		logger.fine("sql=" + sql);
		try {
			obd.getConnection().prepareStatement(sql).execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ShardExecutionException(sql);
		}
	}

	public void removeSource(String src) {
		Integer srcId = this.getNodeInternalId(src);
		String[] tbls = {LINK_TABLE,NODE_TABLE};

		// expensive operation, cannot generally do in a transaction
		for (String tbl : tbls) {
			RelationalQuery q = new SqlQueryImpl();
			q.addTable(tbl);
			String col = tbl+"_id";
			q.setSelectClause(col);
			q.getWhereClause().addEqualityConstraint("source_id", srcId);
			ResultSet rs;
			int iid = 0;
			try {
				String sql = "DELETE FROM "+tbl+" WHERE " + col +" = ?";
				PreparedStatement ps = obd.getConnection().prepareStatement(sql);
				rs = execute(q);
				while (rs.next()) {
					iid = rs.getInt(col);
					ps.setInt(1, iid);
					ps.execute();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void removeNode(String nid) throws ShardExecutionException {
		try {
			String sql = "DELETE FROM node WHERE uid = ?";
			PreparedStatement ps = obd.getConnection().prepareStatement(sql);
			ps.setString(1, nid);
			ps.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ShardExecutionException("Error deleting " + nid);
		}
	}

	public void putStatement(Statement s) {

		String srcId = s.getSourceId();
		if (srcId == null)
			srcId = "";
		try {
			String comb = "";
			if (s.isIntersectionSemantics()) {
				comb = "I";
			} else if (s.isUnionSemantics()) {
				comb = "U";
			}
			if (s instanceof LinkStatement) {
				LinkStatement ls = (LinkStatement) s;
				int reiflinkNodeInternalId;
				if (s.getSubStatements().size() > 0) {
					reiflinkNodeInternalId = (Integer) callSqlFunc("store_annotation", s
							.getNodeId(), s.getRelationId(), s.getTargetId(),
							srcId, s.isNegated());
					for (Statement ss : s.getSubStatements()) {
						if (ss instanceof LiteralStatement) {
							String dt = ((LiteralStatement) ss).getDatatype();
							if (dt == null)
								dt = "xsd:string";
							callSqlFunc("store_tagval_i",
									reiflinkNodeInternalId, ss.getRelationId(),
									((LiteralStatement) ss).getValue(), dt,
									srcId);
						} else {
							callSqlFunc("store_link_si",
									reiflinkNodeInternalId, ss.getRelationId(),
									ss.getTargetId(), comb, false, srcId);
						}
					}
				} else {
					reiflinkNodeInternalId = (Integer)callSqlFunc("store_link", s
							.getNodeId(), s.getRelationId(), s.getTargetId(),
							comb, false, srcId);
				}
			} else if (s instanceof NodeAlias) {
				NodeAlias a = (NodeAlias) s;
				int iid =  (Integer)callSqlFunc("store_node", s.getNodeId());
				callSqlFunc("store_node_synonym_i", iid, a.getScope(), null, a
						.getValue());
			} else if (s instanceof LiteralStatement) {
				callSqlFunc("store_tagval", s.getNodeId(), s.getRelationId(),
						((LiteralStatement) s).getValue(), srcId);
			} else {

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void putNode(Node n) {
		try {
			callSqlFunc("store_node", n.getId(), n.getLabel(), n.getSourceId(),
			"I");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Statement s : n.getStatements()) {
			putStatement(s);
		}
	}

	public void realizeRule(InferenceRule rule) {
		if (rule instanceof RelationCompositionRule) {
			RelationCompositionRule rcr = (RelationCompositionRule) rule;
			realizeLinkChain(rcr.getImpliedRelation(), rcr.getLeftRelation(),
					rcr.getRightRelation(), rcr.isLeftInverted(), rcr
					.isRightInverted());
		} else {
			super.realizeRule(rule);
		}
	}

	/**
	 * realize the rule: newRel < rel1 o rel2
	 * 
	 * directionality of sub-rule can be inverted
	 * 
	 * e.g. influences < inv(variant_of) o influences G influences P IF:- 
	 * A variant_of G, A influences P
	 * 
	 * @param newRel
	 * @param rel1
	 * @param rel2
	 * @param isRel1Inverted
	 * @param isRel2Inverted
	 */
	public void realizeLinkChain(String newRel, String rel1, String rel2,
			boolean isRel1Inverted, boolean isRel2Inverted) {
		int rel1iid = getNodeInternalId(rel1);
		int rel2iid = getNodeInternalId(rel2);
		int newReliid = getNodeInternalId(newRel);
		RelationalQuery rq = new SqlQueryImpl();
		rq.addTable(LINK_TABLE, "link1");
		rq.addTable(LINK_TABLE, "link2");
		SelectClause sc = rq.getSelectClause();
		String j1;
		String j2;
		String c1;
		String c2;

		if (isRel1Inverted) {
			sc.addColumn("link1.object_id AS node_id");
			j1 = "link1.node_id";
			c1 = "link1.object_id";
		} else {
			sc.addColumn("link1.node_id AS node_id");
			j1 = "link1.object_id";
			c1 = "link1.node_id";
		}
		if (isRel2Inverted) {
			sc.addColumn("link2.node_id AS object_id");
			j2 = "link2.object_id";
			c2 = "link2.node_id";
		} else {
			sc.addColumn("link2.object_id AS object_id");
			j2 = "link2.node_id";
			c2 = "link2.object_id";
		}
		sc.addColumn("link1.reiflink_node_id AS reiflink1_node_id");
		sc.addColumn("link2.reiflink_node_id AS reiflink2_node_id");
		sc.addColumn("link2.source_id AS source_id"); // link2 is always the
		// source

		WhereClause wc = rq.getWhereClause();
		wc.addJoinConstraint(j1, j2);
		wc.addEqualityConstraint("link1.predicate_id", rel1iid);
		wc.addEqualityConstraint("link2.predicate_id", rel2iid);

		// no dupes
		wc.addConstraint("NOT EXISTS (SELECT * FROM link WHERE link.node_id="+c1+" AND link.predicate_id="+
				newReliid+" AND link.object_id="+c2+")");


		try {
			Connection conn = obd.getConnection();
			ResultSet rs = execute(rq);
			// TODO - link provenance
			String insertSql = "INSERT INTO link (node_id,predicate_id,object_id,reiflink_node_id,source_id,is_inferred) VALUES (?,?,?,?,?,'t')";
			PreparedStatement sqlStmt = conn.prepareStatement(insertSql);
			while (rs.next()) {
				int nid = rs.getInt(NODE_INTERNAL_ID_COLUMN);
				int tid = rs.getInt(LINK_TARGET_INTERNAL_ID_COLUMN);
				Integer sourceId = rs.getInt(LINK_SOURCE_INTERNAL_ID_COLUMN);
				Integer reiflink1NodeId = rs.getInt("reiflink1_node_id");
				Integer reiflink2NodeId = rs.getInt("reiflink2_node_id");
				// if either is an annotation, carry annotation metadata forward
				Integer reiflinkNodeId = (reiflink2NodeId == null) ? reiflink1NodeId
						: reiflink2NodeId;
				// System.out.println(nid+" "+tid+" :: "+reiflinkNodeId+" "+
				// reiflink1NodeId+" "+reiflink2NodeId);
				sqlStmt.setInt(1, nid);
				sqlStmt.setInt(2, newReliid);
				sqlStmt.setInt(3, tid);
				if (reiflinkNodeId != null && reiflinkNodeId != 0)
					sqlStmt.setInt(4, reiflinkNodeId);
				if (sourceId != null && sourceId != 0)
					sqlStmt.setInt(5, sourceId);
				sqlStmt.execute();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void mergeIdentifierByIDSpaces(String fromIdSpace, String toIdSpace)
	throws ShardExecutionException {
		LinkQueryTerm lq = new LinkQueryTerm();
		lq.setNode(new LabelQueryTerm(AliasType.ID, fromIdSpace + ":",
				Operator.STARTS_WITH)); // eg NCBI_Gene
		lq.setTarget(new LabelQueryTerm(AliasType.ID, toIdSpace + ":",
				Operator.STARTS_WITH)); // eg ZFIN
		lq.setRelation(tvocab.HAS_DBXREF());
		lq.setQueryAlias(LINK_TABLE);
		RelationalQuery rq = this.translateQueryForLinkStatement(lq);
		rq.setSelectClause("link.node_id,link.object_id");
		ResultSet rs;
		try {
			rs = this.execute(rq);

			Connection conn = obd.getConnection();
			PreparedStatement updateFromLinkSqlStmt = conn
			.prepareStatement("UPDATE link SET node_id=? WHERE node_id=?");
			PreparedStatement updateToLinkSqlStmt = conn
			.prepareStatement("UPDATE link SET object_id=? WHERE object_id=?");
			PreparedStatement updateAliasSqlStmt = conn
			.prepareStatement("UPDATE alias SET node_id=? WHERE node_id=?");
			PreparedStatement updateDescriptionSqlStmt = conn
			.prepareStatement("UPDATE description SET node_id=? WHERE node_id=?");
			PreparedStatement updateTagValSqlStmt = conn
			.prepareStatement("UPDATE tagval SET node_id=? WHERE node_id=?");
			//			PreparedStatement updateNameSqlStmt = conn
			//			.prepareStatement("UPDATE node SET label=(SELECT label FROM node WHERE node_id=?) WHERE node_id=?");
			while (rs.next()) {
				int fromId = rs.getInt(NODE_INTERNAL_ID_COLUMN); // eg NCBI
				int toId = rs.getInt(LINK_TARGET_INTERNAL_ID_COLUMN); // eg ZFIN

				updateFromLinkSqlStmt.setInt(1, toId);
				updateFromLinkSqlStmt.setInt(2, fromId);
				updateFromLinkSqlStmt.execute();

				updateToLinkSqlStmt.setInt(1, toId);
				updateToLinkSqlStmt.setInt(2, fromId);
				updateToLinkSqlStmt.execute();

				updateAliasSqlStmt.setInt(1, toId);
				updateAliasSqlStmt.setInt(2, fromId);
				updateAliasSqlStmt.execute();

				updateDescriptionSqlStmt.setInt(1, toId);
				updateDescriptionSqlStmt.setInt(2, fromId);
				updateDescriptionSqlStmt.execute();

				updateTagValSqlStmt.setInt(1, toId);
				updateTagValSqlStmt.setInt(2, fromId);
				updateTagValSqlStmt.execute();

			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ShardExecutionException("Error executing sql");

		}

	}

	// can be overridden in adapters for other schemas
	public void setNodeIdEqualityClause(WhereClause whereClause, String id,
			boolean direct) {
		if (direct)
			whereClause.addEqualityConstraint(NODE_EXPOSED_ID_COLUMN, id);
		else
			whereClause.addEqualityConstraint(LINK_NODE_EXPOSED_ID_COLUMN, id);

	}

	// can be overridden in adapters for other schemas
	public void setNodeIdEqualityClause(WhereClause whereClause, String col,
			String id) {
		whereClause.addEqualityConstraint(col, id);
	}

	public void renameIdentifierSpace(String from, String to) {
		SqlQueryImpl rq = new SqlQueryImpl("node_id, uid", NODE_TABLE, "uid like '"
				+ from + "%'");
		Map<String, Integer> uid2iid = new HashMap<String, Integer>();
		try {
			ResultSet rs = this.execute(rq);
			while (rs.next()) {
				int iid = rs.getInt(NODE_INTERNAL_ID_COLUMN);
				String uid = rs.getString(NODE_EXPOSED_ID_COLUMN);
				uid2iid.put(uid, iid);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String testSql = "SELECT node_id FROM node WHERE  uid=?";
			PreparedStatement testPS = obd.getConnection().prepareStatement(testSql);
			String updateSql = "UPDATE node SET uid=? WHERE node_id=?";
			PreparedStatement updatePS = obd.getConnection().prepareStatement(updateSql);
			for (String uid : uid2iid.keySet()) {
				int iid = uid2iid.get(uid);
				String newUid = uid.replace(from, to);
				testPS.setString(1, newUid);
				ResultSet rs = testPS.executeQuery();
				if (rs.next()) {
					mapInternalIdentifier(iid,rs.getInt("node_id"));
				}
				else {
					System.err.println(uid+" -> "+newUid);
					updatePS.setString(1, newUid);
					updatePS.setInt(2, iid);
					updatePS.execute();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void mapInternalIdentifier(int iidFrom, int iidTo) throws SQLException {
		Node curNode = this.getNodeByInternalId(iidFrom);
		String[] updates =
		{
				"UPDATE node SET source_id=? WHERE source_id=?",
				"UPDATE link SET node_id=? WHERE node_id=?",
				"UPDATE link SET object_id=? WHERE object_id=?",
				"UPDATE link SET predicate_id=? WHERE predicate_id=?",
				"UPDATE link SET source_id=? WHERE source_id=?",
				"UPDATE link SET reiflink_node_id=? WHERE reiflink_node_id=?",
				"UPDATE sameas SET node_id=? WHERE node_id=?",
				"UPDATE sameas SET object_id=? WHERE object_id=?",
				"UPDATE sameas SET source_id=? WHERE source_id=?",
				"UPDATE tagval SET node_id=? WHERE node_id=?",
				"UPDATE tagval SET tag_id=? WHERE tag_id=?",
				"UPDATE tagval SET source_id=? WHERE source_id=?",
				"UPDATE alias SET node_id=? WHERE node_id=?",
				"UPDATE alias SET type_id=? WHERE type_id=?",
				"UPDATE alias SET source_id=? WHERE source_id=?",
				"UPDATE description SET node_id=? WHERE node_id=?",
				"UPDATE description SET type_id=? WHERE type_id=?",
				"UPDATE description	SET source_id=? WHERE source_id=?"
		};
		for (String updateSql : updates) {
			PreparedStatement ps = obd.getConnection().prepareStatement(updateSql);
			ps.setInt(1, iidTo);
			ps.setInt(2, iidFrom);
			ps.executeUpdate();
		}
		System.err.println(iidFrom+" merge-> "+iidTo);
		PreparedStatement ps;

		if (curNode.getLabel() != null) {
			ps = obd.getConnection().prepareStatement("UPDATE node SET label=?, metatype='"+curNode.getMetatype().name().substring(0,1)+
			"' WHERE node_id = ?");
			ps.setString(1, curNode.getLabel());
			ps.setInt(2, iidTo);
			ps.execute();
		}

		ps = obd.getConnection().prepareStatement("DELETE FROM node WHERE node_id = "+iidFrom);
		ps.execute();
	}

	public void switchRelationIdGlobally(String from, String to) {
		try {
			int fromId = getNodeInternalId(from);
			int toId = getNodeInternalId(to);

			String sql = "SELECT link_id FROM link WHERE predicate_id= "
				+ fromId;
			ResultSet rs = obd.getConnection().prepareStatement(sql)
			.executeQuery();
			Collection<Integer> ids = new HashSet<Integer>();
			while (rs.next()) {
				ids.add(rs.getInt("link_id"));
			}
			String usql = "UPDATE link SET predicate_id=? WHERE link_id=?";
			PreparedStatement ps = obd.getConnection().prepareStatement(usql);
			for (int link_id : ids) {
				ps.setInt(1, toId);
				ps.setInt(2, link_id);
				ps.execute();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void unsetLinkAppliesToAllForRelation(String rel) {
		try {

			String sql = "SELECT link_id FROM link INNER JOIN node AS rel ON (rel.node_id=link.predicate_id) WHERE applies_to_all='t' AND rel.uid LIKE '"
				+ rel + "'";
			ResultSet rs = obd.getConnection().prepareStatement(sql)
			.executeQuery();
			Collection<Integer> ids = new HashSet<Integer>();
			while (rs.next()) {
				ids.add(rs.getInt("link_id"));
			}
			String usql = "UPDATE link SET applies_to_all='f', object_quantifier_some='f' WHERE link_id=?";
			PreparedStatement ps = obd.getConnection().prepareStatement(usql);
			for (int link_id : ids) {
				ps.setInt(1, link_id);
				ps.execute();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void unsetLinkAppliesToAllForMetadataRelations() {
		try {

			String sql = "SELECT link_id FROM link INNER JOIN node AS rel ON (rel.node_id=link.predicate_id) WHERE applies_to_all='t' AND rel.is_metadata='t'";
			ResultSet rs = obd.getConnection().prepareStatement(sql)
			.executeQuery();
			Collection<Integer> ids = new HashSet<Integer>();
			while (rs.next()) {
				ids.add(rs.getInt("link_id"));
			}

			String usql = "UPDATE link SET applies_to_all='f', object_quantifier_some='f' WHERE link_id=?";
			PreparedStatement ps = obd.getConnection().prepareStatement(usql);
			for (int link_id : ids) {
				ps.setInt(1, link_id);
				ps.execute();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setSourceId(Node s, Integer iid) {
		String sourceId = null;
		if (iid == null)
			return;
		if (iid == 0)
			return;
		if (iid2nodeId.containsKey(iid)) {
			sourceId = iid2nodeId.get(iid);
		} else {
			// TODO: make this more general
			RelationalQuery rq = new SqlQueryImpl();
			rq.addTable(NODE_TABLE);
			rq.setSelectClause(NODE_EXPOSED_ID_COLUMN);
			rq.getWhereClause().addEqualityConstraint(NODE_INTERNAL_ID_COLUMN, iid);
			ResultSet rs;
			try {
				rs = execute(rq);
				if (rs.next()) {
					sourceId = rs.getString(NODE_EXPOSED_ID_COLUMN);
					iid2nodeId.put(iid, sourceId);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (sourceId != null)
			s.setSourceId(sourceId);
	}

	// from: http://www.lampos.net/?q=node/171
	public LinkedHashMap sortHashMapByValuesD(HashMap passedMap, boolean isReverse) {
		List mapKeys = new ArrayList(passedMap.keySet());
		List mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues);
		if (isReverse)
			Collections.reverse(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap sortedMap = 
			new LinkedHashMap();

		Iterator valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Object val = valueIt.next();
			Iterator keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				String comp1 = passedMap.get(key).toString();
				String comp2 = val.toString();

				if (comp1.equals(comp2)){
					passedMap.remove(key);
					mapKeys.remove(key);
					//sortedMap.put((String)key, (Double)val);
					sortedMap.put(key, val);
					break;
				}

			}

		}
		return sortedMap;
	}


}