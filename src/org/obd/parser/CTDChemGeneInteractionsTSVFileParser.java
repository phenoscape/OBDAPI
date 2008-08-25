package org.obd.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.query.LinkQueryTerm;
import org.obd.query.ComparisonQueryTerm.Operator;

/**
 * Parses tab-delimited text files from the CTD project
 * <p>
 * http://ctd.mdibl.org/downloads
 * 
 * This parser needs to have the shard set to the chemical ontology source
 * and the dataShard set to the repository of genes
 *
 * @author cjm
 * 
 */
public class CTDChemGeneInteractionsTSVFileParser extends TabularInfoParser {

	protected String src = "CTD";
	protected Map<String,Collection<Node>> label2node = new HashMap<String,Collection<Node>>();
	protected Map<String,Collection<Node>> xref2node = new HashMap<String,Collection<Node>>();

	public CTDChemGeneInteractionsTSVFileParser() {
		super();
	}

	public CTDChemGeneInteractionsTSVFileParser(String path) {
		super(path);
	}

	public int numCols = 9;

	public void parseColVals(String[] colVals) throws Exception {
		if (lineNum == 1) {
			return;
		}
		String chemLabel = colVals[0];
		String chemMeshId = colVals[1];
		String chemCasId = colVals[2];
		String geneNcbiId = entrezgeneId(colVals[4]);
		String ixnTxt = colVals[7]; // TODO : parse this
		String[] pmids = colVals[8].split("\\|");
		
		Collection<Node> chemNodes = lookupId("ChemIDplus:"+chemCasId);
		if (chemNodes.size() == 0)
			chemNodes = lookupTerm(chemLabel);
		// TODO - use MESH
		if (chemNodes.size() == 0)
			return;

		String chebiId = chemNodes.iterator().next().getId();
		LinkStatement annot = new LinkStatement();
		annot.setNodeId(geneNcbiId);
		annot.setRelationId("OBO_REL:interacts_with");
		annot.setTargetId(chebiId);
		annot.setSourceId(src);
		annot.addSubLiteralStatement("dc:description", ixnTxt);
		for (String pmid : pmids) {
			annot.addSubLinkStatement("oban:has_data_source", pubmedId(pmid));
		}
		addStatement(annot);


	}

	public Collection<Node> lookupTerm(String label) {
		Collection<Node> nodes = new HashSet<Node>();
		if (label2node.containsKey(label))
			nodes = label2node.get(label);
		else {
			try {
				nodes = shard.getNodesBySearch(label, Operator.EQUAL_TO);
				label2node.put(label, nodes);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return nodes;
	}
	
	public Collection<Node> lookupId(String xref) {
		Collection<Node> nodes = new HashSet<Node>();
		if (xref2node.containsKey(xref))
			nodes = xref2node.get(xref);
		else {
			try {
				nodes = shard.getNodesByQuery(new LinkQueryTerm(xref));
				xref2node.put(xref, nodes);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return nodes;
	}




}
