package org.obd.query;

import org.obd.model.CompositionalDescription;
import org.obd.model.CompositionalDescription.Predicate;
import org.obd.query.BooleanQueryTerm.BooleanOperator;

/**
 * A {@link QueryTerm} for finding nodes that satisfy a {@link CompositionalDescription}
 * <p>
 * A compositional description (class expression) is in fact very similar to a query;
 * the description provides conditions for membership.
 * <p>
 * Here we translate the description to a query for class nodes subsumed by that
 * description.
 * For example, if the description is:
 * <code>
 * Car THAT hasColor blue AND hasMake Skoda
 * </code>
 * Then the query will be
 * <code>
 * And(Link(IS_A,Car), Link(hasColor,blue), Link(hasMake, skoda))
 * </code>
 * 
 * @author cjm
 *
 */
public class CompositionalDescriptionQueryTerm extends QueryTerm {

	protected CompositionalDescription compositionalDescription;
	protected LinkQueryTerm mapQueryTerm;

	public CompositionalDescriptionQueryTerm(CompositionalDescription desc) {
		super();
		this.compositionalDescription = desc;
	}

	public CompositionalDescriptionQueryTerm() {
		super();
	}

	public CompositionalDescription getCompositionalDescription() {
		return compositionalDescription;
	}

	public void setCompositionalDescription(
			CompositionalDescription compositionalDescription) {
		this.compositionalDescription = compositionalDescription;
	}
	
	public LinkQueryTerm getMapQueryTerm() {
		return mapQueryTerm;
	}

	/**
	 * EXPERIMENTAL: 
	 * setting this will apply a transform to all leaf nodes in the query object
	 * graph
	 * @param mapQueryTerm
	 */
	public void setMapQueryTerm(LinkQueryTerm mapQueryTerm) {
		this.mapQueryTerm = mapQueryTerm;
	}

	public QueryTerm translateToQueryTerm() throws Exception {
		return translateToQueryTerm(this.getCompositionalDescription());
	}
	public static QueryTerm translateToQueryTerm(CompositionalDescription d) throws Exception {
		QueryTerm qt = null;
		qt = new BooleanQueryTerm();
		if (d.isAtomic()) {
			LinkQueryTerm lq = new LinkQueryTerm();
			lq.setNode(d.getNodeId());
			return lq;
		}
		else {

			if (d.getPredicate().equals(Predicate.RESTRICTION)) {
				qt = new LinkQueryTerm();
				qt.setRelation(d.getRelationId());
				((LinkQueryTerm)qt).
				setTarget(translateToQueryTerm(d.getFirstArgument()));
			}
			else {

				qt = new BooleanQueryTerm();
				BooleanQueryTerm bqt = (BooleanQueryTerm)qt;

				if (d.getPredicate().equals(Predicate.INTERSECTION)) {
					bqt.setOperator(BooleanOperator.AND);
				}
				else if (d.getPredicate().equals(Predicate.UNION)) {
					bqt.setOperator(BooleanOperator.OR);
				}
				else if (d.getPredicate().equals(Predicate.COMPLEMENT)) {
					bqt.setOperator(BooleanOperator.NOT);
				}
				else {
					throw new Exception("not yet"); // TODO
				}
			
				for (CompositionalDescription arg : d.getArguments()) {
					bqt.addQueryTerm(translateToQueryTerm(arg));
				}
			}
		}
		return qt;
	}
	
	public String toString() {
		return compositionalDescription.toString();
	}

}
