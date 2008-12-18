package org.obd.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.bbop.rdbms.RelationalQuery;
import org.bbop.rdbms.SelectClause;
import org.bbop.rdbms.WhereClause;
import org.bbop.rdbms.impl.SqlQueryImpl;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.stats.AggregateStatisticCollection;
import org.obd.model.stats.AggregateStatistic.AggregateType;
import org.obd.query.LiteralQueryTerm;
import org.obd.query.QueryTerm;

public class OBDSimpleSQLShard extends OBDSQLShard {

	public OBDSimpleSQLShard() throws SQLException, ClassNotFoundException {
		super();
		NODE_TABLE = "nknode";
		LINK_TABLE = "nklink";
		LITERAL_TABLE = "nkliteral";
		NODE_INTERNAL_ID_COLUMN = "subj";
		NODE_EXPOSED_ID_COLUMN = "subj";
		NODE_SOURCE_EXPOSED_ID_COLUMN = "src";
		NODE_SOURCE_INTERNAL_ID_COLUMN = "src";
		
		LINK_NODE_EXPOSED_ID_COLUMN = "subj";
		LINK_TARGET_EXPOSED_ID_COLUMN = "targ";
		LINK_RELATION_EXPOSED_ID_COLUMN = "rel";
		LINK_SOURCE_EXPOSED_ID_COLUMN = "src";
		LINK_REIF_EXPOSED_ID_COLUMN = "reif";

		LINK_NODE_INTERNAL_ID_COLUMN = "subj";
		LINK_TARGET_INTERNAL_ID_COLUMN = "targ";
		LINK_RELATION_INTERNAL_ID_COLUMN = "rel";
		LINK_SOURCE_INTERNAL_ID_COLUMN = "src";
		LINK_REIF_INTERNAL_ID_COLUMN = "reif";

		IMPLIED_ANNOTATION_LINK_ALIAS = "implied_annotation_link";
	}

	public boolean isUsesSurrogateKeys() {
		return false;
	}

	

	@Override
	public Node createNodeFromResultSet(ResultSet rs) throws SQLException {
		Node n = new Node(rs.getString(NODE_EXPOSED_ID_COLUMN ));
		n.setMetatype(rs.getString("metatype"));
		n.setLabel(rs.getString("label"));
		n.setSourceId(rs.getString(NODE_SOURCE_EXPOSED_ID_COLUMN));
		return n;
	}
	public LiteralStatement createLiteralStatementFromResultSet(String tbl, ResultSet rs)
	throws SQLException {

		LiteralStatement s = new LiteralStatement();
		s.setNodeId(rs.getString(LINK_NODE_EXPOSED_ID_COLUMN));
		s.setRelationId(rs.getString(LINK_RELATION_EXPOSED_ID_COLUMN));
		s.setValue(rs.getString(LITERAL_VALUE_COLUMN));
		// TODO setSourceId(s,rs.getInt(LINK_SOURCE_INTERNAL_ID_COLUMN));
		return s;
	}


	/*
	@Override
	public Collection<LiteralStatement> getLiteralStatementsByQuery(
			QueryTerm queryTerm) {
		return getLiteralStatementsByQuery(queryTerm, "nkliteral");
	}

	@Override // don't need additional joins
	protected RelationalQuery translateQueryForLiteral(QueryTerm qt, String tbl,
			boolean joinRelationNode) {
		RelationalQuery rq = new SqlQueryImpl();
		WhereClause wc = rq.getWhereClause();

		rq.addTable(tbl);
		translateQuery(qt, rq, tblCol(tbl, LINK_NODE_INTERNAL_ID_COLUMN));

		SelectClause selectClause = rq.getSelectClause();
		selectClause.setDistinct(true);
		selectClause.addColumn(tbl + ".*");
		return rq;

	}
*/

}
