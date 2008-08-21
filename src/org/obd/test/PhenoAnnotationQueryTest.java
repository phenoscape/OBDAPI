package org.obd.test;

import java.util.Collection;

import org.obd.model.Node;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm.Aspect;
import org.purl.obo.vocab.RelationVocabulary;

/**
 * Tests for queries over phenotypes
 * @author cjm
 *
 */
public class PhenoAnnotationQueryTest extends AbstractOBDTest {

	RelationVocabulary rv = new RelationVocabulary();
	
	public PhenoAnnotationQueryTest(String n) {
		super(n);
	}

	
	/**
	 * select P where influences(G,P)
	 */
	public void testGenoPhenoQuery() {
		
		String gtId = "OMIM:601653.0008";
		
		LinkQueryTerm infQt = new LinkQueryTerm();
		infQt.setRelation("OBO_REL:occurs_with");
		infQt.setNode(gtId);
		infQt.setAspect(Aspect.TARGET);
		
		Collection<Node> nodes = shard.getNodesByQuery(infQt);
		for (Node n : nodes)
			System.out.println(n);
		assertTrue(nodes.size()>0);

		/*
		Collection<Statement> stmts = shard.getStatementsByQuery(infQt);
					
		for (Statement s : stmts) {
			System.out.println(s);
		}
		
		assertTrue(stmts.size()>0);
		*/
	}
	

	/*
	private void testGenoPhenoEntityQuery() {

		String gtId = "OMIM:601653.0008";

		LinkQueryTerm gpQt = new LinkQueryTerm();
		gpQt.setQueryAlias("g2p");
		gpQt.setRelation("OBO_REL:occurs_with");
		gpQt.setNode(gtId);
		//infQt.setAspect(Aspect.TARGET);

		LinkQueryTerm inhQt = new LinkQueryTerm();
		inhQt.setQueryAlias("inh");
		gpQt.setTarget(inhQt);

		inhQt.setRelation("OBO_REL:inheres_in");
		//inhQt.setNode(infQt);
		inhQt.setAspect(Aspect.TARGET);

		Collection<Node> nodes = shard.getNodesByQuery(gpQt);
		for (Node n : nodes)
			System.out.println(n);
		assertTrue(nodes.size()>0);
	}
	 */

	/**
	 * select E where G influences _P inheres_in E
	 */
	public void testGenotypePhenoEntityQuery() {

		String gtId = "OMIM:601653.0008";

		LinkQueryTerm gpQt = new LinkQueryTerm();
		gpQt.setQueryAlias("g2p");
		gpQt.setRelation(rv.occurs_with());
		gpQt.setNode(gtId);
		gpQt.setAspect(Aspect.TARGET);

		LinkQueryTerm inhQt = new LinkQueryTerm();
		inhQt.setQueryAlias("inh");

		inhQt.setRelation(rv.inheres_in());
		inhQt.setNode(gpQt);
		inhQt.setAspect(Aspect.TARGET);

		Collection<Node> nodes = shard.getNodesByQuery(inhQt);
		for (Node n : nodes)
			System.out.println(n);
		assertTrue(nodes.size()>0);
	}
	
	
	
	/**
	 * select E where Gt influences _P inheres_in E, Gt variant_of Gn
	 */
	public void testGenePhenoEntityQuery() {

		String geneId = "NCBI_Gene:2138";

		// Gt variant_of Gn
		LinkQueryTerm varQt = new LinkQueryTerm();
		varQt.setQueryAlias("variant_of");
		varQt.setRelation("OBO_REL:variant_of");
		varQt.setTarget(geneId);
		
		// 
		LinkQueryTerm gpQt = new LinkQueryTerm();
		gpQt.setQueryAlias("g2p");
		gpQt.setRelation("OBO_REL:occurs_with");
		gpQt.setNode(varQt);
		gpQt.setAspect(Aspect.TARGET);

		LinkQueryTerm inhQt = new LinkQueryTerm();
		inhQt.setQueryAlias("inh");

		inhQt.setRelation("OBO_REL:inheres_in");
		inhQt.setNode(gpQt);
		inhQt.setAspect(Aspect.TARGET);

		Collection<Node> nodes = shard.getNodesByQuery(inhQt);
		for (Node n : nodes)
			System.out.println(n);
		assertTrue(nodes.size()>0);
	}

}
