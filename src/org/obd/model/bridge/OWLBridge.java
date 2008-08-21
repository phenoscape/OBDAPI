package org.obd.model.bridge;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.bbop.dataadapter.DataAdapterException;
import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obo.datamodel.OBOSession;
import org.obo.owl.dataadapter.OWLAdapter;
import org.obo.owl.datamodel.impl.AxiomAnnotationBasedOWLMetadataMapping;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.OWLXMLOntologyFormat;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyFormat;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.SimpleURIMapper;

public class OWLBridge {



	public static OWLOntology graph2owlOntology(Graph graph, OWLOntologyManager manager, String uri) {

		OBOSession session = OBOBridge.graph2obosession(graph);
		OWLAdapter ad = new OWLAdapter();
		ad.setManager(manager);
		OWLAdapter.OWLAdapterConfiguration config = new OWLAdapter.OWLAdapterConfiguration();

		//OWLAdapterConfiguration config = (OWLAdapterConfiguration)ad.getConfiguration();
		
		if (uri == null)
			uri = "http://purl.org/obo/all";
		URI ontologyURI = URI.create(uri);
		AxiomAnnotationBasedOWLMetadataMapping mapping = 
			new AxiomAnnotationBasedOWLMetadataMapping();
		config.addMetadataMapping(mapping);
		config.setAllowLossy(true);
		ad.setConfiguration(config);
		OWLOntology owlOntology;
		try {
			owlOntology = ad.obo2owl(session, ontologyURI);
		} catch (DataAdapterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return owlOntology;
	}

	public static CharSequence toOWLString(Collection<Statement> stmts, String uriString) {
		Graph g = new Graph(stmts);
		return toOWLString(g, uriString);
	}
	public static String toOWLString(Collection<Statement> stmts) {
		Graph g = new Graph(stmts);
		return toOWLString(g);
	}
	public static CharSequence toOWLString(Node node) {
		Graph g = new Graph();
		g.addNode(node);
		return toOWLString(g);
	}

	public static String toOWLString(Graph graph) {
		return toOWLString(graph, null);
	}

	public static String toOWLString(Graph graph, String uri) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology oo = OWLBridge.graph2owlOntology(graph, manager, uri);
		String owlString = null;
		File tempFile;
		try {
			tempFile = File.createTempFile("graph","owl");
			OWLBridge.writeOwlOntology(oo, manager, tempFile.toURL().toString());
			owlString =  BridgeUtil.readFileAsString(tempFile);
			tempFile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return owlString;

	}



	public static void writeOwlOntology(OWLOntology oo, 
			OWLOntologyManager manager, String file) {
		writeOwlOntology(oo, manager, file, 
				new OWLXMLOntologyFormat());
	}
	public static void writeOwlOntology(OWLOntology oo, OWLOntologyManager manager, String file, OWLOntologyFormat fmt) {
		// Create a physical URI which can be resolved to point to where our ontology will be saved.
		// TODO: make this smarter. absolute vs relative path
		URI physicalURI;
//		if (!file.contains("file:"))
//		file = "file:"+file;
		physicalURI = URI.create(file);

		//URI ontologyURI = URI.create("http://purl.org/obo/all");

		// Set up a mapping, which maps the ontology URI to the physical URI
		//SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
		SimpleURIMapper mapper = new SimpleURIMapper(oo.getURI(), physicalURI);
		manager.addURIMapper(mapper);
		manager.setPhysicalURIForOntology(oo, physicalURI);

		try {
			manager.saveOntology(oo, fmt);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}



}
