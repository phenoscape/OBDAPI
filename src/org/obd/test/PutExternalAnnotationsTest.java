package org.obd.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.obd.bio.GeneFactory;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.rule.InferenceRule;
import org.obd.model.rule.RelationCompositionRule;
import org.obd.parser.CTDChemGeneInteractionsTSVFileParser;
import org.obd.parser.EvocDataMappingParser;
import org.obd.parser.GeneAssociationParser;
import org.obd.parser.GeneticAssociationDatabaseTextFileParser;
import org.obd.parser.HPRDInteractionParser;
import org.obd.parser.HomologeneParser;
import org.obd.parser.MGIGenotypePhenotypeParser;
import org.obd.parser.NCBIGeneParser;
import org.obd.parser.Parser;
import org.obd.parser.ZFINGeneMorpholinoParser;
import org.obd.parser.ZFINGenotypeFeatureParser;
import org.obd.parser.ZFINGenotypePhenotypeOBOParser;
import org.obd.parser.ZFINGenotypePhenotypeParser;
import org.obd.parser.ZFINPhenotypeEnvironmentParser;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.exception.ShardExecutionException;
import org.obd.query.impl.MutableOBOSessionShard;
import org.obd.query.impl.OBDSQLShard;
import org.obd.util.PhenotypeHelper;

/**
 * tests ability to write to Shard. Also tests various subclasses of {@link Parser}
 * @author cjm
 *
 */
public class PutExternalAnnotationsTest extends AbstractOBDTest {

	String writeJdbcPath = "jdbc:postgresql://localhost:5432/obdtest";
	String dbUsername = "cjm";
	String dbPassword = "";

	public PutExternalAnnotationsTest(String n) {
		super(n);
	}

	@Override
	public void connectShard() throws SQLException, ClassNotFoundException {
		((OBDSQLShard)shard).connect(writeJdbcPath,dbUsername,dbPassword);	
		initLogger();
	}
	
	public void initLogger() {
		initLogger(Level.INFO);
	}


	public static Test suite() {
		TestSuite suite = new TestSuite();
		addTests(suite);
		return suite;
	}

	public static void addTests(TestSuite suite) {

		suite.addTest(new PutExternalAnnotationsTest("putMGIGenotypePhenotypeTest"));
		suite.addTest(new PutExternalAnnotationsTest("putZFINGenotypePhenotypeTest"));

		suite.addTest(new PutExternalAnnotationsTest("putLinkChainTest"));
	
		suite.addTest(new PutExternalAnnotationsTest("putGeneTest"));
		suite.addTest(new PutExternalAnnotationsTest("putGADTest"));
		suite.addTest(new PutExternalAnnotationsTest("putGenericAnatomyTest"));

		suite.addTest(new PutExternalAnnotationsTest("putZFINGenotypeFeatureTest"));

		suite.addTest(new PutExternalAnnotationsTest("putCTDTest"));

	

		suite.addTest(new PutExternalAnnotationsTest("putEVOCTest"));
		suite.addTest(new PutExternalAnnotationsTest("putAssocTest"));
		suite.addTest(new PutExternalAnnotationsTest("putHPRDTest"));

		suite.addTest(new PutExternalAnnotationsTest("putOBDMetadataTest"));

		suite.addTest(new PutExternalAnnotationsTest("putHomologeneTest"));
		suite.addTest(new PutExternalAnnotationsTest("putSOTest"));
		suite.addTest(new PutExternalAnnotationsTest("mergeIdentifierByIDSpacesTest"));
		 

	}

	public void mergeIdentifierByIDSpacesTest() throws ShardExecutionException {
		shard.mergeIdentifierByIDSpaces("NCBI_Gene","ZFIN");
	}
	/**
	 * @see HomologeneParser
	 * @throws Exception
	 */
	public void putHomologeneTest() throws Exception {

		String path =
			getResourcePath() + "/test_homologene.data";
		HomologeneParser p = new HomologeneParser(path);
		p.parse();
		((OBDSQLShard)shard).putGraph(p.getGraph());

	}
	/**
	 * @see NCBIGeneParser
	 * @throws Exception
	 */
	public void putGeneTest() throws Exception {

		String path =
			getResourcePath() + "/test_ncbigene.data";
		Parser p = Parser.createParser("ncbigene",path);
		p.parse();
		shard.putGraph(p.getGraph());
		Collection<LinkStatement> isaLinks = shard.getLinkStatementsByQuery(new LinkQueryTerm("NCBI_Gene:2138", relationVocabulary.is_a(), null));
		boolean ok = false;
		for (LinkStatement s : isaLinks) {
			System.out.println(s);
			if (s.getTargetId().equals(GENE_ID))
				ok = true;

		}
		assertTrue(ok);

	}

	/**
	 * @see HPRDInteractionParser
	 * @throws Exception
	 */
	public void putHPRDTest() throws Exception {

		String path =
			getResourcePath() + "/hprd_test.txt";
		Parser p = Parser.createParser(HPRDInteractionParser.class,path);
		p.parse();
		shard.putGraph(p.getGraph());
	}

	/**
	 * @see GeneAssociationParser
	 * @throws Exception
	 */
	public void putAssocTest() throws Exception {

		String path =
			getResourcePath() + "/test.assoc";
		Parser p = Parser.createParser(GeneAssociationParser.class,path);
		p.parse();
		shard.putGraph(p.getGraph());
	}
	/**
	 * @see ZFINGenotypeFeatureParser
	 * @throws Exception
	 */
	public void putZFINGenotypeFeatureTest() throws Exception {

		String path =
			getResourcePath() + "/test_zfin_genotype_features.txt";
		MutableOBOSessionShard moss = new MutableOBOSessionShard();
		moss.loadFile(getResourcePath() + "/so.obo");

		Parser p = Parser.createParser(ZFINGenotypeFeatureParser.class,
				path);
		p.setShard(moss);
		p.parse();
		shard.putGraph(p.getGraph());

	}



	/**
	 * @see ZFINGenotypePhenotypeParser
	 * @throws Exception
	 */
	public void putZFINGenotypePhenotypeTest() throws Exception {
		initLogger(Level.INFO);
		String path =
			getResourcePath() + "/test_zfin_genotype_phenotypes.txt";
		MutableOBOSessionShard moss = new MutableOBOSessionShard();
		//moss.loadFile(getResourcePath() + "/so.obo");
		//moss.load("quality");
		//moss.load("zebrafish_anatomy");

		Parser p = Parser.createParser(ZFINGenotypePhenotypeOBOParser.class,
				path);
		p.setShard(moss);
		p.parse();
		shard.putGraph(p.getGraph());

		parseAndSave("test_zfin_pheno_environment.txt", ZFINPhenotypeEnvironmentParser.class);
		parseAndSave("test_zfin_morpholinos.txt", ZFINGeneMorpholinoParser.class);

		Node mn = shard.getNode("ZFIN:ZDB-MRPHLNO-060930-1");
		assertTrue(mn != null);
		LinkQueryTerm hasMorph = new LinkQueryTerm(mn.getId());
		hasMorph.setQueryAlias("has_morph");
		LinkQueryTerm morphAnnotQt = new LinkQueryTerm();
		morphAnnotQt.setPositedBy(new LinkQueryTerm(hasMorph));
		//morphAnnotQt.setPositedBy(hasMorph);
		morphAnnotQt.setQueryAlias("annotq");
		initLogger(Level.FINEST);
		Collection<LinkStatement> stmts = shard.getLinkStatementsByQuery(morphAnnotQt);
		initLogger(Level.INFO);

		int n=0;
		for (LinkStatement s : stmts) {
			System.out.println("morph annot: "+s);
			n++;
		}
		assertTrue(n>0);

		QueryTerm towardsQt = new LinkQueryTerm(relationVocabulary.towards(),
		"ZFA:0000150"); // pronephric duct
		Collection<Node> nodes = 
			shard.getNodesByQuery(towardsQt);
		for (Node node : nodes) {
			System.out.println(node);
		}

		QueryTerm inhQt = new LinkQueryTerm(relationVocabulary.inheres_in(),
				towardsQt);

		/*
		 * annot queries don't work if no reasoning was performed
		 * 
		Collection<LinkStatement> stmts = 
			shard.getLinkStatementsByQuery(new AnnotationLinkQueryTerm(
					"ZFA:0000150"));
			//shard.getLinkStatementsByQuery(new AnnotationLinkQueryTerm());

		 */
		stmts = 
			shard.getLinkStatementsByQuery(new LinkQueryTerm(relationVocabulary.influences(),
					towardsQt));
		//shard.getLinkStatementsByQuery(new AnnotationLinkQueryTerm());
		n=0;
		for (LinkStatement s : stmts) {
			System.out.println("annot: "+s);
			n++;
		}
		assertTrue(n>0);
	}

	/**
	 * @see MGIGenotypePhenotypeParser
	 * @throws Exception
	 */
	public void putMGIGenotypePhenotypeTest() throws Exception {

		String path =
			getResourcePath() + "/test_mgi_PhenoGenoMP.rpt";

		Parser p = Parser.createParser(MGIGenotypePhenotypeParser.class,
				path);
		p.setDataShard(shard);
		p.parse();
		shard.putGraph(p.getGraph());

		Collection<LinkStatement> stmts = 
			shard.getLinkStatementsByQuery(new LinkQueryTerm(relationVocabulary.influences(),
			"MP:0005430")); // absent fibula
		//shard.getLinkStatementsByQuery(new AnnotationLinkQueryTerm());
		int n=0;
		for (LinkStatement s : stmts) {
			System.out.println(s);
			for (Statement gs : shard.getStatementsByNode(s.getNodeId())) {
				System.out.println(gs);

			}
			n++;
		}
		assertTrue(n>0);
	}

	/**
	 * @see GeneticAssociationDatabaseTextFileParser
	 * @throws Exception
	 */
	public void putGADTest() throws Exception {

		String path =
			getResourcePath() + "/gad_test.txt";
		MutableOBOSessionShard moss = new MutableOBOSessionShard();
		moss.load("disease_ontology");
		moss.load("mammalian_phenotype");
		moss.load("chebi");
		//moss.load("mesh");

		Parser p = Parser.createParser(GeneticAssociationDatabaseTextFileParser.class,
				path);
		p.setShard(moss);
		p.setDataShard(shard);
		p.parse();
		shard.putGraph(p.getGraph());
	}

	/**
	 * @see CTDChemGeneInteractionsTSVFileParser
	 * @throws Exception
	 */
	public void putCTDTest() throws Exception {

		String path =
			getResourcePath() + "/test_chem_gene_ixns.tsv";
		MutableOBOSessionShard moss = new MutableOBOSessionShard();
		moss.load("chebi");

		Parser p = Parser.createParser(CTDChemGeneInteractionsTSVFileParser.class,
				path);
		p.setShard(moss);
		p.setDataShard(shard);
		p.parse();
		shard.putGraph(p.getGraph());
	}

	/**
	 * @see EvocDataMappingParser
	 * @throws Exception
	 */
	public void putEVOCTest() throws Exception {

		String path =
			getResourcePath() + "/celltype.evoc";
		MutableOBOSessionShard moss = new MutableOBOSessionShard();
		moss.load("cell");
		//moss.load("mammalian_phenotype");
		//moss.load("mesh");

		Parser p = Parser.createParser(EvocDataMappingParser.class,
				path);
		p.setShard(moss);
		p.setDataShard(shard);
		p.parse();
		shard.putGraph(p.getGraph());
	}

	public void putSOTest() throws Exception {

		String path =
			getResourcePath() + "/so.obo";
		//MutableOBOSessionShard moss = new MutableOBOSessionShard();
		//moss.loadFile(path);
		//moss.load("quality");
		Parser p = Parser.createParser(org.obd.parser.OBOFormatParser.class,
				path);
		p.parse();
		shard.putGraph(p.getGraph());


		//shard.transferFrom(moss);
	}

	/**
	 * must first run:
	 * - putGeneTest
	 * - putMGIGenotypePhenotypeTest
	 * 
	 * promotes annotations from genotype level to gene level
	 * @throws Exception
	 */
	public void putOBDMetadataTest() throws Exception {

		String path =
			getResourcePath() + "/metadata.txt";

		Parser p = Parser.createParser(org.obd.parser.OBDSimpleMetadataParser.class,
				path);
		p.parse();
		shard.putGraph(p.getGraph());
	}

	public void putBIRNAnnotationsTest() throws Exception {
		// TODO
	}

	public void putLinkChainTest() throws Exception {
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
		System.out.println("annotations promoted to Shh:");
		Collection<Statement> stmts = shard.getAnnotationStatementsForAnnotatedEntity("MGI:98297", null, null);
		int n = 0;
		for (Statement s : stmts) {
			System.out.println(s);
			n++;
		}
		assertTrue(n>0);
	}

	public void putGenericAnatomyTest() throws Exception {
		PhenotypeHelper ph = new PhenotypeHelper();
		ph.setShard(shard);
		ph.linkPhenotypeDescriptionsToGenericAnatomy();
	}

	public void putPhenotePhenotypeAnnotationsTest() throws Exception {
		String path =
			getResourcePath() + "TNNT2-OMIM-zfin.tab";
		// TODO

	}

	protected String getResourcePath() {
		return "test_resources";
	}

	private void parseAndSave(String filename, Class parserClass) throws IOException, Exception {
		String path =
			getResourcePath() + "/"+filename;
		//MutableOBOSessionShard moss = new MutableOBOSessionShard();
		//moss.loadFile(getResourcePath() + "/so.obo");

		Parser p = Parser.createParser(parserClass,
				path);
		//p.setShard(moss);
		p.parse();
		shard.putGraph(p.getGraph());
	}



}
