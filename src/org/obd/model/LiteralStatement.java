package org.obd.model;

/**
 * A {@link Statement} relating a node to a datatype literal
 * <p>
 * Examples:
 * <ul>
 *  <li> ZFIN:0000001[node] hasLabel[rel] "Sonic hedgehog" [value] </li>
 * </ul>
 * 
 * @author cjm
 *
 */
public class LiteralStatement extends Statement {
	protected String datatype;
	protected Object value;

	public enum Datatype {
		STRING("xsd:string"),
		DATE("xsd:date"),
		DECIMAL("xsd:decimal"),
		NEGATIVE_INTEGER("xsd:negativeInteger"),
		POSITIVE_INTEGER("xsd:positiveInteger"),
		ANY_URI("xsd:anyURI"),
		BOOLEAN("xsd:boolean"),
		INTEGER("xsd:integer");

		String s;
		Datatype(String s) {
			this.s = s;
		}
		public String toString() {
			return s;
		}
	}


	public LiteralStatement() {
		super();
		// TODO Auto-generated constructor stub
	}
	public LiteralStatement(String id, String val) {
		super(id);
		setValue(val);
	}

	public LiteralStatement(String n, String r, Object value, String datatype) {
		super(n);
		setRelationId(r);
		this.datatype = datatype;
		this.value = value;
	}
	
	public LiteralStatement(String n, String r, Object value, Datatype datatype) {
		super(n);
		setRelationId(r);
		this.datatype = datatype.toString();
		this.value = value;
	}
	
	public LiteralStatement(String nid, String relId, Object val) {
		super(nid);
		setRelationId(relId);
		this.datatype = null;
		this.value = val;
	}
	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	/**
	 * We can use a controlled enum to set the datatype - however, the
	 * underlying storage is string-ID based in order to allow extensible
	 * datatypes
	 * 
	 * @param datatype
	 */
	public void setDatatype(Datatype datatype) {
		this.datatype = datatype.toString();
	}

	public String getSValue() {
		return (String)value;
	}

	public Object getValue() {
		return value;
	}
	
	public String getTargetId() {
		return getSValue();
	}
	public void setTargetId(String targetId) {
		setValue(targetId);
	}


	public void setValue(String value) {
		this.value = value;
	}

	public boolean isAlias() {
		if (relationId != null) {
			if (relationId.equals("oboInOwl:hasSynonym"))
				return true;
		}
		return false;
	}
	
	@Deprecated
	public boolean isTextDescription() {
		if (relationId != null) {
			if (relationId.equals("oboInOwl:hasDefinition"))
				return true;
		}
		return false;
	}
	
	public boolean isLiteral() {
		return true;
	}

	
	public String toString() {
		String s = super.toString();
		s = s + " value:"+getValue();
		return s;
	}
	
	public String getSkolemId() {
		String s = nodeId+"--"+relationId+"--"+getValue();
		return s;
	}

	
	

}
