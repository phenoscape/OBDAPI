package org.obd.parser;

import java.util.HashMap;
import java.util.Map;

import org.obd.model.LinkStatement;
import org.obd.model.LiteralStatement;
import org.obd.model.Node;
import org.obd.model.NodeAlias;
import org.obd.model.Statement;
import org.obd.model.Node.Metatype;

/**
 * Parses GO-Style gene association files
 * 
 * @author cjm
 * 
 * TODO: DRY - see also ZFINGenotypePhenotypeParser
 *
 */
public class GeneAssociationParser extends TabularInfoParser {

	public GeneAssociationParser() {
		super();
	}

	public GeneAssociationParser(String path) {
		super(path);
	}

	@Override
	public Boolean canParse(String fileName) {		// TODO Auto-generated method stub
		return fileName.contains("gene_association.") && !fileName.endsWith(".gz");
	}


	protected String lastSubjectID = null;

	public void parseColVals(String[] colvals) throws Exception {
		if (colvals[0].startsWith("!"))
			return;
		LinkStatement ann = new LinkStatement();

		String dbspace = colvals[0];
		String localID = colvals[1];
		String name = colvals[2];
		String subjectID = dbspace+":"+localID;
		String qual = colvals[3];
		String objectID = colvals[4];
		String refVal = colvals[5];
		String evCode = colvals[6];
		String with = colvals[7];
		String aspect = colvals[8];
		String fullName = colvals[9];
		String synVal = colvals[10];
		String type = colvals[11]; 
		String taxID = colvals[12]; // TODO - multi-species
		String date = colvals[13];
		String assignedBy = colvals[14];

		ann.setNodeId(subjectID);
		ann.setTargetId(objectID);
		ann.setSourceId(dbspace);	      
		String rel = "OBO_REL:has_role"; // TODO
		ann.setRelationId(rel);

		addStatement(ann);

		parseReferenceField(ann, refVal);
		parseEvidence(ann,evCode,with,refVal,assignedBy);
		if (!subjectID.equals(lastSubjectID)) {
			lastSubjectID = subjectID;
			Node node = addFeatureNode(subjectID);
			node.setLabel(name);
			parseSynonymField(ann,fullName,node);
			parseSynonymField(ann,synVal,node);
			parseTaxonField(ann,taxID,node);
		}

		parseQualifierField(ann,qual);
		// TODO parseTypeField(ann,type);
		parseDateField(ann,date);
		parseAssignedByField(ann,assignedBy);

	}

	protected void parseQualifierField(Statement ann, String qualField) {
		for(String q : qualField.split("\\|")) {
			if (q.equals(""))
				continue;

			if (q.equals("NOT")) {
				ann.setNegated(true);
			}
			else {
				System.err.println("Cannot handle qual:"+q);
				// TODO
			}
		}
	}

	protected void parseAspect(Statement ann, String aspect) {
		// needed for roundtripping?
	}

	protected void parseSynonymField(Statement ann, String synField, Node ae) {
		for(String s : synField.split("\\|")) {
			NodeAlias na = new NodeAlias(ae.getId(),s);
			this.addStatement(na);
		}
	}

	protected void parseReferenceField(Statement ann, String refField) {
		for(String s : refField.split("\\|")) {
			addSubStatement(ann,"oban:has_data_source", s);
		}
	}


	protected void parseDateField(Statement ann, String dateField) {
		if (dateField.equals(""))
			return;
		addSubStatementLiteral(ann,"oban:date", "xsd:string", dateField);
	}

	protected void parseAssignedByField(Statement ann, String abField) {
		addSubStatement(ann,"oban:assigned_by", abField);
	}
	protected void parseEvidence(Statement ann, String evCode, String withExpr, String refVal, String src) {
		Node evInst = new Node();
		evInst.setMetatype(Metatype.INSTANCE);
		this.addNode(evInst);

		String evId;
		evId = evInst.assignUniqueId(ann.getSkolemId(),refVal);
		//evId = evInst.assignAnonymousId(); // TODO - use a skolem instead
		addSubStatement(ann,"oban:has_evidence",evId);

		String[] withVals = withExpr.split("\\|");
		for (String s: withVals) {
			addLink(evId,"oban:with",s,src);
			//this.addNode(evInst);
		}

	}

	protected void parseTaxonField(Statement ann, String taxVal, Node node) {
		String[] taxIDs = taxVal.split("\\|");

		for (int i=0; i<taxIDs.length; i++) {
			String taxID = convertTaxonId(taxIDs[i]);
			if (i==0) {
				node.addStatement(new LinkStatement(ann.getNodeId(),
						IN_ORGANISM,
						taxID));
			}
			else {
				// multi-species interactions, 2ary taxon. TODO
				// makeLink(ann,null,taxObj);
			}
		}
	}

	protected String convertTaxonId(String taxId) {
		String[] toks = taxId.split(":",2);
		return ncbitaxId(toks[1]);
	}


}
