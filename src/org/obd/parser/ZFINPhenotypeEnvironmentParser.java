package org.obd.parser;

import java.util.Collection;

import org.obd.model.Node;
import org.obd.model.Node.Metatype;
import org.obd.query.ComparisonQueryTerm.Operator;


/**
 * parses ZFIN-produced files for linking genotype to gene
 * <p>
 * @see http://zfin.org/data_transfer/Downloads/phenotype_environment.txt
 * @author cjm
 *
 */
public class ZFINPhenotypeEnvironmentParser extends TabularInfoParser {

	protected String src = "ZFIN";
	private String taxId = "NCBITax:7955";

	
	public ZFINPhenotypeEnvironmentParser() {
		super();
	}

	public ZFINPhenotypeEnvironmentParser(String path) {
		super(path);
	}
	

	public void parseColVals(String[] colVals) throws Exception {
		if (colVals.length < 6) {
			throw new Exception("not enough vals: "+currentLine);
		}
		String envId = createId(colVals[0]);

		String condGroup = colVals[1]; // eg morpholino
		String condContent = colVals[2]; // morpholino ID if morpholino
		String value = colVals[3];
		String unit = colVals[4];
		String comment = colVals[5];


		Node envNode = addEnvNode(envId);
		String label = condGroup+" "+condContent+" "+value+unit;
		envNode.setLabel(label);		
		//addAllSomeLink(gtId,"OBO_REL:is_a",GENOTYPE_ID,src); // genotype

		if (condGroup.equals("morpholino")) {
			String mid = createId(condContent);
			addLink(envId,"ZFIN:has_morpholino",mid,src);
		}
		
		String unitId;
		if (unit.equals("mM")) {
			// ...
		}
		
		if (!comment.equals("")) {
			this.addLiteral(envId, "dc:comment", comment, "xsd:string", src);
		}
	

	}
	
	public Node addEnvNode(String id) {
		Node node = super.addNode(id);
		node.setMetatype(Metatype.INSTANCE);
		node.setSourceId(src);
		addAllSomeLink(id, "OBO_REL:in_organism",
				taxId, src);	
		return node;
	}


	
}
