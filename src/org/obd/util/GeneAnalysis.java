package org.obd.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import org.obd.bio.GeneFactory;
import org.obd.model.CompositionalDescription;
import org.obd.model.HomologyView;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.rule.InferenceRule;
import org.obd.model.rule.RelationCompositionRule;
import org.obd.model.stats.ScoredNode;
import org.obd.model.stats.SimilarityPair;
import org.obd.model.vocabulary.TermVocabulary;
import org.obd.query.BooleanQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.impl.OBDSQLShard;
import org.purl.obo.vocab.RelationVocabulary;

/**
 * Tests for capability to retrieve class expressions from a {@link org.obd.model.Shard}
 * @see CompositionalDescription
 * @author cjm
 *
 */
public class GeneAnalysis extends CommandLineTool  {


	GeneFactory geneFactory ;

	public Shard getShard() {
		return shard;
	}

	public void setShard(Shard shard) {
		this.shard = shard;
	}



	public static void main(String[] args) throws Exception {

		String jdbcPath = defaultJdbcPath;

		String cmd = "";
		String taxonId = "NCBITaxon:7955";
		//String taxonId = "NCBITaxon:9606";
		for (int i = 0; i < args.length; i++)
			System.err.println("args[" + i + "] = |" + args[i] + "|");

		Collection<String> files = new LinkedList<String>();
		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-d")) {
				i++;
				jdbcPath = args[i];
			}
			else if (args[i].equals("-x")) {
				i++;
				cmd = args[i];
			}
			else if (args[i].equals("-t") || args[i].equals("--taxon")) {
				i++;
				taxonId = args[i];
			}
			else {
				files.add(args[i]);
			}
		}

		GeneAnalysis ph = new GeneAnalysis();
		//ph.initLogger(Level.FINEST);

		try {

			OBDSQLShard obd = new OBDSQLShard();
			obd.connect(jdbcPath);
			ph.shard = obd;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ph.performAllByAllGenes(taxonId);
	}

	public void performAllByAllGenes(String taxonId) {

		geneFactory = new GeneFactory(shard);
		Vector<Node> genes = new Vector(geneFactory.getAllGenesByTaxon(taxonId));
		System.out.println("genes:");
		for (int i=0; i<genes.size(); i++) {
			for (int j=i+1; j<genes.size(); j++) {
				Node g1 = genes.elementAt(i);
				Node g2 = genes.elementAt(j);
				String uid1 = g1.getId();
				String uid2 = g2.getId();

				SimilarityPair sp = this.shard.compareAnnotationsByAnnotatedEntityPair(uid1,uid2);
				this.shard.calculateInformationContentMetrics(sp);

				System.out.println(g1+"\t"+g2+"\t"+sp.getNodesInCommon().size()+"\t"+
						sp.getNonRedundantNodesInCommon().size()+"\t"+
						sp.getPostPartitionedNonRedundantNodesInUnion().size()+"\t"+
						sp.getBasicSimilarityScore() + "\t" +
						sp.getInformationContentRatio());

			}
		}
	}

	public void performAllByAllGenesQuick(String taxonId, String src) {

		geneFactory = new GeneFactory(shard);
		Vector<Node> genes = new Vector(geneFactory.getAllGenesByTaxon(taxonId));
		System.out.println("genes:");
		for (int i=0; i<genes.size(); i++) {
			Node g1 = genes.elementAt(i);
			String uid1 = g1.getId();
			List<ScoredNode> sns = shard.getSimilarNodes(uid1, src);
			for (ScoredNode sn : sns) {
				String uid2 = sn.getNodeId();
				Node g2 = shard.getNode(uid2);
				System.out.println(g1+"\t"+g2+"\t"+sn.getScore());

				/*
				SimilarityPair sp = this.shard.compareAnnotationsByAnnotatedEntityPair(uid1,uid2);
				this.shard.calculateInformationContentMetrics(sp);
				System.out.println(g1+"\t"+g2+"\t"+sp.getNodesInCommon().size()+"\t"+
				sp.getNonRedundantNodesInCommon().size()+"\t"+
				sp.getPostPartitionedNonRedundantNodesInUnion().size()+"\t"+
				sp.getBasicSimilarityScore() + "\t" +
				sp.getSimilarityByInformationContentRatio());
				 */

			}


		}

	}
	public void performAllByAllHomologs(String taxonId, String src) {

		geneFactory = new GeneFactory(shard);
		Vector<Node> genes = new Vector(geneFactory.getAllGenesByTaxon(taxonId));
		System.out.println("genes:");
		for (int i=0; i<genes.size(); i++) {
			Node g1 = genes.elementAt(i);
			String uid1 = g1.getId();
			//qt = new LinkQueryTerm()
			List<ScoredNode> sns = shard.getSimilarNodes(uid1, src);
			for (ScoredNode sn : sns) {
				String uid2 = sn.getNodeId();
				Node g2 = shard.getNode(uid2);
				System.out.println(g1+"\t"+g2+"\t"+sn.getScore());

				/*
				SimilarityPair sp = this.shard.compareAnnotationsByAnnotatedEntityPair(uid1,uid2);
				this.shard.calculateInformationContentMetrics(sp);
				System.out.println(g1+"\t"+g2+"\t"+sp.getNodesInCommon().size()+"\t"+
				sp.getNonRedundantNodesInCommon().size()+"\t"+
				sp.getPostPartitionedNonRedundantNodesInUnion().size()+"\t"+
				sp.getBasicSimilarityScore() + "\t" +
				sp.getSimilarityByInformationContentRatio());
				 */

			}


		}

	}

	public void showPairwiseSimilarGenesByHomolog(String label) {
		HomologyView hv = new HomologyView(shard);

		Vector<Node> genes = new Vector(hv.getHomologousGenesByGeneLabel(label));

		for (int i=0; i<genes.size(); i++) {
			Node g1 = genes.elementAt(i);
			if (g1.getLabel() == null)
				continue;
			for (int j=i+1; j<genes.size(); j++) {
				Node g2 = genes.elementAt(j);
				if (g2.getLabel() == null)
					continue;
				writeSimilarityRow(g1,g2);
			}
		}

	}

	public void showSimilarGenes(String uid1, String src) {
		Node n1 = shard.getNode(uid1);
		showSimilarGenes(n1, src);
	}
	public void showSimilarGenesByLabel(String label, String src) throws Exception {
		for (Node n1 : shard.getNodesBySearch(label, Operator.EQUAL_TO))
			showSimilarGenes(n1, src);
	}
	public void showSimilarGenesByLabels(String label1, String label2) throws Exception {
		for (Node n1 : shard.getNodesBySearch(label1, Operator.EQUAL_TO))
			for (Node n2 : shard.getNodesBySearch(label2, Operator.EQUAL_TO))
				writeSimilarityRow(n1, n2);
	}
	public void showSimilarGenes(Node n1, String src) {

		List<ScoredNode> nodes = shard.getSimilarNodes(n1.getId(), src);

		int num = 0;
		for (ScoredNode sn : nodes) {
			num++;
			String uid2 = sn.getNodeId();
			Node n2 = shard.getNode(uid2);
			writeSimilarityRow(n1,n2);
			if (num > 20)
				break;
		}
	}
	public void writeSimilarityRow(Node n1, Node n2) {
		String uid1 = n1.getId();
		String uid2 = n2.getId();
		SimilarityPair sp = shard.compareAnnotationsByAnnotatedEntityPair(uid1,uid2);
		this.shard.calculateInformationContentMetrics(sp);

		System.out.println(n2s(n1)+"\t"+n2s(n2)+"\t"+sp.getNodesInCommon().size()+"\t"+
				sp.getNonRedundantNodesInCommon().size()+"\t"+
				sp.getPostPartitionedNonRedundantNodesInUnion().size()+"\t"+
				sp.getBasicSimilarityScore() + "\t" +
				sp.getInformationContentRatio());
	}

	public String n2s(Node n) {
		Node[] orgs = n.getTargetNodes(IN_ORGANISM);
		if (orgs.length > 0)
			return n+" "+orgs[0].getLabel();
		else
			return n.toString();

	}


}
