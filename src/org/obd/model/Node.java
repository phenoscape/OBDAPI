package org.obd.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Fundamental unit of representation: any entity can be represented using a Node
 * <p>
 * equivalent to an RDF resource. can represent instances, relations/slots and universals
 * <p>
 * Note that {@link Statement}s can also be nodes, in that they can be the subject or
 * target of other statements
 * 
 * @author cjm
 *
 */
public class Node implements Serializable, Comparable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum Metatype {CLASS, INSTANCE, RELATION};
	protected String id;
	protected String label;
	protected Collection<Statement> statements;
	protected String sourceId;
	protected boolean isAnonymous;
	protected boolean isObsolete;
	protected CompositionalDescription compositionalDescription;
	protected Metatype metatype;
	

	public Node() {
		statements = new HashSet<Statement>();
	}

	public Node(String id) {
		this.id = id;
		statements = new HashSet<Statement>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String name) {
		this.label = name;
	}
	

	public Metatype getMetatype() {
		return metatype;
	}

	public void setMetatype(Metatype metatype) {
		this.metatype = metatype;
	}
	public void setMetatype(String metatype) {
		if (metatype== null)
			this.metatype = Metatype.INSTANCE;
		else if (metatype.equals("C"))
			this.metatype = Metatype.CLASS;
		else if (metatype.equals("R"))
			this.metatype = Metatype.RELATION;
		else this.metatype = Metatype.INSTANCE;
		
	}

	// WSDL-friendly : use arrays
	public Statement[] getStatements() {
		return (Statement[])statements.toArray(new Statement[0]);
	}
	
	public Node[] getTargetNodes(String relId) {
		LinkedList<Node> tns = new LinkedList<Node>();
		for (Statement s : getStatements()) {
			if (s instanceof LinkStatement) {
				if (s.getRelationId() != null && s.getRelationId().equals(relId)) {
					tns.add(((LinkStatement)s).getTargetNode());
				}
			}
		}
		return (Node[]) tns.toArray(new Node[0]);
	}
	
	/**
	 * finds all statements directly attached to node that form part of
	 * owl:intersectionOf constructs
	 * 
	 * @return all Statements in which {@link Statement#intersectionSemantics()} is true
	 */
	public LinkStatement[] getIntersectionStatements() {
		LinkedList<Statement> statements = new LinkedList<Statement>();
		for (Statement s : getStatements()) {
			if (s instanceof LinkStatement) {
					LinkStatement ls = (LinkStatement)s;
					if (ls.intersectionSemantics)
						statements.add(ls);
			}
		}
		return (LinkStatement[])statements.toArray(new LinkStatement[0]);
	}
	

	public void setStatements(Collection<Statement> statements) {
		this.statements = statements;
	}
	
	public void addStatement(Statement statement) {
		if (statements == null)
			statements = new HashSet<Statement>();
		statements.add(statement);
	}
	
	public void addStatements(Collection<Statement> newStatements) {
		if (statements == null)
			statements = new HashSet<Statement>();
		statements.addAll(newStatements);
	}

	public void clearStatements() {
		statements = new LinkedList<Statement>();
	}
	
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public boolean isAnonymous() {
		return isAnonymous;
	}

	public void setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
	}

	public boolean isObsolete() {
		return isObsolete;
	}

	public void setObsolete(boolean isObsolete) {
		this.isObsolete = isObsolete;
	}

	/**
	 * @return null if no description set
	 */
	public CompositionalDescription getCompositionalDescription() {
		return compositionalDescription;
	}

	public void setCompositionalDescription(
			CompositionalDescription compositionalDescription) {
		this.compositionalDescription = compositionalDescription;
	}



	public String toString() {
		String s = id + " \""+label+"\"";
		if (isAnonymous)
			s = " ANON:"+id;
		if (sourceId != null)
			s = s + " src:"+sourceId;
		return s;
	}
	

	public int hashCode() {
		return id.hashCode();
	}
	
	public boolean equals(Object o) {
		if (o instanceof Node)
			return o.toString().equals(toString());
		return false;
	}

	public int compareTo(Object n2) {
		if (!(n2 instanceof Node))
			throw new ClassCastException("A Node object expected.");
		return getId().compareTo(((Node)n2).getId());  
	}

	
}
