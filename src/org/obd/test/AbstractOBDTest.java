package org.obd.test;

import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.Shard;
import org.obd.query.impl.OBDSQLShard;
import org.obd.query.impl.OBDSimpleSQLShard;
import org.obo.datamodel.impl.DefaultLinkDatabase;
import org.obo.reasoner.impl.ForwardChainingReasoner;
import org.purl.obo.vocab.RelationVocabulary;

public abstract class AbstractOBDTest extends TestCase {

	static String jdbcPath = "jdbc:postgresql://localhost:9999/obdp";
	//static String jdbcPath = "jdbc:postgresql://localhost:5432/obdp808";
	//static String jdbcPath = "jdbc:postgresql://localhost:9999/obd_phenotype_200805";


	// Set these values if you don't want to use the environmental user / no password database connection defaults. 
	static String dbUsername ="cjm";
	static String dbPassword;
	protected Shard shard;
	protected RelationVocabulary relationVocabulary = new RelationVocabulary();
	protected String MELANOCYTE = "CL:0000148";
	protected String NEURON = "CL:0000540";
	protected String BEHAVIOR = "GO:0007610";
	protected String NEUROBLAST = "CL:0000031";
	protected String HUMAN_EYE = "FMA:54448";
	protected String GENE_ID = "SO:0000704";


	public static int sleepTime = 0;

	public AbstractOBDTest(String n) {
		super(n);
	}

	@Override
	public void setUp() throws Exception {
		System.out.println("Setting up: " + this);
		getShard();
		initLogger();
	}

	public void initLogger() {
		initLogger(Level.FINEST);
		//Logger logger = Logger.getLogger(AbstractOBOTest.class); 
		Properties props = new Properties();
		props.setProperty("log4j.rootLogger","DEBUG, A1");

		props.setProperty("log4j.appender.A1","org.apache.log4j.ConsoleAppender");
		props.setProperty("log4j.appender.A1.layout","org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.A1.layout.ConversionPattern","%d [%t] %-5p %c - %m%n");

		PropertyConfigurator.configure(props);


		// SessionManager.getManager().setSession(session);

	}


	public void initLogger(Level level) {
		Handler[] handlers = Logger.getLogger( "" ).getHandlers();
		for ( int index = 0; index < handlers.length; index++ ) {
			handlers[index].setLevel( level );
		}
		Logger.getLogger( "org.bbop.rdbms").setLevel(level);
		Logger.getLogger( "org.obd").setLevel(level);

	}

	@Override
	public void tearDown() throws Exception {
		System.out.println("tearDown");
		if (shard != null)
			shard.disconnect();
		shard = null;
	}

	public Shard initShard() throws SQLException, ClassNotFoundException {
		//return new OBDSimpleSQLShard();
		return new OBDSQLShard();
	}
	public void connectShard() throws SQLException, ClassNotFoundException {
		((OBDSQLShard)shard).connect(jdbcPath,dbUsername,dbPassword);		
	}

	public Shard getShard() throws SQLException, ClassNotFoundException {
		try {
			if (sleepTime > 0)
				Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		shard = initShard();
		connectShard();
		return shard;
	}

	public static void printGraph(Graph g) {
		System.out.println("Graph num nodes: "+g.getNodes().size());
		for (Node n : g.getNodes()) {
			System.out.println("node="+n);
			for (Statement s : n.getStatements()) {
				System.out.println("    "+s);
			}	
		}
		for (Statement s : g.getStatements()) {
			System.out.println(s);
		}	

	}
	public static void printNonEmptyGraph(Graph g) {
		assertTrue(g.getNodes().size() > 0);
		printGraph(g);
	}


}
