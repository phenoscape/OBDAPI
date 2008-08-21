package org.obd.test;


import org.obd.model.stats.AggregateStatistic.AggregateType;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.SourceQueryTerm;
import org.obd.query.QueryTerm.Aspect;

/**
 * Tests aggregate query requirements are fulfilled
 * 
 * An aggregate query is any query that performs some collation operation on a set
 * of returned entities
 * 
 * @author cjm
 *
 */
public class AggregateQueryTest extends AbstractOBDTest {

	
	public AggregateQueryTest(String n) {
		super(n);
	}
	
	/**
	 * Requirement: every query operation must have a corresponding
	 * {@link AggregateType}.COUNT operation
	 * 
	 * @throws Exception
	 */
	public void testAggregateQuery() throws Exception 	{
		String id = "MP:0000830";
		runQuery(new LinkQueryTerm(id), 
				AggregateType.COUNT,
				"number of terms that have "+id+" as a parent");
		
		runQuery(new AnnotationLinkQueryTerm(id), 
				AggregateType.COUNT,
				"number of entities that are annotated to "+id);
	
		QueryTerm sqt = new SourceQueryTerm("cell");
		//sqt.setAspect(Aspect.SOURCE);
		runQuery(sqt, AggregateType.COUNT, "number of terms in cell ontology");
		
		LinkQueryTerm qt = new LinkQueryTerm();
		qt.setNode(id);
		qt.setAspect(Aspect.TARGET);
		runQuery(qt, AggregateType.COUNT,
				"number of terms that have "+id+" as a child");
	}
	protected int runQuery(QueryTerm qt, AggregateType at, String msg) throws Exception {
		
		int num = 
			shard.getNodeAggregateQueryResults(qt,at);
		System.out.println(msg+" NODES: "+num);
		assertTrue(num>0);

		/*
		Collection<QueryTerm> groups = new LinkedList<QueryTerm>();
		QueryTerm sq = new LinkQueryTerm();
		sq.setAspect(Aspect.SOURCE);
		groups.add(sq);
		num = 
			((OBDSQLShard)shard).getNodeAggregateQueryResults(qt,AggregateType.COUNT,groups);
		System.out.println(msg+" NODES BY : "+num);
		assertTrue(num>0);
		*/
		
		num = 
			shard.getLinkAggregateQueryResults(qt,at);
		System.out.println(msg+" LINKS: "+num);
		assertTrue(num>0);
		
		qt.setInferred(false);
		num = 
			shard.getLinkAggregateQueryResults(qt,at);
		System.out.println(msg+" LINKS[ASSERTED]: "+num);
		assertTrue(num>0);
		return num;
	}


}
