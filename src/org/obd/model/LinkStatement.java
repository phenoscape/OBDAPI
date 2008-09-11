package org.obd.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.obd.model.vocabulary.TermVocabulary;
import org.purl.obo.vocab.RelationVocabulary;

/**
 * A {@link Statement} connecting two Nodes
 * <p>
 * Examples:
 * <ul>
 *  <li> p53[node] participates_in[rel] DNA_Repair[target] </li>
 * </ul>
 * 
 * @author cjm
 *
 */
public class LinkStatement extends Statement {

	private static RelationVocabulary rvocab ;
	private static TermVocabulary tvocab ;
	protected Node targetNode;
	
	static {
		rvocab = new RelationVocabulary();
		tvocab = new TermVocabulary();
	}

	public LinkStatement(String su, String rel, String ob) {
		super(su,rel,ob);
	}
	public LinkStatement() {
		super();
	}

	public LinkStatement(String su, String rel, String ob, String sourceId) {
		super(su,rel,ob);
		this.sourceId = sourceId;
	}

	public LinkStatement(Node su, String rel, Node ob, String sourceId) {
		super(su.getId(),rel,ob.getId());
		this.sourceId = sourceId;
	}

	public LinkStatement(LinkStatement s) {
		super(s.getNodeId(),s.getRelationId(),s.getTargetId());
	
	}
	public Node getTargetNode() {
		return targetNode;
	}
	public void setTargetNode(Node targetNode) {
		this.targetNode = targetNode;
	}
	
	/**
	 * @return true if this link connects a class to its superclass
	 */
	public boolean isSubClassSemantics() {
		// TODO - use RDFS vocabulary
		return relationId.equals(rvocab.is_a()) || relationId.equals("rdfs:subClassOf");
	}
	public void setSubClassSemantics(boolean set) {
		if (set)
			setRelationId(rvocab.is_a());
		else
			if (isSubClassSemantics())
				setRelationId(null);
	}

	/**
	 * @return true if this link connects a node to a dbxref node
	 * (i.e. a mapping)
	 */
	public boolean isXref() {
		return relationId.equals(tvocab.HAS_DBXREF());
	}

	/**
	 * R(X,X) is a reflexive relation. Reflexive relations may be computed
	 * by the shard if these relations are entailed by the axioms of the ontology.
	 * It may be desirable to filter these at the presentation layer, in which
	 * case this test can be applied 
	 * 
	 * @return true if node is the same as target
	 */
	public boolean isReflexive() {
		return nodeId.equals(targetId);
	}

	public static LinkStatement[] toUniqueArray(Collection<LinkStatement> statements) {
		Set<String> names = new HashSet<String>();
		Collection<LinkStatement> uniqueStatements = new LinkedList<LinkStatement>();
		for (LinkStatement statement : statements) {
			String name = statement.toString();
			if (names.contains(name))
				continue;
			uniqueStatements.add(statement);
			names.add(name);
		}
		return (LinkStatement[])uniqueStatements.toArray(new LinkStatement[0]);
	}

	public String toString() {
		String s = super.toString();
		if (this.intersectionSemantics)
			s = s + " [^]";
		return s;
	}

}
