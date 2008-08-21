package org.obd.parser;

import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.NodeAlias;
import org.obd.model.Statement;
import org.obd.model.LiteralStatement.Datatype;


/**
 * Parses tab-delimited files from NCBI Gene (aka EntrezGene, formerly LocusLink)
 * @author cjm
 *
 */
public class NCBIGeneParser extends TabularInfoParser {

	protected String src = "NCBI:gene";
	
	public NCBIGeneParser() {
		super();
	}
	public NCBIGeneParser(String path) {
		super(path);
	}

	public void parseColVals(String[] colVals) {
		String taxId = ncbitaxId(colVals[0]);
		String id = entrezgeneId(colVals[1]);
		String sym = colVals[2];
		String synStr = colVals[4];
		String xrefStr = colVals[5];
		String chrom = colVals[6];
		String mapLoc = colVals[7];
		String desc = colVals[8];
		Node node = addNode(id);
		node.setSourceId(src);
		addIsAGeneLink(id, src);
		addInOrganismLink(id, taxId, src);	
		if (!sym.equals('-'))
			node.setLabel(sym);
		if (!synStr.equals('-')) {
			for (String syn : synStr.split("\\|")) {
				Statement s = new NodeAlias(id,syn);
				s.setSourceId(src);
				addStatement(s,src);
			}
		}
		if (!xrefStr.equals('-')) {
			for (String x : xrefStr.split("\\|")) {
				addXref(id,x,src);
			}
		}
		if (!desc.equals('-')) {
			Statement s = new LiteralStatement(id,"dc:description",desc,Datatype.STRING); // TODO
			addStatement(s,src);
		}
	}


	
}
