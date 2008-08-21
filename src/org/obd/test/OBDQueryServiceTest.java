package org.obd.test;

import junit.framework.TestCase;

/**
 * @deprecated
 * @author cjm
 *
 * This tests OBDQueryService, which is now deprecated (was created to support 
 * Axis2/SOAP)
 */
public class OBDQueryServiceTest extends TestCase {

	/*
	OBDQueryService service;
	
	
	public OBDQueryServiceTest(String n) {
		super(n);
	}


	public void setUp() throws Exception {
		System.out.println("Setting up: " + this);
		service = new OBDQueryService();
		
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		addTests(suite);
		return suite;
	}

	public static void addTests(TestSuite suite) {
		suite.addTest(new OBDQueryServiceTest("testQuery"));
	}
	
	public void testQuery() throws Exception {
		String id = "GO:0006811";
		Node node = 
			service.getNode(id);
		Logger.getLogger("org.obd.test").info("node="+node.getLabel());
		assertTrue(node != null);
		
		for (Statement s : service.getStatementsForNode(id)) {
			Logger.getLogger("org.obd.test").info("parentLink="+s);
		}

		for (Statement s : service.getStatementsForTarget(id)) {
			Logger.getLogger("org.obd.test").info("childLink="+s);
		}
		
		for (Statement s : service.getSubjectStatements(null, 
				null, null, null, id, null, null, true, true)) {
			Logger.getLogger("org.obd.test").info("annot="+s);
		}
		
		Graph g = service.getAnnotationGraphAroundNode(id, null, null);
		g.nestStatementsUnderNodes();
		for (Node n : g.getNodes()) {
			System.out.println(n);
			for (Statement s : n.getStatements()) {
				System.out.println("    "+s);
			}	
		}
		
		for (Node n : service.getNodesBySearch("AB%")) {
			System.out.println(n);
			for (Statement s : n.getStatements()) {
				System.out.println("    "+s);
			}	
			
		}

	}
	*/

}
