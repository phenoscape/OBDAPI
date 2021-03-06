package org.obd.parser;

import org.obd.query.impl.MutableOBOSessionShard;

/**
 * wraps an obo parser
 * 
 * @author cjm
 *
 */
public class OBOFormatParser extends Parser {


	public OBOFormatParser(String path) {
		super();
		this.path = path;
	}

	public OBOFormatParser() {
		super();
	}
	
	public Boolean canParse(String fileName) {		// TODO Auto-generated method stub
		return fileName.endsWith(".obo");
	}


	public void parse() throws Exception {
		MutableOBOSessionShard moss = new MutableOBOSessionShard();
		moss.loadFile(path);
		graph = moss.getGraph();
	}





}
