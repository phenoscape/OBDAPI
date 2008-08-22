package org.obd.test;

import org.obd.model.Statement;
import org.obd.query.Shard;

/**
 * Tests annotation metadata requirements are fulfilled
 * 
 * Annotation metadata is modeled as sub-statements on a statement.
 * See {@link Statement#getSubStatements}
 * 
 * @author cjm
 *
 */
public class AnnotationMetadataTest extends AbstractOBDTest {

	
	public AnnotationMetadataTest(String n) {
		super(n);
	}

	/**
	 * Requirement: annotation provenance metadata should be automatically
	 * attached to annotation statements when they are fetched from a shard,
	 * e.g. via {@link Shard#getStatementsForNode}
	 *
	 */
	public void testFetchAnnotationProvenanceMetadata() {
		
		int n=0;
		for (Statement s : shard.getStatementsByNode("ZFIN:ZDB-GENO-960809-7")) {
			System.out.println(s);
			for (Statement ss : s.getSubStatements()) {
				n++;
				System.out.println("  "+ss);
				
			}
		}
		assertTrue(n>0);
	}



}
