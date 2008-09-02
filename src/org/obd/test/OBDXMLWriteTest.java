package org.obd.test;

import java.sql.SQLException;
import java.util.Collection;

import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.query.QueryTerm;
import org.obd.query.Shard.EntailmentUse;
import org.obd.query.Shard.GraphTranslation;
import org.obd.query.impl.OBDSQLShard;

public class OBDXMLWriteTest extends AbstractOBDTest{
	
	public OBDXMLWriteTest(String n){
		super(n);
	}
	
	
	public void testQuery() throws SQLException, ClassNotFoundException {
		
		
		/*
		if (true) {
			LabelQueryTerm lqt = new LabelQueryTerm(AliasType.PRIMARY_NAME, new ComparisonQueryTerm(Operator.CONTAINS,"eye"));
			//SourceQueryTerm sq = new SourceQueryTerm("cell");
			//BooleanQueryTerm bq = new BooleanQueryTerm(BooleanOperator.AND,lqt,sq);
			
			
			Collection<Node> ns = this.runNodeQuery(bq, null);
			for (Node n : ns){
				oxw.node(n);
				System.out.println(oxw.toString());
			}
			    
			Graph g =  this.shard.getAnnotationGraphAroundNode("CL:0000149" , null, null);
			for (Statement s : g.getAllStatements()){
				oxw.statement(s);
				System.out.println(oxw.toString());
			}
			
		}
		*/
		
	}
	
	public Collection<Node> runNodeQuery(QueryTerm qt, String nid) throws SQLException, ClassNotFoundException {
		Collection<Node> nodes = ((OBDSQLShard)this.shard).getNodesByQuery(qt);
		return nodes;
	}
	
	public Graph runGraphQuery(QueryTerm qt, String nid){
		//RelationalQuery rq = ((OBDSQLShard)this.shard).translateQueryForNode(qt);
		
		GraphTranslation gt = new GraphTranslation();
		gt.setIncludeSubgraph(true);
		Graph g = ((OBDSQLShard)this.shard).getGraphByQuery(qt, 
				EntailmentUse.USE_IMPLIED, 
				gt);
		return g;
	}
	

}