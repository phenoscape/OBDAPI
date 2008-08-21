package org.obd.query;

/**
 * A {@link LinkQueryTerm} in which the node is annotated to the target
 * <p>
 * this query is shorthand for:
 * Link(inf=f,positedBy=NOT_NULL,target=Link(target=COI))
 * 
 * We want to return annotations of R(X,Y) if R'(Y,Z),
 * where Z is the class of interest and X is the annotated entity.
 * R(X,Y) is asserted.
 * We do this with a nested link query
 * newqt = Link(inf=f,positedBy=NOT_NULL,target=Link(target=COI))

 * @author cjm
 *
 */
public class AnnotationLinkQueryTerm extends LinkQueryTerm {

	protected QueryTerm ontologyRelation;

	public AnnotationLinkQueryTerm() {
		super();
	}

	public AnnotationLinkQueryTerm(QueryTerm target) {
		super(target);
	}

	public AnnotationLinkQueryTerm(String targetAtom) {
		super(targetAtom);
	}

	public AnnotationLinkQueryTerm(String relationAtom, QueryTerm target) {
		super(relationAtom,target);
	}
	public AnnotationLinkQueryTerm(String relationAtom, String targetAtom) {
		super(relationAtom,targetAtom);
	}

	
	public QueryTerm getOntologyRelation() {
		return ontologyRelation;
	}

	/**
	 * We want to return annotations of R(X,Y) if R'(Y,Z),
	 * where Z is the class of interest and X is the annotated entity.
	 * R(X,Y) is asserted.
	 * Here we call R' the ontologyRelation.
	 * For example, if the annotation is:
	 *   participatesIn(mcm2,S_phase_of_mitotic_cell_cycle) [asserted]
	 * and the ontology has
	 *   partOf(S_phase_of_mitotic_cell_cycle, mitosis) [implied]
	 * Then setting the ontologyRelation to partOf will have the result of
	 * INCLUDING this annotation (as will leaving it unbound).
	 * Contrast this with {@link #setRelation(QueryTerm)}, which filters on
	 * the relation of the asserted annotation link.
	 * @param ontologyRelation - a query object for matching relations
	 */
	public void setOntologyRelation(QueryTerm ontologyRelation) {
		this.ontologyRelation = ontologyRelation;
	}
	/**
	 * @param relationAtom - an identifier of a relation
	 */
	public void setOntologyRelation(String relationAtom) {
		if (relationAtom != null)
			this.ontologyRelation = new LabelQueryTerm(relationAtom);
	}


}
