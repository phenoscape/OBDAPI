package org.obd.test;

import java.sql.SQLException;

import org.obd.model.Node;
import org.obd.model.stats.ScoredNode;
import org.obd.query.impl.OBDSQLShard;
import org.obd.util.GeneAnalysis;

public class GeneAnalysisTest extends AbstractOBDTest{
	
	GeneAnalysis ga;
	
	public GeneAnalysisTest(String n) throws SQLException, ClassNotFoundException{
		super(n);
		this.ga = new GeneAnalysis();
		this.ga.setShard(this.getShard());
	}
	
	public void testGeneAnalysis() throws Exception{
		String[] geneNames = {"ATP2A1","EPB41","EXT2","EYA1","FECH","PAX2","SHH","SOX9","SOX10","TNNT2","TTN"};
		
		for (String geneName : geneNames){
			System.out.println("Similar genes to " + geneName + "\n===========================================");
			this.ga.showSimilarGenesByLabel(geneName, "uberon");
			System.out.println("\n");
		}
		
		
	}
}