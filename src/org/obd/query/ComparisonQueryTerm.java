package org.obd.query;

/**
 * A {@link QueryTerm} comparing two values
 * <p>
 * Example:
 * LabelQueryTerm(ComparisonQueryTerm(STARTS_WITH, "transcription"))
 * =>
 * matches entities whose label starts with "transcription"
 *
 * @author cjm
 *
 */
public class ComparisonQueryTerm extends QueryTerm {
	/**
	 * predicate comparing two literal values
	 * @author cjm
	 *
	 */
	public enum Operator {
		EQUAL_TO("="),
		LESS_THAN("<"),
		LESS_THAN_OR_EQUAL_TO("<="),
		GREATER_THAN(">"),
		GREATER_THAN_OR_EQUAL_TO(">"),
		NOT_EQUAL_TO("!="),
		
		MATCHES("matches"),
		MATCHE_REGEXS("matches_regex"),
		CONTAINS("contains"),
		CONTAINS_ANY("contains_any"),
		CONTAINS_ALL("contains_all"),
		STARTS_WITH("starts_with");
		
		String s;
		Operator(String s) {
			this.s = s;
		}
		public String toString() {
			return s;
		}
	}
	
	protected Operator operator;
	protected QueryTerm value;	

	/**
	 * shorthand for
	 * new ComparisonQueryTerm(new AtomicQueryTerm(valueAtom))
	 * @see AtomicQueryTerm
	 * @param valueAtom
	 */
	public ComparisonQueryTerm(String valueAtom) {
		super();
		this.operator = Operator.EQUAL_TO;
		this.value = new AtomicQueryTerm(valueAtom);
	}
	
	public ComparisonQueryTerm(Integer valueAtom) {
		super();
		this.operator = Operator.EQUAL_TO;
		this.value = new AtomicQueryTerm(valueAtom);
	}
	public ComparisonQueryTerm(QueryTerm value) {
		super();
		this.operator = Operator.EQUAL_TO;;
		this.value = value;
	}
	public ComparisonQueryTerm(Operator operator, String valueAtom) {
		super();
		this.operator = operator;
		this.value = new AtomicQueryTerm(valueAtom);
	}
	public ComparisonQueryTerm(Operator operator, QueryTerm value) {
		super();
		this.operator = operator;
		this.value = value;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public QueryTerm getValue() {
		return value;
	}

	public void setValue(QueryTerm value) {
		this.value = value;
	}
	
	public String toString() {
		String s = operator.toString();
		if (getValue() != null)
			s = s + " "+getValue().toString();
		return s;
	}

	@Override
	public Object getAtomicValue() {
		return getValue().getAtomicValue();
	}

}
