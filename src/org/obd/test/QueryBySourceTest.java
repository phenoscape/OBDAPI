package org.obd.test;

import java.util.Collection;

import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.BooleanQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.BooleanQueryTerm.BooleanOperator;
import org.obd.query.QueryTerm.Aspect;

/**
 * tests the ability to filter annotation retrieval by source
 * (source of annotated entity and source of annotation)
 * @author cjm
 *
 */
public class QueryBySourceTest extends AbstractOBDTest {
	
	public QueryBySourceTest(String n) {
		super(n);
	}

	/**
	 * @see Shard.getStatementsByQuery()
	 */
	public void testStatementsQuery() {
		
		String src = "omim_phenotype_bbop";
		LinkQueryTerm q = new LinkQueryTerm();
		q.setSource(src);
		Collection<Statement> stmts = shard.getStatementsByQuery(q);
		for (Statement s : stmts)
			System.out.println(s);

	}
	
	/**
	 * @see Shard.getNodesByQuery()
	 */
	public void testNodeQuery() {
		
		String src = "omim_phenotype_bbop";
		LinkQueryTerm q = new LinkQueryTerm();
		q.setSource(src);
		Collection<Node> nodes = shard.getNodesByQuery(q);
		for (Node n : nodes)
			System.out.println(n);

	}
	
	public void testFetchGenotypesAnnotatedByAll() {
			String[] srcs = {"omim_phenotype_bbop",
				"omim_phenotype_zfin",
				"omim_phenotype_fb"};
		BooleanQueryTerm bq = new BooleanQueryTerm();
		bq.setOperator(BooleanOperator.AND);
		for (String src : srcs) {
			QueryTerm q = new LinkQueryTerm();
			q.setSource(src);
			//q.setAspect(Aspect.SOURCE);
			bq.addQueryTerm(q);
		}
		Collection<Node> nodes = shard.getNodesByQuery(bq);
		for (Node n : nodes)
			System.out.println(n);

	
	}	
	
	
	// link(node=and(link(src=a) link(src=b) link(src=c)))/TARGET
	public void testFetchGenesAnnotatedByAll() {
			String[] srcs = {"omim_phenotype_bbop",
				"omim_phenotype_zfin",
				"omim_phenotype_fb"};
		BooleanQueryTerm bq = new BooleanQueryTerm();
		bq.setOperator(BooleanOperator.AND);
		for (String src : srcs) {
			QueryTerm q = new LinkQueryTerm();
			q.setSource(src);
			bq.addQueryTerm(q);
		}
		LinkQueryTerm lq = new LinkQueryTerm();
		lq.setNode(bq);
		lq.setRelation("OBO_REL:variant_of");
		lq.setAspect(Aspect.TARGET);
		Collection<Node> nodes = shard.getNodesByQuery(lq);
		for (Node n : nodes)
			System.out.println(n);

	
	}	
	
	



}
