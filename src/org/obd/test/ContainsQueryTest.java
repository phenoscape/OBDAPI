package org.obd.test;



import java.sql.SQLException;
import java.util.Collection;

import junit.framework.Assert;

import org.obd.model.Node;
import org.obd.query.QueryTerm;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.impl.OBDSQLShard;

/**
 * tests for ability to query by arbitrarily complex and nested {@link QueryTerm} objects
 * @author cjm
 *
 */
public class ContainsQueryTest extends AbstractOBDTest {


	public ContainsQueryTest(String n) {
		super(n);
	}

	public void testContainsQuery() throws Exception {
		Collection<Node> nodes = ((OBDSQLShard) this.shard).getNodesBySearch("Hodgkin's lymph neck", Operator.CONTAINS_ALL,null,null);
		Assert.assertTrue(nodes.size()>0);
		for (Node n : nodes ){
			System.out.println(n.getId() + "\t" + n.getLabel());
		}
		
		nodes =  ((OBDSQLShard) this.shard).getNodesBySearch("Hodgkin's lymph neck", Operator.CONTAINS_ANY, null,null);
		Assert.assertTrue(nodes.size()>0);
		for (Node n : nodes){
			System.out.println(n.getId() + "\t" + n.getLabel());
		}
	}
}
