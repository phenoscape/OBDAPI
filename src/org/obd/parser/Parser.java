package org.obd.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.NodeAlias;
import org.obd.model.Statement;
import org.obd.model.vocabulary.TermVocabulary;
import org.obd.query.Shard;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.purl.obo.vocab.RelationVocabulary;


/**
 * Root parser class
 * <p>
 * Generates a {@link Graph} object.
 *
 * 
 * @author cjm
 * 
 */
public abstract class Parser {

	protected Graph graph = new Graph();
	protected Shard shard;
	protected Shard dataShard;
	protected String path;
	protected Logger logger = Logger.getLogger("org.obd.parser");
	protected TermVocabulary termVocabulary = new TermVocabulary();
	protected RelationVocabulary relationVocabulary = new RelationVocabulary();
	protected String IN_ORGANISM = "OBO_REL:in_organism";
	protected String GENOTYPE_ID = "SO:0001027";
	protected String GENE_ID = "SO:0000704";
	protected String ALLELE_ID ="SO:0001023";
	
	Map <String,String> termIndex = new HashMap<String,String>();
	protected Map<String,Collection<Node>> label2node = new HashMap<String,Collection<Node>>();

	boolean isIndexed = false;

	
	public Parser() {
		super();
	}
	
	
	
	public Parser(String path) {
		super();
		this.path = path;
	}



	public String entrezgeneId(String local) {
		return "NCBI_Gene:"+local;
	}
	
	public String pubmedId(String local) {
		return "PMID:"+local;
	}
	
	public String gadId(String local) {
		return "GAD:"+local;
	}
	
	public String zfinId(String local) {
		return "ZFIN:"+local;
	}
	
	public String ncbitaxId(String local) {
		return "NCBITaxon:"+local;
	}
	
	public String unigeneId(String local) {
		return "UniGene:"+local;
	}
	
	public String homologeneId(String local) {
		return "NCBIHomologene:"+local;
	}
	public static Parser createParser(String fmt, String path) {
		if (fmt.equals("homologene"))
			return new HomologeneParser(path);
		else if (fmt.equals("ncbigene"))
			return new NCBIGeneParser(path);
		else
			try {
				Parser parser = (Parser)Class.forName(fmt).newInstance();
				parser.setPath(path);
				return parser;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return null;
	}
	public static Parser createParser(Class parserClass, String path) throws InstantiationException, IllegalAccessException {
		Parser parser = (Parser)parserClass.newInstance();
		parser.setPath(path);
		return parser;
	}
	
	public void warn(String message) {
		System.err.println(message); // TODO - cache; show line no; warning levels
	}
	public void parse() throws IOException, Exception {
	}
	
	public Statement addLink(String su, String rel, String ob, String src) {
		Statement s = new LinkStatement(su, rel, ob);
		s.setSourceId(src);
		graph.addStatement(s);
		return s;
	}
	public void addAllSomeLink(String su, String rel, String ob, String src) {
		Statement s = new LinkStatement(su, rel, ob);
		s.setExistential(true);
		s.setAppliesToAllInstancesOf(true);
		s.setSourceId(src);
		graph.addStatement(s);
	}
	public void addLiteral(String su, String rel, String val, String dt, String src) {
		Statement s = new LiteralStatement(su, rel, val, dt);
		s.setSourceId(src);
		graph.addStatement(s);
	}
	
	public void addAlias(String su, String label) {
		Statement s = new NodeAlias(su,label);
		graph.addStatement(s);
	}
	
	public void addLabel(String su, String label) {
		Node node = graph.getNode(su);
		node.setLabel(label);
	}
	public void addStatement(Statement s) {
		graph.addStatement(s);
	}
	public void addStatement(Statement s, String src) {
		s.setSourceId(src);
		graph.addStatement(s);
	}

	public void addXref(String id, String x, String src) {
		addAllSomeLink(id,termVocabulary.HAS_DBXREF(),x,src);
	}
	
	public void addInOrganismLink(String id, String taxId, String src) {
		addAllSomeLink(id, IN_ORGANISM,
				taxId, src);	
	}

	public void addIsAGeneLink(String id, String src) {
		Statement s = new LinkStatement(id, relationVocabulary.is_a(), GENE_ID);
		addStatement(s,src);
	}

	
	public Node addNode(String id) {
		Node node = graph.getNode(id);
		if (node == null)
			node = new Node(id);
		graph.addNode(node);
		return node;
	}
	
	public Node addNode(String id, String label) {
		Node node = graph.getNode(id);
		if (node == null) {
			node = new Node(id);
			node.setLabel(label);
		}
		graph.addNode(node);
		return node;
	}
	public void addNode(Node n) {
		graph.addNode(n);
	}

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}
	
	public Shard getShard() {
		return shard;
	}
	public void setShard(Shard shard) {
		this.shard = shard;
	}
	
	public Shard getDataShard() {
		return dataShard;
	}

	public void setDataShard(Shard dataShard) {
		this.dataShard = dataShard;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String createId(String local) {
		return local;
	}
	
	public String lookup(String id) {
		return lookup(id,null);
	}
	
	public String lookup(String id, String label) {
		if (id != null) {
			if (termIndex.containsKey(id))
				return termIndex.get(id);
			if (termIndex.containsKey(createId(id)))
				return termIndex.get(createId(id));
		}
		if (label != null)
			if (termIndex.containsKey(label))
				return termIndex.get(label);
		return null;
	}
	
	// redundant with above?
	public Collection<Node> lookupTerm(String label) {
		Collection<Node> nodes = new HashSet<Node>();
		if (label2node.containsKey(label))
			nodes = label2node.get(label);
		else {
			try {
				nodes = shard.getNodesBySearch(label, Operator.EQUAL_TO);
				label2node.put(label, nodes);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return nodes;
	}


	/**
	 * 
	 *
	 */
	public void index() {
		if (isIndexed)
			return;
		for (Node n : shard.getNodes()) {
			String nid = n.getId();
			termIndex.put(n.getLabel(), nid);
			for (Statement s : shard.getStatementsByNode(nid)) {
				if (s instanceof LinkStatement) {
					LinkStatement ls = (LinkStatement)s;
					if (ls.isXref()) {
						termIndex.put(ls.getTargetId(), nid);
					}
				}
			}
		}
		System.err.println("index size="+termIndex.size());
		isIndexed = true;
	}



}
