package org.obd.query;

/**
 * A {@link QueryTerm} matching a node that is related to a literal datatype value
 * @author cjm
 * @see LiteralStatement
 */
public class LiteralQueryTerm extends QueryTerm {
	protected QueryTerm value;
	protected boolean isAlias;
	
	
	public LiteralQueryTerm(QueryTerm relation, String valueAtom) {
		super();
		this.value = new ComparisonQueryTerm(new AtomicQueryTerm(valueAtom));
		setRelation(relation);
	}

	public LiteralQueryTerm(String relationAtom, QueryTerm value) {
		super();
		this.value = value;
		setRelation(new ComparisonQueryTerm(new AtomicQueryTerm(relationAtom)));
	}
	public LiteralQueryTerm(QueryTerm value) {
		super();
		this.value = value;
	}
	public LiteralQueryTerm(String valueAtom) {
		super();
		this.value = new ComparisonQueryTerm(new AtomicQueryTerm(valueAtom));
	}
	
	public LiteralQueryTerm() {
		super();
	}

	public QueryTerm getValue() {
		return value;
	}

	public void setValue(QueryTerm value) {
		this.value = value;
	}

	

	public boolean isAlias() {
		if (isAlias)
			return true;
		if (getRelation() != null) {
			// TODO
		}
		return false;
	}

	public void setAlias(boolean isAlias) {
		this.isAlias = isAlias;
	}

	public String toString() {
		String s = "Link[ ";
		if (node != null) {
			s = s + "<from: "+getNode()+"> ";
		}
		if (getRelation() != null)
			s =s +"<rel: "+getRelation()+"> ";
		if (getValue() != null)
			s = s + "<to: "+getValue().toString()+"> ";
		s = s+"]";
		if (aspect != null)
			s=s+"/"+aspect+" ";
		return s;
	}
}
