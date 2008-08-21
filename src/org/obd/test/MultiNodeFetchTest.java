package org.obd.test;

import java.util.Arrays;

import org.obd.model.Node;
import org.obd.query.Shard;

/**
 * tests ability to fetch by multiple IDs
 * @author cjm
 *
 */
public class MultiNodeFetchTest extends AbstractOBDTest {

	
	public MultiNodeFetchTest(String n) {
		super(n);
	}

	
	/**
	 * 
	 * @see Shard#getNodesBelowNodeSet
	 * 
	 */
	public void testNodesBelowQuery() {
		
		// heart development & blood cell
		String[] ids =
		{"GO:0003007", "PATO:0000001"};
		
		for (Node n : shard.getNodesBelowNodeSet(Arrays.asList(ids) , null, null)) {
			System.out.println(n);
		}

		String[] ids2 =
		{"GO:0003007", "CL:0000081"};

		for (Node n : shard.getAnnotatedEntitiesBelowNodeSet(Arrays.asList(ids2) , null, null)) {
			System.out.println(n);
		}

		
	}

}
