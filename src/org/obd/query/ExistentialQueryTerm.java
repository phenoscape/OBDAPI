package org.obd.query;

/**
 * A {@link QueryTerm} that tests for the existence of some entity (ie NOT NULL)
 * <p>
 * Used in AnnotationQueryTerm
 * We want to return annotations of R(X,Y) if R'(Y,Z),
 * where Z is the class of interest and X is the annotated entity.
 * R(X,Y) is asserted.
 * We do this with a nested link query
 * newqt = Link(inf=f,positedBy=NOT_NULL,target=Link(target=COI))

 * @author cjm
 *
 */
public class ExistentialQueryTerm extends QueryTerm {

	public ExistentialQueryTerm() {
		super();
	}

}
