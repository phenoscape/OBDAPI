package org.obd.query;

/**
 * A {@link QueryTerm} that matches links connecting a node and a target
 * Example: anything that is part of something that develops from the imaginal disc
 *   LinkStatementQueryTerm(partOf,
 *           new LinkStatementQueryTerm(developsFrom, 
 *                     new LiteralStatementQueryTerm(NAME, "imaginal disc")))
 *                     
 * @see LinkStatement
 * @author cjm
 *
 */
public class LinkQueryTerm extends QueryTerm {
	protected QueryTerm target;
	protected boolean isDescriptionLink = false;

	/**
	 * Example: anything that is part of something that develops from the imaginal disc
	 *   LinkStatementQueryTerm(partOf,
	 *           new LinkStatementQueryTerm(developsFrom, 
	 *                     new LiteralStatementQueryTerm(NAME, "imaginal disc")))
	 *                     
	 * @param relation
	 * @param target
	 */
	public LinkQueryTerm(String relationAtom, QueryTerm target) {
		super();
		//this.relation = new AtomicQueryTerm(relation);
		this.relation = new LabelQueryTerm(relationAtom);
		this.target = target;
	}
	public LinkQueryTerm(QueryTerm relation, QueryTerm target) {
		super();
		this.relation = relation;
		this.target = target;
	}
	public LinkQueryTerm(QueryTerm node, QueryTerm relation, QueryTerm target) {
		super();
		this.relation = relation;
		this.node = node;
		this.target = target;
	}
	public LinkQueryTerm(QueryTerm target) {
		super();
		this.target = target;
	}

	public LinkQueryTerm(String relationAtom, String targetAtom) {
		super();
		this.relation = new LabelQueryTerm(relationAtom);
		this.target = new LabelQueryTerm(targetAtom);
	}
	public LinkQueryTerm(String nodeAtom, String relationAtom, String targetAtom) {
		super();
		setRelation(relationAtom);
		setNode(nodeAtom);
		setTarget(targetAtom);
	}

	public LinkQueryTerm(String targetAtom) {
		super();
		this.target = new LabelQueryTerm(targetAtom);
	}
	public LinkQueryTerm() {
		super();
	}

	public QueryTerm getTarget() {
		return target;
	}

	public void setTarget(QueryTerm target) {
		this.target = target;
	}
	public void setTarget(String nodeAtom) {
		if (nodeAtom != null)
			this.target = new LabelQueryTerm(nodeAtom);
	}

	public boolean isDescriptionLink() {
		return isDescriptionLink;
	}
	public void setDescriptionLink(boolean isDescriptionLink) {
		this.isDescriptionLink = isDescriptionLink;
	}


	public String toString() {
		String s = "Link[ ";
		if (node != null) {
			s = s + "<from: "+getNode()+"> ";
		}
		if (getRelation() != null)
			s =s +"<rel: "+getRelation()+"> ";
		if (getTarget() != null)
			s = s + "<to: "+getTarget().toString()+"> ";
		if (getPositedBy() != null)
			s = s + "<positedBy: "+getPositedBy().toString()+"> ";
		s = s+"]";
		if (aspect != null)
			s=s+"/"+aspect+" ";
		return s;
	}


}
