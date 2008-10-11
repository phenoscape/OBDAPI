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
import org.obd.query.Shard.GraphTranslation;

/**
 * A Shard/Repository that is capable of performing statistical and/or data mining operations on the Shard contents.
 * 
 * The two core capabilities of this module are
 * <ul>
 * <li>Annotation Profile-based <b>Search</b> against a database - {@link #getSimilarNodes(String)}
 * <li><b>Pairwise Similarity</b> between nodes - {@link #compareAnnotationsByAnnotatedEntityPair(String, String)}
 * </ul>
 * 
 * Note that at this time search is not guaranteed to find all similar nodes.
 * Once you have two nodes (for example, in search results) you can perform a pairwise comparison.
 * 
 * This is somewhat analagous to doing an initial blast on a whole database, then doing a smith-waterman on the resulting pairs
 * 
 * <h2>Search</h2>
 * 
 * The main method is {@link #getSimilarNodes(SimilaritySearchParameters, String)}, or {@link #getSimilarNodes(String)}
 * to use default parameters.
 * <p>
 * Given a query node q (for example, a gene like sox9 in Danio rerio), the search should return
 * a set of hits h<sub>1</sub> , h<sub>2</sub>... h<sub>n</sub> such that each hit is of the same
 * type as q (ie genes are compared with genes), and each hit bears some resemblance to the query in
 * terms of its <i>annotation profile</i>.
 * <h3>Annotation Profile</h3>
 * The annotation profile is the set of classes used to annotate that
 * entity, and their ancestors, via some relevant relation(s). We write the annotation
 * profile for an entity x as <b>A(x)</b>
 * <pre>
 * c &isin; A(q) <i>iff</i> link(r,q,c)
 * </pre>
 * link(r,q,c) may be computed via reasoning. For example:
 * <pre>
 * link(influences,sox9,curvature-of-tibia) &rarr; link(influences,sox9,morphology-of-bone)
 * </pre>
 * For the purpose of search we often ignore the relation, r.
 * <p>
 * <h3>Search Algorithm<h3>
 * Given a query node <b>q</b>, with annotation profile <b>A(q)</b>
 * we try to find all h &isin; H(q) such that:
 * <ul>
 * <li> type(h) = type(q)
 * <li> <b>A(h)</b> is similar to <b>A(q)</b> (i.e. they have similar annotation profiles) 
 * </ul>
 * Comparing q with every entity in the database is too time-consuming;
 * Instead we try to find <i>candidate hits</i> for q, <b>H'(q)</b> 
 * by use of a <i>search profile</i> for q, called <b>S(q)</b>.
 * 
 * <h3>Search Profile</h3>
 * The search profile S(q) is a set of classes relevant to q. The search profile is always a subset
 * of the annotation profile:
 * <pre>
 * S(q) &sube; A(q)
 * </pre>
 * 
 * <p>
 * The entire search profile is then used
 * as a disjunctive query, fetching candidate hit nodes that have at least one annotation (direct or ancestor)
 * to any class in the profile:
 * <pre>
 * h &isin; H'(q) <i>iff</i> c &isin; S(q) <b>and</b> c &isin; A(h)
 * </pre>
 * Each candidate h is then compared against q using the entire annotation set for h, yielding
 * a basic similarity score.
 * It is excluded or included from the final list of hits based on this score.
 * <p>
 * Candidate hits are prioritized according to how close they are to the profile.
 * They are ordered in descending order by the number of classes in common,
 * | A(h) &cap; H'(q) |, and the first N are chosen as the final set, H(q)
 * 
 * <h4>Selection of search profile</h4>
 * Selecting an appropriate S(q) is necessary to ensure that the search is sensitive, and runs in
 * a minimum amount of time.
 * <p>
 * If S(q) contains a class that is too general, then we will get too many candidate hits, and the resulting search will
 * be extremely slow because we must perform individual pairwise comparisons. There is no loss of specificity - 
 * it is just less efficient. If S(q) contains only highly specific classes,
 * then we lose sensitivity -- in extreme cases, only q is returned in H(q)
 * <p>
 * The basic method for selecting S(q) is to take the first N classes connected to q via an implied_annotation_link,
 * ordered by number of annotations for that class (ascending). This has the result of selecting the most specific
 * classes first (because they have fewest annotations). This is equivalent to ordering by information content (IC).
 * <p>
 * typically we have a threshold <code>max_annots</code> such that for each class c &isin; S(q), |annots(c)| < max_annots
 * <p>
 * The main problem with this method is that we may end up with S(q) containing too specific classes; especially
 * if we are doing inter-species search and q is largely annotated using a species-centric anatomy ontology. One way around this
 * is to constrain S(q) to only include classes from a specific ontology - see {@link #getSimilarNodes(String, String).
 * (currently it is not possible to specify a list of ontologies)
 * <h3>Mixed Ontology Search Profiles</h3>
 * One way of avoiding species-centric bias is to give multiple different ontologies representation in S(q). Note that this
 * assumes that q is related via annotation to classes in multiple ontologies (which will be the case in either annotation
 * that relies on {@link org.obd.model.CompositionalDescription} s or ontologies with inter-ontology links).
 * <p>
 * With this method, we still select the first N classes, but we limit S(q) such that each ontology has a maximum of M classes
 * in S(q). 
 * <h3>ensuring wide distribution of search profile</h3>
 * Ideally the search profile will consist of independent classes reflecting different aspects of the query
 * entity. For example, if A(q) includes anatomical classes including the <i>hippocampal region</i>, the <i>heart</i> and
 * the <i>trigeminal nerve</i> then a S(q) that is clustered around <i>hippocampus</i> but lacks classes around the
 * other two anatomical parts will be poor, as it may not be sensitive enough to include candidate hits
 * that are similar in these other respects
 * <p>
 * Other than forcing a S(q) from different ontologies, there is no attempt to ensure a distributed
 * S(q) within an ontology. In future semantic similarity measures between classes may be used to ensure
 * a wide spread
 * 
 * <h3>Calculating scores for candidate hits</h3>
 * Once we have a (truncated) list of candidate hits we calculate a basic similarity score between q and each h &isin H(q).
 * see {@link SimilarityPair#getBasicSimilarityScore()} 
 * | H(q) &cap; A(q) | / | H(q) &cup; A(q) |
 * We do not use IC based metrics at this point, as these take longer to calculate and the search profile may
 * give a large number of candidate hits to assess.
 * <p>
 * The candidate hits are then returned in order of score (possibly truncated if the API caller requests this - TODO).
 * At this point, more detailed scores involving IC can be computed on the resulting pairs.
 * 
 * <h2>Pairwise Similarity</h2>
 * 
 * An AnalysisCapableRepository can calculate a {@link SimilarityPair} object for any two given nodes. See
 * the SimilarityPair documentation.
 * <p>
 * First the API caller calls {@link #compareAnnotationsByAnnotatedEntityPair(String, String)} on two nodes, getting
 * a {@link SimilarityPair} object. Then, optionally, we can call {@link #calculateInformationContentMetrics(SimilarityPair)} on the object
 * 
 * 
 * @author cjm
 * @see SimilarityPair
 */
public interface AnalysisCapableRepository {

	/**
	 * Parameterizes searches in {@link AnalysisCapableRepository#getSimilarNodes(SimilaritySearchParameters, String)}
	 * 
	 * @see AnalysisCapableRepository
	 * @author cjm
	 *
	 */
	public class SimilaritySearchParameters {
		/**
		 * restriction on the size of S(q). For every class c &isin; S(q), |annots(c)| < this number.
		 * 
		 * Setting this number too low reduces the sensitivity of searches.
		 * Setting this too high increases the time spent searching, but does not
		 * reduce sensitivity
		 */
		public Integer search_profile_max_annotated_entities_per_class = 3000;
		
		/**
		 * Maximum number of hits returned. Note that these are not guaranteed to
		 * be the best.
		 * 
		 * This is the value used to truncate the selection of candidate hits, based
		 * on querying using the search profile
		 */
		public Integer max_candidate_hits = 50;
		
		/**
		 * To ensure maximal distribution of S(q) across ontologies, we limit the maximum number
		 * of classes in S(q) to this number per ontology
		 */
		public Integer search_profile_max_classes_per_source = 50;
		
		/**
		 * restrict hits to be in this organism
		 */
		public String in_organism;
		
		/**
		 * 
		 */
		public QueryTerm hitNodeFilter;
		
		public String ontologySourceId;
		
		/**
		 *  experimental: if true tests query against all target nodes
		 */
		public boolean isExhaustive = false;
		
		public String toString() {
			return (isExhaustive ? "X" : "") + "org:"+in_organism;
		}
		
	}


	/**
	 * retrieves summary of contents of this shard
	 * @return
	 */
	public AggregateStatisticCollection getSummaryStatistics();

	/**
	 * @param nodeId
	 * @return
	 */
	public List<ScoredNode> getSimilarNodes(String nodeId);

	/**
	 * given an annotated entity node (e.g. a PMID or a genotype or gene ID)
	 * this will return similar nodes (with score) for that node.
	 * The similarity is determined by finding other annotated entities with
	 * similar annotation profiles.
	 * <p>
	 * 
	 * @param params
	 * @param nodeId
	 * @return all comparable nodes found to be similar
	 */
	public List<ScoredNode> getSimilarNodes(SimilaritySearchParameters params, String nodeId);


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
	public List<ScoredNode> getSimilarNodes(SimilaritySearchParameters params,QueryTerm nodeQueryTerm);

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
	 * @param aeid1
	 * @param aeid2
	 * @return
	 */
	public Double getBasicSimilarityScore(String aeid1, String aeid2);

	
	/**
	 * This must be called in order to populate various fields in the SimilarityPair object
	 * (the Shard must be queried to find IC for nodes)
	 * @param sp
	 */
	public void calculateInformationContentMetrics(SimilarityPair sp);

	
}
