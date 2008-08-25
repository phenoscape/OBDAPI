package org.obd.parser;


/**
 * Parses tab-delimited text files from NCBI Homologene
 * <p>
 * Each homologene ID is treated as a hypothetical ancestral gene, and linked
 * to other genes (denoted via NCBI Gene IDs) via descended_from relations
 *
 * @author cjm
 * 
 */
public class HomologeneParser extends TabularInfoParser {

	protected String src = "NCBI:homologene";
	
	public HomologeneParser() {
		super();
		
	}
	public HomologeneParser(String path) {
		super(path);
	}
	
	@Override
	public Boolean canParse(String fileName) {		// TODO Auto-generated method stub
		return fileName.contains("homologene");
	}
	@Override
	public String getDefaultURL() {
		return "ftp://ftp.ncbi.nih.gov/pub/HomoloGene/current/homologene.data";
	}



	/* (non-Javadoc)
	 * 
	 * - HomolsetID
	 * - TaxID
	 * - GeneID
	 * - Symbol
	 * - Prtn GI
	 * - Prtn ACC
	 * @see org.obd.parser.TabularInfoParser#parseColVals(java.lang.String[])
	 */
	public void parseColVals(String[] colVals) {
		addAllSomeLink(entrezgeneId(colVals[2]), "OBO_REL:descended_from", // TODO
				homologeneId(colVals[0]), src);		
	}

	
}
