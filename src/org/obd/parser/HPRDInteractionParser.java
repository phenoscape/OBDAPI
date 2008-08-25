package org.obd.parser;

import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.Statement;

/**
 * Parses interaction annotations from tab-delimited text files from HPRD
 * <p>
 * populates symmetric pairwise interacts_with relations
 *
 * @author cjm
 * 
 */
public class HPRDInteractionParser extends TabularInfoParser {

	protected String src = "HPRD";

	public HPRDInteractionParser() {
		super();
	}
	public HPRDInteractionParser(String path) {
		super(path);
	}

	public void parseColVals(String[] colVals) {
		if (lineNum == 0)
			return;
		String proteinA = entrezgeneId(colVals[1]);
		String proteinB = entrezgeneId(colVals[3]);
		String exptType = entrezgeneId(colVals[4]);
		String pmids = entrezgeneId(colVals[5]);
		String rel = "OBO_REL:interacts_with";
		Statement s1 = addLink(proteinA, rel, proteinB, src);
		Statement s2 = addLink(proteinB, rel, proteinA, src);
		for (String pmid : pmids.split(",")) {
			pmid = pubmedId(pmid);
			Node pub = this.addFeatureNode(pmid);
			LinkStatement pubLink = new LinkStatement();
			pubLink.setRelationId("oban:has_data_source");
			pubLink.setTargetId(pmid);
			s1.addSubStatement(pubLink);
			s2.addSubStatement(pubLink);
		}
		for (String et : exptType.split(";")) {
			/*
			pmid = pubmedId(pmid);
			Node pub = this.addNode(pmid);
			LinkStatement pubLink = new LinkStatement();
			pubLink.setRelationId("oban:has_data_source");
			pubLink.setTargetId(pmid);
			s1.addSubStatement(pubLink);
			s2.addSubStatement(pubLink);
			*/
		}
	}
	



}
