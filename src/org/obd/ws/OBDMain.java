package org.obd.ws;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.bbop.dataadapter.DataAdapterException;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.parser.Parser;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.Shard;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.impl.MultiShard;
import org.obd.query.impl.MutableOBOSessionShard;
import org.obd.query.impl.OBDSQLShard;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Command line utils [move?]
 * @author cjm
 *
 */
public class OBDMain {

	static String defaultJdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_all";
	protected Shard shard;

    public static void main(String[] args) throws Exception {
        // Create a component
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getClients().add(Protocol.FILE);
        String fmt = "homologene";
        MutableOBOSessionShard oboShard = new MutableOBOSessionShard();
        MutableOBOSessionShard metadataShard = null;

		String jdbcPath = defaultJdbcPath;

		for (int i = 0; i < args.length; i++)
			System.err.println("args[" + i + "] = |" + args[i] + "|");
		
	      // TODO: configurable
        // for now we hardcode a multishard wrapping an obosession
        // and a SQL shard
        MultiShard multiShard = new MultiShard();


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
			else if (args[i].equals("--removeIdPrefix")) {
				i++;
				Collection<Node> delNodes = multiShard.getNodesBySearch(args[i], ComparisonQueryTerm.Operator.STARTS_WITH, null, AliasType.ID);
				
				for (Node dn : delNodes) {
					System.err.println("Removing: "+dn);
					multiShard.removeNode(dn.getId());
				}
			}
			else {
				files.add(args[i]);
			}
		}
		      
  		
		for (String file : files) {
			//System.out.println("Parsing "+file+" fmt: "+fmt);
			Parser p = Parser.createParser(fmt, file);
			p.setDataShard(multiShard);
			p.setShard(oboShard);
			p.parse();
			multiShard.putGraph(p.getGraph());
		}

    }
    
    public static void loadSource(Shard metadataShard, String id) throws DataAdapterException {
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
    
    public void storeGraph(Graph g) {
    	shard.putGraph(g);
    }
    
    public void printUsage() {
    	// TODO
    }

}
