package org.obd.query;

import org.obd.query.ComparisonQueryTerm.Operator;

/**
 * A {@link LiteralQueryTerm} that matches based on some label of the entity
 * @author cjm
 *
 */
public class LabelQueryTerm extends QueryTerm {
	/**
	 * relation between label and node
	 *
	 */
	public enum AliasType {
		ID, INTERNAL_ID, PRIMARY_NAME, ALTERNATE_LABEL, ANY_LABEL, ANY_LITERAL }
	
	protected AliasType aliasType = AliasType.ID;
	protected QueryTerm value;
	
	/**
	 * shorthand for
	 * new LabelQueryTerm(
	 *   aliasType,
	 *   new ComparisonQueryTerm(MATCHES,new AtomicQueryTerm(valueAtom)))
	 * @see ComparisonQueryTerm
	 * @param aliasType
	 * @param valueAtom
	 */
	public LabelQueryTerm(AliasType aliasType, String valueAtom) {
		super();
		this.value = 
			new ComparisonQueryTerm(valueAtom);
		this.aliasType = aliasType;
	}
		
	public LabelQueryTerm(AliasType aliasType, Integer valueAtom) {
		super();
		this.value = 
			new ComparisonQueryTerm(valueAtom);
		this.aliasType = aliasType;
	}

	public LabelQueryTerm(AliasType aliasType, QueryTerm value) {
		super();
		this.value = value;
		this.aliasType = aliasType;
	}
	public LabelQueryTerm(QueryTerm value) {
		super();
		this.value = value;
	}
	public LabelQueryTerm(String valueAtom) {
		super();
		this.value = new ComparisonQueryTerm(new AtomicQueryTerm(valueAtom));
	}
	/**
	 * Example: 
	 * <code>
	 * new LabelQueryTerm(AliasType.ANY_LABEL,"SOX",Operator.STARTS_WITH)
	 * </code>
	 * 
	 * @param aliasType
	 * @param valueAtom
	 * @param op
	 */
	public LabelQueryTerm(AliasType aliasType, String valueAtom, Operator op) {
		super();
		this.value = 
			new ComparisonQueryTerm(op, valueAtom);
		this.aliasType = aliasType;
	}


	public QueryTerm getValue() {
		return value;
	}

	public void setValue(QueryTerm value) {
		this.value = value;
	}

	public AliasType getAliasType() {
		return aliasType;
	}
	public void setAliasType(AliasType aliasType) {
		this.aliasType = aliasType;
	}

	public String toString() {
		String s = getAliasType().toString();
		if (getValue() != null)
			s = s + " "+getValue().toString();
		return s;
	}
	
	@Override
	public Object getAtomicValue() {
		return getValue().getAtomicValue();
	}

}
