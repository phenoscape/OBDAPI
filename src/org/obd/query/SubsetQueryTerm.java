package org.obd.query;

/**
 * A {@link LinkQueryTerm} in which the relation is one of belonging to a subset
 * <p>
 * Here the target is the subset.
 * <p>
 * For example, assume the GALEN ontology has a segment delineated corresponding
 * to classes of relevance to immunity (lymph nodes, B cell etc). The following
 * call will select all classes in this subset:
 * <p>
 * <code>
 * shard.getNodesByQuery(new SubsetQueryTerm("GALEN:immunology-subset"))
 * </code>
 * <p>
 * @author cjm
 *
 */
public class SubsetQueryTerm extends LinkQueryTerm {

	private QueryTerm subsetRelation;
	public SubsetQueryTerm() {
		super();
	}

	public SubsetQueryTerm(QueryTerm target) {
		super(target);
	}

	/**
	 * Creates a query object matching classes in the given named subset
	 * @param subsetId   -- example: GALEN:immunology-ssubset
	 */
	public SubsetQueryTerm(String subsetId) {
		super(subsetId);
	}
	
	@Override
	public Boolean isInferred() {
		return false;
	}
	
	@Override
	public QueryTerm getRelation() {
		if (subsetRelation == null)
			subsetRelation = new LabelQueryTerm(getTermVocabulary().IN_SUBSET());
		return subsetRelation;
	}

}
