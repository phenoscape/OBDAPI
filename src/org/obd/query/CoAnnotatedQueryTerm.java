package org.obd.query;

/**
 * A {@link LinkQueryTerm} in which the link type is one of co-annotation
 * @author cjm
 * CoAnnotatedQueryTerm(A,B) will return statements in which A and B
 * are annotated to the same node
 */
public class CoAnnotatedQueryTerm extends LinkQueryTerm {

	public CoAnnotatedQueryTerm(QueryTerm target) {
		super(target);
		// TODO Auto-generated constructor stub
	}

	public CoAnnotatedQueryTerm(String targetAtom) {
		super(targetAtom);
		// TODO Auto-generated constructor stub
	}

}
