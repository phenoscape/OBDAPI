package org.obd.test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.obd.model.CompositionalDescription;
import org.obd.model.Graph;
import org.obd.model.HomologyView;
import org.obd.model.Node;
import org.obd.model.stats.SimilarityPair;
import org.obd.query.LabelQueryTerm;
import org.obd.query.ComparisonQueryTerm.Operator;
import org.obd.query.LabelQueryTerm.AliasType;
import org.obd.query.impl.OBDSQLShard;
import org.obd.query.impl.AbstractShard;
import org.obd.query.impl.AbstractShard.CongruentPair;

/**
 * Tests for capability to retrieve class expressions from a {@link org.obd.model.Shard}
 * @see CompositionalDescription
 * @author cjm
 *
 */
public class CongruenceTest extends AbstractOBDTest {
		
	Map<String,String> labelmap = new HashMap<String,String>();
	SimilarityPair sp;

	public CongruenceTest(String n) {
		super(n);
	}
	
	public void initLogger() {
		initLogger(Level.INFO);
	}

	/*
	 */
	public void xxxGetCongruence() throws SQLException, ClassNotFoundException {
		
		String[] labels = {"TTN", "EYA1", "SOX10", "SOX9", "PAX2", "SHH"};
		
		OBDSQLShard obd = (OBDSQLShard)getShard();
		for (String label : labels) {
			 Collection<Node> nodes = obd.getNodesByQuery(new LabelQueryTerm(AliasType.PRIMARY_NAME, label, Operator.EQUAL_TO));
			 for (Node n : nodes) {
				 System.out.println(n);
			 
				 Collection<CongruentPair> cps = obd.getAnnotationSourceCongruenceForAnnotatedEntity(n.getId());
				 for (CongruentPair cp : cps) {
					 System.out.println("  "+cp);
				 }
			 }
				 
		}
	}
	
	public void xxxCompareHomol() throws SQLException, ClassNotFoundException {

		HomologyView hv = new HomologyView();
		hv.setShard(shard);
		String[] labels = {"SOX9","SOX10","PAX2","TTN","EYA1","SHH"};
		for (String label : labels) {
			System.out.println("homologs for: "+label);
			Collection<Node> hsets = hv.getGeneHomolSetByGeneLabel(label);
			List<String> nids = new LinkedList<String>();
			for (Node hset : hsets) {
				Collection<Node> nodes = hv.getNodesInHomolSet(hset.getId());
				for (Node n : nodes) {
					System.out.println("  N: "+n);

					nids.add(n.getId());
				}
			}

			compareNodeIds(nids);
		}


		String[][] as = {
				{"MGI:98297","NCBI_Gene:6469"},  // mouse Shh vs human
				{"ZFIN:ZDB-GENE-980526-166","NCBI_Gene:6469"} // ZFIN:shha vs human SHH

		};
		for (String[] a : as)
			compareNodeIds(Arrays.asList(a));
	}
	
	public void testCompare() throws SQLException, ClassNotFoundException {

		String[] a = {"OMIM:600725.0007/+","OMIM:600725.0019"};
		compareNodeIds(Arrays.asList(a));
	}

	
	public void testCompareAnnotationsByAnnotatedEntityPair() throws SQLException, ClassNotFoundException {

		String[] labels = {"SOX9","SOX10","PAX2","TTN","EYA1","SHH","eya1","shh","sox10a","sox9a","Eya1","Shh"};
		//String[] labels = {"BRCA1","COMT"};
		//String[] labels = {"PAX2 OMIM genotype 0007","PAX2 OMIM genotype 0001"};

		Collection<String> nids = new HashSet<String>();
		for (String label : labels) {
			Collection<Node> nodes = shard.getNodesByQuery(new LabelQueryTerm(AliasType.PRIMARY_NAME, label, Operator.EQUAL_TO));
			for (Node n : nodes) {
				nids.add(n.getId());
				System.out.println(n.getId());
				labelmap.put(n.getId(),label);
			}
		}
		compareNodeIds(Collections.list((Enumeration<String>) nids));
	}
	
	public void compareNodeIds(List<String> nids) {
		// yes this is redundant; but is useful test of symmetry of score..
		for (String i : nids) {
			for (String j : nids) {
				if (i.equals(j))
					continue;
				System.out.println("comparing "+i+" with "+j);
				sp = shard.compareAnnotationsByAnnotatedEntityPair(i,j);
				shard.calculateInformationContentMetrics(sp);
				System.out.println(i+" "+getLabel(i)+" <==> "+j+" "+getLabel(j));
				System.out.println(sp);
				for (String k : sp.getInverseClosureMap().keySet()) {
//					System.out.println(k + "->" + sp.getInverseClosureMap().get(k));
				}
				System.out.println("  nonredundant nodes in common: ");
				summariseSet("both",sp.getNonRedundantNodesInCommon());
				summariseSet("s1",sp.getNonRedundantNodesInSet1());
				summariseSet("s2",sp.getNonRedundantNodesInSet2());
				if (sp.getNodeWithMaximumInformationContent() != null) {
					Node n = shard.getNode(sp.getNodeWithMaximumInformationContent());
					System.out.println("  node with max IC: "+n);
				}
			}			 
		}
		
	}
	
	private void summariseSet(String name, Collection<String> nids) {
		for (String nid : nids) {
			Node n = shard.getNode(nid);
			System.out.println("  NR: "+name+" : "+n+" IC: "+sp.getInformationContent(nid));
		}
		
	}
	
	public String getLabel(String i) {
		if (labelmap.containsKey(i))
			return labelmap.get(i);
		return i;
	}
	
	public void testCompareAnnotationsBySourcePair() throws SQLException, ClassNotFoundException {
		
		String[] labels = {"TTN", "EYA1", "SOX10", "SOX9", "PAX2", "SHH"};
		
		for (String label : labels) {
			 Collection<Node> nodes = getShard().getNodesByQuery(new LabelQueryTerm(AliasType.PRIMARY_NAME, label, Operator.EQUAL_TO));
			 for (Node n : nodes) {
				 System.out.println(n);
			 
				 SimilarityPair cp = 
					 getShard().compareAnnotationsBySourcePair(n.getId(),"omim_phenotype_zfin","omim_phenotype_fb");
				 System.out.println(cp);
			 }			 
		}
	}


}
