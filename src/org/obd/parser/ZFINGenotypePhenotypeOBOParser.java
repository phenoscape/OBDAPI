package org.obd.parser;

import java.util.HashSet;

import org.obd.model.CompositionalDescription;
import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.model.Node.Metatype;

/**
 * parses ZFIN-produced files for linking genotype to phenotype
 * <p>
 *
 * @see 	http://zfin.org/data_transfer/Downloads/pheno_obo.txt
 * @author cjm
 *
 */
public class ZFINGenotypePhenotypeOBOParser extends ZFINTabularParser {

	protected String defaultURL = 
		"http://zfin.org/data_transfer/Downloads/pheno_obo.txt";


	public ZFINGenotypePhenotypeOBOParser() {
		super();
	}

	public ZFINGenotypePhenotypeOBOParser(String path) {
		super(path);
	}

	public Boolean canParse(String fileName) {		// TODO Auto-generated method stub
		return fileName.contains("pheno_obo.txt");
	}


	public void parseColVals(String[] colVals) throws Exception {
		//index();
		if (colVals.length < 6) {
			throw new Exception("not enough vals: "+currentLine);
		}
		int n = 0;
		String gtId = createId(colVals[0]);
		String gtLabel = colVals[1];

		String startId = colVals[2];
		String endId = colVals[3];
		String entityId = colVals[4];
		String qualityId = colVals[5];
		String towardsId = colVals[6];
		String ab = colVals[7];
		String pubId = colVals[8];
		String environmentId = createId(colVals[9]);

		Node gtNode = addFeatureNode(gtId);
		gtNode.setLabel(gtLabel);		

		HashSet<LinkStatement> diffs = new HashSet<LinkStatement>();
		if (startId != null && !startId.equals("ZFS:0000000")) {
			LinkStatement d = new LinkStatement();
			d.setRelationId(relationVocabulary.starts_during());
			d.setTargetId(startId);
			diffs.add(d);
		}
		if (endId != null && !endId.equals("ZFS:0000000")) {
			LinkStatement d = new LinkStatement();
			d.setRelationId(relationVocabulary.ends_during());
			d.setTargetId(endId);
			diffs.add(d);
		}
		if (entityId != null) {
			LinkStatement d = new LinkStatement();
			d.setRelationId(relationVocabulary.inheres_in());
			d.setTargetId(entityId);
			diffs.add(d);
		}
		if (towardsId != null && !towardsId.equals("")) {
			LinkStatement d = new LinkStatement();
			d.setRelationId(relationVocabulary.towards());
			d.setTargetId(towardsId);
			diffs.add(d);
		}
		if (ab != null) {
			for (String qual : ab.split("/")) {
				LinkStatement d = new LinkStatement();
				d.setRelationId(relationVocabulary.has_qualifier());
				String patoId;
				if (qual.equals("normal"))
					patoId = "PATO:0000461";
				else if (qual.equals("absent"))
					patoId = "PATO:0000462";
				else if (qual.equals("present"))
					patoId = "PATO:0000467";
				else
					patoId = "PATO:0000460"; // abnormal
				d.setTargetId(patoId);
				diffs.add(d);
			}
		}
		else {
			return; 
		}
		if (qualityId != null) {
			CompositionalDescription desc = new CompositionalDescription(qualityId, diffs);
			desc.setSourceId(src);
			String phenoId = desc.generateId();
			desc.setId(phenoId);
			graph.addStatements(desc);


			LinkStatement annot = new LinkStatement();
			annot.setNodeId(gtId);
			annot.setRelationId(relationVocabulary.influences());

			if (!pubId.equals("")) {
				LinkStatement pubLink = new LinkStatement();
				pubLink.setRelationId("oban:has_data_source");
				pubLink.setTargetId(pubId);
				annot.addSubStatement(pubLink);
			}

			if (!environmentId.equals("")) {
				LinkStatement envLink = new LinkStatement();
				envLink.setRelationId("OBO_REL:has_environment");
				envLink.setTargetId(environmentId);
				annot.addSubStatement(envLink);
			}


			LinkStatement abLink = new LinkStatement();
			abLink.setRelationId("oban:assigned_by");
			abLink.setTargetId("ZFIN");
			annot.addSubStatement(abLink);

			annot.setTargetId(phenoId);
			annot.setSourceId(src);
			this.addStatement(annot);
		}
		else {
			warn("cannot find quality");
		}

	}

	public Node addFeatureNode(String id) {
		Node node = super.addFeatureNode(id);
		node.setMetatype(Metatype.CLASS);
		node.setSourceId(src);
		addInOrganismLink(id, taxId, src);	
		return node;
	}


}
