package org.obd.query;

import java.util.Collection;

/**
 * A {@link BooleanQueryTerm} in which the operator is AND
 * @author cjm
 *
 */
public class BooleanIntersectionQueryTerm extends BooleanQueryTerm {

		
	public BooleanIntersectionQueryTerm(Collection<QueryTerm> queryTerms) {
		super();
		this.queryTerms = queryTerms;
	}

	public BooleanIntersectionQueryTerm(QueryTerm... queryTerms) {
		super();
		for (QueryTerm qt : queryTerms) {
			addQueryTerm(qt);
		}
	}

	public BooleanOperator getOperator() {
		return BooleanOperator.AND;
	}

	public void setOperator(BooleanOperator operator) {
		this.operator = operator;
	}

	public Collection<QueryTerm> getQueryTerms() {
		return queryTerms;
	}

	public void setQueryTerms(Collection<QueryTerm> queryTerms) {
		this.queryTerms = queryTerms;
	}

}
