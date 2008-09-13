package org.obd.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.Node.Metatype;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;

/**
 * parses MGI-produced files for linking genotype to phenotype
 * <p>
 *
 * @see 	ftp://ftp.informatics.jax.org/pub/reports/MGI_PhenoGenoMP.rpt
 * @author cjm
 *
 */
public class MGIGenotypePhenotypeParser extends TabularInfoParser {

	protected String src = "MGI";
	protected String defaultURL = 
		"ftp://ftp.informatics.jax.org/pub/reports/MGI_PhenoGenoMP.rpt";
	private String taxId = ncbitaxId("10090");

	protected Map<String,Integer> label2idnum = null;
	int maxIdnum = 0; // current maximum local ID; next assigned will be maxIdnum+1


	public MGIGenotypePhenotypeParser() {
		super();
	}

	public MGIGenotypePhenotypeParser(String path) {
		super(path);
	}
	
	public Boolean canParse(String fileName) {		// TODO Auto-generated method stub
		return fileName.endsWith("MGI_PhenoGenoMP.rpt");
	}
	@Override
	public String getDefaultURL() {
		return "ftp://ftp.informatics.jax.org/pub/reports/MGI_PhenoGenoMP.rpt";
	}


	// TODO - read existing IDs from database
	public String createId(String label) {
		Integer num;
		if (label2idnum == null) {
			label2idnum = new HashMap<String,Integer>();
			Collection<Node> nodes;
			try {
				nodes = dataShard.getNodesBySearch("OBD:MGI_GT",Operator.STARTS_WITH, "MGI", AliasType.ID);
				for (Node n : nodes) {
					int idnum = Integer.parseInt(n.getId().replaceFirst("OBD:MGI_GT:", ""));
					label2idnum.put(n.getLabel(),idnum);
					if (idnum > maxIdnum)
						maxIdnum = idnum;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (label2idnum.containsKey(label))
			num = label2idnum.get(label);
		else {
			maxIdnum++;
			num = maxIdnum;
			label2idnum.put(label, num);
		}
		//System.out.println(num+" -> "+label);
		return "OBD:MGI_GT:"+num;
	}


	public void parseColVals(String[] colVals) throws Exception {
		if (colVals.length < 4) {
			throw new Exception("not enough vals: "+currentLine);
		}
		String gtLabel = colVals[0];
		String involves = colVals[1];
		String mpId = colVals[2];
		String pmid = pubmedId(colVals[3]);
		String geneIdsAtom = colVals.length < 5 ? "" : colVals[4]; // can be blank

		String gtId = createId(gtLabel);


		Node gtNode = addFeatureNode(gtId);
		gtNode.setLabel(gtLabel);		

		if (mpId != null) {

			LinkStatement annot = new LinkStatement();
			annot.setNodeId(gtId);
			annot.setRelationId(relationVocabulary.influences());

			if (pmid != null) {
				LinkStatement pubLink = new LinkStatement();
				pubLink.setRelationId("oban:has_data_source");
				pubLink.setTargetId(pmid);
				annot.addSubStatement(pubLink);
			}

			if (!involves.equals("")) {
				LiteralStatement sl = new LiteralStatement();
				sl.setRelationId("dc:comment");
				sl.setValue(involves);
				annot.addSubStatement(sl);
			}

			LinkStatement abLink = new LinkStatement();
			abLink.setRelationId("oban:assigned_by");
			abLink.setTargetId("MGI");
			annot.addSubStatement(abLink);

			addAllSomeLink(gtId,relationVocabulary.is_a(),GENOTYPE_ID,src); // genotype
			for (String geneId : geneIdsAtom.split(",")) {
				addAllSomeLink(gtId,relationVocabulary.variant_of(),geneId,src);
			}

			annot.setTargetId(mpId);
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

