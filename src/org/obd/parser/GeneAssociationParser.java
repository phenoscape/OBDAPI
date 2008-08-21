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

	protected String src = "ZFIN";
	private String taxId = "NCBITax:7955";
	protected String defaultURL = 
		"http://zfin.org/data_transfer/Downloads/pheno_obo.txt";


	public GeneAssociationParser() {
		super();
	}

	public GeneAssociationParser(String path) {
		super(path);
	}

	public String createId(String local) {
		return "ZFIN:"+local;
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
		String ref = colvals[7];
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
	      
		addStatement(ann);

		parseReferenceField(ann, refVal);
		parseEvidence(ann,evCode,ref);
		String rel = "OBO_REL:has_role";
  		ann.setRelationId(rel);
		if (!subjectID.equals(lastSubjectID)) {
			lastSubjectID = subjectID;
			Node node = addNode(subjectID);
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
	
	protected void addSubStatement(Statement ann, String relationId, String targetId) {
		if (targetId.equals(""))
			return;
		LinkStatement ss = new LinkStatement();
		ss.setRelationId(relationId);
		ss.setTargetId(targetId);
		ann.addSubStatement(ss);		
	}
	
	protected void addSubStatementLiteral(Statement ann, String relationId, String dt, String val) {
		if (val.equals(""))
			return;
		LiteralStatement ss = new LiteralStatement();
		ss.setRelationId(relationId);
		ss.setValue(val);
		ss.setDatatype(dt);
		ann.addSubStatement(ss);		
	}

	protected void parseDateField(Statement ann, String dateField) {
		if (dateField.equals(""))
			return;
		addSubStatementLiteral(ann,"oban:date", "xsd:string", dateField);
	}

	protected void parseAssignedByField(Statement ann, String abField) {
		addSubStatement(ann,"oban:assigned_by", abField);
	}
	protected void parseEvidence(Statement ann, String evCode, String withExpr) {
		/*
		Pattern p = Pattern.compile("|");
		String[] withVals = p.split(withExpr);
		String evidenceID = "_:ev"+nextEvidenceID;
			nextEvidenceID++;
		Instance ev = 
			(Instance) session.getObjectFactory().
			createObject(evidenceID, OBOClass.OBO_INSTANCE, true);
		session.addObject(ev);
		OBOClass evCodeClass = TermUtil.castToClass(getSessionLinkedObject(evCode));
		ev.setType(evCodeClass);
		//ev.setType(evCode);
		for (String s: withVals) {
			LinkedObject withObj =
				(LinkedObject) session.getObjectFactory().
				createObject(s, OBOClass.OBO_INSTANCE, true);
			OBOProperty ev2withRel = null;
			// TODO: 
			session.getObjectFactory().createPropertyValue("with", s);
			Link ev2with = new InstancePropertyValue(ev);
			ev2with.setParent(withObj);
			ev2with.setChild(ev);
		}
		ann.addEvidence(ev);
		*/
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
	


	public Node addNode(String id) {
		Node node = super.addNode(id);
		node.setMetatype(Metatype.CLASS);
		node.setSourceId(src);
		addInOrganismLink(id, taxId, src);	
		return node;
	}

	public String lookup(String id) {
		return lookup(id,null);
	}

	public String lookup(String id, String label) {
		if (id != null) {
			if (termIndex.containsKey(id))
				return termIndex.get(id);
			if (termIndex.containsKey(createId(id)))
				return termIndex.get(createId(id));
		}
		if (label != null)
			if (termIndex.containsKey(label))
				return termIndex.get(label);
		return null;
	}

	Map <String,String> termIndex = new HashMap<String,String>();
	boolean isIndexed = false;
	public void index() {
		if (isIndexed)
			return;
		for (Node n : shard.getNodes()) {
			String nid = n.getId();
			termIndex.put(n.getLabel(), nid);
			//System.out.println("indexing: "+n.getLabel()+" "+nid);
			for (Statement s : shard.getStatementsForNode(nid)) {
				if (s instanceof LinkStatement) {
					LinkStatement ls = (LinkStatement)s;
					if (ls.isXref()) {
						termIndex.put(ls.getTargetId(), nid);
					}
				}
			}
			// TODO: xrefs
		}
		System.err.println("index size="+termIndex.size());
		isIndexed = true;
	}


}
