package org.obd.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.bbop.dataadapter.DataAdapterException;
import org.obd.io.GraphVizWriter;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.stats.SimilarityPair;
import org.obd.parser.Parser;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.Shard;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.impl.MultiShard;
import org.obd.query.impl.MutableOBOSessionShard;
import org.obd.query.impl.OBDSQLShard;
import org.oboedit.gui.Preferences;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Command line utils [move?]
 * @author cjm
 *
 */
public class OBDMain {

	static String defaultJdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_all";
	MutableOBOSessionShard oboShard = new MutableOBOSessionShard();
	MutableOBOSessionShard metadataShard = null;
	MultiShard multiShard = new MultiShard();
	String fmt = null;
	boolean isStoreGraph = false;


	public static void main(String[] args) throws Exception {
		OBDMain main = new OBDMain();
		main.run(args);

	}
	public  void run(String[] args) throws Exception {
		
		setupLog4j();
		
		// Create a component
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, 8182);
		component.getClients().add(Protocol.FILE);

		String jdbcPath = defaultJdbcPath;

		for (int i = 0; i < args.length; i++)
			System.err.println("args[" + i + "] = |" + args[i] + "|");

		// TODO: configurable
		// for now we hardcode a multishard wrapping an obosession
		// and a SQL shard

		Collection<String> files = new LinkedList<String>();
		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-d")) {
				i++;
				jdbcPath = args[i];
				try {
					OBDSQLShard obd = new OBDSQLShard();
					obd.connect(jdbcPath);
					multiShard.addShard(obd);
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}
			else if (args[i].equals("--ontology")) {
				i++;
				oboShard.load(args[i]);
			}
			else if (args[i].equals("--metadata")) {
				i++;
				if (metadataShard == null)
					metadataShard = new MutableOBOSessionShard();
				metadataShard.load(args[i]);
			}
			else if (args[i].equals("--source")) {
				i++;
				loadSource(metadataShard,args[i]);
			}
			else if (args[i].equals("--format")) {
				i++;
				fmt = args[i];
			}
			else if (args[i].equals("--removeSource")) {
				i++;
				multiShard.removeSource(args[i]);
			}
			else if (args[i].equals("--mergeIdSpaces")) {
				i++;
				multiShard.mergeIdentifierByIDSpaces(args[i], args[i+1]);
				i++;
			}
			else if (args[i].equals("--removeIdPrefix")) {
				i++;
				Collection<Node> delNodes = multiShard.getNodesBySearch(args[i], ComparisonQueryTerm.Operator.STARTS_WITH, null, AliasType.ID);

				for (Node dn : delNodes) {
					System.err.println("Removing: "+dn);
					multiShard.removeNode(dn.getId());
				}
			}
			else if (args[i].equals("--draw")) {
				i++;
				if (args[i].equals("[")) {
					Collection<String> nids = new HashSet<String>();
					i++;
					while (!args[i].equals("]")) {
						nids.add(args[i]);
						i++;
					}
					i++;
					draw(nids);
				}
				else
					draw(args[i]);
			}
			else if (args[i].equals("--compare")) {
				String nid1 = args[++i];
				String nid2 = args[++i];
				i++;
				compareNodes(false,nid1,nid2);
			}
			else if (args[i].equals("--store")) {
				isStoreGraph = true;
			}
			else {
				files.add(args[i]);
			}
		}


		for (String file : files) {
			//System.out.println("Parsing "+file+" fmt: "+fmt);
		}

	}

	public void storeFile(String file) throws Exception {
		Parser p = Parser.createParser(fmt, file);
		if (p == null) {
			System.err.println("could not find a parser for "+file+" fmt:"+fmt);
			System.exit(1);
		}
		p.setDataShard(multiShard);
		p.setShard(oboShard);
		p.parse();
		if (isStoreGraph)
			multiShard.putGraph(p.getGraph());

	}

	public void loadSource(Shard metadataShard, String id) throws DataAdapterException {
		if (metadataShard == null) {	
			metadataShard = new MutableOBOSessionShard("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/website/cgi-bin/annotations.txt");
		}
		Graph g = metadataShard.getGraph();
		g.nestStatementsUnderNodes();
		Node md = g.getNode(id);
		for (Node sn : md.getTargetNodes("source")) {

		}
		//String src = metadataShard.getLi TODO
	}

	public void draw(String id) {
		System.err.println("drawing: "+id);
		Graph g = multiShard.getGraphAroundNode(id, null, null);
		GraphVizWriter gvw = new GraphVizWriter(g);
		System.out.println(gvw.generate());
	}
	public void draw(Collection<String> ids) {
		System.err.println("drawing: "+ids);
		Graph g = multiShard.getGraphByNodes(ids, null);
		GraphVizWriter gvw = new GraphVizWriter(g);
		System.out.println(gvw.generate());
	}
	public void compareNodes(boolean calcIC, String uid1, String uid2) {
		System.out.println("COMPARING: "+uid1+" "+uid2);

		SimilarityPair sp = multiShard.compareAnnotationsByAnnotatedEntityPair(uid1,uid2);
		if (calcIC)
			multiShard.calculateInformationContentMetrics(sp);
		//Graph g = sp.getGraph();
		System.out.println("NR:");
		for (String nid : sp.getNonRedundantNodesInCommon()) {
				System.out.println(getNode(nid));
		}
		System.out.println("ALL:");
		for (String nid : sp.getNodesInCommon()) {
			System.out.println(getNode(nid));
		}
		System.out.println("SET 1:");
		for (String nid : sp.getNodesInSet1()) {
			System.out.println(getNode(nid));
		}
		System.out.println("SET 2:");
		for (String nid : sp.getNodesInSet2()) {
			System.out.println(getNode(nid));
		}

	}
	public String getNode(String id) {
		return multiShard.getNode(id).toString();
	}

	public void setupLog4j() {
		Properties props = new Properties();
		try {
			InputStream configStream = getClass().getResourceAsStream("/log4j.properties");
			props.load(configStream);
			configStream.close();
		} catch(IOException e) {
			System.err.println("Error: Cannot load logger configuration file log4j.properties from jar. " + e.getMessage());
			Preferences.getPreferences().setLogfile("(Could not configure logging--log4j.properties not found)");  // there won't be a log file
		}
		props.setProperty("log4j.rootLogger","DEBUG, A1, A2");

		props.setProperty("log4j.appender.A1","org.apache.log4j.ConsoleAppender");
		props.setProperty("log4j.appender.A1.layout","org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.A1.layout.ConversionPattern","%m%n");

		props.setProperty("log4j.appender.A2","org.apache.log4j.RollingFileAppender");
		//props.setProperty("log4j.appender.A2.file",logFile);
		props.setProperty("log4j.appender.A2.MaxFileSize","1MB");
		props.setProperty("log4j.appender.A2.MaxBackupIndex","10");
		props.setProperty("log4j.appender.A2.append","true");
		props.setProperty("log4j.appender.A2.layout","org.apache.log4j.PatternLayout");
		props.setProperty("log4j.appender.A2.layout.ConversionPattern","%d [%t] %-5p %c - %m%n");
		LogManager.resetConfiguration();
		PropertyConfigurator.configure(props);
	}

	public void printUsage() {
		// TODO
	}

}
