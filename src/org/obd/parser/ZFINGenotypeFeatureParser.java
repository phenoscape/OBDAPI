package org.obd.parser;

import java.util.Collection;

import org.obd.model.Node;
import org.obd.model.Node.Metatype;
import org.obd.query.ComparisonQueryTerm.Operator;


/**
 * parses ZFIN-produced files for linking genotype to gene
 * <p>
 * @see http://zfin.org/data_transfer/Downloads/genotype_features.txt
 * @author cjm
 *
 */
public class ZFINGenotypeFeatureParser extends ZFINTabularParser {

	@Override
	public Boolean canParse(String fileName) {		// TODO Auto-generated method stub
		return fileName.contains("genotype_features.txt");
	}
	
	@Override
	public String[] requires() {
		return new String[]{"sequence"};
	}

	@Override
	public String getDefaultURL() {
		return "http://zfin.org/data_transfer/Downloads/genotype_features.txt";
	}

	public ZFINGenotypeFeatureParser() {
		super();
	}

	public ZFINGenotypeFeatureParser(String path) {
		super(path);
	}
	

	public void parseColVals(String[] colVals) throws Exception {
		if (colVals.length < 6) {
			throw new Exception("not enough vals: "+currentLine);
		}
		String gtId = createId(colVals[0]);

		String gtLabel = colVals[1];
		String gtUniqueLabel = colVals[2]; // unused
		String alleleId = createId(colVals[3]);
		String alleleSym = colVals[4];
		String alleleAbbrev = colVals[5]; // unused
		String alleleTypeLabel = colVals[6]; // allele type (from SO?)
		String alleleDisplayTypeLabel = colVals[6]; // not used
		

		String geneLabel = null;
		String geneId = null;
		if (colVals.length > 7) {
			geneLabel = colVals[8];
			geneId = createId(colVals[9]);
		}

		// genotype : variant_of gene
		Node gtNode = addFeatureNode(gtId);
		gtNode.setLabel(gtLabel);		
		addAllSomeLink(gtId,relationVocabulary.is_a(),GENOTYPE_ID,src); // genotype


		// allele type - from SO
		Collection<Node> alleleTypeNodes = 
			shard.getNodesBySearch(alleleTypeLabel, Operator.EQUAL_TO);
		Node alleleTypeNode = null;
		for (Node n : alleleTypeNodes) {
			if (n.getSourceId().equals("sequence")) {
				if (alleleTypeNode != null)
					throw new Exception("multiple matching nodes "+alleleTypeNode+" "+n);
				alleleTypeNode = n;
			}
		}
		String alleleTypeNodeId = ALLELE_ID; // allele - default
		if (alleleTypeNode == null) {
			warn("cannot find "+alleleTypeLabel+" in SO");
			//throw new Exception("no matching nodes "+typeLabel);
		}
		else {
			alleleTypeNodeId = alleleTypeNode.getId();
		}

		// Allele
		Node alleleNode = addFeatureNode(alleleId);
		alleleNode.setLabel(alleleSym);
		addAllSomeLink(alleleId,relationVocabulary.is_a(),alleleTypeNodeId,src);

		// Gene
		if (geneId != null) {
			addAllSomeLink(alleleId,relationVocabulary.variant_of(),geneId,src);

			Node geneNode = addFeatureNode(geneId);
			gtNode.setMetatype(Metatype.CLASS);
			gtNode.setSourceId(src);
			if (geneLabel != null)
				geneNode.setLabel(geneLabel);
			addAllSomeLink(alleleId,relationVocabulary.is_a(),GENE_ID,src);
			addAllSomeLink(gtId, relationVocabulary.variant_of(),
					geneId, src);	
		}
	}
	
	public Node addFeatureNode(String id) {
		Node node = super.addFeatureNode(id);
		node.setMetatype(Metatype.CLASS);
		node.setSourceId(src);
		this.addInOrganismLink(id, taxId, src);
		return node;
	}


	
}
