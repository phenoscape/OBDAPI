package org.obd.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.obd.model.Graph;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.BasicRepository;
import org.obd.model.Statement.StatementFilter;
import org.obd.model.vocabulary.TermVocabulary;

public class GraphVizWriter {

	BasicRepository graph;
	StringBuffer text = new StringBuffer();
	protected static final int MAX_LINE_LENGTH = 25;
	protected boolean isRemoveDisjoint = true;
	protected boolean isRemoveTermMetamodel = true;
	TermVocabulary termVocab = new TermVocabulary();

	public GraphVizWriter(BasicRepository graph) {
		super();
		this.graph = graph;
	}

	public BasicRepository getGraph() {
		return graph;
	}

	public void setGraph(BasicRepository graph) {
		this.graph = graph;
	}

	private void processGraph() {
		if (graph instanceof Graph) {
			Graph g = (Graph)graph;
			if (isRemoveDisjoint) {
				g.removeStatements(new StatementFilter() {
					public boolean exclude(Statement s) {
						return s.getRelationId().equals("disjoint_from"); // TODO
					}
				});
			}
			if (isRemoveTermMetamodel) {
				g.removeStatements(new StatementFilter() {
					public boolean exclude(Statement s) {
						return s.getRelationId().startsWith("oboInOwl"); // TODO
					}
				});
			}
			
		}
	}
	
	public String generate() {
		processGraph();
		write("digraph {\n");
		Map<String,Collection<Node>> sgmap = new HashMap<String,Collection<Node>>();
		for (Node n : graph.getNodes()) {
			String sg = getSubGraph(n);
			if (!sgmap.containsKey(sg))
				sgmap.put(sg, new ArrayList<Node>());
			sgmap.get(sg).add(n);
		}
		for (String sg : sgmap.keySet()) {
			write("subgraph "+safeId(sg)+" {\n");
			write("label="+quote(sg)+"\n");
			for (Node n : sgmap.get(sg)) {
				writeNode(n);
			}
			write("}\n");
		}
		for (Statement s : graph.getStatements()) {
			if (s instanceof LinkStatement)
				writeLinkStatement((LinkStatement)s);
		}
		write("}");
		return text.toString();
	}

	public String getSubGraph(Node n) {
		String[] parts = n.getId().split(":");
		if (parts.length > 0)
			return parts[0];
		return "";
	}

	public String safeId(String id) {
		return "node__"+id.replaceAll("[:\\^\\(\\)\\-]","_");
	}

	public String quote(String txt) {
		return '"'+formatLabel(txt.replace('"', '\''))+'"';
	}

	public void writeNode(Node n) {
		String id = safeId(n.getId());
		String label = n.getLabel();
		if (label == null)
			label = n.getId();
		write(id+" [ ");
		write("label="+quote(label));
		
		write("]\n");
	}
	public void writeLinkStatement(LinkStatement s) {
		String sid = safeId(s.getNodeId());
		String tid = safeId(s.getTargetId());
		Node rn = graph.getNode(s.getRelationId());
		String rel = s.getRelationId();
		if (rn != null && rn.getLabel() != null) {
			rel = rn.getLabel();
		}
		write(sid+" -> "+tid+" [ ");
		write("label="+quote(rel));
		write("]\n");
	}

	public void write(String s) {
		text.append(s);
	}

	public StringBuffer getText() {
		return text;
	}

	// taken directly from OE GrahvizCanvas
	protected String formatLabel(String name) {
		//System.out.println("GraphvizCanvas: formatLabel() method.");

		StringBuffer out = new StringBuffer();
		String spacerTokens = "-_, \t";
		StringTokenizer tokenizer = new StringTokenizer(name, spacerTokens,
				true);
		int linelength = 0;
		boolean first = true;
		while (tokenizer.hasMoreElements()) {
			String str = (String) tokenizer.nextElement();
			linelength += str.length();
			out.append(str);
			if (!first && spacerTokens.indexOf(str) != -1) {
				if (linelength + str.length() > MAX_LINE_LENGTH) {
					out.append("\\n");
					linelength = 0;
				}
			}
			first = false;
		}
		return out.toString();
	}


}
