package org.obd.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;



public class AllTests extends TestCase {

	protected AllTests(String name) {
		super(name);
	}

	
	public static Test suite() {
		TestSuite out = new TestSuite();
		
		// 3 second pause between tests
		AbstractOBDTest.sleepTime = 300;

		out.addTestSuite(AggregateQueryTest.class);
		out.addTestSuite(AnnotationGraphTest.class);
		out.addTestSuite(CompositionalDescriptionTest.class);
		out.addTestSuite(ExecuteQueryTest.class);
		out.addTestSuite(MiniTest.class);
		out.addTestSuite(MultiNodeFetchTest.class);
		out.addTestSuite(OBDQueryServiceTest.class);
		out.addTestSuite(OWLGraphTest.class);
		out.addTestSuite(PutExternalAnnotationsTest.class);
		out.addTestSuite(QueryBySourceTest.class);
		out.addTestSuite(QueryTest.class);
		out.addTestSuite(SearchTest.class);
		out.addTestSuite(SimilarityTest.class);

		return out;
	}

}
