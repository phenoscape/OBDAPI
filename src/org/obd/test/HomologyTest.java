package org.obd.test;

import org.obd.model.HomologyView;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.vocabulary.TermVocabulary;
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
public class HomologyTest extends AbstractOBDTest {

	protected TermVocabulary termVocabulary = new TermVocabulary();
	protected RelationVocabulary relationVocabulary = new RelationVocabulary();

	protected HomologyView hv = new HomologyView();
	
	String DESCENDED_FROM = "OBO_REL:descended_from"; // TODO
	String IN_ORGANISM = "OBO_REL:in_organism"; // TODO

	public HomologyTest(String n) {
		super(n);
	}

	public void testGeneHomolSet() {
		hv.setShard(shard);
		String hsetId = "NCBIHomologene:294";
		for (Node n : hv.getAnnotatedEntitiesByGeneHomolSet(hsetId)) {
			System.out.println(n);
		}
	}
	
	/*
	public void test() {
		Map<String,Node> nodeMap = new HashMap<String,Node>();
		hv.setShard(shard);
		Collection<Node> nodes = hv.getAnnotatedEntitiesByHomolSet("NCBIHomologene:5055");
		for (Node n : nodes)
			System.out.println(n);
		Collection<LinkStatement> annots = hv.fetchAnnotationLinks(nodes);
		Graph g = hv.initializeGraphsFromAnnotations(annots);
		Graph annotGraph = new Graph();
		for (LinkStatement s : annots)
			annotGraph.addStatement(s);
		for (String nid : annotGraph.getSubjectIds()) {
			annotGraph.addStatements(shard.getStatementsForNode(nid));
			nodeMap.put(nid,shard.getNode(nid));
		}
		for (String nid : g.getReferencedNodeIds()) {
			nodeMap.put(nid,shard.getNode(nid));
		}
		for (String nid : annotGraph.getTargetIds()) {
			for(Statement s : shard.getLiteralStatementsByQuery(new LinkQueryTerm(nid,null,null)))
				annotGraph.addStatement(s);
			nodeMap.put(nid,shard.getNode(nid));
		}
		for (Statement s : g.getAllStatements())
			System.out.println(s);
		System.out.println(g.getAllStatements().length);
		g.storeSimpleClosure(2); // extend
		System.out.println("extended="+g.getAllStatements().length);

		//Map<NodeSet, Collection<LinkStatement>> amap = hv.groupByHomolSet(annots, g);
		Map<NodeSet, Collection<LinkStatement>> amap = hv.getAnnotStatementsByNodeSetMap();
		for (NodeSet hset : amap.keySet()) {
			System.out.println("--------------------");
			System.out.println(hset);
			System.out.println("--------------------");
			Map<String,Collection<LinkStatement>> annotByQuality = 
				new HashMap<String,Collection<LinkStatement>>();
			for (LinkStatement annot : amap.get(hset)) {
				String nid = annot.getNodeId();
				String tid = annot.getTargetId();
				for (LinkStatement p : g.getAllLinkStatementsForNode(tid)) {
					String qid = p.getTargetId();
					if (qid.startsWith("PATO")) {
						if (!annotByQuality.containsKey(qid))
							annotByQuality.put(qid, new HashSet<LinkStatement>());
						annotByQuality.get(qid).add(annot);
					}
				}
				System.out.println("  SUBJ:"+nodeMap.get(nid).getLabel());
				System.out.println("  TARG:"+nodeMap.get(tid).getLabel());
				System.out.println("  ANNO:"+annot);
				System.out.println("  ");
			}
			System.out.println("  Breakdown by PATO:");
			for (String qid : annotByQuality.keySet()) {
				System.out.println("  PATO:"+nodeMap.get(qid).getLabel());
				for (LinkStatement annot : annotByQuality.get(qid)) {
					String nid = annot.getNodeId();
					String tid = annot.getTargetId();
					System.out.println("    SUBJ:"+nodeMap.get(nid).getLabel());
					System.out.println("    TARG:"+nodeMap.get(tid).getLabel());
					System.out.println("    ANNO:"+annot);
					System.out.println("    ");
					
				}
			}

		}
	}		

	public void testFetchHumanFishHomologyLinksByDescent() {

		LinkQueryTerm invDescQt = new LinkQueryTerm();
		invDescQt.setRelation(DESCENDED_FROM);
		invDescQt.setQueryAlias("with_descendant");
		invDescQt.setNode(new LinkQueryTerm(IN_ORGANISM,"NCBITaxon:9606")); // TODO
		//invDescQt.setNode(new LinkQueryTerm("NCBITaxon:9606")); // TODO
		invDescQt.setAspect(Aspect.TARGET); 

		LinkQueryTerm descQt = new LinkQueryTerm();
		descQt.setRelation(DESCENDED_FROM);
		descQt.setQueryAlias("descended_from");
		descQt.setNode(new LinkQueryTerm(IN_ORGANISM,"NCBITaxon:7955")); // TODO
		//descQt.setNode(new LinkQueryTerm("NCBITaxon:7955")); // TODO

		descQt.setTarget(invDescQt);

		int n=0;
		for (Statement s : shard.getLinkStatementsByQuery(descQt)) {
			System.out.println(s);
			n++;
		}
		assertTrue(n>0);
	}
	public void testFetchHomologyLinks() {


		LinkQueryTerm qt = new LinkQueryTerm("OBO_REL:homologous_to",
				new LabelQueryTerm(AliasType.ANY_LABEL,
						"SOX",
						Operator.STARTS_WITH));
		int n=0;
		for (Statement s : shard.getLinkStatementsByQuery(qt)) {
			System.out.println(s);
			n++;
		}
		assertTrue(n>0);
	}
	
	public void testFetchHomologyLinksByDescent() {

		QueryTerm geneQt = 
			new LabelQueryTerm(AliasType.ANY_LABEL,
					"SOX10",
					Operator.EQUAL_TO);
		geneQt.setQueryAlias("gene_match");

		LinkQueryTerm invDescQt = new LinkQueryTerm();
		invDescQt.setRelation(DESCENDED_FROM);
		invDescQt.setQueryAlias("with_descendant");
		invDescQt.setAspect(Aspect.TARGET);
		invDescQt.setNode(geneQt);

		LinkQueryTerm descQt = new LinkQueryTerm(DESCENDED_FROM, invDescQt);
		descQt.setQueryAlias("descended_from");

		int n=0;
		for (Statement s : shard.getLinkStatementsByQuery(descQt)) {
			System.out.println(s);
			n++;
		}
		assertTrue(n>0);
	}

*/



}
