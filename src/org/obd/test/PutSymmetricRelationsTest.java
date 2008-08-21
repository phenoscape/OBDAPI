package org.obd.test;

import java.util.Collection;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.obd.model.Graph;
import org.obd.model.HomologyView;
import org.obd.model.LinkStatement;
import org.obd.model.Statement;
import org.obd.parser.Parser;
import org.obd.query.Shard;
import org.obd.query.impl.OBDSQLShard;

/**
 * tests ability to write to Shard. Also tests various subclasses of {@link Parser}
 * @author cjm
 *
 */
public class PutSymmetricRelationsTest extends AbstractOBDTest {

	Shard shard;
	String jdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_all";
	String dbUsername = "cjm";
	String dbPassword = "";
	
	public PutSymmetricRelationsTest(String n) {
		super(n);
	}


	public void setUp() throws Exception {
		System.out.println("Setting up: " + this);
		shard = new OBDSQLShard();
		((OBDSQLShard)shard).connect(jdbcPath,dbUsername,dbPassword);
		
	}


	String HOMOLOGOUS_TO = "OBO_REL:homologous_to"; // TODO
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		addTests(suite);
		return suite;
	}

	public static void addTests(TestSuite suite) {
		suite.addTest(new PutSymmetricRelationsTest("putSymmetricRelationsTest"));
	
		
	}
	public void putSymmetricRelationsTest() throws Exception {
		HomologyView hv = new HomologyView(shard);
		Collection<LinkStatement> links = hv.getAllHomologyLinks();
		System.out.println(links.size());

		Collection<Statement> newLinks = new HashSet<Statement>();
		
		for (LinkStatement s : links) {
			LinkStatement invLink = createReverseLinkStatement(s);
			if (links.contains(invLink))
				System.err.println("got: "+invLink);
			else
				newLinks.add(invLink);
			
		}
		System.out.println(newLinks.size());
		Graph g = new Graph();
		g.addStatements(newLinks);
		shard.putGraph(g);
		
		assertTrue(true);
	}
	
	public LinkStatement createReverseLinkStatement(LinkStatement s) {
		LinkStatement invLink = new LinkStatement();
		invLink.setSourceId(s.getSourceId());
		invLink.setNodeId(s.getTargetId());
		invLink.setTargetId(s.getNodeId());
		invLink.setRelationId(s.getRelationId());
		return invLink;
	}

	


}
