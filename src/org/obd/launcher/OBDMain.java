package org.obd.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.bbop.dataadapter.DataAdapterException;
import org.obd.io.GraphVizWriter;
import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.CompositionalDescription.Predicate;
import org.obd.model.stats.ScoredNode;
import org.obd.model.stats.SimilarityPair;
import org.obd.model.stats.AggregateStatistic.AggregateType;
import org.obd.parser.Parser;
import org.obd.query.AnnotationLinkQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.ComparisonQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;
import org.obd.query.AnalysisCapableRepository.SimilaritySearchParameters;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.QueryTerm.Aspect;
import org.obd.query.impl.MultiShard;
import org.obd.query.impl.MutableOBOSessionShard;
import org.obd.query.impl.OBDSQLShard;
import org.oboedit.gui.Preferences;
import org.purl.obo.vocab.RelationVocabulary;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Command line utils [move?]
 * @author cjm
 *
 */
public class OBDMain {

	static String defaultJdbcPath = "jdbc:postgresql://localhost:5432/obd_phenotype_all";
	RelationVocabulary rv = new RelationVocabulary();
	MutableOBOSessionShard oboShard = new MutableOBOSessionShard();
	OBDSQLShard obdsql;
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
					obdsql = new OBDSQLShard();
					obdsql.connect(jdbcPath);
					multiShard.addShard(obdsql);
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
			else if (args[i].equals("--store")) {
				i++;
				storeFile(args[i]);
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
			else if (args[i].equals("--rename-idspace")) {
				i++;
				obdsql.renameIdentifierSpace(args[i], args[i+1]);
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
			else if (args[i].equals("--nrlinks")) {
				i++;
				showNonRedundantLinksFrom(args[i]);
			}
			else if (args[i].equals("--links")) {
				i++;
				boolean ic = false;
				if (args[i].equals("--ic")) {
					ic = true;
					i++;
				}
				showAllLinks(args[i],ic);
			}
			else if (args[i].equals("--allparents")) {
				i++;
				boolean ic = false;
				if (args[i].equals("--ic")) {
					ic = true;
					i++;
				}
				String nid = args[i];
				if (args[i].equals("--lookup")) {
					i++;
					nid = lookup(args[i]);
					i++;
				}

				showAllParents(nid,ic);
			}
			else if (args[i].equals("--cacheall")) {
				i++;
				cacheAllSimilarityScores();
			}
			else if (args[i].equals("--cache")) {
				i++;
				cacheSimilarityScores(args[i], args[++i]);
				i++;
			}
			else if (args[i].equals("--cachegene")) {
				i++;
				cacheSimilarityScoresByGene(args[i], args[++i]);
				i++;
			}
			else if (args[i].equals("--compare")) {
				i++;
				boolean calcIC = false;
				boolean summary = false;
				if (args[i].equals("--ic")) {
					i++;
					calcIC = true;
				}
				if (args[i].equals("--summary")) {
					i++;
					summary = true;
				}

				String nid1 = args[i];
				i++;
				String nid2 = args[i];
				i++;
				compareNodes(calcIC,nid1,nid2,summary);
			}
			else if (args[i].equals("--compare-sources")) {
				i++;
				boolean calcIC = false;
				String rel = null;
				if (args[i].equals("--ic")) {
					i++;
					calcIC = true;
				}
				if (args[i].equals("--rel")) {
					i++;
					rel = args[i];
					i++;
				}

				String ae = args[i];
				i++;
				String nid1 = args[i];
				i++;
				String nid2 = args[i];
				i++;
				if (rel != null) 
					compareAnnotationSources(calcIC,ae,nid1,nid2,rel);
				else 
					compareAnnotationSources(calcIC,ae,nid1,nid2);
			}
			else if (args[i].equals("--findsim")) {
				i++;
				SimilaritySearchParameters ssp = new SimilaritySearchParameters();
				boolean useIC = false;
				boolean getSP = false;
				while (args[i].startsWith("-")) {
					if (args[i].equals("--ic")) {
						useIC = true;
						i++;
					}
					else if (args[i].equals("--sp")) {
						getSP = true;
						i++;
					}
					else if (args[i].equals("--exhaustive") || args[i].equals("-x")) {
						ssp.isExhaustive = true;
						i++;
					}
					else if (args[i].equals("--org")) {
						i++;
						ssp.in_organism = args[i];
						i++;
					}
					else if (args[i].equals("-M") || args[i].equals("--max_annots")) {
						i++;
						ssp.search_profile_max_annotated_entities_per_class = Integer.parseInt(args[i]);
						i++;
					}
					else if (args[i].equals("-O") || args[i].equals("--max_per_src")) {
						i++;
						ssp.search_profile_max_classes_per_source = Integer.parseInt(args[i]);
						i++;
					}
					else if (args[i].equals("-H") || args[i].equals("--max_hits")) {
						i++;
						ssp.max_candidate_hits = Integer.parseInt(args[i]);
						i++;
					}
					else {
						System.err.println("IGNORING: "+args[i]);
						i++;
					}
				}
				String nid = args[i];
				i++;
				findSimilar(useIC,getSP,ssp,nid);
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

	/**
	 * @param file
	 * @throws Exception
	 */
	public void storeFile(String file) throws Exception {
		Parser p = Parser.createParser(fmt, file);
		if (p == null) {
			System.err.println("could not find a parser for "+file+" fmt:"+fmt);
			System.exit(1);
		}
		for (String ont : p.requires()) {
			System.err.println("fetching "+ont);
			oboShard.load(ont);
		}
		p.setDataShard(multiShard);
		p.setShard(oboShard);
		p.parse();
		System.err.println("storing...");
		//if (isStoreGraph)
		multiShard.putGraph(p.getGraph());

	}

	public void loadSource(Shard metadataShard, String id) throws DataAdapterException {
		if (metadataShard == null) {	
			metadataShard = new MutableOBOSessionShard("http://obo.cvs.sourceforge.net/*checkout*/obo/obo/website/cgi-bin/annotations.txt");
		}
		Graph g = metadataShard.getGraph();
		g.nestStatementsUnderNodes();
		Node md = g.getNode(id); // metadata for this particular source
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

	public void showNonRedundantLinksFrom(String id) {
		System.err.println("nr links from: "+id);
		Collection<Statement> stmts = multiShard.getNonRedundantStatementsForNode(id);
		Graph g = new Graph(stmts);
		//System.out.println(g.toString());
		for (Statement s : stmts) {
			System.out.println(s);
		}
	}

	public void showAllLinks(String id, boolean showIC) throws Exception {
		System.err.println("nr links from: "+id);
		System.out.println("\n** NR LINKS FROM:");
		Collection<Statement> stmts = multiShard.getNonRedundantStatementsForNode(id);
		showStatements(stmts, showIC, true);

		System.out.println("\n** LINKS TO:");
		stmts = multiShard.getStatementsForTarget(id);
		showStatements(stmts, showIC, false);

		System.out.println("\n** ANNOTS TO:");
		int numTo = multiShard.getNodeAggregateQueryResults(new AnnotationLinkQueryTerm(id),
				AggregateType.COUNT);
		System.out.println("\n   count: "+numTo);
		
		stmts = multiShard.getAnnotationStatementsForNode(id, null, null);
		showStatements(stmts, showIC, false);

		System.out.println("\n** ANNOTATED WITH:");
		stmts = multiShard.getAnnotationStatementsForAnnotatedEntity(id, null, null);
		showStatements(stmts, showIC, true);
	}

	public void showAllParents(String id, boolean showIC) {
		System.err.println("nr links from: "+id);
		LinkQueryTerm qt = new LinkQueryTerm(id, null, null);
		qt.setAspect(Aspect.TARGET);
		Collection<Node> nodes = multiShard.getNodesByQuery(qt);
		System.out.println("\n** PARENTS:");
		for (Node node : nodes) {
			if (showIC) {
				Double ic = multiShard.getInformationContentByAnnotations(node.getId());
				System.out.print(ic+" :: ");
			}
			System.out.println(this.getNodeDisp(node));
		}
	}

	public void showStatements(Collection<Statement> stmts, boolean showIC, boolean isParent) {

		for (Statement s : stmts) {
			showStatement(s, showIC, isParent);
		}

	}

	public void showStatement(Statement stmt, boolean showIC, boolean isParent) {
		if (!isParent) {
			if (showIC) {
				Double ic = multiShard.getInformationContentByAnnotations(stmt.getNodeId());
				System.out.print(ic+" :: ");
			}
			System.out.print(this.getNodeDisp(stmt.getNodeId()));
		}
		System.out.print(" --["+stmt.getRelationId()+"]-- ");
		if (isParent) {
			if (stmt instanceof LiteralStatement)
				System.out.print(((LiteralStatement)stmt).getValue());
			else {
				System.out.print(this.getNodeDisp(stmt.getTargetId()));
				if (showIC) {
					Double ic = multiShard.getInformationContentByAnnotations(stmt.getTargetId());
					System.out.print(" :: "+ic);
				}
			}

		}
		System.out.println("");
	}


	public void compareNodes(boolean calcIC, String uid1, String uid2, boolean summary) {
		System.out.println("COMPARING: "+uid1+" "+uid2);

		SimilarityPair sp = multiShard.compareAnnotationsByAnnotatedEntityPair(uid1,uid2);
		if (calcIC)
			multiShard.calculateInformationContentMetrics(sp);
		System.out.println("BSS: "+sp.getBasicSimilarityScore());
		System.out.println("SIM: "+uid1+" -vs- "+uid2+
				" BSS: "+sp.getBasicSimilarityScore()+
				" SIMIC: "+ (calcIC ? sp.getInformationContentRatio() : "na")+
				" DISAG: "+ sp.getDisagreementScore() +
				" ICCS: "+ (calcIC ? sp.getCommonSubsumerAverageIC() : "na") +
				" TOTAL: "+sp.getAssertedNodesInSet1().size()+", "+sp.getAssertedNodesInSet2().size());

		if (summary) {
			return;
		}
		if (calcIC) {
			System.out.println("getInformationContentRatio: "+sp.getInformationContentRatio());
			System.out.println("getInformationContentSumForNRNodesInCommon: "+sp.getInformationContentSumForNRNodesInCommon());
		}
		//Graph g = sp.getGraph();
		System.out.println("\n** NR IN COMMON:");
		for (String nid : sp.getNonRedundantNodesInCommon()) {
			System.out.println("NRIC: "+getNodeDisp(nid));
		}
		for (String nid : sp.getBestCommonSubsumers()) {
			System.out.println("BestCC: "+getNodeDisp(nid));
		}
		System.out.println("\n** ALL NODES IN COMMON:");
		for (String nid : sp.getNodesInCommon()) {
			System.out.println("NIC: "+getNodeDisp(nid));
		}
		System.out.println("\n** SET 1 (all):");
		for (String nid : sp.getNodesInSet1()) {
			System.out.println("SET1: "+getNodeDisp(nid));
		}
		System.out.println("\n** SET 2 (all):");
		for (String nid : sp.getNodesInSet2()) {
			System.out.println("SET2: "+getNodeDisp(nid));
		}

	}

	public void compareAnnotationSources(boolean calcIC, String ae, String src1, String src2, String rel) {
		QueryTerm qt = new LinkQueryTerm(rel, ae);
		Collection<Node> nodes = multiShard.getNodesByQuery(qt);
		for (Node n : nodes) {
			compareAnnotationSources(calcIC,n.getId(),src1,src2);
		}
	}


	public void compareAnnotationSources(boolean calcIC, String ae, String src1, String src2) {
		Node n = multiShard.getNode(ae);
		System.out.println("COMPARING: "+ae+" '"+n.getLabel()+"' sources: "+src1+" -vs- "+src2);

		SimilarityPair sp = multiShard.compareAnnotationsBySourcePair(ae, src1, src2);
		if (calcIC)
			multiShard.calculateInformationContentMetrics(sp);
		System.out.println("SIM: "+ae+" "+n.getLabel()+" sources: "+src1+" -vs- "+src2+
				" BSS: "+sp.getBasicSimilarityScore()+
				" IC: "+ (calcIC ? sp.getInformationContentRatio() : "na")+
				" DISAG: "+ sp.getDisagreementScore() +
				" TOTAL: "+sp.getAssertedNodesInSet1().size()+", "+sp.getAssertedNodesInSet2().size());
		if (calcIC) {
			System.out.println("getInformationContentRatio: "+sp.getInformationContentRatio());
			System.out.println("getInformationContentSumForNRNodesInCommon: "+sp.getInformationContentSumForNRNodesInCommon());
		}
		//Graph g = sp.getGraph();
		System.out.println("\n** NR IN COMMON:");
		for (String nid : sp.getNonRedundantNodesInCommon()) {
			System.out.println("NRIC: "+getNodeDisp(nid));
		}
		System.out.println("\n** ALL NODES IN COMMON:");
		for (String nid : sp.getNodesInCommon()) {
			System.out.println("NIC: "+getNodeDisp(nid));
		}
		System.out.println("\n** SET 1 (NR):");
		for (String nid : sp.getNonRedundantNodesInSet1()) {
			System.out.println("SRC1: "+getNodeDisp(nid));
		}
		System.out.println("\n** SET 2 (NR):");
		for (String nid : sp.getNonRedundantNodesInSet2()) {
			System.out.println("SRC2: "+getNodeDisp(nid));
		}
		System.out.println("\n** SET 1 (IRREC):");
		for (String nid : sp.getIrreconcilableNodesInSet1()) {
			System.out.println("UNIQ1: "+getNodeDisp(nid));
		}
		System.out.println("\n** SET 2 (IRREC):");
		for (String nid : sp.getIrreconcilableNodesInSet2()) {
			System.out.println("UNIQ2: "+getNodeDisp(nid));
		}

	}

	public void findSimilar(boolean useIC, boolean getSP, SimilaritySearchParameters ssp, String nid) {
		Node qn = multiShard.getNode(nid);
		System.out.println("Query: "+qn);
		System.out.println("Params: "+ssp);
		List<ScoredNode> sns = multiShard.getSimilarNodes(ssp,nid);
		int n = 0;
		for (ScoredNode sn : sns) {
			n++;
			String hid = sn.getNodeId();
			Node hn = multiShard.getNode(hid);
			if (getSP || useIC) {
				SimilarityPair sp = multiShard.compareAnnotationsByAnnotatedEntityPair(nid, hid);
				if (useIC) {
					multiShard.calculateInformationContentMetrics(sp);
				}
				System.out.println(hid+"\t"+hn.getLabel()+"\t"+sn.getScore() + "\t"+
						sp.getBasicSimilarityScore()+"\t"+sp.getInformationContentRatio()+"\t"+
						sp.getInformationContentSumForNRNodesInCommon()+"\t"+
						sp.getMaximumInformationContentForNodesInCommon()+"\t"+
						getNodeDisp(sp.getNonRedundantNodesInCommon()));
				// getSP is expensive : limited amount
				if (n >= 40) // HARDCODE ALERT - TODO
					break;
			}
			else {
				System.out.println(sn.getScore() + " : "+getNodeDisp(sn.getNodeId()));
			}
		}
	}

	public void cacheSimilarityScoresByGene(String geneId, String taxId) {
		QueryTerm qt1 = 
			new LabelQueryTerm(AliasType.ID,
					geneId,
					Operator.EQUAL_TO);
		QueryTerm qt2 = 
			new LinkQueryTerm(
					"OBO_REL:in_organism",taxId);
		qt2.setQueryAlias("in_organism_link");
		LinkQueryTerm gqt = new LinkQueryTerm(rv.is_a(), "SO:0000704");
		gqt.setQueryAlias("isa_gene_link");
		qt2.setNode(gqt);
		obdsql.cacheSimilarityScores(qt1, qt2);	
	}


	public void cacheSimilarityScores(String ns1, String ns2) {
		QueryTerm qt1 = 
			new LabelQueryTerm(AliasType.ID,
					ns1,
					Operator.STARTS_WITH);
		QueryTerm qt2 = 
			new LabelQueryTerm(AliasType.ID,
					ns2,
					Operator.STARTS_WITH);
		obdsql.cacheSimilarityScores(qt1, qt2);

	}
	public void cacheAllSimilarityScores() {
		obdsql.cacheSimilarityScores(null); //all

	}

	public String getNodeDispCD(CompositionalDescription cd) {
		Predicate predicate = cd.getPredicate();
		Collection<CompositionalDescription> arguments = cd.getArguments();
		if (cd.isAtomic())
			return getNodeDisp(cd.getNodeId());
		StringBuffer sb = new StringBuffer();
		if (cd.isGenusDifferentia()) {
			// compact string for GD defs
			sb.append(getNodeDisp(cd.getGenus()));
			sb.append(" ");
			for (CompositionalDescription d : cd.getDifferentiaArguments()) {
				sb.append(getNodeDispCD(d));
				sb.append(" ");
			}			
		}
		else {
			//sb.append(predicate.toString());
			//sb.append("( ");
			if (predicate.equals(Predicate.RESTRICTION)) {
				LinkStatement restriction = cd.getRestriction();
				//sb.append(cd.getRelationId());
				//sb.append(" ");
			}
			for (CompositionalDescription d : arguments) {
				sb.append(getNodeDispCD(d));
				sb.append(" ");
			}
			//sb.append(")");
		}
		return sb.toString();
	}


	public String getNodeDisp(Collection<String> nids) {
		StringBuffer sb = new StringBuffer();
		for (String nid : nids) {
			sb.append(" < "+getNodeDisp(nid)+" > ");
		}
		return sb.toString();
	}


	Map<String,Node> nodemap = new HashMap<String,Node>();
	public String getNodeDisp(String id) {
		Node n;
		if (nodemap.containsKey(id)) {
			n = nodemap.get(id);
		}
		else {
			n =  multiShard.getNode(id);
			nodemap.put(id, n);
		}
		return getNodeDisp(n);
	}


	public String getNodeDisp(Node n) {
		if (n instanceof CompositionalDescription)
			return getNodeDispCD((CompositionalDescription)n);
		String id = n.getId();
		String label = n.getLabel();
		if (label != null) {
			return id+" "+label;
		}
		if (id.contains("^")) {
			CompositionalDescription cd = multiShard.getCompositionalDescription(id, true);
			return getNodeDispCD(cd);
		}
		return id;
	}

	public String lookup(String txt) {
		Collection<Node> nodes = multiShard.getNodesByQuery(new LabelQueryTerm(txt));
		return nodes.iterator().next().getId();
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
		props.setProperty("log4j.rootLogger","INFO, A1, A2");

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
