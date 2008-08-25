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
public class ZFINGeneMorpholinoParser extends ZFINTabularParser {

	
	public ZFINGeneMorpholinoParser() {
		super();
	}

	public ZFINGeneMorpholinoParser(String path) {
		super(path);
	}
	

	public void parseColVals(String[] colVals) throws Exception {
		if (colVals.length < 6) {
			throw new Exception("not enough vals: "+currentLine);
		}
		String geneId = createId(colVals[0]);
		String morphId = createId(colVals[2]);
		
		String morphLabel = colVals[3];
		String morphSeq = colVals[4];

		String notes = colVals[5];

		

		Node mNode = addFeatureNode(morphId);
		mNode.setLabel(morphLabel);		
		addAllSomeLink(morphId,relationVocabulary.is_a(),"SO:0000034",src); // morpholino_oligo

		addAllSomeLink(morphId,relationVocabulary.variant_of(),geneId,src);
		
		addLiteral(morphId, "SO:has_sequence", morphSeq, "xsd:string", src);
		if (notes != "")
			addLiteral(morphId, "dc:comment", notes, "xsd:string", src);

	
	}
	
	public Node addFeatureNode(String id) {
		Node node = super.addFeatureNode(id);
		node.setMetatype(Metatype.CLASS);
		node.setSourceId(src);
		addAllSomeLink(id, IN_ORGANISM,
				taxId, src);	
		return node;
	}


	
}
