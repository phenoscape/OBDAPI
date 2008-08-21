package org.obd.parser;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Parses simple stanza-based metadata files
 * 
 * @author cjm
 *
 */
public abstract class StanzaInfoParser extends Parser {

	public class Stanza {
		Map<String,Collection<String>> tagValMap = new HashMap<String,Collection<String>>();
		List<String> tags = new LinkedList<String>();

		public void setTagVal(String col, String value) {
			tagValMap.put(col, Collections.singleton(value));
			tags.add(col);
		}
		public String getTagVal(String col) {
			if (tagValMap.containsKey(col))
				return tagValMap.get(col).iterator().next();
			else
				return null;
		}
		public Collection<String> getTagVals(String col) {
			if (tagValMap.containsKey(col))
				return tagValMap.get(col);
			else
				return null;
		}
		
		public String getId() {
			return getTagVal("id");
		}
		public List<String> getTags() {
			return tags;
		}
		public void setTags(List<String> tags) {
			this.tags = tags;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (String tag : tags) {
				sb.append(tag+":");
				for (String val :getTagVals(tag))
					sb.append(" "+val);
				sb.append("; ");
			}
			
			return sb.toString();
		}
	}

	protected List<Stanza> stanzas = new LinkedList<Stanza>();

	public StanzaInfoParser(String path) {
		super();
		this.path = path;
	}

	public StanzaInfoParser() {
		super();
	}

	public void parse() throws Exception {
		LineNumberReader lnr = 
			new LineNumberReader(new FileReader(path));
		Stanza stanza = new Stanza();
		stanzas.add(stanza);
		for (String line=lnr.readLine(); line != null; line = lnr.readLine()) {
			currentLine = line;
			String[] colVals = line.split("\t");

			if (colVals.length < 2) {
				if (stanza != null) {

					stanza = new Stanza();
					stanzas.add(stanza);
				}
			}
			else if (colVals.length == 2) {
				stanza.setTagVal(colVals[0], colVals[1]);
			}
			else {
				throw new Exception("two many tabs: "+line+"; Extra="+colVals[2]);
			}
		}
		translateStanzas();
	}

	protected String currentLine;
	public abstract void translateStanzas() throws Exception ;




}
