package org.obd.test;

import org.obd.model.stats.SimilarityPair;

/**
 * 
 * 
 * @author cjm
 *
 */
public class SimilarityPairTest extends AbstractOBDTest {
	
	
	
	public SimilarityPairTest(String n) {
		super(n);
	}

	
	
	public void testSimilarityPair() {
		//String uid1 = "OBD:MGI_GT:1222";
		//String uid2 = "OBD:MGI_GT:1223";
		//String uid1 = "ZFIN:ZDB-GENE-070613-1";
		//String uid2 = "NCBI_Gene:2138";
		//String uid1 = "NCBI_Gene:6662";
		//String uid2 = "MGI:98371";
		String uid1 = "OMIM:608160.0001";
		String uid2 = "OMIM:608160.0005";
		SimilarityPair sp = this.shard.compareAnnotationsByAnnotatedEntityPair(uid1,uid2);
		this.shard.calculateInformationContentMetrics(sp);
		
		SimilarityPair sp2 = this.shard.compareAnnotationsByAnnotatedEntityPair(uid2,uid1);
		this.shard.calculateInformationContentMetrics(sp2);
		

		System.out.println("sp1nic:"+sp.getNodesInCommon().size());
		System.out.println("sp2nic:"+sp2.getNodesInCommon().size());
		assertTrue(sp.getNodesInCommon().size() == sp2.getNodesInCommon().size());
		
		System.out.println("sp1nrnic:"+sp.getNonRedundantNodesInCommon().size());
		System.out.println("sp2nrnic:"+sp2.getNonRedundantNodesInCommon().size());
		assertTrue(sp.getNonRedundantNodesInCommon().size() == sp2.getNonRedundantNodesInCommon().size());

		System.out.println("sp1:"+sp.getPostPartitionedNonRedundantNodesInUnion().size());
		System.out.println("sp2:"+sp2.getPostPartitionedNonRedundantNodesInUnion().size());
		
		System.out.println("sp1:"+sp.getBasicSimilarityScore() + "\t" + sp.getSimilarityByInformationContentRatio());
		System.out.println("sp2:"+sp2.getBasicSimilarityScore() + "\t" + sp2.getSimilarityByInformationContentRatio());
	
		assertTrue(sp.getBasicSimilarityScore() == sp2.getBasicSimilarityScore());
		assertTrue(sp.getSimilarityByInformationContentRatio() == sp2.getSimilarityByInformationContentRatio());

	}
	
}