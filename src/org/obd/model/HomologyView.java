package org.obd.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.obd.model.vocabulary.TermVocabulary;
import org.obd.model.NodeSet;
import org.obd.query.ExistentialQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.SourceQueryTerm;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.QueryTerm.Aspect;
import org.purl.obo.vocab.RelationVocabulary;

import antlr.collections.List;

/**
 *
 * Provides a terminological view over a Graph
 * <p>
 * this is essentially a facade that hides the terminology metamodel
 * 
 * NOTE: may move to separate package
 * @author cjm
 */
public class HomologyView implements Serializable {

	private transient Logger logger;

	private Graph ontolGraph = new Graph(); // ontology graph
	private Graph annotGraph = new Graph(); // annotation links plus links from AEs to organism etc

	private transient Shard shard; // must be re-set after de-serialization
	private Map<String, NodeSet> nodeToHomolSetMap;
	private Node focusNode;
	private Collection<LinkStatement> annotStatements;
	private Collection<Node> annotatedEntities;

	private static TermVocabulary vocab = new TermVocabulary();
	private static RelationVocabulary relationVocabulary = new RelationVocabulary();
	private String DESCENDED_FROM = "OBO_REL:descended_from"; // TODO
	private String IN_ORGANISM = "OBO_REL:in_organism";
	private String HOMOLOGOUS_TO = "OBO_REL:homologous_to"; // TODO
	private Map <NodeSet, Collection<LinkStatement>> annotStatementsByNodeSetMap = 
		new HashMap <NodeSet, Collection<LinkStatement>> ();
	private Map<NodeSet,Map<String,Collection<LinkStatement>>> annotStatementsByNodeSetThenQualityIdMap = 
		new HashMap<NodeSet,Map<String,Collection<LinkStatement>>>();

	public HomologyView() {
		super();
		initRelIds();
	}

	public HomologyView(Graph graph) {
		super();
		this.ontolGraph = graph;
		initRelIds();
	}

	public HomologyView(Shard shard) {
		super();
		this.shard = shard;
		initRelIds();
	}

	HashSet<String> aeRelIds = new HashSet<String>();
	HashSet<String> aeRevRelIds = new HashSet<String>();
	
	private void initRelIds() {
		aeRelIds.add(relationVocabulary.variant_of());
		aeRelIds.add(IN_ORGANISM);
		aeRelIds.add(vocab.HAS_DBXREF());
		aeRevRelIds.add(vocab.HAS_DBXREF());

	}

	public Shard getShard() {
		return shard;
	}

	public void setShard(Shard shard) {
		this.shard = shard;
	}

	/**
	 * Given an identifier for a set of genes (e.g. Homologene ID),
	 * this will
	 * <ul>
	 * <li>Find all annotations for each gene and associated entities (genotypes)
	 * <li>create an ontology graph seed from all classes used in annotations
	 * <li>extend the graph; eg to create inheres_in*part_of links
	 * <li>group all annotations by ontology NodeSets, where each NodeSet corresponds to
	 * a collection of comparable classes (e.g. ZFA:kidney + FMA:Kidney + MA:kidney)
	 * </ul>
	 * 
	 * The object is then ready for reporting
	 * @param geneHsetId
	 */
	public void initializeFromGeneHomolSet(String geneHsetId) {
		annotatedEntities = getAnnotatedEntitiesByGeneHomolSet(geneHsetId);
		for (Node node : annotatedEntities)
			getLogger().fine(" annotated entity: "+node);
		Collection<LinkStatement> annotStatements = fetchAnnotationLinks(annotatedEntities);
		initializeGraphsFromAnnotations(annotStatements);
		
		//System.out.println(g.getAllStatements().length);
		ontolGraph.storeSimpleClosure(2); // extend; e.g. to get X po Y ht Z
		//System.out.println("extended="+graph.getAllStatements().length);
		groupByHomolSet(annotStatements, ontolGraph);


	}
	
	/**
	 * given a gene label (e.g. SOX10) find all gene homology sets
	 * (ie those containing fly sox10, fish sox10, ...)
	 * @param label
	 * @return
	 */
	public Collection<Node> getGeneHomolSetByGeneLabel(String label) {
		LinkQueryTerm dqt = new LinkQueryTerm();
		dqt.setNode(new LabelQueryTerm(AliasType.PRIMARY_NAME,label));
		dqt.setRelation(DESCENDED_FROM);
		dqt.setAspect(Aspect.TARGET);
		Collection<Node> hsetNodes = shard.getNodesByQuery(dqt);
		return hsetNodes;
	}
	
	public Collection<Node> getNodesInHomolSet(String hsetId) {
		LinkQueryTerm dqt = new LinkQueryTerm(DESCENDED_FROM,hsetId);
		Collection<Node> geneNodes = shard.getNodesByQuery(dqt);
		Collection<Node> origNodes = new HashSet<Node>();
		for (Node gn : geneNodes) {
			LinkQueryTerm xqt = new LinkQueryTerm(gn.getId(),vocab.HAS_DBXREF(),
					null);
			xqt.setTarget(new LinkQueryTerm(null,IN_ORGANISM,null));
			xqt.setAspect(Aspect.TARGET);
			Collection<Node> xNodes = shard.getNodesByQuery(xqt);
			if (xNodes.size() == 0) {
				origNodes.add(gn);
			}
			else {
				origNodes.addAll(xNodes);
			}
		}
		return origNodes;
	}
	public Collection<Node> getHomologousGenesByGeneLabel(String label) {
		Collection<Node> genes = new HashSet<Node>();
		for (Node hs : getGeneHomolSetByGeneLabel(label)) {
			genes.addAll(getNodesInHomolSet(hs.getId()));
		}
		for (Node g : genes) {
			LinkQueryTerm oqt = new LinkQueryTerm(g.getId(), IN_ORGANISM, null);
			oqt.setInferred(false);
			Collection<LinkStatement> links = shard.getLinkStatementsByQuery(oqt);
			for (LinkStatement s : links) {
				Node sp = shard.getNode(s.getTargetId());
				s.setTargetNode(sp);
				g.addStatement(s);
			}
		}
		return genes;
	}

	/**
	 * @return all NodeSets that can be built using homologous_to links in the shard
	 */
	public Collection<NodeSet> getAllHomologySets() {
		Collection<LinkStatement> links = shard.getLinkStatementsByQuery(new LinkQueryTerm(null, HOMOLOGOUS_TO, null));
		Map<String, NodeSet> nsMap = shard.createNodeSetMap(links);
		return nsMap.values();
	}

	/**
	 * @param src  -- e.g. "fma"
	 * @return
	 */
	public Collection<LinkStatement> getAllHomologyLinks(String src) {
		LinkQueryTerm tq = new LinkQueryTerm(HOMOLOGOUS_TO,new SourceQueryTerm(src));
		tq.setInferred(false);
		Collection<LinkStatement> links = 
			shard.getLinkStatementsByQuery(tq);
		return links;
	}

	/**
	 * @return all homologous_to links in the shard
	 */
	public Collection<LinkStatement> getAllHomologyLinks() {
		LinkQueryTerm tq = new LinkQueryTerm();
		tq.setRelation(HOMOLOGOUS_TO);
		tq.setInferred(false);
		Collection<LinkStatement> links = 
			shard.getLinkStatementsByQuery(tq);
		return links;
	}
	
	public Collection<NodeSet> getNodeSets() {
		LinkedList<NodeSet> nodesets = new LinkedList<NodeSet>(getAnnotStatementsByNodeSetMap().keySet());
		//nodesets.addAll(getAnnotStatementsByNodeSetMap().keySet());
		Collections.sort(nodesets);
		return nodesets;
	}

	/**
	 * Given annotated entity (presumed to be a genotype or gene)
	 * find the corresponding organism type
	 * @param annotatedEntityId
	 * @return
	 */
	public String getSpeciesId(String annotatedEntityId) {
		for (Statement s : annotGraph.getAllLinkStatementsForNode(annotatedEntityId)) {
			if (s.getRelationId().equals(IN_ORGANISM))
				return s.getTargetId();
			if (s.getRelationId().equals(relationVocabulary.variant_of()))
				return getSpeciesId(s.getTargetId());
		}
		return null;
	}


	/**
	 * @param hset - a NodeSet consisting of like classes in the ontology
	 * @return
	 */
	public Collection<String> getSpeciesIdsAnnotatedToSet(NodeSet hset) {
		Collection<String> speciesIds = new HashSet<String>();
		for (LinkStatement annot : getAnnotStatementsByNodeSetMap().get(hset)) {
			speciesIds.add(getSpeciesId(annot.getNodeId()));
		}
		return speciesIds;
	}
	
	/**
	 * all S where ae in_organism S
	 * 
	 * note: appends a null id
	 * @return
	 */
	public Collection<String> getAllSpeciesIds() {
		Collection<String> speciesIds = new HashSet<String>();
		boolean hasNull = false;
		for (Node ae : getAnnotatedEntities()) {
			String spId = getSpeciesId(ae.getId());
			if (spId != null)
				speciesIds.add(spId);
			else
				hasNull = true;
		}
		LinkedList<String> sortedIds = new LinkedList<String>();
		sortedIds.addAll(speciesIds);
		Collections.sort(sortedIds);
		if (hasNull)
			sortedIds.add(null);
		return sortedIds;
	}
	
	public Collection<Node> getAnnotatedEntitiesBySpeciesId(String speciesId) {
		Collection<Node> aeNodes = new HashSet<Node>();
		for (Node ae : getAnnotatedEntities()) {
			if (speciesId == null) {
				if (getSpeciesId(ae.getId()) == null)
					aeNodes.add(ae);
			}
			else if (speciesId.equals(getSpeciesId(ae.getId())))
				aeNodes.add(ae);
		}
		return aeNodes;
	}
	
	public Collection<String> getSourceIdsAnnotatedToSet(NodeSet hset) {
		Collection<String> srcIds = new HashSet<String>();
		for (LinkStatement annot : getAnnotStatementsByNodeSetMap().get(hset)) {
			srcIds.add(annot.getSourceId());
		}
		return srcIds;
	}


	private Map<String,Collection<String>> qualityIdsByClassMap = 
		new HashMap<String,Collection<String>>();
	public Collection<String> getQualityIdsForAnnotationClass(String tid) {
		if (qualityIdsByClassMap.containsKey(tid))
			return qualityIdsByClassMap.get(tid);
		Collection<String> qids = new HashSet<String>();
		for (LinkStatement p : ontolGraph.getAllLinkStatementsForNode(tid)) {
			String qid = p.getTargetId();
			Node qnode = ontolGraph.getNode(qid);
			if (qnode != null && qnode.getSourceId() != null && 
					qnode.getSourceId().equals("quality"))
				qids.add(qid);
		}
		qualityIdsByClassMap.put(tid, qids);
		return qids;
	}


	public Collection<Node> getAnnotatedEntitiesByGeneHomolSet(String hsetId) {

		Collection<Node> geneNodes = 
			shard.getNodesByQuery(new LinkQueryTerm(DESCENDED_FROM,hsetId));

		Collection<Node> nodes = new HashSet<Node>();
		nodes.addAll(geneNodes);
		for (Node geneNode : geneNodes) {
			String gid = geneNode.getId();

			LinkQueryTerm vqt = 
				new LinkQueryTerm(relationVocabulary.variant_of(),gid);

			nodes.addAll(shard.getNodesByQuery(vqt));

			// TODO - ugly hack because MGI gts are linked to MGI IDs
			LinkQueryTerm xrefq = new LinkQueryTerm();
			xrefq.setRelation(vocab.HAS_DBXREF()); // TODO
			xrefq.setNode(gid);
			xrefq.setAspect(Aspect.TARGET);
			vqt = 
				new LinkQueryTerm(xrefq);
			nodes.addAll(shard.getNodesByQuery(vqt));
		}
		return nodes;
	}
	
	public boolean isAnnotatedToNodeSet(Node ae, NodeSet hset) {
		String aeId = ae.getId();
		for (LinkStatement s : annotStatementsByNodeSetMap.get(hset))
			if (aeId.equals(s.getNodeId()))
				return true;
		return false;
	}
	
	

	public Collection<LinkStatement> getAnnotStatements() {
		return annotStatements;
	}

	public void setAnnotStatements(Collection<LinkStatement> annotStatements) {
		this.annotStatements = annotStatements;
	}

	/**
	 * populates annots
	 *  
	 * @param annotatedEntities
	 * @return
	 */
	public Collection<LinkStatement> fetchAnnotationLinks(Collection<Node> annotatedEntities) {
		annotStatements = new LinkedList<LinkStatement>();
		for (Node n : annotatedEntities) {
			LinkQueryTerm qt = new LinkQueryTerm();
			qt.setInferred(false);
			qt.setPositedBy(new ExistentialQueryTerm());
			qt.setNode(n.getId());
			annotStatements.addAll(
					shard.getLinkStatementsByQuery(qt));
		}
		return annotStatements;
	}


	public Graph initializeGraphsFromAnnotations(Collection<LinkStatement> annots) {
		Collection<String> targetIds = new HashSet<String>();
		for (LinkStatement s : annots) {
			targetIds.add(s.getTargetId());
		}

		// build the main ontology graph : all annotated classes plus
		// the closure
		ontolGraph = shard.getGraphFromSeeds(targetIds, null);

		// the annotation graph should contain the annotated entities
		// plus closure over certain relations to get gene, organism etc
		annotGraph = new Graph();
		for (LinkStatement s : annots)
			annotGraph.addStatement(s);

		for (String nid : annotGraph.getSubjectIds()) {
			shard.simpleClosureOver(annotGraph,nid,aeRelIds,aeRevRelIds);
			//graph.addNode(shard.getNode(nid));

		}
		/*
		for (String nid : annotGraph.getTargetIds()) {
//			for(Statement s : shard.getLiteralStatementsByQuery(new LinkQueryTerm(nid,null,null)))
//				annotGraph.addStatement(s);
			graph.addNode(shard.getNode(nid));
		}
		 */


		return ontolGraph; // TODO - not required
	}

	public Graph fetchAnnotationGraph(String aeId) {
		shard.simpleClosureOver(annotGraph,aeId,aeRelIds,aeRevRelIds);
		return annotGraph;
	}



	private Map <NodeSet, Collection<LinkStatement>> groupByHomolSet(Collection<LinkStatement> annots, Graph g) {

		Collection<LinkStatement> homolLinks = new LinkedList<LinkStatement>();
		for (LinkStatement s : g.getLinkStatements()) {
			if (s.getRelationId().equals(HOMOLOGOUS_TO) && !s.isInferred()) {
				homolLinks.add(s);
			}
		}
		System.out.println("hlinks="+homolLinks.size());
		nodeToHomolSetMap = 
			shard.createNodeSetMap(homolLinks);

		annotStatementsByNodeSetMap = 
			new HashMap <NodeSet, Collection<LinkStatement>> ();
		for (LinkStatement annot : annots) {
			String annotatedTo = annot.getTargetId();
			System.out.println("closure for: "+annotatedTo);
			Collection<NodeSet> hsets = mapClassIdToHomolSet(annotatedTo, g);
			for (NodeSet hset : hsets) {
				if (!annotStatementsByNodeSetMap.containsKey(hset)) 
					annotStatementsByNodeSetMap.put(hset, new HashSet<LinkStatement>());
				annotStatementsByNodeSetMap.get(hset).add(annot);
				System.out.println(":: "+hset+" ++ "+annot);
				
				if (!annotStatementsByNodeSetThenQualityIdMap.containsKey(hset))
					annotStatementsByNodeSetThenQualityIdMap.put(hset, 
							new HashMap<String,Collection<LinkStatement>>());
				Map<String, Collection<LinkStatement>> annotStatementsByQualityIdMap = 
					annotStatementsByNodeSetThenQualityIdMap.get(hset);
				for (String qid : getQualityIdsForAnnotationClass(annotatedTo)) {
					if (!annotStatementsByQualityIdMap.containsKey(qid))
						annotStatementsByQualityIdMap.put(qid, new HashSet<LinkStatement>());
					annotStatementsByQualityIdMap.get(qid).add(annot);
				}		
			}
		}
		return annotStatementsByNodeSetMap; // TODO - use accessor instead
	}
	
	public Collection<LinkStatement> getAnnotStatementsByNodeSetQuality(NodeSet hset, String qid) {
		if (annotStatementsByNodeSetThenQualityIdMap.containsKey(hset)) {
			Map<String, Collection<LinkStatement>> annotStatementsByQualityIdMap = 
				annotStatementsByNodeSetThenQualityIdMap.get(hset);
			if (annotStatementsByQualityIdMap.containsKey(qid)) {
				return annotStatementsByQualityIdMap.get(qid);
			}
		}
		return new HashSet<LinkStatement>();
	}
	
	public Collection<LinkStatement> getAnnotStatementsByNodeSet(NodeSet hset) {
		return getAnnotStatementsByNodeSetMap().get(hset);
	}

	
	public Collection<String> getRelevantQualityIdsByNodeSet(NodeSet hset) {
		if (annotStatementsByNodeSetThenQualityIdMap.containsKey(hset))
			return annotStatementsByNodeSetThenQualityIdMap.get(hset).keySet();
		return new HashSet<String>();
	}

	private Map<String,Collection<NodeSet>> hsetMapCache = new HashMap<String,Collection<NodeSet>>();
	public Collection<NodeSet> mapClassIdToHomolSet(String id, Graph g) {
		if (hsetMapCache.containsKey(id))
			return hsetMapCache.get(id);

		System.out.println("calculating closure for: "+id);
		Collection<NodeSet> hsets = new HashSet<NodeSet>();
		//for (String tid : g.simpleClosure(id,2)) {
		for (LinkStatement s : g.getAllLinkStatementsForNode(id)) {
			String tid = s.getTargetId();
			if (nodeToHomolSetMap.containsKey(tid)) {
				NodeSet hset = nodeToHomolSetMap.get(tid);
				if (hset != null) {
					hsets.add(hset);
				}
			}
		}
		hsetMapCache.put(id,hsets);
		return hsets;
	}

	public Graph getOntolGraph() {
		return ontolGraph;
	}

	public void setOntolGraph(Graph graph) {
		this.ontolGraph = graph;
	}

	public Graph getAnnotGraph() {
		return annotGraph;
	}

	public void setAnnotGraph(Graph annotGraph) {
		this.annotGraph = annotGraph;
	}

	public Map<NodeSet, Collection<LinkStatement>> getAnnotStatementsByNodeSetMap() {
		return annotStatementsByNodeSetMap;
	}

	public void setAnnotStatementsByNodeSetMap(
			Map<NodeSet, Collection<LinkStatement>> annotsByNodeSet) {
		this.annotStatementsByNodeSetMap = annotsByNodeSet;
	}

	public Collection<Node> getAnnotatedEntities() {
		return annotatedEntities;
	}

	public void setAnnotatedEntities(Collection<Node> annotatedEntities) {
		this.annotatedEntities = annotatedEntities;
	}
	
	public Logger getLogger() {
		if (logger == null)
			logger = Logger.getLogger("org.obd.bio");
		return logger;
	}
	
	




}
