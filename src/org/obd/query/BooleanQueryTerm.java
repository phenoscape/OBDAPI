package org.obd.query;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A {@link QueryTerm} that performs a boolean operation over 1 or more sub-terms
 * @author cjm
 *
 */
public class BooleanQueryTerm extends QueryTerm {

	public enum BooleanOperator {AND, OR, XOR, NOT}
	
	protected BooleanOperator operator = BooleanOperator.AND;
	
	protected Collection<QueryTerm> queryTerms = new LinkedList<QueryTerm>();

	public BooleanQueryTerm() {
		super();
	}
	
	public BooleanQueryTerm(Collection<QueryTerm> queryTerms) {
		super();
		this.queryTerms = queryTerms;
	}

	public BooleanQueryTerm(QueryTerm... queryTerms) {
		super();
		for (QueryTerm qt : queryTerms) {
			addQueryTerm(qt);
		}
	}

	public BooleanQueryTerm(BooleanOperator operator, Collection<QueryTerm> queryTerms) {
		super();
		this.operator = operator;
		this.queryTerms = queryTerms;
	}

	public BooleanQueryTerm(BooleanOperator operator, QueryTerm... queryTerms) {
		super();
		this.operator = operator;
		for (QueryTerm qt : queryTerms) {
			addQueryTerm(qt);
		}
	}

	public void addQueryTerm(QueryTerm qt) {
		queryTerms.add(qt);
	}

	public String toString() {
		return queryTerms.toString();
	}



	public BooleanOperator getOperator() {
		return operator;
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
