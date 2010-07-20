package org.obd.query.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang.ObjectUtils;
import org.bbop.rdbms.RelationalQuery;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.query.LabelQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obo.dataadapter.OBDSQLDatabaseAdapter;
import org.obo.dataadapter.OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration;
import org.obo.datamodel.IdentifiedObject;

/**
 * Base class for Shard implementations that use a JDBC connection
 * 
 * @author cjm
 *
 */
public abstract class AbstractSQLShard extends AbstractShard implements Shard {
	
	Logger logger = Logger.getLogger("org.obd.shard.AsbtactSQLShard");
	protected OBDSQLDatabaseAdapter obd = new OBDSQLDatabaseAdapter();
	protected OBDSQLDatabaseAdapterConfiguration obdconfig = new OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration();
	
	public void connect(String jdbcPath) throws SQLException, ClassNotFoundException {
		connect(jdbcPath, null, null); // TODO - pooling - no hardcoding
		// usernames!!!
	}

	public void connect(String jdbcPath, String username, String password) throws SQLException, ClassNotFoundException {
		obdconfig.setReadPath(jdbcPath);
		obdconfig.setDbUsername(username);
		obdconfig.setDbPassword(password);
		obd = new OBDSQLDatabaseAdapter();
		obd.setConfiguration(obdconfig);
		obd.connect();
	}
	
	public void connect(DataSource dataSource) throws SQLException, ClassNotFoundException {
	    this.obdconfig = new OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration(dataSource);
	    this.obd = new OBDSQLDatabaseAdapter();
        this.obd.setConfiguration(this.obdconfig);
        obd.connect();
	}

	public Connection getConnection() {
		return obd.getConnection();
	}

	public void disconnect() {
		try {
			obd.getConnection().close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public boolean isUsesSurrogateKeys() {
		return true;
	}
	
	public Node getNodeByInternalId(int iid) {
		Collection<Node> nodes = getNodesByQuery(new LabelQueryTerm(
				AliasType.INTERNAL_ID, iid));
		if (nodes.size() == 0)
			return null;
		else if (nodes.size() == 1)
			return (Node) nodes.toArray()[0];
		else
			return null; // TODO exception; this should never happen
	}

	public abstract Node createNodeFromResultSet(ResultSet rs) throws SQLException;
	public abstract LinkStatement createLinkStatementFromResultSet(ResultSet rs) throws SQLException;
	public abstract LiteralStatement createLiteralStatementFromResultSet(String tbl, ResultSet rs)
	throws SQLException;


	public Collection<Node> getNodesByQuery(RelationalQuery rq) {
		Collection<Node> nodes = new LinkedList<Node>();
		try {
			ResultSet rs = execute(rq);
			while (rs.next()) {
				Node n = createNodeFromResultSet(rs);
				nodes.add(n);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodes;
	}


	public Collection<Node> getNodesByQuery(QueryTerm queryTerm) {
		RelationalQuery rq = translateQueryForNode(queryTerm);
		return getNodesByQuery(rq);
	}
	
	public abstract RelationalQuery translateQueryForNode(QueryTerm queryTerm);
	public abstract String translateQuery(QueryTerm qt, RelationalQuery rq, 
			String x);


	public abstract void setSourceId(Node s, Integer iid);


	// ****************
	// UTIL
	// ****************

	public ResultSet execute(RelationalQuery q) throws SQLException {
		ResultSet rs = q.execute(obd.getConnection());
		return rs;
	}

	// TODO: move to generic place
	protected Object callSqlFunc(String func, Object... args) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("{?= call " + func + "(");
		boolean isFirst = true;
		for (Object arg : args) {
			if (isFirst)
				isFirst = false;
			else
				sql.append(",");
			sql.append("?");
		}
		sql.append(")}");
		logger.fine("sql=" + sql);
		for (Object ob : args) {
			logger.fine("arg: " + ob);
		}
		CallableStatement stmt = obd.getConnection().prepareCall(sql.toString());
		stmt.registerOutParameter(1, Types.INTEGER);
		// System.out.println("stmt="+stmt);
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg instanceof Integer) {
				stmt.setInt(i + 2, (Integer) arg);
			} else if (arg instanceof Boolean) {
				stmt.setBoolean(i + 2, (Boolean) arg);
			} else if (arg instanceof IdentifiedObject) {
				stmt.setString(i + 2, ((IdentifiedObject) arg).getID());
			} else {
				stmt.setString(i + 2, ObjectUtils.toString(arg));
			}
		}

		boolean rs = stmt.execute();
		return stmt.getInt(1);
	}

	// TODO: move to generic place
	protected CallableStatement executeSqlFunc(String func, int returnType,
			Object... args) throws SQLException {
		StringBuffer sql = new StringBuffer();
		sql.append("{?= call " + func + "(");
		boolean isFirst = true;
		for (Object arg : args) {
			if (isFirst)
				isFirst = false;
			else
				sql.append(",");
			sql.append("?");
		}
		sql.append(")}");
		logger.fine("sql=" + sql);
		for (Object ob : args) {
			logger.fine("arg: " + ob);
		}
		CallableStatement stmt = obd.getConnection()
		.prepareCall(sql.toString());
		stmt.registerOutParameter(1, returnType);
		// System.out.println("stmt="+stmt);
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg instanceof Integer) {
				stmt.setInt(i + 2, (Integer) arg);
			} else if (arg instanceof Boolean) {
				stmt.setBoolean(i + 2, (Boolean) arg);
			} else if (arg instanceof IdentifiedObject) {
				stmt.setString(i + 2, ((IdentifiedObject) arg).getID());
			} else {
				stmt.setString(i + 2, (String) arg);
			}
		}

		boolean rs = stmt.execute();
		return stmt;
	}

	public Double fetchDouble(String sql) throws SQLException {
		ResultSet rs = obd.getConnection().prepareStatement(sql).executeQuery();
		if (rs.next()) {
			return rs.getDouble(1);
		}
		return null;
	}

	protected String tblCol(String tbl, String col) {
		return tbl + "." + col;
	}

		


	
}

