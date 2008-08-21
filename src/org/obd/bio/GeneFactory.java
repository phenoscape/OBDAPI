package org.obd.bio;

import java.util.Collection;
import java.util.Vector;

import org.obd.model.Node;
import org.obd.query.BooleanQueryTerm;
import org.obd.query.LinkQueryTerm;
import org.obd.query.QueryTerm;
import org.obd.query.Shard;

public class GeneFactory {
	protected Shard shard;
	protected final String GENE_ID = "SO:0000704";
	
	
	
	public GeneFactory(Shard shard) {
		super();
		this.shard = shard;
	}



	public Collection<Node> getAllGenesByTaxon(String taxonId) {
		LinkQueryTerm isaGeneQt = new LinkQueryTerm(GENE_ID);
		LinkQueryTerm taxonQt = new LinkQueryTerm(taxonId);
		
		QueryTerm qt = new BooleanQueryTerm(isaGeneQt, taxonQt);

		return shard.getNodesByQuery(qt);

	}
}
