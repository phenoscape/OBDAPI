package org.obd.model.bridge;

import java.util.Collection;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;


public class OBDXMLBridge {
	
	public static String toXML(Node node) {
		// TODO: use JAXB/XMLBeans
		OBDXMLWriter w = new OBDXMLWriter();
		w.node(node);
		return w.toString();
	}
	public static String toXML(CompositionalDescription desc) {
		// TODO: use JAXB/XMLBeans
		OBDXMLWriter w = new OBDXMLWriter();
		w.description(desc);
		return w.toString();
	}
	public static String toXML(Statement s) {
		// TODO: use JAXB/XMLBeans
		OBDXMLWriter w = new OBDXMLWriter();
		w.statement(s);
		return w.toString();
	}
	public static String toXML(Collection<Statement> stmts) {
		// TODO: use JAXB/XMLBeans
		OBDXMLWriter w = new OBDXMLWriter();
		w.startElement("Statements");
		for (Statement s : stmts)
			w.statement(s);
		w.endElement("Statements");
		return w.toString();
	}
	public static CharSequence toXML(Graph g) {
		OBDXMLWriter w = new OBDXMLWriter();
		w.startElement("Graph");
		for (Node n : g.getNodes())
			w.node(n);
		for (Statement s : g.getStatements())
			w.statement(g,s);
		w.endElement("Graph");
		return w.toString();
	}
	

}
