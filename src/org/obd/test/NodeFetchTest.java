package org.obd.test;

import java.util.Arrays;

import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.Shard;

/**
 * tests ability to fetch by multiple IDs
 * @author cjm
 *
 */
public class NodeFetchTest extends AbstractOBDTest {

	
	public NodeFetchTest(String n) {
		super(n);
	}

	
	/**
	 * 
	 * @see Shard#getNodesBelowNodeSet
	 * 
	 */
	public void testGetNode() {
		
		// heart development & blood cell
		String[] ids =
		{"GO:0003007", "NCBI_Gene:7137"};
		
		for (String id : ids) {
			Node n = shard.getNode(id);
			System.out.println(n);
			for (Statement s : n.getStatements()) {
				System.out.println(s);
			}
		}

		
	}

}
