package org.obd.test;

import java.util.Collection;

import org.obd.model.CompositionalDescription;
import org.obd.model.Node;
import org.obd.model.Statement;
import org.obd.model.stats.ScoredNode;
import org.purl.obo.vocab.RelationVocabulary;

/**
 * Tests for queries over phenotypes
 * @author cjm
 *
 */
public class InformationContentTest extends AbstractOBDTest {

	RelationVocabulary rv = new RelationVocabulary();
	
	public InformationContentTest(String n) {
		super(n);
	}

	
	public void testFetchIC() {

		String id = "OMIM:601653";

		Collection<Statement> annots = shard.getAnnotationStatementsForAnnotatedEntity(id, null, null);
		
		for (Statement s : annots) {
			String tid = s.getTargetId();
			showIC(tid);
			CompositionalDescription cd = shard.getCompositionalDescription(tid, false);
			for (CompositionalDescription arg : cd.getArguments()) {
				String xid = null;
				if (arg.isAtomic())
					xid = arg.getNodeId();
				else if (arg.getRestriction() != null)
					xid = arg.getRestriction().getTargetId();
				else
					continue;
				showIC(xid);
			}
		}
	}
	
	private void showIC(String id) {
		Double ic = shard.getInformationContentByAnnotations(id);
		Node n = shard.getNode(id);
		System.out.println(id+" "+n.getLabel()+" "+ic);	
	}
	
	public void testFetchCoAnnotatedNode() {
		
		try {
			Collection<ScoredNode> sns = shard.getCoAnnotatedClasses("CL:0000148");
			for (ScoredNode sn :sns) {
				System.out.println(sn);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




	

}
