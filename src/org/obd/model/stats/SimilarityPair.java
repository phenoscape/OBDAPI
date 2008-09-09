package org.obd.model.stats;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * <h2>References</h2>
 * The following references may be of use:
 * <ul>
 * <li>
 * <a href="http://www.pubmedcentral.nih.gov/articlerender.fcgi?tool=pubmed&pubmedid=17932054">FunSimMat: a comprehensive functional similarity database</a>
 * </ul>
 * 
 * @author cjm
 * @see org.obd.query.Shard#compareAnnotationsByAnnotatedEntityPair(String, String)
 * @see org.obd.query.Shard#compareAnnotationsBySourcePair(String, String, String)
 *
 */
public class SimilarityPair {
	private String id1;
	private String id2;

	Set<String> nodesInUnion ;
	Set<String> nodesInSet1 ;
	Set<String> nodesInSet2;
	Set<String> nodesInCommon;

	private int totalNodesInCommon;
	private int totalNodesInUnion;
	private int totalNodesInSet1;
	private int totalNodesInSet2;
	private Double congruence;
	private Double maximumInformationContent;
	private Double similarityByInformationContentRatio;
	private Double similarityByInformationContentInCommonSum;
	private String nodeWithMaximumInformationContent;
	private Map<String,Set<String>> closureMap;
	private Map<String,Set<String>> inverseClosureMap;
	private List<ScoredNode> scoredNodes;
	Map<String,Double> nodeInformationContentMap = new HashMap<String,Double>();
 
	public SimilarityPair() {
		super();
		// TODO Auto-generated constructor stub
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
						includeMe = false;
						break; // will NOT get added to nric
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
	 * note that this object can not calculate this metric by itself - it must be populated externally
	 * @return max(IC(t) forall t in both)
	 */
	public Double getMaximumInformationContent() {
		return maximumInformationContent;
	}
	public void setMaximimumInformationContent(double maximimumInformationContent) {
		this.maximumInformationContent = maximimumInformationContent;
	}
	
	
	public String getNodeWithMaximumInformationContent() {
		return nodeWithMaximumInformationContent;
	}
	public void setNodeWithMaximumInformationContent(
			String nodeWithMaximimumInformationContent) {
		this.nodeWithMaximumInformationContent = nodeWithMaximimumInformationContent;
	}
	public void setSimilarityByInformationContentRatio(
			Double similarityByInformationContentRatio) {
		this.similarityByInformationContentRatio = similarityByInformationContentRatio;
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
	public Double getSimilarityByInformationContentRatio() {
		if ((similarityByInformationContentRatio == null) || (similarityByInformationContentRatio.isNaN())){
			return 0.0;
		} else  {
			return similarityByInformationContentRatio;
			
		}
	}
	public void setSimilarityByInformationContentRatio(
			double similarityByInformationContentRatio) {
		this.similarityByInformationContentRatio = similarityByInformationContentRatio;
	}
	
	
	
	/**
	 * @return sum( ic : forall ic in NonRedundantNodesInCommon )
	 */
	public Double getSimilarityByInformationContentInCommonSum() {
		return similarityByInformationContentInCommonSum;
	}
	public void setSimilarityByInformationContentInCommonSum(
			Double similarityByInformationContentInCommonSum) {
		this.similarityByInformationContentInCommonSum = similarityByInformationContentInCommonSum;
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
		" maxIC:"+(getMaximumInformationContent() != null ? getMaximumInformationContent() : "?") +
		" maxNodeIC:"+(getNodeWithMaximumInformationContent() != null ? getNodeWithMaximumInformationContent() : "?") +
		" simGIC:" + (this.getSimilarityByInformationContentRatio() != null ? this.getSimilarityByInformationContentRatio() : "?")  ;
	}

}