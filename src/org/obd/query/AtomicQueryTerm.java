package org.obd.query;

/**
 * Leaf node in a query tree. A literal: String, number etc
 * 
 * @author cjm
 *
 */
public class AtomicQueryTerm extends QueryTerm {

	protected Object value;
	protected Class datatype;

	public AtomicQueryTerm(Object value) {
		super();
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}
	public String getSValue() {
		return value.toString();
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Class getDatatype() {
		return datatype;
	}

	public void setDatatype(Class datatype) {
		this.datatype = datatype;
	}

	public String toString() {
		return getValue().toString();
	}
	
	@Override
	public Object getAtomicValue() {
		return getValue();
	}


}
