package org.obd.parser;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class GZIPParser extends Parser {

	@Override
	public Boolean canParse(String fileName) {		// TODO Auto-generated method stub
		return fileName.endsWith(".gz");
	}

	@Override
	public void parse() throws IOException, Exception {
		FileInputStream fis = new FileInputStream(path);
		String unzippedFakePath = path.replace(".gz", "");
		Parser p = Parser.createParser((String)null, unzippedFakePath);
		p.setInputStream(new GZIPInputStream(fis));
		p.setPath(null);
		p.parse();
		setGraph(p.getGraph());
	}
	
	

}
