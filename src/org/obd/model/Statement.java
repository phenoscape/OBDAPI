package org.obd.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Representation of some assertion about the world
 * <p>
 * A statement is some proposition, relation one or more entities via a predicate.
 * Examples: John loves Mary, all cell nucleus part_of cell, p53 participates_in DNA_Repair
 *
 * Within the structure of the document, 
 * Statements can be nested beneath the nodes which they relate, or they can be nested 
 * underneath the Graph node. These are semantically identical. Clients may request one
 * form or another from servers in order to process them more easily.
 * For example: if a client requests all statements pertaining to the node for "holoprosencephaly",
 * the server may return these statements at the top level. However, all the nodes related to
 * holoprosencephaly node in this statement set may have additional metadata attached as LiteralStatements
 * at the node level
 *
 * corresponds to an RDF triple, or in some cases, a bundle of triples
 * -- more accurately, corresponds to an OWL axiom, or fact

 * @author cjm
 *
 */
public class Statement extends Node implements Serializable, Cloneable {
	protected String nodeId;
	protected String relationId;
	protected String targetId;
	protected String positedByNodeId;
	protected String contextId;
	protected boolean isInferred;
	protected boolean isNegated;
	protected Collection<Statement> subStatements;
	protected boolean appliesToAllInstancesOf;
	protected boolean intersectionSemantics;
	protected boolean unionSemantics;
	protected InstanceQuantifier instanceQuantifier;

	public Statement() {
		super();
	}
	public Statement(String id) {
		super();
		nodeId = id;
	}
	public Statement(String n, String r, String t) {
		super();
		nodeId = n;
		relationId = r;
		targetId = t;
	}

	public boolean isAppliesToAllInstancesOf() {
		return appliesToAllInstancesOf;
	}
	public void setAppliesToAllInstancesOf(boolean appliesToAllInstancesOf) {
		this.appliesToAllInstancesOf = appliesToAllInstancesOf;
	}

	public boolean isExistential() {
		if (instanceQuantifier == null)
			return false;
		else
			return instanceQuantifier.isExistential();
	}
	public void setExistential(boolean isExistential) {
		if (instanceQuantifier == null)
			instanceQuantifier = new InstanceQuantifier();
		instanceQuantifier.setExistential(isExistential);
	}
	public boolean isUniversal() {
		if (instanceQuantifier == null)
			return false;
		else
			return instanceQuantifier.isUniversal();
	}
	public void setUniversal(boolean isUniversal) {
		if (instanceQuantifier == null)
			instanceQuantifier = new InstanceQuantifier();
		instanceQuantifier.setUniversal(isUniversal);
	}
	public boolean isIntersectionSemantics() {
		return intersectionSemantics;
	}
	public void setIntersectionSemantics(boolean hasIntersectionSemantics) {
		this.intersectionSemantics = hasIntersectionSemantics;
	}
	public boolean isUnionSemantics() {
		return unionSemantics;
	}
	public void setUnionSemantics(boolean hasUnionSemantics) {
		this.unionSemantics = hasUnionSemantics;
	}
	public boolean isInferred() {
		return isInferred;
	}
	public void setInferred(boolean isImplied) {
		this.isInferred = isImplied;
	}
	public InstanceQuantifier getInstanceQuantifier() {
		return instanceQuantifier;
	}
	public void setInstanceQuantifier(InstanceQuantifier instanceQuantifier) {
		this.instanceQuantifier = instanceQuantifier;
	}

	public String getPositedByNodeId() {
		return positedByNodeId;
	}
	public void setPositedByNodeId(String positedByNodeId) {
		this.positedByNodeId = positedByNodeId;
	}
	/**
	 * @return the identifier of the node of which this statement is about
	 */
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	/**
	 * @return the identifier of the relation node used in this statement
	 */
	public String getRelationId() {
		return relationId;
	}
	public void setRelationId(String relationId) {
		this.relationId = relationId;
	}
	/**
	 * @return the identifier of the relatum node
	 */
	public String getTargetId() {
		return targetId;
	}
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getContextId() {
		return contextId;
	}
	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	/**
	 * Statements can either be factual statements (of the kind found in ontologies)
	 * or annotation statements (e.g. conclusions or hypotheses deriving from experiments).
	 * The semantic dividing line is somewhat fuzzy, the final decision is down to the data producer
	 * and how they wish their statements to be treated.
	 * factual statements are assumed to be true within a strict context;
	 * annotation statements are assumed to be contextually true (e.g. they are generalizations derived from instances)
	 * 
	 * @return true if this statement is an annotation statement
	 */
	public boolean isAnnotation() {
		if (getSubStatements().size() == 0)
			return false;
		return true;
	}

	public Collection<Statement> getSubStatements() {
		if (subStatements == null)
			subStatements = new LinkedList<Statement>();
		return subStatements;
	}
	public void setSubStatements(Collection<Statement> subStatements) {
		this.subStatements = subStatements;
	}
	public void addSubStatement(Statement s) {
		if (subStatements == null)
			subStatements = new LinkedList<Statement>();
		subStatements.add(s);
	}

	public void addSubLiteralStatement(String relationId, String value) {
		LiteralStatement s = new LiteralStatement();
		s.setRelationId(relationId);
		s.setValue(value);
		s.setSourceId(getSourceId());
		addSubStatement(s);
	}

	public void addSubLinkStatement(String relationId, String targetId) {
		LinkStatement s = new LinkStatement();
		s.setRelationId(relationId);
		s.setTargetId(targetId);
		s.setSourceId(getSourceId());
		addSubStatement(s);
	}



	public boolean isNegated() {
		return isNegated;
	}
	public void setNegated(boolean isNegated) {
		this.isNegated = isNegated;
	}
	public String toString() {
		String s = nodeId+" "+relationId+" "+targetId;
		if (isInferred)
			s = s + " [Implied]";
		if (positedByNodeId != null)
			s = s + " positedBy:"+positedByNodeId;
		if (sourceId != null)
			s = s + " source:"+sourceId;
		for (Statement ss : getSubStatements()) {
			s = s + " <<"+ss.toString()+">> ";
		}
		return s;
	}
	
	public String getSkolemId() {
		String s = nodeId+"--"+relationId+"--"+targetId;
		return s;
	}

	public static Statement[] toUniqueArray(Collection<Statement> statements) {
		Set<String> names = new HashSet<String>();
		Collection<Statement> uniqueStatements = new LinkedList<Statement>();
		for (Statement statement : statements) {
			String name = statement.toString();
			if (names.contains(name))
				continue;
			uniqueStatements.add(statement);
			names.add(name);
		}
		return (Statement[])uniqueStatements.toArray(new Statement[0]);
	}

	public boolean isLiteral() {
		return false;
	}
	

	public int hashCode() {
		// TOOD: either make fields immutable or use alternate hashing mechanism
		return toString().hashCode();

	}
	
	public Object clone() {
		Statement clone = new Statement();
		clone.setNodeId(getNodeId());
		clone.setRelationId(getRelationId());
		clone.setTargetId(getTargetId());
        return clone;
	}

}
