package org.obd.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.obd.model.CompositionalDescription;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.rule.InferenceRule;
import org.obd.model.rule.RelationCompositionRule;
import org.obd.model.vocabulary.TermVocabulary;
import org.obd.query.LabelQueryTerm;
import org.obd.query.LinkQueryTerm;
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
public class PhenotypeHelper  {

	private Shard shard;
	static String defaultJdbcPath = "jdbc:postgresql://localhost:5432/obdtest";
	String dbUsername = "cjm";
	String dbPassword = "";
	protected RelationVocabulary relationVocabulary = new RelationVocabulary();
	protected TermVocabulary termVocabulary = new TermVocabulary();

	final String ABNORMAL = "PATO:0000460";
	final String QUALITY = "PATO:0000001";
	
	

	public Shard getShard() {
		return shard;
	}

	public void setShard(Shard shard) {
		this.shard = shard;
	}



	public static void main(String[] args) throws Exception {

		String jdbcPath = defaultJdbcPath;

		String cmd = "";
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
			else {
				files.add(args[i]);
			}
		}

		PhenotypeHelper ph = new PhenotypeHelper();
		
		try {

			OBDSQLShard obd = new OBDSQLShard();
			obd.connect(jdbcPath);
			ph.shard = obd;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (cmd.equals("fix")) {
			ph.fixAbnormal();
		}
		else if (cmd.equals("parents-of-abnormal")) {
			ph.makeParentsOfAbnormalPhenotypeDescriptions();
		}
		else if (cmd.equals("influences")) {
			ph.putInfluencesLinkChain();
		}
		else if (cmd.equals("link-uberon")) {
			ph.linkPhenotypeDescriptionsToGenericAnatomy();
		}
		else {
			System.out.println("wrong cmd: "+cmd);
		}

	}

	// IN PROGRESS
	// TODO: if there are other species-specific differentia it makes no sense to make these
	// into a parent. Just do it for EQ
	/**
	 * if exists (Q that inheres_in AO:x and ..), make (Q that inheres_in UBERON:x)
	 * use reasoner to make is_a links
	 * @throws Exception
	 */
	public void linkPhenotypeDescriptionsToGenericAnatomy() throws Exception {

		LabelQueryTerm qt = new LabelQueryTerm(AliasType.ID,
				"PATO:",Operator.STARTS_WITH);
		Collection<Node> nodes = shard.getNodesByQuery(qt);
		for (Node node : nodes) {
			String id = node.getId();
			CompositionalDescription desc = shard.getCompositionalDescription(id, false);
			if (desc.isAtomic())
				continue;
			//System.out.println("desc-in="+desc);
			Collection<CompositionalDescription> args = desc.getArguments();
			for (CompositionalDescription arg : args) {
				if (arg.getRestriction() != null && 
						arg.getRestriction().getRelationId().equals(relationVocabulary.inheres_in())) {
					String ssAOId = arg.getRestriction().getTargetId();
					// the xref goes from UBERON to the ssAO ID
					LinkQueryTerm homolQt = new LinkQueryTerm(termVocabulary.HAS_DBXREF(),ssAOId);
					Collection<Node> gAONodes =
						shard.getNodesByQuery(homolQt);
					for (Node n : gAONodes) {
						if (!n.getSourceId().equals("uberon"))
							continue;
						//arg.setArguments(Collections.singleton(new CompositionalDescription(n.getId())));
						arg.getRestriction().setTargetId(n.getId());
						
						CompositionalDescription newDesc = new CompositionalDescription(desc.getGenus().getNodeId(),
								Collections.singleton(arg.getRestriction()));
						String newId = newDesc.generateId();			
						newDesc.setId(newId);
						//System.out.println("desc-new="+newDesc);
						shard.putCompositionalDescription(newDesc);
					}
				}
			}
			
		}
	}

	/**
	 * if (P has_qualifier Abn) exists, also create a description P
	 * (the is_a link between these will later be calculated by a reasoner)
	 * @throws Exception
	 */
	public void makeParentsOfAbnormalPhenotypeDescriptions() throws Exception {

		LinkQueryTerm qt = new LinkQueryTerm(ABNORMAL);
		qt.setRelation(relationVocabulary.has_qualifier());
		qt.setDescriptionLink(true);
		qt.setInferred(false);
		Collection<Node> nodes = shard.getNodesByQuery(qt);
		for (Node node : nodes) {
			String id = node.getId();
			CompositionalDescription desc = shard.getCompositionalDescription(id, false);
			System.out.println("desc-in="+desc);
			Collection<CompositionalDescription> args = desc.getArguments();
			CompositionalDescription abnLink = null;
			for (CompositionalDescription arg : args) {
				if (arg.getRestriction() != null && arg.getRestriction().getTargetId().equals(ABNORMAL)) {
					abnLink = arg;
				}
			}
			if (abnLink != null) {
				desc.removeArgument(abnLink);
			}
			else {
				throw new Exception("xx");
			}
			System.out.println("desc-new="+desc);
			desc.setNodeId(desc.generateId());
			shard.putCompositionalDescription(desc);
		}
	}

	// test_zfin_genotype_phenotypes.txt must be loaded
	// TODO - do not overwrite MPs
	/**
	 * (abnormal that ...) => (quality that ... and qualifier abnormal)
	 * @throws Exception
	 */
	public void fixAbnormal() throws Exception {

		LinkQueryTerm qt = new LinkQueryTerm(ABNORMAL);
		qt.setRelation(relationVocabulary.is_a());
		qt.setDescriptionLink(true);
		qt.setInferred(false);
		Collection<LinkStatement> links = shard.getLinkStatementsByQuery(qt);
		Collection<Node> nodes = shard.getNodesByQuery(qt);
		OBDSQLShard obd = (OBDSQLShard) shard;
		PreparedStatement ps = obd.getConnection().prepareStatement("UPDATE link SET object_id=? WHERE object_id=?");
		for (Node node : nodes) {
			String id = node.getId();
			int iid = obd.getNodeInternalId(id);
			System.out.println("node: "+id+" // "+iid);
			CompositionalDescription desc = shard.getCompositionalDescription(id, false);
			System.out.println("desc-in="+desc);
			Collection<CompositionalDescription> args = desc.getArguments();
			boolean found = false;
			for (CompositionalDescription arg : args) {
				if (arg.isAtomic() && arg.getNodeId().equals(ABNORMAL)) {
					arg.setNodeId(QUALITY);
					found = true;
				}
			}
			desc.addArgument(relationVocabulary.has_qualifier(), ABNORMAL);
			if (!found)
				throw new Exception("xx");
			System.out.println("desc-new="+desc);
			String newId = desc.generateId();
			if (id.startsWith("MP:")) {
				newId = id; // do not expand MP IDs
			}
			shard.putCompositionalDescription(desc);
			int newIid = obd.getNodeInternalId(newId);
			System.out.println(iid+" -->> "+newIid);
			ps.setInt(1, newIid);
			ps.setInt(2, iid);
			ps.execute();
			//break;
		}
		


	}

	/**
	 * propagates annotations to genotypes up to genes
	 * @throws Exception
	 */
	public void putInfluencesLinkChain() throws Exception {
		RelationCompositionRule rcr =
			new RelationCompositionRule(
					relationVocabulary.influences(),
					relationVocabulary.variant_of(),
					relationVocabulary.influences(),
					true,
					false);
		Set<InferenceRule> rules = new HashSet<InferenceRule>();
		rules.add(rcr);
		shard.realizeRules(rules);
	}

}
