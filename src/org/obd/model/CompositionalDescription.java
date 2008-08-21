package org.obd.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * A constructing formed from a composition of other nodes.
 * 
 *   aka Logical Definition, cross-product, class expression.
 * A recursive construct for composing descriptions of entities using
 * logical connectives, classes and relations.
 * Note that this is purely a convenience class: a CompositionalDescription is 
 * semantically equivalent to a collection of link statements with intersection or union
 * semantics
 * <p>
 * For the full grammar see:
 * {@link <a href="doc-files/obd-xml-xsd.html#element-type-CompositionalDescription">CompositionalDescription Grammar</a>}
 * <p>
 * Examples:
 * The SO class SO:0000634 polycistronic_mRNA is composed from SO:0000234 "mRNA" and
 * SO:0000880 "polycistonic".
 * This is the result of cd.toString() on the CompositionalDescription of this class:
 * <code>
 * INTERSECTION( RESTRICTION( has_quality SO:0000880 ) SO:0000234 ) 
 * </code>
 * The OBDXML looks like this (which reflects the underlying object structure):
 * <code>
 * <pre>
 * &lt;CompositionalDescription predicate="INTERSECTION"&gt;
 *  &lt;CompositionalDescription predicate="RESTRICTION"&gt;
 *   &lt;restriction&gt;
 *    &lt;LinkStatement appliesToAllInstancesOf="true" hasIntersectionSemantics="true"&gt;
 *     &lt;node about="SO:0000634"/&gt;
 *     &lt;relation about="has_quality"/&gt;
 *     &lt;target about="SO:0000880"/&gt;
 *     &lt;source about="sequence"/&gt;
 *    &lt;/LinkStatement&gt;
 *   &lt;/restriction&gt;
 *   &lt;Atom&gt;
 *    SO:0000880
 *   &lt;/Atom&gt;
 *    &lt;/CompositionalDescription&gt;
 *   &lt;Atom&gt;
 *    SO:0000234
 *   &lt;/Atom&gt;
 *  &lt;/CompositionalDescription&gt;
 * &lt;/pre&gt;
 * </pre>
 * </code>
 * @author cjm
 *
 */
public class CompositionalDescription extends Node implements Comparable {
	public enum Predicate { INTERSECTION, UNION, COMPLEMENT, RESTRICTION, ATOM }

	protected Predicate predicate = Predicate.ATOM;
	protected Collection<CompositionalDescription> arguments;
	protected LinkStatement restriction;
	protected String nodeId;
	protected Graph sourceGraph;

	public CompositionalDescription(Predicate predicate) {
		super();
		this.predicate = predicate;
		arguments = new HashSet<CompositionalDescription>();
	}
	public CompositionalDescription (String genusId, Collection<LinkStatement> diffs) {
		super();
		this.predicate = Predicate.INTERSECTION;
		arguments = new HashSet<CompositionalDescription>();
		addArgument(genusId);
		for (LinkStatement s : diffs) {
			CompositionalDescription restr = 
				new CompositionalDescription(Predicate.RESTRICTION);
			restr.setRestriction(s);
			restr.addArgument(s.getTargetId());
			addArgument(restr);
		}
	}

	public CompositionalDescription(String nodeId) {
		super();
		setNodeId(nodeId);
	}

	public CompositionalDescription() {
		super();
	}

	// TODO: should this delegate to id?
	// no - the description is distinct from the node
	/**
	 * @return 
	 */
	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public LinkStatement getRestriction() {
		return restriction;
	}

	public void setRestriction(LinkStatement restriction) {
		this.restriction = restriction;
	}


	public Collection<CompositionalDescription> getArguments() {
		return arguments;
	}

	public CompositionalDescription getFirstArgument() {
		return (CompositionalDescription)arguments.toArray()[0];
	}

	public void setArguments(Collection<CompositionalDescription> arguments) {
		this.arguments = arguments;
	}
	public void addArgument(CompositionalDescription argument) {
		if (arguments == null)
			arguments = new HashSet<CompositionalDescription>();
		arguments.add(argument);
	}
	
	/**
	 * treated as an existential restriction by default
	 * @param relationId
	 * @param targetId
	 */
	public void addArgument(String relationId, String targetId) {
		CompositionalDescription restr = new CompositionalDescription(Predicate.RESTRICTION);
		LinkStatement link = new LinkStatement(null,relationId,targetId);
		restr.addArgument( new CompositionalDescription(targetId) );
		link.setAppliesToAllInstancesOf(true);
		link.setExistential(true);
		restr.setRestriction(link);
		addArgument(restr);
	}

	public void addArgument(String id) {
		CompositionalDescription d = new CompositionalDescription(Predicate.ATOM);
		d.setNodeId(id);
		addArgument(d);
	}
	public void removeArgument(CompositionalDescription argument) {
		arguments.remove(argument);
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public void setPredicate(Predicate operator) {
		this.predicate = operator;
	}

	public String getRelationId() {
		if (restriction != null)
			return restriction.getRelationId();
		return null;
	}

	public void setSourceGraph(Graph sourceGraph) {
		this.sourceGraph = sourceGraph;
	}

	public boolean isAtomic() {
		return predicate.equals(Predicate.ATOM);
	}

	public Graph getSourceGraph() {
		return sourceGraph;
	}
	public boolean isValid() {
		boolean valid = false;
		if (predicate == null)
			return false;
		if (predicate.equals(Predicate.ATOM)) {
			return arguments == null || arguments.size() == 0;
		}
		else if (predicate.equals(Predicate.RESTRICTION)) {
			return restriction != null;
		}
		else if (predicate.equals(Predicate.COMPLEMENT)) {
			return arguments.size() == 0;
		}
		else if (predicate.equals(Predicate.INTERSECTION) ||
				predicate.equals(Predicate.UNION)) {
			return arguments.size() >1;
		}
		else {
			return false;
		}
	}

	public boolean isGenusDifferentia() {
		if (predicate.equals(Predicate.INTERSECTION)) {

			CompositionalDescription genus = null;
			for (CompositionalDescription arg : arguments) {
				if (arg.getPredicate().equals(Predicate.RESTRICTION)) {
					// potential differentium
				}
				else {
					// genus
					if (genus == null) {
						// none assigned so far
						genus = arg;
					}
					else {
						return false;
					}
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	public CompositionalDescription getGenus() {
		if (predicate.equals(Predicate.INTERSECTION)) {

			CompositionalDescription genus = null;
			for (CompositionalDescription arg : arguments) {
				if (arg.getPredicate().equals(Predicate.RESTRICTION)) {
					// potential differentium
				}
				else {
					// genus
					if (genus == null) {
						// none assigned so far
						genus = arg;
					}
					else {
						return null;
					}
				}
			}
			return genus;
		}
		else {
			return null;
		}

	}


	/**
	 * if this is a genus-differentia style definition, return all
	 * the differentia. These will be returned as CDs in themselves,
	 * equivalent to owl:Restrictions
	 * 
	 * @return CompositionalDescription of Predicate.RESTRICTION
	 */
	public Collection<CompositionalDescription> getDifferentiaArguments() {
		if (predicate.equals(Predicate.INTERSECTION)) {

			Collection<CompositionalDescription> diffs = new LinkedList<CompositionalDescription>();
			CompositionalDescription genus = null;
			for (CompositionalDescription arg : arguments) {
				if (arg.getPredicate().equals(Predicate.RESTRICTION)) {
					diffs.add(arg);
				}
				else {
					// genus
					if (genus == null) {
						// none assigned so far
						genus = arg;
					}
					else {
						return null;
					}
				}
			}

			return diffs;		
		}
		return null;
	}

	public Collection<Statement> getDescriptionAsStatements() {
		Graph g = new Graph();
		g.addStatements(this);		
		return g.getStatementCollection();
	}

	public String toString() {
		if (isAtomic())
			return nodeId;
		StringBuffer sb = new StringBuffer(predicate.toString());
		sb.append("( ");
		if (predicate.equals(Predicate.RESTRICTION)) {
			sb.append(getRelationId());
			if (restriction.isExistential())
				sb.append(" SOME");
			if (restriction.isUniversal())
				sb.append(" ALL");
			sb.append(" ");
		}
		for (CompositionalDescription d : arguments) {
			sb.append(d.toString());
			sb.append(" ");
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * 
	 * @return auto-generated skolem ID
	 */
	public String generateId() {
		if (isAtomic())
			return nodeId;
		if (getId() != null)
			return getId();
		StringBuffer sb = new StringBuffer("");
		if (predicate.equals(Predicate.RESTRICTION)) {
			sb.append(getRelationId());
			sb.append("(");
			sb.append(arguments.iterator().next().
					generateId());
			sb.append(")");
		}
		else {
			String conn = "^";
			if (predicate.equals(Predicate.UNION))
				conn = "|";
			if (predicate.equals(Predicate.COMPLEMENT))
				conn = "--";
			int n=0;
			LinkedList<CompositionalDescription> sortedArguments = new LinkedList(arguments);
			Collections.sort(sortedArguments);
			for (CompositionalDescription d : sortedArguments) {
				if (n > 0)
					sb.append("^");
				n++;
				sb.append(d.generateId());
			}
		}
		return sb.toString();
	}

	public int hashCode() {
		if (nodeId == null)
			return toString().hashCode();
		else
			return nodeId.hashCode();
	}

	/**
	 * CDs are sorted such that restrictions always come last;
	 * this provides a natural separation into genus-differentia order
	 */
	public int compareTo(Object o) throws ClassCastException  {
		if (!(o instanceof CompositionalDescription))
			throw new ClassCastException("A CompositionalDescription object expected.");

		CompositionalDescription d = (CompositionalDescription)o;
		if (predicate.equals(Predicate.RESTRICTION)) {
			if(d.getPredicate().equals(Predicate.RESTRICTION)) {
				// both are restrictions
				int c=
					restriction.getRelationId().compareTo(
							d.getRestriction().getRelationId());
				if (c==0) {
					return
					restriction.getTargetId().compareTo(
							d.getRestriction().getTargetId());
				}
				else {
					return c;
				}
			}
			else {
				// restriction always trumped by a non-restriction
				return 1;
			}
		}
		else {
			// non-restriction always trumps a restriction
			if(d.getPredicate().equals(Predicate.RESTRICTION)) {
				return -1;
			}
			else {
				// sorting should not be performed in toString
				return toString().compareTo(d.toString());
			}
		}
	}

}
