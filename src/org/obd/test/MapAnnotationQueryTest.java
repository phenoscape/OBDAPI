package org.obd.test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.obd.model.LinkStatement;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.purl.obo.vocab.RelationVocabulary;

/**
 * Tests for queries over phenotypes
 * @author cjm
 *
 */
public class MapAnnotationQueryTest extends AbstractOBDTest {

	RelationVocabulary rv = new RelationVocabulary();
	
	public MapAnnotationQueryTest(String n) {
		super(n);
	}

	
	/**
	 * map an ID to slims
	 */
	
	public void testMapIdPATO() {

		String id = "PATO:0000591";
		String subsetId = "attribute_slim";

		Set<String> nids = shard.mapNodeToSubset(id, subsetId);
		for (String nid: nids)
			System.out.println(id+" -> "+nid);
		assertTrue(nids.size() == 1);
	}

	public void testMapIdFMA() {

		String id = "FMA:58098"; // Wall of eyeball
		String subsetId = "fma_organ_slim";

		Set<String> nids = shard.mapNodeToSubset(id, subsetId);
		for (String nid: nids)
			System.out.println(id+" -> "+nid);
		assertTrue(nids.size() == 1);
		assertTrue(nids.iterator().next().equals("FMA:12513")); // Eyeball
	}
	 
	String HOMOLOGOUS_TO = "OBO_REL:homologous_to"; // TODO
	public void testMapAnnotations() {
		//LinkQueryTerm qt = new LinkQueryTerm();
		LinkQueryTerm qt = new AnnotationLinkQueryTerm();
		qt.setNode("ZFIN:ZDB-GENO-980202-1565");
		LinkQueryTerm hqtInner = new LinkQueryTerm(HOMOLOGOUS_TO,(QueryTerm)null);
		hqtInner.setInferred(false);
		qt.setTarget(hqtInner);
		//qt.setTarget(new LinkQueryTerm("PATO:0000628"));
		//qt.setTarget("PATO:0000628");
		//qt.setTarget(new LinkQueryTerm(new LinkQueryTerm(HOMOLOGOUS_TO,(QueryTerm)null)));
		//for (Statement s : shard.getStatementsByQuery(qt))
		//	System.out.println(s);
		Map<String, Collection<LinkStatement>> map = shard.getAnnotationStatementMapByQuery(qt);
		for (String mapped : map.keySet()) {
			System.out.println(mapped);
			for (LinkStatement s : map.get(mapped)) {
				System.out.println("  "+s);
			}
		}
		assertTrue(map.keySet().size() > 0);
	}

	/*public void testMapAnnotations() {

		String subsetId = "fma_organ_slim";

		AnnotationLinkQueryTerm qt = new AnnotationLinkQueryTerm();
		qt.setSource("omim_phenotype_zfin");
		qt.setNode("OMIM:601653");
		LinkQueryTerm sqt = new LinkQueryTerm(subsetId);
		qt.setTarget(sqt);
		Collection<Statement> annots = shard.getStatementsByQuery(qt);
		System.out.println("n annots:"+annots.size());
		for (Statement annot : annots) {
			String tid = annot.getTargetId();
			System.out.println("mapping "+tid);
			Set<String> mappedIds = shard.mapNodeToSubset(tid, subsetId);
			for (String mappedId : mappedIds) {
				System.out.println("mapped to "+mappedId);
			}
		}
	}
	 */


	

}
