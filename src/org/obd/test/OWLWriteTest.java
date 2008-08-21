package org.obd.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.obd.model.Graph;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.bridge.OWLBridge;
import org.obd.query.ExistentialQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.DefaultOntologyFormat;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OWLWriteTest extends AbstractOBDTest{
	
	public OWLWriteTest(String n){
		super(n);
	}
	
	
	public void testOWLWrite() throws SQLException, ClassNotFoundException {

		
		
		
		for (Node sourceNode : this.getShard().getLinkStatementSourceNodes()){
			if (!sourceNode.getId().trim().equals("")){
				List<Statement> statements = new ArrayList<Statement>();
				QueryTerm qt = new LinkQueryTerm();
				qt.setInferred(false);
				qt.setPositedBy(new ExistentialQueryTerm());
				qt.setSource(sourceNode.getId());
				
				for (Statement s : this.shard.getLinkStatementsByQuery(qt)){
					statements.add(s);
				}
				
				System.out.println(sourceNode.getId() + " SIZE: " + statements.size());
				
				if (statements.size()>1){
					String fileName = sourceNode.getId().replace(":", "_");
					System.out.println("FileName:" + fileName);
					this.writeOWL(statements, fileName);
				}
			}
			
			
		}


	}
	
	private void writeOWL(Collection<Statement> statements,String fileName){
		Graph graph = new Graph();
		graph.addStatements(statements);
		fileName = "file:///tmp/" + fileName + ".owl";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology oo = OWLBridge.graph2owlOntology(graph, manager, null);
		DefaultOntologyFormat fmt = new DefaultOntologyFormat();
		OWLBridge.writeOwlOntology(oo, manager,fileName,fmt);
		System.out.println("Writing to " + fileName);
	}
	

}