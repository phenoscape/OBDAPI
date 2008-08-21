package org.obd.test;

import java.util.Collection;

import org.obd.model.Statement;
import org.obd.query.LinkQueryTerm;
import org.obd.query.Shard;

/**
 * Tests for capability to fetch annotations plus contextual graph around node
 * @author cjm
 *
 */
public class AnnotationGraphTest extends AbstractOBDTest {

	public AnnotationGraphTest(String n) {
		super(n);
	}

	
	/**
	 * given an entity of interest, find annotations about this entity
	 * 
	 * @see Shard#getAnnotationStatementsForAnnotatedEntity()
	 */
	public void testAnnotationGraphQuery() {
		
		for (Statement s : shard.getAnnotationStatementsForAnnotatedEntity("ZFIN:ZDB-GENO-960809-7", null, null)) {
			System.out.println(s);
		}
		
		printGraph(shard.getAnnotationGraphAroundNode("FMA:54448", null, null));
		printGraph(shard.getAnnotationGraphAroundNode("CL:0000148", null, null));

		LinkQueryTerm lq = new LinkQueryTerm();
    	lq.setRelation("OBO_REL:is_a");
    	lq.setNode("CL:0000148");
    	Collection<Statement> stmts = shard.getStatementsByQuery(lq);
    	int n = 0;
		for (Statement s : stmts) {
			System.out.println(s);
			if (s.isInferred())
				n++;
		}
		assertTrue(n>0);
	}

}
