package org.obd.test;

import java.util.HashMap;
import java.util.Map;

import org.obd.model.Node;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.exception.ShardExecutionException;

/**
 * Tests {@link org.obd.query.Shard#getAnnotatedEntityCountBySubset} - quantifying annotations using
 * ontology subsets.
 * <p>
 * Requirement: given a subset or view of an ontology (e.g. "organs" in FMA)
 * map all annotations to classes in that subset (using the deductive closure
 * over some or all relations) and return a summary showing the number of
 * distinct entities annotated to the mapped class.
 * <p>
 * This tests gives the summary for all annotations in the shard to 
 * a test subset of FMA.
 * <p>
 * The test database used here contains NCBO OMIM annotations. These
 * are genotype-phenotype annotations, where the phenotype is represented
 * using a class expression of the form:
 * <code>
 * 	  <Quality> THAT inheres_in (some) <BearerEntity>
 * </code>
 * Where BearerEntity may be some kind of anatomical entity; for example:
 * <code>
 *    PATO:Thickness THAT inheres_in (some) Wall-of-eyeball
 * </code>
 * <p>
 * If Wall-of-eyeball is partOf Eyeball, and Eyeball is in the organ subset,
 * then an annotation to this class expression will be counted
 * <p>
 * The tests here involve the logic of the following classes/methods:
 * <ul>
 *  <li>{@link org.obd.query.Shard#getAnnotatedEntityCountBySubset}
 *  <li>{@link org.obd.query.impl.OBDSQLShard#getAnnotatedEntityCountBySubset}
 * </ul>
 * It also makes use of:
 * <ul>
 *  <li>{@link org.obd.query.AnnotationLinkQueryTerm}
 *  <li>{@link org.obd.query.SubsetQueryTerm}
 * </ul>
 * @author cjm
 *
 */
public class MapToSubsetCountTest extends AbstractOBDTest {

	public MapToSubsetCountTest(String n) {
		super(n);
	}

	/**
	 * tests for quantification over all annotations in shard
	 * @throws ShardExecutionException 
	 */
	public void testGetAnnotationCount() throws ShardExecutionException {

		String subsetId = "fma_organ_slim";

		AnnotationLinkQueryTerm qt = new AnnotationLinkQueryTerm();
		Map<Node, Integer> cmap = shard.getAnnotatedEntityCountBySubset(qt, subsetId);
		boolean ok = false;
		for (Node n : cmap.keySet()) {
			System.out.println(n+" COUNT: "+cmap.get(n));
			if (n.getId().equals("FMA:7203"))
				ok = cmap.get(n) > 0;
		}
		assertTrue(ok);
	}

	/**
	 * As {@link #testGetAnnotationCount()}, broken down by annotation source.
	 * This test provides a summary broken down by the 3 annotation sources
	 * for OMIM annotation
	 * @throws ShardExecutionException 
	 */
	public void testGetAnnotationCountByAnnotationSource() throws ShardExecutionException {

		String subsetId = "fma_organ_slim";
		String[] sources = {"omim_phenotype_zfin","omim_phenotype_fb", "omim_phenotype_bbop"};

		AnnotationLinkQueryTerm qt = new AnnotationLinkQueryTerm();
		Map<Node, Map<String,Integer>> smapByClass = new HashMap<Node, Map<String,Integer>>();
		for (String source : sources) {
			qt.setSource(source);
			Map<Node, Integer> cmap = shard.getAnnotatedEntityCountBySubset(qt, subsetId);
			for (Node n : cmap.keySet()) {
				if (!smapByClass.containsKey(n))
					smapByClass.put(n, new HashMap<String,Integer>());
				smapByClass.get(n).put(source, cmap.get(n));
			}
		}
		for (Node n : smapByClass.keySet()) {
			System.out.println(n);
			Map<String, Integer> smap = smapByClass.get(n);
			for (String source : smap.keySet()) {
				System.out.println("  "+source+" : "+smap.get(source));

			}
		}	
	}


}
