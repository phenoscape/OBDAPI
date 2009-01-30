package org.obd.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.obd.model.LinkStatement;
import org.obd.model.Node;
import org.obd.query.BooleanQueryTerm;
import org.obd.query.LabelQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.BooleanQueryTerm.BooleanOperator;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;

/**
 * Parses tab-delimited text files from the GAD project
 * <p>
 * This parser needs to have the shard set to the disease/phenotype ontology source
 * and the dataShard set to the repository of genes
 *
 * @author cjm
 * 
 */
public class GeneticAssociationDatabaseTextFileParser extends TabularInfoParser {

	protected String src = "NCBI:GAD";
	protected Map<String,Collection<Node>> label2node = new HashMap<String,Collection<Node>>();

	public GeneticAssociationDatabaseTextFileParser() {
		super();
	}

	public GeneticAssociationDatabaseTextFileParser(String path) {
		super(path);
	}

	@Override
	public String[] requires() {
		return new String[]{"mammalian_phenotype", "disease_ontology"};
	}



	public int numCols = 45;

	/* (non-Javadoc)
	 * @see org.obd.parser.TabularInfoParser#parseColVals(java.lang.String[])
	 */
	public void parseColVals(String[] colVals) throws Exception {
		if (colVals.length < 44) {
			if (lineNum < 4) {
				return;
			}
			else {
				throw new Exception(colVals.length+" :: not enough vals: "+currentLine);
			}
		}
		String valID = colVals[0];
		String valAssociationYN = colVals[1];

		/*
		 * We try and match this to DO, MP and whatever else is available..
		 */
		String valBroadPhenotype = colVals[2];

		/*
		 * AGING
		 * CANCER
		 * CARDIOVASCULAR
		 * CHEMDEPENDENCY
		 * DEVELOPMENTAL
		 * HEMATOLOGICAL
		 * IMMUNE
		 * INFECTION
		 * METABOLIC
		 *  MITOCHONDRIAL
		 * NEUROLOGICAL
		 * NORMALVARIATION
		 * OTHER
		 * PHARMACOGENOMIC
		 * PSYCH
		 * RENAL
		 * REPRODUCTION
		 * UNKNOWN
		 * VISION
		 */
		String valDiseaseClass = colVals[3];

		String valDiseaseClassCode = colVals[4];
		String valMeSHDiseaseTerms = colVals[5];
		String valChromosom = colVals[6];
		String valChrBand = colVals[7];
		String valGene = colVals[8];
		String valDNAStart = colVals[9];
		String valDNAEnd = colVals[10];
		String valPValue = colVals[11];
		String valReference = colVals[12];
		String valPubmedID = colVals[13];
		String valAlleleAuthorDiscription = colVals[14];
		String valAlleleFunctionalEffects = colVals[15];
		String valPolymophismClass = colVals[16];
		String valGeneName = colVals[17];
		String valRefSeq = colVals[18];
		String valPopulation = colVals[19];
		String valMeSHGeolocation = colVals[20];
		String valSubmitter = colVals[21];
		String valLocusNumber = colVals[22];
		String valUnigene = colVals[23];
		String valNarrowPhenotype = colVals[24];
		String valMolePhenotype = colVals[25];
		String valJournal = colVals[26];
		String valTitle = colVals[27];
		String valrsNumber = colVals[28];
		String valOMIMID = colVals[29];
		String val = colVals[30];
		String valGADCDC = colVals[31];
		String valYear = colVals[32];
		String valConclusion = colVals[33];
		String valStudyInfo = colVals[34];
		String valEnvFactor = colVals[35];
		String valGIGeneA = colVals[36];
		String valGIAlleleofGeneA = colVals[37];
		String valGIGeneB = colVals[38];
		String valGIAlleleofGeneB = colVals[39];
		String valGIGeneC = colVals[40];
		String valGIAlleleofGeneC = colVals[41];
		String valGIAssociation = colVals[42];
		String valGIcombineEnvFactor = colVals[43];
		String valGIrelevanttoDisease = colVals[44];

		Node geneNode = getGene(valGene);
		if (geneNode == null) {
			System.out.println("cannot find: "+valGene);
			return;
		}
		String geneId = geneNode.getId();
		Collection<Node> pNodes = new HashSet<Node>();
		Collection<Node> eNodes = new HashSet<Node>();
		for (String searchTerm : valBroadPhenotype.split(";\\s+")) {
			System.out.println("term: "+searchTerm);
			pNodes.addAll(shard.getNodesBySearch(searchTerm, Operator.EQUAL_TO));
		}
		for (String searchTerm : valMeSHDiseaseTerms.split("(\\||;\\s+)")) {
			System.out.println("term: "+searchTerm);
			pNodes.addAll(shard.getNodesBySearch(searchTerm, Operator.EQUAL_TO));
		}
		for (String searchTerm : valGIrelevanttoDisease.split(" and ")) {
			System.out.println("term: "+searchTerm);
			pNodes.addAll(shard.getNodesBySearch(searchTerm, Operator.EQUAL_TO));
		}
		for (String searchTerm : valEnvFactor.split("\\s+")) {
			System.out.println("term: "+searchTerm);
			eNodes.addAll(shard.getNodesBySearch(searchTerm, Operator.EQUAL_TO));
		}
		//pNodes.addAll(shard.getNodesBySearch(valMeSHDiseaseTerms,Operator.EQUAL_TO));
		System.out.println(geneNode+" found: "+pNodes.size());
		for (Node n : pNodes) {
			System.out.println("  pnode="+n);
			LinkStatement annot = new LinkStatement();
			if (valAssociationYN.equals("N"))
				annot.setNegated(true);
			annot.setNodeId(geneId);
			annot.setRelationId("OBO_REL:influences");
			annot.setTargetId(n.getId());
			annot.setSourceId(src);
			annot.setId(gadId(valID));
			addStatement(annot);
			if (!valPubmedID.equals(""))
				annot.addSubLinkStatement("oban:has_data_source", pubmedId(valPubmedID));
			if (!valConclusion.equals(""))
				annot.addSubLiteralStatement("oban:has_conclusion", valConclusion);
			if (!valStudyInfo.equals(""))
				annot.addSubLiteralStatement("oban:study_info", valStudyInfo);
			if (!valEnvFactor.equals(""))
				annot.addSubLiteralStatement("oban:has_environmental_factor", valEnvFactor);
			for (Node e : eNodes) {
				LinkStatement envLink = new LinkStatement();
				envLink.setRelationId("OBO_REL:has_environment");
				envLink.setTargetId(e.getId());
				annot.addSubStatement(envLink);
			}
			
		}

	}

	public Collection<Node> lookupTerm(String label) {
		Collection<Node> nodes = new HashSet<Node>();
		if (label2node.containsKey(label))
			nodes = label2node.get(label);
		else {
			try {
				nodes = shard.getNodesBySearch(label, Operator.EQUAL_TO);
				label2node.put(label, nodes);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return nodes;
	}
	public Node getGene(String label) {
		Collection<Node> nodes;
		if (label2node.containsKey(label))
			nodes = label2node.get(label);
		else {
			try {
				BooleanQueryTerm qt = new BooleanQueryTerm(BooleanOperator.AND,
						new LabelQueryTerm(AliasType.ANY_LABEL,label),
						new LinkQueryTerm(ncbitaxId("9606")));
				nodes = dataShard.getNodesByQuery(qt);
				label2node.put(label, nodes);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		Node geneNode = null;
		for (Node n : nodes) {
			geneNode = n;
			// TODO - checks?
		}
		return geneNode;	
	}


}
