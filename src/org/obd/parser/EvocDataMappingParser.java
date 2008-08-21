package org.obd.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.query.impl.MutableOBOSessionShard;
import org.obo.datamodel.Dbxref;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.PropertyValue;

/**
 * Parses tab-delimited text files from the GAD project
 * <p>
 * This parser needs to have the shard set to the disease/phenotype ontology source
 * and the dataShard set to the repository of genes
 *
 * @author cjm
 * 
 */
public class EvocDataMappingParser extends Parser {

	protected String src = "EVOC";
	protected MutableOBOSessionShard oss;

	public EvocDataMappingParser() {
		super();
	}

	public EvocDataMappingParser(String path) {
		super(path);
	}




	public int numCols = 45;

	public void parse() throws Exception {
	
		oss = new MutableOBOSessionShard();
		oss.loadFile(path);
		
		OBOSession session = oss.getSession();
		
		for (IdentifiedObject io : session.getObjects()) {
			if (io instanceof OBOObject) {
				OBOObject oo = (OBOObject) io;
				if (oo.getName().equals("unclassifiable"))
					continue;
				Set<PropertyValue> pvs = oo.getPropertyValues();
				Map<String,String> libMap = new HashMap<String,String>();
				for (PropertyValue pv : pvs) {
					if (pv.getProperty().equals("cdnalib")) {
						String cdnalib = pv.getValue();
						String[] toks = cdnalib.split(" ");
						String libId = unigeneId(toks[toks.length-1]);
						libMap.put(libId,cdnalib);
					}
				}
				if (libMap.size() == 0)
					continue;
				String targetId = null;
				for (Dbxref x : oo.getDbxrefs()) {
					String xref = x.getDatabase() + ":" + x.getDatabaseID();
					IdentifiedObject targObj = session.getObject(xref);
					if (targObj != null) {
						targetId = targObj.getID();
					}
				}
				if (targetId == null) {
					Collection<Node> nodes = lookupTerm(oo.getName());
					if (nodes.size() > 0) {
						targetId = nodes.iterator().next().getId();
					}
				}
				if (targetId == null) {
					targetId = oo.getID(); // just use evoc ID
				}
				for (String libId : libMap.keySet()) {
					Node n = addNode(libId,libMap.get(libId));
					n.setSourceId(src);
					Statement s = new LinkStatement(libId,"OBO_REL:expressed_in",targetId);
					LinkStatement abLink = new LinkStatement();
					abLink.setRelationId("oban:assigned_by");
					abLink.setTargetId(src);
					s.addSubStatement(abLink);

					addStatement(s, src);
				}
			}
		}
		
	}
	

}
