package org.obd.query;

/**
 * A {@link QueryTerm} that matches links or nodes with the specified source
 * @author cjm
 *
 */
public class SourceQueryTerm extends QueryTerm {
	protected QueryTerm target;
	

	public SourceQueryTerm(QueryTerm target) {
		super();
		this.target = target;
	}
	public SourceQueryTerm(String targetAtom) {
		super();
		this.target = new LabelQueryTerm(targetAtom);
	}

	public QueryTerm getTarget() {
		return target;
	}

	public void setTarget(QueryTerm target) {
		this.target = target;
	}

	
}
