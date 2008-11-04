package org.obd.model.stats;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Node;

/**
 * This object represents a pair of comparable nodes (such as classes in ontologies) that can have other nodes in common (e.g. via annotation)
 * 
 * For example:
 * 
 * <ul>
 * <li>two gene IDs that potentially shared ontology classes via annotations (e.g. two homologous genes in different species)
 * <li>two annotation sources that annotate the same entity (e.g. in NCBO OMIM 3x annotation coverage experiment)
 * <li>two classes in an ontology that potentially share ancestors
 * </ul>
 * 
 * <h2>Getting Started</h2>
 * 
 * Use of this class assumes you already have two nodes you wish to compare. These might be two genotypes from the same gene - or two orthologous genes.
 * This class will NOT perform search. For that you need  {@link org.obd.query.AnalysisCapableRepository#getSimilarNodes(String)}. This class assumes you have the pair of nodes
 * in advance.
 * 
 * <h2>Basic Concepts</h2>
 * 
 * Nodes are deemed similar on the basis of <i>what they have in common</i>. This class is flexible with respect to what kinds of properties can be used as
 * a basis for similarity. However, the most common case is where the two nodes are entities that are annotated (such as genes), and we are looking for
 * similarity on the basis of <i>shared annotations</i> to classes in an ontology, or to compositional description classes (see {@link CompositionalDescription}).
 * <p>
 * In these cases, we used <i>inferred</i> annotations. E.g. if geneA is annotated to Leg and geneB to Wing, they have Appendage in common.
 * 
 * <h2>Node Sets</h2>
 * 
 * Scoring is typically a measure of what the nodes have in common vs what one node has that the other one does not.
 * <p>
 * the set of classes annotated for node1 is referred to here as nodesInSet1 (accessed via {@link #getNodesInSet1()}), and likewise for node2 the set is
 * nodesInSet2 (accessed via {@link #getNodesInSet2()})
 * <p>
 * the set of classes in common to both node1 and node2 inferred annotations is called nodesInCommon (see {@link #getNodesInCommon()}).
 * We write this as:  
 * <pre>
 *    nodesInSet1 &cap; nodesInSet2
 * </pre>
 * 
 * The size of this set is written as:
 * 
 * <pre>
 *   | nodesInSet1 &cap; nodesInSet2 |
 * </pre>
 * 
 * The entire set of classes used to annotate both nodes (including inferred annotations) is called nodesInUnion (see {@link #getNodesInUnion()}).
 * We write this as:  
 * 
 * <pre>
 *    nodesInSet1 &cup; nodesInSet2
 * </pre>
 * 
 * We size of this set is written as:
 * 
 * <pre>
 *   | nodesInSet1 &cup; nodesInSet2 |
 * </pre>
 * 
 * Note that the size of this set is not the sum | nodesInSet1 | + | nodesInSet2 |, because there are usually some nodes in common
 * <p>
 * For the purposes of reporting similar annotations, we want to avoid reporting redundant classes; e.g. two genes have annotations to "femur" they also
 * have inferred annotations to "bone". There is no sense in reporting "bone", so we can access the non-redundant nodes in common via {@link #getNonRedundantNodesInCommon()}.
 * See also {@link #getNonRedundantNodesInSet1()} and {@link #getNonRedundantNodesInSet2()}
 * 
 * <h2>Scoring</h2>
 * 
 * With these values calculated, we can assign similarity scores. One measure of similarity is simply the number of nodes in common. However,
 * this would mean that genes with high numbers of annotations would be counted as similar by chance. We also want to penalise differences.
 * <p>
 * The basicSimilarityScore (aka class overlap) is the ratio of nodesInCommon to nodesInUnion
 * <pre>
 * | nodesInSet1 &cap; nodesInSet2 | / | nodesInSet1 &cup; nodesInSet2 |
 * </pre>
 * also written as:
 * 
 * <img src="http://www.pubmedcentral.nih.gov/picrender.fcgi?artid=2238903&blobname=gkm806um7.jpg" alt="formula for UI(p,q)"> 
 * <p>
 * 
 * See {@link #getBasicSimilarityScore()}
 * <p>
 * Recall that this includes inferred annotations. This is desirable for two reasons: it allows approximate matching for non-exact classes, and it penalises general matches
 * in favour of specific matches.
 * <p>
 * To see why this is true, consider two genes annotated to very specific classes - e.g. geneA to "permeability of ER membrane of pyramidal neuron" vs geneB to "permability
 * of ER membrane of interneuron". Although not an exact match, they will have very many inferred classes in common, because they are so deep in the graph. Thus |nodesInCommon|
 * will be high
 * <p>
 * Contrast with distant annotations: these may trivially have nodesInCommon inferred near the top of the ontology graph: {"quality", "organism", "organ"} etc. As these
 * nodes have fewer ancestors, |nodesInCommon| will be lower.
 * <p>
 * This works both ways: differences between the genes may be highly specific too, resulting in a bigger penalty. This may be more pronounced across species, where
 * species specific anatomy ontologies are used.
 * <p>
 * Some studies have found that class overlap metrics are sufficient; however, the graph may be a poor proxy for what constitutes an important match. For that we have to use
 * information content
 * 
 * <h3>Information Content Metrics</h3>
 * 
 * The information content of a class is a measure of how "surprised" we are to see it in an annotation.
 * <p>
 * "organism" has a low IC because it occurs so frequently (taking inference into account). If we see two genes A and B annotated to this it may well be by chance, the
 * genes are not necessarily related.
 * <p>
 * "permeability of ER membrane in pyramidal neuron on CA4 hippocampal region" in contrast has high IC. Two genes A and B annotated to this are less likely to occur by chance
 * (although there is of course the possibility of annotation bias: if this was a heavily studied region we would expect to see more)
 * <p>
 * The IC of a term/class t is:
 * <pre>
 *    IC(t) = -log<SUB>2</SUB>p(t)
 * </pre>
 * Here p(t) is the probability of an annotated entity chosen at random being annotated (directly or via inference) with t
 * <pre>
 *   p(t) = | annot(t) | / total_annotated_entities
 * </pre>
 * Use {@link #getInformationContent(String)} to get the IC for any class in the similarity set.
 * <p>
 * We can use the IC of a class to improve comparison metrics. One common technique is to take the single most informative (highest IC) class for any given pair of nodes
 * in an ontology. For this we use {@link #getMaximumInformationContentForNodesInCommon()}. This is the same metric used by Lord et al in <a href="http://bioinformatics.oxfordjournals.org/cgi/reprint/19/10/1275">Investigating semantic similarity measures 
across the Gene Ontology: the relationship 
between sequence and annotation</a>.
 * <p>
 * This approach masks the kind of similarity where the two nodes share multiple different things in common. For example,
 * if two genes are co-annotated to the same 3 distinct brain regions then only the max would be taken into account.
 * <p>
 * Another metric is to sum IC the non-redundant nodesInCommon. See {@link #getInformationContentSumForNRNodesInCommon()}. We use non-redundant nodes to better
 * reflect independent annotations.
 * <p>
 * The most sophisticated measure here is to get the ratio of IC for the nodesInCommon vs the IC for nodesInUnion. This penalises
 * nodes that have differing annotations. We can write this as:
 * <img src="http://www.pubmedcentral.nih.gov/picrender.fcgi?artid=2238903&blobname=gkm806um8.jpg" alt="formula for simGIC"/> 
 * See {@link #getInformationContentRatio()}
 * 
 * <h2>Inferred annotations and use of the reasoner</h2>
 * 
 * This object makes use of pre-computed reasoner results. It relies on the object which builds the SimilarityPair object to populate a closure map, which indicates the
 * ancestors for any given class. See {@link org.obd.query.AnalysisCapableRepository#compareAnnotationsByAnnotatedEntityPair(String, String)}.
 * <p>
 * The most common case is to populate the closure map using the full set of reasoner results, but ignoring the final inferred relation. Thus "cell" is treated as an ancestor of
 * both "nucleus" and "T-cell". However, the closure map can also be built to only reflect is_a or partonomic relations, if preferred.
 * <p>
 * The pre-reasoned results are essential for finding nodesInCommon - annotations do not necessarily match exactly - they may match further
 * up the graph. They are also used when calculating nonRedundantNodesInCommmon ({@link #getNonRedundantNodesInCommon()}, so we do not report or double-count
 * nodes that subsume existing nodes. For example, if nodesInCommon = {neuron, nervous system, morphology-of-tibia, morphology-of-bone} then nonRedundantNodesInCommon =
 * {neuron, morphology-of-tibia} because two of the nodes are redundant (assumes the closureMap treates all relations equally. If we build a closure map from is_a only, then
 * nervous system is NOT redundant with neuron).
 * <p>
 * {@link org.obd.model.CompositionalDescription} s are treated much like other classes. For example, the EQ description "shortened dendrite" is treated as an ontology-less class. It is an
 * is_a descendant of "size of cell projection" and of "size". It is a descendant via inheres_in to "dendrite" and "cell projection". It is a descendant via inheres_in_part_of
 * to "cell", "organism" etc. The distinctions between the relations are generally lost when the SimilarityPair object is constructed.
 * 
 * <h3>Important point</h3>
 * 
 * The reasoner will only know about ontology classes or pre-existing CompositionalDescriptions. For example, if we have annotations to both
 * curvature-of-mouse-tibia and shape-of-human-tibia we would like to use morphology-of-uberon-tibia as the node in common.
 * <p>
 * However, if the composition morphology-of-uberon-tibia has not been added to the database in advance, then nothing is known about this composition! the reasoner
 * does NOT compare all <i>potential</i> compositions as this would be too expensive. The most likely nodeInCommon in this particular case may turn out to be a pre-coordinated
 * ontology class such as uberon-tibia.
 * <p>
 * This is partly a problem as far as compositional descriptions are concerned.
 * <p>
 * One way around this is to pre-populate the database with likely compositions. The {@link PhenotypeHelper} class does this for phenotype-specific combinations. For
 * example, the database is pre-populated with uberon-based compositions whenever there is a species-centric anatomy ontology composition. The reasoner can then place
 * this in the subsumption hierarchy correctly, and potentially use this as a nodeInCommon in a SimilarityPair comparison.
 * <p>
 * One possible future expansion is to make SimilarityPair smarter with respect to <i>potential</i> combinations such that these do not have to be pre-populated.
 * 
 * 
 * 
 * <h2>References</h2>
 * The following references may be of use:
 * <ul>
 * <li>
 * <a href="http://www.pubmedcentral.nih.gov/articlerender.fcgi?tool=pubmed&pubmedid=17932054">FunSimMat: a comprehensive functional similarity database</a>
 * </li>
 * <li>
 * <a href="http://www.biomedcentral.com/1471-2105/9/327">Gene Ontology term overlap as a measure of gene functional similarity</a>
 * </li>
 * </ul>
 * 
 * @author cjm
 * @see org.obd.query.AnalysisCapableRepository#compareAnnotationsByAnnotatedEntityPair(String, String)
 * @see org.obd.query.AnalysisCapableRepository#compareAnnotationsBySourcePair(String, String, String)
 *
 */
public class SimilarityPair {
	private String id1;
	private String id2;

	Set<String> nodesInUnion ;
	Set<String> nodesInSet1 ;
	Set<String> nodesInSet2;
	Set<String> nodesInCommon;
	Set<String> assertedNodesInSet1 ;
	Set<String> assertedNodesInSet2;
	protected Collection<String> irreconcilableInSet1 = null;
	protected Collection<String> irreconcilableInSet2 = null;


	private int totalNodesInCommon;
	private int totalNodesInUnion;
	private int totalNodesInSet1;
	private int totalNodesInSet2;
	private Double congruence;
	private Double maximumInformationContentForNodesInCommon;
	private Double informationContentRatio;
	private Double informationContentSumForNRNodesInCommon;
	private String nodeWithMaximumInformationContent;
	private Map<String,Set<String>> closureMap;
	private Map<String,Set<String>> inverseClosureMap;
	private Map<String,String> bestCommonNodeMap;
	private Map<String,String> bestMatchMap;
	private List<ScoredNode> scoredNodes;
	Map<String,Double> nodeInformationContentMap = new HashMap<String,Double>();
	private Graph graph;

	public SimilarityPair() {
		super();
		// TODO Auto-generated constructor stub
	}


	public Graph getGraph() {
		return graph;
	}


	public void setGraph(Graph graph) {
		this.graph = graph;
	}


	/**
	 * @return identifier of first node in pair (may be ontology class, annotation source, annotated entity, ...)
	 * This node is associated with a set of other nodes (e.g. annotated entities)
	 */
	public String getId1() {
		return id1;
	}
	public void setId1(String baseNode) {
		this.id1 = baseNode;
	}
	/**
	 * @return identifier of second node in pair (may be ontology class, annotation source, annotated entity, ...)
	 */
	public String getId2() {
		return id2;
	}
	public void setId2(String targetNode) {
		this.id2 = targetNode;
	}
	/**
	 *  | nodesInSet1 &cup; nodesInSet2 |
	 */
	public int getTotalNodesInUnion() {
		if (nodesInUnion == null)
			return totalNodesInUnion;
		return nodesInUnion.size();
	}
	public void setTotalNodesInUnion(int totalNodes) {
		this.totalNodesInUnion = totalNodes;
	}
	/**
	 *  | nodesInSet1 &cap; nodesInSet2 |
	 */
	public int getTotalNodesInCommon() {
		if (nodesInCommon == null)
			return totalNodesInCommon;
		return nodesInCommon.size();
	}
	public void setTotalNodesInCommon(int totalNodesInCommon) {
		this.totalNodesInCommon = totalNodesInCommon;
	}

	public int getTotalNodesInSet1() {
		if (nodesInSet1 == null)
			return totalNodesInSet1;
		return nodesInSet1.size();
	}
	public void setTotalNodesInSet1(int totalNodesInSet1) {
		this.totalNodesInSet1 = totalNodesInSet1;
	}
	public int getTotalNodesInSet2() {
		if (nodesInSet2 == null)
			return totalNodesInSet2;
		return nodesInSet2.size();
	}
	public void setTotalNodesInSet2(int totalNodesInSet2) {
		this.totalNodesInSet2 = totalNodesInSet2;
	}

	/**
	 *  nodesInSet1 &cap; nodesInSet2
	 */
	public Set<String> getNodesInCommon() {
		return nodesInCommon;
	}
	/**
	 * @return all n : n &isin; nodesInSet1 &cap; nodesInSet2 & invclosure(n) &notin; nodesInSet1 &cap; nodesInSet2
	 */
	public Set<String> getNonRedundantNodesInCommon() {
		return extractNonRedundantSet(getNodesInCommon());
	}


	public void setNodesInCommon(Set<String> nodesInCommon) {
		this.nodesInCommon = nodesInCommon;
	}
	public Set<String> getNodesInSet1() {
		return nodesInSet1;
	}
	/**
	 * @return all n : n &isin; nodesInSet1  & invclosure(n) &notin; nodesInSet1
	 */
	public Set<String> getNonRedundantNodesInSet1() {
		return extractNonRedundantSet(getNodesInSet1());
	}

	public void setNodesInSet1(Set<String> nodesInSet1) {
		this.nodesInSet1 = nodesInSet1;
	}
	public Set<String> getNodesInSet2() {
		return nodesInSet2;
	}


	/**
	 * @return all n : n &isin; nodesInSet1 & n is directly asserted to be in set1 (ie not computed by ontology closure)
	 */
	public Set<String> getAssertedNodesInSet1() {
		return assertedNodesInSet1;
	}


	public void setAssertedNodesInSet1(Set<String> assertedNodesInSet1) {
		this.assertedNodesInSet1 = assertedNodesInSet1;
	}


	/**
	 * @return all n : n &isin; nodesInSet2 & n is directly asserted to be in set2 (ie not computed by ontology closure)
	 */
	public Set<String> getAssertedNodesInSet2() {
		return assertedNodesInSet2;
	}


	public void setAssertedNodesInSet2(Set<String> assertedNodesInSet2) {
		this.assertedNodesInSet2 = assertedNodesInSet2;
	}


	/**
	 * @return all n : n &isin; nodesInSet2  & invclosure(n) &notin; nodesInSet2
	 */
	public Set<String> getNonRedundantNodesInSet2() {
		return extractNonRedundantSet(getNodesInSet2());
	}

	public void setNodesInSet2(Set<String> nodesInSet2) {
		this.nodesInSet2 = nodesInSet2;
	}
	/**
	 *  nodesInSet1 &cup; nodesInSet2
	 */
	public Set<String> getNodesInUnion() {
		return nodesInUnion;
	}

	/**
	 * Note: this is different from the nonredundant set in the union. Instead
	 * we take 3 sets: NRnodesInSetBoth, NRnodesInSet1, NRnodesInSet2
	 * (these are all guaranteed internally non-redundant) and take the union of these
	 * sets. The final set will NOT be itself non-redundant.
	 * For example, NRset1 may contain "left hand", NRset2 may contain "right hand" and
	 * NRinBoth may contain "hand". The post-partitioned union will contain all 3,
	 * despite the fact they are not non-redundant with eachother, only within their
	 * respective sets
	 * @return NRnodesInSetBoth &cup; NRnodesInSet1 &cup; NRnodesInSet2 &cup;
	 */
	public Set<String> getPostPartitionedNonRedundantNodesInUnion() {
		Set<String> nriu = new HashSet<String>();
		nriu.addAll(this.getNonRedundantNodesInCommon());
		nriu.addAll(this.getNonRedundantNodesInSet1());
		nriu.addAll(this.getNonRedundantNodesInSet2());
		return nriu;
	}
	public void setNodesInUnion(Set<String> nodesInUnion) {
		this.nodesInUnion = nodesInUnion;
	}



	public Map<String, Set<String>> getClosureMap() {
		return closureMap;
	}
	public void setClosureMap(Map<String, Set<String>> closureMap) {
		this.closureMap = closureMap;
		calculateInverseClosureMap();
	}

	/**
	 * gets the parents of a node, and the node itself
	 * @param id
	 * @return n &cup; closure(n)
	 */
	public Set<String> getReflexiveClosure(String id) {
		Set<String> pids = new HashSet<String>();
		if (closureMap.get(id) != null)
			pids.addAll(closureMap.get(id));
		pids.add(id);
		return pids;
	}



	public Map<String, Set<String>> getInverseClosureMap() {
		return inverseClosureMap;
	}
	public void calculateInverseClosureMap() {
		inverseClosureMap = new HashMap<String, Set<String>>();
		for (String c : closureMap.keySet()) {
			for (String p : closureMap.get(c)) {
				if (!inverseClosureMap.containsKey(p))
					inverseClosureMap.put(p, new HashSet<String>());
				inverseClosureMap.get(p).add(c);
			}
		}
	}

	private Set<String> extractNonRedundantSet(Set<String> nic) {
		if (getInverseClosureMap() == null) {
			return nic;
		}
		Set<String> nric = new HashSet<String>();
		for (String n : nic) {
			boolean includeMe = true;
			if (getInverseClosureMap().containsKey(n)) {
				Set<String> cset = getInverseClosureMap().get(n);

				// check all parents of n : if n has a parent already in
				// the nodes-in-common set we exclude it
				for (String cn : cset) {
					// we also have to take the reflexive case into account
					if (!cn.equals(n) && nic.contains(cn)) {
						if (getInverseClosureMap().containsKey(cn) &&
								getInverseClosureMap().get(cn).contains(n)) {

							System.err.println("  :: warning: cycle between "+cn+" and "+n+" --  neither is considered redundant");
						}
						else {
							includeMe = false;
							//System.err.println("  :: excluded "+n+" because we have the more specific "+cn);
							break; // will NOT get added to nric
						}
					}
				}
			}
			if (includeMe) {
				nric.add(n);
			}
		}
		return nric;

	}

	public void setInformationContent(String nid, Double ic) {
		nodeInformationContentMap.put(nid, ic);
	}
	/**
	 * Given a node identifier, return the information content for that node. Must be a node within the set being analyzed (or the closure of that set).
	 * 
	 * @param nid
	 * @return -log2(p(nid))
	 */
	public Double getInformationContent(String nid) {
		return nodeInformationContentMap.get(nid);
	}
	/**
	 * @see #getInformationContent(String)
	 * @return map between node IDs and information content
	 */
	public Map<String, Double> getNodeInformationContentMap() {
		return nodeInformationContentMap;
	}
	public void setNodeInformationContentMap(
			Map<String, Double> nodeInformationContentMap) {
		this.nodeInformationContentMap = nodeInformationContentMap;
	}
	public void sortScoredNodes() {		
		Collections.sort(scoredNodes);
	}
	public List<ScoredNode> getScoredNodes() {
		return scoredNodes;
	}
	public void setScoredNodes(List<ScoredNode> scoredNodes) {
		this.scoredNodes = scoredNodes;
	}
	/**
	 * the maximum shannon information of all nodes in intersection set.
	 * <p>
	 * note that this object can not calculate this metric by itself - it must be populated externally.
	 * This is the same metric used by Lord et al in <a href="http://bioinformatics.oxfordjournals.org/cgi/reprint/19/10/1275">
	 * Investigating semantic similarity measures  across the Gene Ontology: the relationship between sequence and annotation</a>.
	 * 
	 * @return max(IC(t) forall t in both)
	 */
	public Double getMaximumInformationContentForNodesInCommon() {
		return maximumInformationContentForNodesInCommon;
	}
	public void setMaximimumInformationContentForNodesInCommon(double maximimumInformationContent) {
		this.maximumInformationContentForNodesInCommon = maximimumInformationContent;
	}


	public String getNodeWithMaximumInformationContent() {
		return nodeWithMaximumInformationContent;
	}
	public void setNodeWithMaximumInformationContent(
			String nodeWithMaximimumInformationContent) {
		this.nodeWithMaximumInformationContent = nodeWithMaximimumInformationContent;
	}
	public void setInformationContentRatio(
			Double similarityByInformationContentRatio) {
		this.informationContentRatio = similarityByInformationContentRatio;
	}
	/**
	 * information content of intersection set divided by information content of union set
	 * <p>
	 * See:
	 * <pre>
	 * Pesquita, C.;Faria, D.;Bastos, H.;Falc‹o, AO.; Couto, FM. 
	 * <i>Evaluating GO-based semantic similarity measures. Proceedings of the 10th Annual Bio-Ontologies Meeting (Bio-Ontologies 2007). </i>
	 * Stevens R, Lord P, McEntire R, Sansone S-A. , editors. Austria: Vienna; 2007. pp. 37Ð40.
	 * </pre>
	 * <p>
	 * Original Formula:
	 * <img src="http://www.pubmedcentral.nih.gov/picrender.fcgi?artid=2238903&blobname=gkm806um8.jpg" alt="formula for simGIC"/>
	 * <p>
	 * from: <a href="http://www.pubmedcentral.nih.gov/articlerender.fcgi?tool=pubmed&pubmedid=17932054">FunSimMat: a comprehensive functional similarity database</a>
	 * <p>
	 * <b>NOTE:</b> we only include non-redundant nodes in the calculation
	 * <p>
	 * note that this object can not calculate this metric by itself - it must be populated externally
	 * 
	 * @return sum(IC(t) where t in NRnodesInCommon) / sum(IC(t) where t in NRnodesInUnion)
	 * @see org.obd.query.Shard#calculateInformationContentMetrics(SimilarityPair)
	 */
	public Double getInformationContentRatio() {
		if ((informationContentRatio == null) || (informationContentRatio.isNaN())){
			return 0.0;
		} else  {
			return informationContentRatio;

		}
	}



	/**
	 * &Sigma;<SUB>t &isin; nrNodesInCommon</SUB> IC(t)
	 * @return sum( IC : forall IC in NonRedundantNodesInCommon )
	 */
	public Double getInformationContentSumForNRNodesInCommon() {
		return informationContentSumForNRNodesInCommon;
	}
	public void setInformationContentSumForNRNodesInCommon(
			Double similarityByInformationContentInCommonSum) {
		this.informationContentSumForNRNodesInCommon = similarityByInformationContentInCommonSum;
	}
	/**
	 * Inititialize based on two sets of class nodes; calculate the set of nodes in common'
	 * 
	 * @param ids1
	 * @param ids2
	 */
	public void setFromNodeSetPair(Collection<String> ids1, Collection<String> ids2) {
		nodesInUnion = new HashSet<String>();
		nodesInSet1 = new HashSet<String>();
		nodesInSet2 = new HashSet<String>();
		nodesInCommon = new HashSet<String>();


		for (String id : ids1) {
			nodesInUnion.add(id);
			nodesInSet1.add(id);
		}
		for (String id : ids2) {
			nodesInUnion.add(id);
			nodesInSet2.add(id);
		}
		for (String nid : nodesInUnion) {
			if (nodesInSet1.contains(nid) && nodesInSet2.contains(nid) ) {
				nodesInCommon.add(nid);
			}
		}
		setTotalNodesInCommon(nodesInCommon.size());
		setTotalNodesInUnion(nodesInUnion.size());
		setTotalNodesInSet1(nodesInSet1.size());
		setTotalNodesInSet2(nodesInSet2.size());

	}
	

	public float getDisagreementScore() {
		float s1 = ((float)getIrreconcilableNodesInSet1().size() / assertedNodesInSet1.size());
		float s2 = ((float)getIrreconcilableNodesInSet2().size() / assertedNodesInSet2.size());
		return (s1+s2)/2;
	}

	/**
	 * @return all n : n &isin; assertedNodesInSet1 & not(n &isin; closure(nodesInSet2)) & not(n &isin; invClosure(nodesInSet2))
	 */
	public Collection<String> getIrreconcilableNodesInSet1() {
		if (irreconcilableInSet1 == null)
			calculateIrreconcilable();		
		return irreconcilableInSet1;
	}
	/**
	 * @return all n : n &isin; assertedNodesInSet2 & not(n &isin; closure(nodesInSet1)) & not(n &isin; invClosure(nodesInSet1))
	 */
	public Collection<String> getIrreconcilableNodesInSet2() {
		if (irreconcilableInSet2 == null)
			calculateIrreconcilable();		
		return irreconcilableInSet2;
	}

	private void calculateIrreconcilable() {
		Collection<String> mapped = new HashSet<String>();
		for (String a : assertedNodesInSet1) {
			if (assertedNodesInSet2.contains(a)) {
				mapped.add(a);
			}
			for (String x : getClosureMap().get(a)) {
				if (assertedNodesInSet2.contains(x)) {
					mapped.add(a);
					mapped.add(x);
				}
			}		
		}
		for (String a : assertedNodesInSet2) {
			if (assertedNodesInSet1.contains(a)) {
				mapped.add(a);
			}
			for (String x : getClosureMap().get(a)) {
				if (assertedNodesInSet1.contains(x)) {
					mapped.add(a);
					mapped.add(x);
				}
			}		
		}
		irreconcilableInSet1 = new HashSet<String>();
		for (String a : assertedNodesInSet1) {
			if (!mapped.contains(a))
				irreconcilableInSet1.add(a);
		}
		irreconcilableInSet2 = new HashSet<String>();
		for (String a : assertedNodesInSet2) {
			if (!mapped.contains(a))
				irreconcilableInSet2.add(a);
		}
	}
	
	public Collection<String> getBestCommonSubsumers() {
		Collection<String> subsumers = new HashSet<String>();
		for (String nid : getAssertedNodesInSet1()) {
			String x = getBestCommonSubsumer(nid);
			if (x != null)
				subsumers.add(getBestCommonSubsumer(nid));
		}
		for (String nid : getAssertedNodesInSet2()) {
			String x = getBestCommonSubsumer(nid);
			if (x != null)
				subsumers.add(getBestCommonSubsumer(nid));
		}
		return subsumers;
	}
	
	public Double getCommonSubsumerAverageIC() {
		Double totalIC = 0.0;
		int n = 0;
		for (String nid : this.getAssertedNodesInSet1()) {
			n++;
			String bcn = getBestCommonNodeMap().get(nid);
			if (bcn != null)
				totalIC += getInformationContent(bcn);
			else
				System.err.println("no CS for "+nid);
		}
		for (String nid : this.getAssertedNodesInSet2()) {
			n++;
			String bcn = getBestCommonNodeMap().get(nid);
			if (bcn != null)
				totalIC += getInformationContent(bcn);
			else
				System.err.println("no CS for "+nid);
		}
		return totalIC / n;
	}
	
	public String getBestCommonSubsumer(String nid) {
		if (bestCommonNodeMap.containsKey(nid))
			return bestCommonNodeMap.get(nid);
		return null;
	}
	
	public Map<String, String> getBestCommonNodeMap() {
		return bestCommonNodeMap;
	}
	public void setBestCommonNodeMap(Map<String, String> bestCommonNodeMap) {
		this.bestCommonNodeMap = bestCommonNodeMap;
	}


	
	/*
	public Collection<String> getIrreconcilableNodes(Collection<String> ids1, Collection<String> ids2) {
		Collection<String> unmapped = new HashSet<String>();
		for (String id1 : ids1) {
			if (!isReconcilable(id1, ids2)) {
				unmapped.add(id1);
			}
		}
		return unmapped;
	}
	
	public boolean isReconcilable(String id1, Collection<String> ids2) {
		boolean mapped = false;
		for (String x : getClosureMap().get(id1)) {
			if (ids2.contains(x)) {
				mapped = true;
				break;
			}
		}
		if (!mapped) {
			for (String x : ids2) {
				if (getClosureMap().get(x).contains(id1)) {
					mapped = true;
					break;					
				}
			}
			for (String x : getInverseClosureMap().get(id1)) {
				if (ids2.contains(x)) {
					mapped = true;
					break;
				}
			}
		}
		return mapped;
	}
	*/



	/**
	 * The basic similarity score is the number of nodes in common divided by the number of nodes in the union. We assume that this has been pre-assigned,
	 * taking into account closure over relations if required.
	 * Formula: <img src="http://www.pubmedcentral.nih.gov/picrender.fcgi?artid=2238903&blobname=gkm806um7.jpg" alt="formula for UI(p,q)"> in
	 * <a href="http://www.pubmedcentral.nih.gov/articlerender.fcgi?tool=pubmed&pubmedid=17932054">FunSimMat: a comprehensive functional similarity database</a>
	 * Note: at this time, this number reflects the complete set - no filtering is done for redundancy.
	 * This is the desired behavior, as the redundant nodes are in proportion to semantic similarity.
	 * For example, if a highly specific node is shared in common, then its ancestors are also included, boosting
	 * the score
	 * @return totalNodesInCommon / totalNodesInUnion
	 */
	public double getBasicSimilarityScore() {
		if (totalNodesInUnion == 0){
			return 0.0;
		} else {
			return (double) totalNodesInCommon / totalNodesInUnion;
		}
	}

	public String toString() {
		return id1 + " <-> " + id2 + " total:" + getTotalNodesInUnion() + " [" + getTotalNodesInSet1()+","+getTotalNodesInSet2()+"] in_common:" + getTotalNodesInCommon() + " sim:"+getBasicSimilarityScore() + 
		" maxIC:"+(getMaximumInformationContentForNodesInCommon() != null ? getMaximumInformationContentForNodesInCommon() : "?") +
		" maxNodeIC:"+(getNodeWithMaximumInformationContent() != null ? getNodeWithMaximumInformationContent() : "?") +
		" simGIC:" + (this.getInformationContentRatio() != null ? this.getInformationContentRatio() : "?")  ;
	}

}