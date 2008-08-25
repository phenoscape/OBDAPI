package org.obd.parser;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * base class for all tabdel based formats
 * @author cjm
 *
 */
public abstract class TabularInfoParser extends Parser {

	protected int lineNum = 0;
	protected String currentLine;


	public TabularInfoParser() {
		super();
	}

	public TabularInfoParser(String path) {
		super();
		this.path = path;
	}

	public void parse() throws Exception {
		LineNumberReader lnr;
		if (path == null) {
			lnr = new LineNumberReader(new InputStreamReader(inputStream));
		}
		else {
			lnr = 
				new LineNumberReader(new FileReader(path));
		}
		for (String line=lnr.readLine(); line != null; line = lnr.readLine()) {
			logger.fine("Parsing lineNum: "+lineNum);
			currentLine = line;
			String[] colVals = line.split("\t",-1); // include trailing separators
			parseColVals(colVals);
			lineNum++;
		}

	}

	public abstract void parseColVals(String[] colVals) throws Exception ;

}
