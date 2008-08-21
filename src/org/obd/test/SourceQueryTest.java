package org.obd.test;

import org.obd.model.Node;
import org.obd.query.BooleanQueryTerm;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.SourceQueryTerm;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;



public class SourceQueryTest extends AbstractOBDTest {

	public SourceQueryTest(String n) {
		super(n);
	}
	
	
	public void testNoSourceQuery(){
		
		
		
		ComparisonQueryTerm cqt = new ComparisonQueryTerm(Operator.CONTAINS_ALL, "afferent neuron");
		LabelQueryTerm q = new LabelQueryTerm(AliasType.PRIMARY_NAME, cqt);
		
		// Seems to make unusable SQL 
		//q.setNodeSource("fma");
		showNodes(q);
		
	}
	
	public void testSourceQuery(){
		
		
		
		ComparisonQueryTerm cqt = new ComparisonQueryTerm(Operator.CONTAINS_ALL, "afferent neuron");
		LabelQueryTerm q = new LabelQueryTerm(AliasType.ANY_LABEL, cqt);
		
		// Seems to make unusable SQL 
		q.setNodeSource("fma");
		showNodes(q);
		
	}

	/**
	 * tests a boolean query, on both source and label
	 */
	public void testBooleanSourceQuery(){ // TODO - make this more efficient
		
		
		
		ComparisonQueryTerm cqt = new ComparisonQueryTerm(Operator.CONTAINS_ALL, "afferent neuron");
		LabelQueryTerm q = new LabelQueryTerm(AliasType.ANY_LABEL, cqt);
		SourceQueryTerm sq = new SourceQueryTerm("fma");
		
		BooleanQueryTerm bq = new BooleanQueryTerm(q,sq);
		// Seems to make unusable SQL 
		
		showNodes(bq);
		
	}
	
	

	
	private void showNodes(QueryTerm q) {
		for (Node n : this.shard.getNodesByQuery(q)){
			System.out.println(n.getSourceId() + "\t" + n.getId() + "\t" + n.getLabel());
		}
		
	}
	

}