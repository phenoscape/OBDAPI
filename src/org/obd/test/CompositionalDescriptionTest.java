package org.obd.test;

import java.util.Collections;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;

/**
 * Tests for capability to retrieve class expressions from a {@link org.obd.model.Shard}
 * @see CompositionalDescription
 * @author cjm
 *
 */
public class CompositionalDescriptionTest extends AbstractOBDTest {
		
	public CompositionalDescriptionTest(String n) {
		super(n);
	}
	
	/*
	 * OBD stores compositional descriptions as links.
	 * This test fetches the description for some pre-composed
	 * ontology classes from the mammalian phenotype ontology
	 * 
	 * Requires pre-loading of MP-XP in OBD
	 */
	public void testGetDescription() {
		
		String id = "MP:0004176"; // ear telangiectases
		Graph g = shard.getAnnotationGraphAroundNode(id, null, null);
		printGraph(g);
		CompositionalDescription desc = g.getCompositionalDescription(id);
		System.out.println("desc1="+desc);
		assertTrue(desc.toString().contains("SOME MA:0000060"));
		
		System.out.println("again:");
		
		desc = shard.getCompositionalDescription(id, false);
		System.out.println("desc2="+desc);
		assertFalse(desc.toString().contains("SOME MA:0000060"));
		assertTrue(desc.toString().contains("SOME MA:0000236"));
		System.out.println("again, full expression:");
		
		desc = shard.getCompositionalDescription(id, true);
		System.out.println("desc3="+desc);
		assertTrue(desc.toString().contains("SOME MA:0000060"));
		
		CompositionalDescription d2 = 
			new CompositionalDescription(desc.getGenus().getNodeId(), 
					Collections.singleton(new LinkStatement(null, "x","y")));
		System.out.println(d2);
		
	
	}


}
