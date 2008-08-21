package org.obd.query.impl;

import java.sql.SQLException;

import org.bbop.rdbms.WhereClause;
import org.obd.query.Shard;

/**
 * @author cjm
 *
 */
public class ChadoSQLShard extends OBDSQLShard implements Shard {

	public ChadoSQLShard() throws SQLException, ClassNotFoundException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setNodeIdEqualityClause(WhereClause whereClause,
			String id, boolean direct) {
		String[] parts = id.split(":", 2);
		String db = parts[0];
		String acc = parts[1];
		if (direct) {
			whereClause.addEqualityConstraint("accession",acc);		
			whereClause.addEqualityConstraint("dbname",db);		
		}
		else {
			whereClause.addEqualityConstraint("node_accession",acc);		
			whereClause.addEqualityConstraint("node_dbname",db);		
		}
	}
	
	// this is a little hacky: replace with a proper re-write framework
	@Override
	public void setNodeIdEqualityClause(WhereClause whereClause,
			String col, String id) {
		String[] colParts = col.split("\\.", 2); // TODO : use sql model
		String[] parts = id.split(":", 2);
		String db = parts[0];
		String acc = parts[1];
		String tbl = colParts[0];
		String localCol = colParts[1];
		if (localCol.equals("uid")) {		
			whereClause.addEqualityConstraint(tbl+".accession",acc);		
			whereClause.addEqualityConstraint(tbl+".dbname",db);		
		}
		else {
			whereClause.addEqualityConstraint(col,id);		
		}
	}



}

