package org.obd.model.bridge;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.InstanceQuantifier;
import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.CompositionalDescription.Predicate;

//TODO: use standard java libs
//TODO: XML namespace

public class OBDXMLWriter {
	
	private StringBuffer sb = new StringBuffer();
	private int depth = 0;

	public OBDXMLWriter() {
		super();
	}

	public void tab() {
		for (int i=0;i<depth;i++){
			sb.append("\t");
		}
	}
	public void openStartElement(String name) {
		sb.append("<"+name);
		
	}
	public void closeStartElement() {
		sb.append(">\n");
	}
	public void startElement(String name) {
		openStartElement(name);
		closeStartElement();
		
	}
	public void endElement() {
		sb.append("/>\n");
	}
	public void endElement(String name) {
		sb.append("</"+name+">\n");
	}
	public void addAttribute(String name, String value) {
		if (value==null)
			return;
		sb.append(" "+name+"=\""+escape(value)+"\"");
	}
	public void addAttribute(String name, boolean value) {
		if (!value)
			return;
		addAttribute(name,"true");
	}
	public void addAttribute(String name, Integer value) {
		if (value == null)
			return;
		addAttribute(name,value.toString());
	}
	public void element(String name, String pcdata) {
		if (pcdata != null){
			startElement(name);
			sb.append(escape(pcdata).trim()+"\n");
			endElement(name);
		}
	}
	public String toString() {
		return sb.toString();
	}


	// TODO: I am so going to hell : use a proper (but lightweight) lib here...
	public String escape(String str) {
		str = replaceString(str,"&","&amp;");
		str = replaceString(str,"<","&lt;");
		str = replaceString(str,">","&gt;");
		str = replaceString(str,"\"","&quot;");
		str = replaceString(str,"'","&apos;");
		return str;
	}  

	// from StringW
	static public String replaceString(String text, String repl, String with) {
		return replaceString(text, repl, with, -1);
	}  
	/**
	 * Replace a string with another string inside a larger string, for
	 * the first n values of the search string.
	 *
	 * @param text String to do search and replace in
	 * @param repl String to search for
	 * @param with String to replace with
	 * @param n    int    values to replace
	 *
	 * @return String with n values replacEd
	 */
	static public String replaceString(String text, String repl, String with, int max) {
		if(text == null) {
			return null;
		}

		StringBuffer buffer = new StringBuffer(text.length());
		int start = 0;
		int end = 0;
		while( (end = text.indexOf(repl, start)) != -1 ) {
			buffer.append(text.substring(start, end)).append(with);
			start = end + repl.length();

			if(--max == 0) {
				break;
			}
		}
		buffer.append(text.substring(start));

		return buffer.toString();
	}    

	// non-generic OBD XML methods here
	public void tag(String tag, String ref) {
		if (ref == null)
			return;
		openStartElement(tag);
		addAttribute("about", escape(ref));
		endElement();
	}
	
	// non-generic OBD XML methods here
	public void tag(String tag, String ref, Graph g) {
		if (ref == null)
			return;
		openStartElement(tag);
		addAttribute("about", escape(ref));
		if (g != null) {
			closeStartElement();
			Node n = g.getNode(ref);
			if (n != null) {
				element("label",n.getLabel());
				tag("source",n.getSourceId());
				//nodeElements(n);
			}
			endElement(tag);
		}
		else {
			endElement();			
		}
	}

	public void node(Node node) {
		
		openStartElement("Node");
		addAttribute("id", node.getId());
		closeStartElement();
		
		nodeElements(node);
		
		endElement("Node");
		
	}
	
	public void nodeElements(Node node) {
		
		element("label",node.getLabel());
		
		tag("source",node.getSourceId());
		for(Statement s : node.getStatements()) {
			statement(s);
		}
	}

	public void statement(Statement s) {
		statement(null,s);
	}
	
	public void statement(Graph g, Statement s) {
		String elt = s instanceof LinkStatement ? "LinkStatement" : "LiteralStatement";
		openStartElement(elt);
		addAttribute("isInferred",s.isInferred());
		addAttribute("appliesToAllInstancesOf",s.isAppliesToAllInstancesOf());
		addAttribute("hasIntersectionSemantics",s.isIntersectionSemantics()); // TODO: unify
		addAttribute("hasUnionSemantics",s.isUnionSemantics());
		InstanceQuantifier iq = s.getInstanceQuantifier();
		closeStartElement();
		if (iq != null) {
			openStartElement("instanceQuantifier");
			//addAttribute("isExistential",iq.isExistential());
			//addAttribute("isUniversal",iq.isUniversal());
			addAttribute("minCardinality",iq.getMinCardinality());
			addAttribute("maxCardinality",iq.getMaxCardinality());
			endElement();
		}
		tag("node",s.getNodeId(),g);
		tag("relation",s.getRelationId(),g);
		tag("positedBy",s.getPositedByNodeId(),g);
		tag("target",s.getTargetId(),g);
		if (s instanceof LiteralStatement) {
			LiteralStatement ls = (LiteralStatement)s;
			openStartElement("value");
			addAttribute("datatype",ls.getDatatype());
			closeStartElement();
			sb.append(escape(ls.getValue().toString()));
			endElement("value");
		}
		tag("source",s.getSourceId(),g);
		tag("context",s.getContextId(),g);

		if (s.getSubStatements().size() > 0) {
			for(Statement ss : s.getSubStatements()) {
				statement(ss);
			}
		}

		endElement(elt);
	}
	
	public void description(CompositionalDescription desc) {
		if (desc.getPredicate().equals(Predicate.ATOM)) {
			element("Atom",desc.getNodeId());
			return;
		}
		openStartElement("CompositionalDescription");
		tag("about", desc.getId());
		addAttribute("predicate",desc.getPredicate().toString());
		closeStartElement();
		if (desc.getPredicate().equals(Predicate.RESTRICTION)) {
			startElement("restriction");
			statement(desc.getRestriction());
			endElement("restriction");
		}
		for (CompositionalDescription arg : desc.getArguments()) {
			description(arg);
		}
		endElement("CompositionalDescription");
		
	}
}
