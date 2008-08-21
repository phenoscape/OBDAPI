package org.obd.test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.query.LinkQueryTerm;
import org.obd.query.Shard;
import org.obd.query.impl.OBDSQLShard;

/**
 * Tests for capability to retrieve class expressions from a {@link org.obd.model.Shard}
 * @see CompositionalDescription
 * @author cjm
 *
 */
public class RealizeCompositionParentsTest extends AbstractOBDTest {
		
	Shard shard;
	String jdbcPath = "jdbc:postgresql://localhost:5432/obdtest";
	String dbUsername = "cjm";
	String dbPassword = "";

	final String ABNORMAL = "PATO:0000460";
	final String QUALITY = "PATO:0000001";

	public RealizeCompositionParentsTest(String n) {
		super(n);
	}

	public void setUp() throws Exception {

		System.out.println("Setting up: " + this);
		shard = new OBDSQLShard();
		((OBDSQLShard)shard).connect(jdbcPath,dbUsername,dbPassword);
		initLogger(Level.FINEST);


	}

	
	// test_zfin_genotype_phenotypes.txt must be loaded
	public void testRealizeCompositionNodeWithoutAbnormal() {

		LinkQueryTerm qt = new LinkQueryTerm(ABNORMAL);
		qt.setRelation(relationVocabulary.has_qualifier());
		qt.setDescriptionLink(true);
		qt.setInferred(false);
		Collection<LinkStatement> links = shard.getLinkStatementsByQuery(qt);
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
				assertFalse(true);
			}
			System.out.println("desc-new="+desc);
			shard.putCompositionalDescription(desc);
			break;
		}


	}
	
	// test_zfin_genotype_phenotypes.txt must be loaded
	public void testNormalizeAbnormal() throws SQLException {

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
			assertTrue(found);
			System.out.println("desc-new="+desc);
			shard.putCompositionalDescription(desc);
			String newId = desc.generateId();
			int newIid = obd.getNodeInternalId(newId);
			System.out.println(iid+" -->> "+newIid);
			ps.setInt(1, newIid);
			ps.setInt(2, iid);
			ps.execute();
			//break;
		}


	}


}
