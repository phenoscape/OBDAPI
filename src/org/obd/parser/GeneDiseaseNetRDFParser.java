package org.obd.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.obd.query.impl.MutableOBOSessionShard;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RSIterator;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;

/**
 * Parses tab-delimited text files from the GAD project
 * <p>
 * This parser needs to have the shard set to the disease/phenotype ontology source
 * and the dataShard set to the repository of genes
 *
 * @author cjm
 * 
 */
public class GeneDiseaseNetRDFParser extends Parser {

	protected String src = "EVOC";
	protected MutableOBOSessionShard oss;
	protected Model model;

	public GeneDiseaseNetRDFParser() {
		super();
	}

	public GeneDiseaseNetRDFParser(String path) {
		super(path);
	}




	public int numCols = 45;

	public void parse() throws Exception {
		
		model = ModelFactory.createMemModelMaker().createModel("test");
		
	   	File file = new File(path);
	   	//    	 Open the bloggers RDF graph from the filesystem
    	InputStream in = new FileInputStream(file);

    	model.read(in,null); // null base URI, since model URIs are absolute
    	in.close();

    	RSIterator si = model.listReifiedStatements();
    	while (si.hasNext()) {
    		ReifiedStatement s = (ReifiedStatement) si.next();
    	}
		
	}
	

}
