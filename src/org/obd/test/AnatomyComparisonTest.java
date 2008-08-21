package org.obd.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.NodeSet;
import org.obd.model.Statement;
import org.obd.model.vocabulary.TermVocabulary;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.SourceQueryTerm;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.QueryTerm.Aspect;
import org.purl.obo.vocab.RelationVocabulary;

/**
 * Tests annotation metadata requirements are fulfilled
 * 
 * Annotation metadata is modeled as sub-statements on a statement.
 * See {@link Statement#getSubStatements}
 * 
 * @author cjm
 *
 */
public class AnatomyComparisonTest extends AbstractOBDTest {

	protected TermVocabulary termVocabulary = new TermVocabulary();
	protected RelationVocabulary relationVocabulary = new RelationVocabulary();

	String DESCENDED_FROM = "OBO_REL:descended_from"; // TODO
	String IN_ORGANISM = "OBO_REL:in_organism"; // TODO
	String HOMOLOGOUS_TO = "OBO_REL:homologous_to"; // TODO

	public AnatomyComparisonTest(String n) {
		super(n);
	}
	


	
	public void addAnnotsToOntologySet(String entityId, 
			Map<NodeSet,Collection<LinkStatement>> annotsByHomolSet,
			Map<String,NodeSet> nodeToHomolSetMap,
			QueryTerm ontolQt) {

		AnnotationLinkQueryTerm aqt = new AnnotationLinkQueryTerm();
		aqt.setNode(entityId);
		aqt.setTarget(ontolQt);
		Map<String, Collection<LinkStatement>> annotMap = shard.getAnnotationStatementMapByQuery(aqt);

		System.out.println("got results for: "+entityId);

		for (String k : annotMap.keySet()) {
			if (k==null)
				continue;
			NodeSet ns = nodeToHomolSetMap.get(k);
			System.out.println("NS="+ns+" k="+k);
			if (ns == null)
				continue;
			for (Statement s : annotMap.get(k))
				System.out.println("  :: "+s);
			if (annotsByHomolSet.containsKey(ns))
				annotsByHomolSet.get(ns).addAll(annotMap.get(k));
			else
				annotsByHomolSet.put(ns,annotMap.get(k));
		}


	}
	
	public Collection<Node> getAnnotatedEntitiesByHomolSet(String hsetId) {
		
		LinkQueryTerm aqt = new LinkQueryTerm();
		aqt.setNode(new LinkQueryTerm(relationVocabulary.variant_of(),
				new LinkQueryTerm(DESCENDED_FROM, hsetId)));
		Collection<Node> nodes = shard.getNodesByQuery(aqt);
		
		// TODO - ugly hack because MGI gts are linked to MGI IDs
 		LinkQueryTerm aqt2 = new LinkQueryTerm();
		LinkQueryTerm xrefq = new LinkQueryTerm();
		xrefq.setRelation("oboMetaModel:xref"); // TODO
		xrefq.setNode(new LinkQueryTerm(DESCENDED_FROM, hsetId));
		xrefq.setAspect(Aspect.TARGET);
		aqt2.setNode(new LinkQueryTerm(relationVocabulary.variant_of(),
				xrefq));
		nodes.addAll(shard.getNodesByQuery(aqt2));

		return nodes;
	}
	
	public void test() {

		// every anatomy term homologous to something in FMA
		LinkQueryTerm qt = new LinkQueryTerm(HOMOLOGOUS_TO,new SourceQueryTerm("fma"));
		qt.setInferred(false); // TODO - should not be necessary
		qt.setQueryAlias("homolTo");
		Collection<LinkStatement> homolLinks = shard.getLinkStatementsByQuery(qt);
		for (LinkStatement s : homolLinks)
			System.out.println(s);

		// create homology set groupings from pairwise homologies
		Map<String, NodeSet> nodeToHomolSetMap = 
			shard.createNodeSetMap(homolLinks);
		for (String nid : nodeToHomolSetMap.keySet()) {
			System.out.println(nid+" "+nodeToHomolSetMap.get(nid));
			for (Node n : nodeToHomolSetMap.get(nid).getNodes()) {
				System.out.println(" :: "+n);
			}
		}

		// get a homolset via a gene
		String geneName = "SOX10";
		LinkQueryTerm dqt = new LinkQueryTerm();
		dqt.setNode(new LabelQueryTerm(AliasType.PRIMARY_NAME,geneName));
		dqt.setRelation(DESCENDED_FROM);
		dqt.setAspect(Aspect.TARGET);
		Collection<Node> hsetNodes = shard.getNodesByQuery(dqt);

		LinkQueryTerm hqtInner = new LinkQueryTerm(HOMOLOGOUS_TO,(QueryTerm)null);
		hqtInner.setInferred(false);
		
		LinkQueryTerm hqt1 = 
			new LinkQueryTerm(hqtInner);
		hqt1.setAspect(Aspect.TARGET);

		LinkQueryTerm hqt2 = 
			new LinkQueryTerm(hqtInner);
		hqt2.getTarget().setAspect(Aspect.TARGET);
		hqt2.setAspect(Aspect.TARGET);


		for (Node hn : hsetNodes) {
			System.out.println(hn);
			Collection<Node> nodes = getAnnotatedEntitiesByHomolSet(hn.getId());

			// initialize the mapping; all annotations to genes in this
			// gene homology set will be collected here
			Map<NodeSet,Collection<LinkStatement>> annotsByHomolSet =
				new HashMap<NodeSet,Collection<LinkStatement>>();

			for (Node n : nodes) {
				addAnnotsToOntologySet(n.getId(),
						annotsByHomolSet,
						nodeToHomolSetMap,
						hqt1);
				addAnnotsToOntologySet(n.getId(),
						annotsByHomolSet,
						nodeToHomolSetMap,
						hqt2);
			}
			showAnnotMap(annotsByHomolSet);
		}

	}

	public void showAnnotMap(Map<NodeSet,Collection<LinkStatement>> annotsByHomolSet) {
		for (NodeSet hset : annotsByHomolSet.keySet()) {
			System.out.println("  "+hset);
			for (LinkStatement s : annotsByHomolSet.get(hset)) {
				System.out.println("    "+s);
			}
		}
	}
	
	/*
	public void test() {
		LinkQueryTerm qt = new LinkQueryTerm(HOMOLOGOUS_TO,new SourceQueryTerm("fma"));
		qt.setInferred(false); // TODO - should not be necessary
		qt.setQueryAlias("homolTo");
		Collection<LinkStatement> homolLinks = shard.getLinkStatementsByQuery(qt);
		for (LinkStatement s : homolLinks)
			System.out.println(s);
		
		// create homology set groupings from pairwise homologies
		 Map<String, NodeSet> nodeToHomolSetMap = 
			 shard.createNodeSetMap(homolLinks);
		 for (String nid : nodeToHomolSetMap.keySet()) {
			 System.out.println(nid+" "+nodeToHomolSetMap.get(nid));
			 for (Node n : nodeToHomolSetMap.get(nid).getNodes()) {
				 System.out.println(" :: "+n);
			 }
		 }
		 
		String geneName = "SOX10";
		LinkQueryTerm dqt = new LinkQueryTerm();
		dqt.setNode(new LabelQueryTerm(AliasType.PRIMARY_NAME,geneName));
		dqt.setRelation(DESCENDED_FROM);
		dqt.setAspect(Aspect.TARGET);
		Collection<Node> hsetNodes = shard.getNodesByQuery(dqt);
		
		for (Node hn : hsetNodes) {
			System.out.println(hn);
			Map<NodeSet,Collection<LinkStatement>> annotsByHomolSet =
				compareHset(hn.getId(), nodeToHomolSetMap);
			for (NodeSet hset : annotsByHomolSet.keySet()) {
				System.out.println("  "+hset);
				for (LinkStatement s : annotsByHomolSet.get(hset)) {
					System.out.println("    "+s);
				}
			}
		}
		
	}
	
	public Map<NodeSet,Collection<LinkStatement>> compareHset(String hsetId, Map<String, NodeSet> nodeToHomolSetMap) {

		AnnotationLinkQueryTerm aqt = new AnnotationLinkQueryTerm();
		aqt.setNode(new LinkQueryTerm(relationVocabulary.variant_of(),
				new LinkQueryTerm(DESCENDED_FROM, hsetId)));
		Collection<LinkStatement> annots = shard.getLinkStatementsByQuery(aqt);
		
		// TODO - ugly hack because MGI gts are linked to MGI IDs
 		AnnotationLinkQueryTerm aqt2 = new AnnotationLinkQueryTerm();
		LinkQueryTerm xrefq = new LinkQueryTerm();
		xrefq.setRelation("oboMetaModel:xref"); // TODO
		xrefq.setNode(new LinkQueryTerm(DESCENDED_FROM, hsetId));
		xrefq.setAspect(Aspect.TARGET);
		aqt2.setNode(new LinkQueryTerm(relationVocabulary.variant_of(),
				xrefq));
		annots.addAll(shard.getLinkStatementsByQuery(aqt2));
		
		Map<NodeSet,Collection<LinkStatement>> annotsByHomolSet =
			new HashMap<NodeSet,Collection<LinkStatement>>();
		for (LinkStatement annot : annots) {
			// this is inefficient
			// we want to get the mapping at initial query time
			// OR pre-load the mapping
			//
			// also: does not get full links...? need to explicitly include parts
			// gt infl p inh e po e2 homol
			LinkQueryTerm hqt1 = 
				new LinkQueryTerm(new LinkQueryTerm(HOMOLOGOUS_TO,(QueryTerm)null));
			hqt1.setNode(annot.getTargetId());
			hqt1.setAspect(Aspect.TARGET);
			Collection<Node> mappedNodes = shard.getNodesByQuery(hqt1);

			// do the reverse homology link - TODO - should not be necessary
			LinkQueryTerm hqt2 = 
				new LinkQueryTerm(new LinkQueryTerm(HOMOLOGOUS_TO,(QueryTerm)null));
			hqt2.setNode(annot.getTargetId());
			hqt2.getTarget().setAspect(Aspect.TARGET);
			hqt2.setAspect(Aspect.TARGET);
			mappedNodes.addAll(shard.getNodesByQuery(hqt2));

			// add annotation links to the appropriate anat-homolset
			// note that annotations can be present in multiple hsets
			for (Node mappedNode : mappedNodes) {
				NodeSet hset = nodeToHomolSetMap.get(mappedNode.getId());
				if (!annotsByHomolSet.containsKey(hset))
					annotsByHomolSet.put(hset, new HashSet<LinkStatement>());
				annotsByHomolSet.get(hset).add(annot);
			}
		}
		return annotsByHomolSet;

	}*/


}
