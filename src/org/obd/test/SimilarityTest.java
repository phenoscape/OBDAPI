package org.obd.test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.stats.ScoredNode;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.QueryTerm.Aspect;
import org.obd.query.impl.OBDSQLShard;
import org.purl.obo.vocab.RelationVocabulary;

/**
 * Tests the ability to find similar nodes based on annotation profile
 * @author cjm
 *
 */
public class SimilarityTest extends AbstractOBDTest {

	public SimilarityTest(String n) {
		super(n);
	}

	/**
	 * Given an entity of interest (e.g. a genotype such as Eya1<tm1Rilm>/Eya1<tm1Rilm>
	 * in mouse), find similar EOIs based on annotation profile (e.g. phenotype)
	 */
	public void xxxSimple() {

		// 
		//String nid = "OMIM:601653.0014";

		String nid = "ZFIN:ZDB-GENE-980526-166";
		//String nid = "NCBI_Gene:2138";
		//String nid = "Eya1<tm1Rilm>/Eya1<tm1Rilm>";
		List<ScoredNode> sns = shard.getSimilarNodes(nid);
		Collections.sort(sns);
		for (ScoredNode sn : sns) {
			Node n = shard.getNode(sn.getNodeId());
			System.out.println(n+" "+sn.getScore());
		}
		assertTrue(sns.size() > 0);

	}
	
	public void xxxUberonSim() {

		// 
		//String nid = "OMIM:601653.0014";

		//String nid = "ZFIN:ZDB-GENE-980526-166";
		String nid = "NCBI_Gene:2138";
		//String nid = "Eya1<tm1Rilm>/Eya1<tm1Rilm>";
		List<ScoredNode> sns = shard.getSimilarNodes(nid, "uberon");
		Collections.sort(sns);
		for (ScoredNode sn : sns) {
			Node n = shard.getNode(sn.getNodeId());
			System.out.println(n+" "+sn.getScore());
		}
		assertTrue(sns.size() > 0);

	}

	/**
	 * find genes with similar phenotypes to one-eyed-pinhead
	 * Requirement: given a gene such as "oep" we should be able to
	 * retrieve similar genes based on annotations
	 */
	public void xxxSimilarNodesQuery() {

		RelationVocabulary rv = new RelationVocabulary();

		LinkQueryTerm q = new LinkQueryTerm(rv.variant_of(), "entrezgene:30304");
		List<ScoredNode> sns = shard.getSimilarNodes(q);
		LinkQueryTerm nq = new LinkQueryTerm();
		nq.setRelation(rv.variant_of());
		nq.setAspect(Aspect.TARGET);
		Collections.sort(sns);
		for (ScoredNode sn : sns) {
			System.out.println(sn);
			nq.setNode(sn.getNodeId());
			Collection<Statement> nstmts = shard.getStatementsByQuery(nq);
			for (Statement stmt : nstmts) {
				System.out.println("  "+stmt.getTargetId());
			}
		}
		assertTrue(sns.size() > 0);
	}
	
	public void testNodeSimilaritySearch() throws Exception{
		
		int limit = 20;
		String[] geneNames = {"ATP2A1","EPB41","EXT2","EYA1","FECH","PAX2","SHH","SOX9","SOX10","TNNT2","TTN"};
		
		for (String geneName : geneNames ){
			Collection<Node> nodes = shard.getNodesBySearch(geneName, Operator.EQUAL_TO);
			System.out.println("Similar Nodes to " + geneName + "\t(" + nodes.size() + ")");
			for (Node n : nodes){
				
				List<ScoredNode> similarNodes =  ((OBDSQLShard)this.shard).getSimilarNodes(n.getId(),"uberon");
				int size = similarNodes.size();
				if (size > limit){
					similarNodes = similarNodes.subList(0, 20);
				}
				System.out.println("\tsimilar to " + n.getId() + "\t(showing " +similarNodes.size() + " of " + size + ")");
				for (ScoredNode sn : similarNodes){
					Node n2 = this.shard.getNode(sn.getNodeId());
					System.out.println("\t\t" + n2.getLabel() + "\t\t" + (-sn.getScore()));
				}
				
			}
			
		}
		
	}

}
