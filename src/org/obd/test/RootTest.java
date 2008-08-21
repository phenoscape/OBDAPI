package org.obd.test;

import java.util.Collection;

import org.obd.model.Node;

/**
 * @author cjm
 *
 */
public class RootTest extends AbstractOBDTest {

	
	public RootTest(String n) {
		super(n);
	}

	
	/**
	 * @see Shard.getRootNodes()
	 */
	public void testQuery() {
		
		Collection<Node> rootNodes = shard.getRootNodes("cell",null);
		for (Node n : rootNodes) {
			System.out.println(n);
		}
		assertTrue(rootNodes.size()>0);
	}


}
