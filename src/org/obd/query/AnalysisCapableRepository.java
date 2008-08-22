package org.obd.query;

import java.util.Collection;
import java.util.List;

import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.stats.AggregateStatisticCollection;
import org.obd.model.stats.ScoredNode;
import org.obd.model.stats.SimilarityPair;
import org.obd.query.Shard.EntailmentUse;
import org.obd.query.Shard.GraphExpansionAlgorithm;

public interface AnalysisCapableRepository {



	
	/**
	 * retrieves summary of contents of this shard
	 * @return
	 */
	public AggregateStatisticCollection getSummaryStatistics();

	/**
	 * given an annotated entity node (e.g. a PMID or a genotype ID)
	 * this will return all similar nodes attached to a score for that node.
	 * The similarity is determined by finding other annotated entities with
	 * similar annotation profiles.
	 * 
	 * The score is an uncorrected p-value (low values close to zero indicate
	 * high degree of similarity).
	 * 
	 * @param nodeId
	 * @return all comparable nodes with similarity p-value
	 */
	public List<ScoredNode> getSimilarNodes(String nodeId);
	
	/**
	 * Experimental: as above, but use a specific ontology (eg UBERON) as basis for
	 * comparison
	 * @param nodeId
	 * @param ontologySource_id
	 * @return
	 */
	public List<ScoredNode> getSimilarNodes(String nodeId, String ontologySource_id);


	/**
	 * As {@link #getSimilarNodes(nodeId)}, finds subject node via query
	 * @param nodeQueryTerm
	 * @return
	 */
	public List<ScoredNode> getSimilarNodes(QueryTerm nodeQueryTerm);
	
	/**
	 * Returns the information content of a class node, calculated based on the number of annotations at or below that node
	 * <p>
	 * Formula: <img src="http://www.pubmedcentral.nih.gov/picrender.fcgi?artid=2238903&blobname=gkm806um2.jpg" alt="formula for information content"/>
	 * @param classNodeId
	 * @return information content
	 */
	public Double getInformationContentByAnnotations(String classNodeId);
	
	public Collection<ScoredNode> getCoAnnotatedClasses(String classNodeId) throws Exception;
	
	/**
	 * Given an annotated entity (such as a human gene or genotype), and two annotation sources (eg fb and zfin), return a 
	 * SimilarityPair object that summarises the total set of attributes of that AE as determined by the two sources;
	 * here the set of attributes is the classes used to annotate the AEs, plus their "parents" as determined by
	 * the closureQueryTerm
	 * 
	 * This method is most useful when there are two or more agents annotating the same entity; for example, as in
	 * double-blind experiments or the NCBO IMIM 3x coverage annotation of OMIM genotypes. Ideally different curators
	 * will annotate the same genotype in different ways (the similiarity score in the SimilarityPair object will
	 * be high), but in practice this will not be the case.
	 * 
	 * @param aeid - e.g. OMIM:601653
	 * @param src1 - e.g. omim_phenotype_zfin
	 * @param src2
	 * @param closureQueryTerm
	 * @return
	 */
	public SimilarityPair compareAnnotationsBySourcePair(String aeid, String src1, String src2, LinkQueryTerm closureQueryTerm);
	
	/**
	 * As before, but with the default closureQueryTerm, i.e. the closure over any relation
	 * @param aeid
	 * @param src1
	 * @param src2
	 * @return
	 */
	public SimilarityPair compareAnnotationsBySourcePair(String aeid, String src1, String src2);
	
	/**
	 * Given two annotated entities (e.g. two genes, perhaps connected by homology), find the attributes
	 * they share (and the attributes that differ between them) based on annotation and deductive closure
	 * as determined bu closureQueryTerm
	 * 
	 * @param aeid1
	 * @param aeid2
	 * @return
	 */
	public SimilarityPair compareAnnotationsByAnnotatedEntityPair(String aeid1, String aeid2, LinkQueryTerm closureQueryTerm, LinkQueryTerm annotationQueryTerm);

	/**
	 * Given two annotated entities (e.g. two genes, perhaps connected by homology), find the attributes
	 * they share (and the attributes that differ between them) based on annotation and deductive closure.
	 * Here the default closureQueryTerm is used (i.e. closure over any relation)
	 * 
	 * @param aeid1
	 * @param aeid2
	 * @return
	 */
	public SimilarityPair compareAnnotationsByAnnotatedEntityPair(String aeid1, String aeid2);
	
	/**
	 * This must be called in order to populate various fields in the SimilarityPair object
	 * (the Shard must be queried to find IC for nodes)
	 * @param sp
	 */
	public void calculateInformationContentMetrics(SimilarityPair sp);

}
