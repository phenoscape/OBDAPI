package org.obd.parser;

import java.util.HashSet;

import org.obd.model.CompositionalDescription;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.Node.Metatype;


/**
 * Parses genotype phenotype tabdel file from ZFIN
 * @see ZFINGenotypePhenotypeOBOParser
 * @author cjm
 *
 */
@Deprecated
public class ZFINGenotypePhenotypeParser extends ZFINTabularParser {

	protected String src = "ZFIN";
	private String taxId = "NCBITax:7955";
	
	protected String defaultURL = 
		"http://zfin.org/data_transfer/Downloads/phenotype.txt";


	public ZFINGenotypePhenotypeParser() {
		super();
	}

	public ZFINGenotypePhenotypeParser(String path) {
		super(path);
	}
	

	public void parseColVals(String[] colVals) throws Exception {
		index();
		if (colVals.length < 6) {
			throw new Exception("not enough vals: "+currentLine);
		}
		String gtId = createId(colVals[0]);
		String gtLabel = colVals[1];
		
		String startId = colVals[2];
		String startLabel = colVals[3];
		String endId = colVals[4];
		String endLabel = colVals[5];
		String entityId = colVals[6];
		String entityLabel = colVals[7];
		String qualityId = null;
		String qualityLabel = colVals[9];
		String ab = colVals[12];
		String pubId = colVals[13];

		Node gtNode = addNode(gtId);
		gtNode.setLabel(gtLabel);		
		// we should have this
		//addLink(gtId,"OBO_REL:is_a",GENOTYPE_ID,src); // genotype

		startId = lookup(startId,startLabel);
		endId = lookup(endId,endLabel);
		entityId = lookup(entityId, entityLabel);
		qualityId = lookup(qualityId, qualityLabel);
		
		HashSet<LinkStatement> diffs = new HashSet<LinkStatement>();
		if (startId != null && !startLabel.equals("Unknown")) {
			LinkStatement d = new LinkStatement();
			d.setRelationId("OBO_REL:starts_during");
			d.setTargetId(entityId);
			diffs.add(d);
		}
		if (endId != null && !endLabel.equals("Unknown")) {
			LinkStatement d = new LinkStatement();
			d.setRelationId("OBO_REL:ends_during");
			d.setTargetId(entityId);
			diffs.add(d);
		}
		if (entityId != null) {
			LinkStatement d = new LinkStatement();
			d.setRelationId("OBO_REL:inheres_in");
			d.setTargetId(entityId);
			diffs.add(d);
		}
		else {
			return; 
		}
		if (qualityId != null) {
			CompositionalDescription desc = new CompositionalDescription(qualityId, diffs);

			String phenoId = desc.generateId();
			desc.setId(phenoId);
			graph.addStatements(desc);


			LinkStatement annot = new LinkStatement();
			annot.setNodeId(gtId);
			annot.setRelationId("OBO_REL:influences");
			
			LinkStatement pubLink = new LinkStatement();
			pubLink.setRelationId("oban:has_data_source");
			pubLink.setTargetId(pubId);
			annot.addSubStatement(pubLink);
			
			LinkStatement abLink = new LinkStatement();
			pubLink.setRelationId("oban:assigned_by");
			pubLink.setTargetId("ZFIN");
			annot.addSubStatement(abLink);
			
			annot.setTargetId(phenoId);
			annot.setSourceId(src);
			this.addStatement(annot);
		}
		else {
			warn("cannot find quality: "+qualityLabel);
		}
		
	}
	
	public Node addNode(String id) {
		Node node = super.addNode(id);
		node.setMetatype(Metatype.CLASS);
		node.setSourceId(src);
		addInOrganismLink(id, taxId, src);	
		return node;
	}

	
	
}
