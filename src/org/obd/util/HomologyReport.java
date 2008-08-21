package org.obd.util;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.obd.model.Graph;
import org.obd.model.HomologyView;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.NodeSet;
import org.obd.model.Statement;
import org.obd.model.vocabulary.TermVocabulary;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.ExistentialQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.QueryTerm.Aspect;
import org.obd.query.impl.OBDSQLShard;
import org.purl.obo.vocab.RelationVocabulary;

/**
 * Generates a report broken down by homolset
 * 
 * @author cjm
 *
 */
public class HomologyReport  {

	protected TermVocabulary termVocabulary = new TermVocabulary();
	protected RelationVocabulary relationVocabulary = new RelationVocabulary();
	static String jdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_all";
	static String dbUsername;
	static String dbPassword;
	protected Shard shard;

	protected HomologyView hv;


	public HomologyReport() throws SQLException, ClassNotFoundException {
		super();
		OBDSQLShard obd = new OBDSQLShard();
		obd.connect(jdbcPath,dbUsername,dbPassword);
		shard = obd;
		hv = new HomologyView(shard);
	}

	public static void main(String[] args) {
		HomologyReport hr;
		String geneLabel = "EXT2";
		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-d")) {
				i++;
				jdbcPath = args[i];
			}
			else {
				geneLabel = args[i];
			}
		}

		try {
			hr = new HomologyReport();
			hr.launch(geneLabel);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void launch(String geneLabel) {
		System.out.println("====================");
		System.out.println("GENE: "+geneLabel);
		System.out.println("====================");
		Collection<Node> hsets = hv.getGeneHomolSetByGeneLabel(geneLabel);

		for (Node hset : hsets)
			System.out.println("got: "+hset);
		for (Node hset : hsets)
			this.reportByHomolSetId(hset.getId());
	}

	public void reportByHomolSetId(String geneHsetId) {

		System.out.println("====================");
		System.out.println("HOMOLSET: "+geneHsetId);
		System.out.println("====================");
		
		hv.initializeFromGeneHomolSet(geneHsetId);

		
		Graph g = hv.getOntolGraph();
		Graph annotGraph = hv.getAnnotGraph();
		
	
		Map<NodeSet, Collection<LinkStatement>> amap = hv.getAnnotStatementsByNodeSetMap();
		for (NodeSet hset : amap.keySet()) {
			System.out.println("--------------------");
			System.out.println(hset);
			System.out.println("--------------------");
			for (String src : hv.getSourceIdsAnnotatedToSet(hset)) {
				System.out.println("** Annotation sources: "+src);
			}
			for (String speciesId : hv.getSpeciesIdsAnnotatedToSet(hset)) {
				System.out.println("** Species annotated: "+speciesId+" "+annotGraph.getNode(speciesId));
			}
			System.out.println("** num annots: "+amap.get(hset).size());
			for (LinkStatement annot : amap.get(hset)) {
				String nid = annot.getNodeId();
				String tid = annot.getTargetId();
				/*
				for (String qid : hv.getQualityIdsForAnnotationClass(tid)) {
					if (!annotStatementsByQualityIdMap.containsKey(qid))
						annotStatementsByQualityIdMap.put(qid, new HashSet<LinkStatement>());
					annotStatementsByQualityIdMap.get(qid).add(annot);

				}
				*/
				System.out.println("  SUBJ: "+annotGraph.getNode(nid).getLabel());
				System.out.println("  TARG: "+g.getNode(tid).getLabel());
				System.out.println("   SRC: "+annot.getSourceId());
				System.out.println("  ANNO: "+annot);
				System.out.println("  ");
			}
			System.out.println("  ~~~~~~~~~~~~~~~~~~");
			System.out.println("  Breakdown by PATO:");
			System.out.println("  ~~~~~~~~~~~~~~~~~~");
			for (String qid : hv.getRelevantQualityIdsByNodeSet(hset)) {
				System.out.println("  PATO:"+g.getNode(qid).getLabel()+ " // all for "+hset);
				for (LinkStatement annot : hv.getAnnotStatementsByNodeSetQuality(hset,qid)) {
					//for (String qid : annotStatementsByQualityIdMap.keySet()) {
					//for (LinkStatement annot : annotStatementsByQualityIdMap.get(qid)) {
					String nid = annot.getNodeId();
					String tid = annot.getTargetId();
					System.out.println("    SUBJ: "+annotGraph.getNode(nid).getLabel());
					System.out.println("    TARG: "+g.getNode(tid).getLabel());
					System.out.println("    SRC: "+annot.getSourceId());
					System.out.println("    ANNO: "+annot);
					System.out.println("    ");

				}
			}

		}
	}		


}
