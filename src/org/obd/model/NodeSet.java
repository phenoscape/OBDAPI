package org.obd.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * An unordered collection of zero or more Nodes.
 * <p>
 * Nodes may be grouped into sets for different reasons: for example, a set of
 * nodes may be all mutually disjoint or different.
 * <p>
 * 
 * @author cjm
 *
 */
public class NodeSet extends Node implements Comparable {
	private String pairwiseRelationId;
	private Collection<Node> nodes = new HashSet<Node>();
	private boolean isConnected;
	private boolean isHoldsForAllPairs;
	private String sourceId;
	private boolean isDisjointWith;
	private boolean isEquivalentTo;
	
	
	public Collection<Node> getNodes() {
		return nodes;
	}
	public void setNodes(Collection<Node> nodes) {
		this.nodes = nodes;
	}
	public void addNodes(Collection<Node> nodes) {
		this.nodes.addAll(nodes);
	}
	
	public void addNode(Node node) {
		this.nodes.add(node);
	}
	/**
	 * this relation holds between members of the set
	 * @return unique global node identifier
	 */
	public String getPairwiseRelationId() {
		return pairwiseRelationId;
	}
	public void setPairwiseRelationId(String relationId) {
		this.pairwiseRelationId = relationId;
	}
	/**
	 * true if the set is connected. That is for any two nodes n1, nX, there is some
	 * path n1 r n2 r n3 r n4 .. r nX that connects them
	 * @return
	 */
	public boolean isConnected() {
		return isConnected;
	}
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	/**
	 * true if {@link #getPairwiseRelationId()} holds between all pairs in set
	 * @return
	 */
	public boolean isHoldsForAllPairs() {
		return isHoldsForAllPairs;
	}
	public void setHoldsForAllPairs(boolean isHoldsForAllPairs) {
		this.isHoldsForAllPairs = isHoldsForAllPairs;
	}

	/**
	 * creates a statement n1 r n2 for all pairs (n1,n2) in set
	 * @return
	 */
	public Collection<LinkStatement> getPairwiseStatements() {
		Collection<LinkStatement> statements = new LinkedList<LinkStatement>();
		for (Node n1 : nodes)
			for (Node n2 : nodes)
				if (!n1.equals(n2))
					statements.add(new LinkStatement(n1,pairwiseRelationId,n2,sourceId));
		return statements;
	}
	
	public String toString() {
		if (getId() != null)
			return super.toString();
		return StringUtils.join(getNodes(), "--");
	}
	
	/**
	 * generates a unique Id
	 * @return
	 */
	public String genId() {
		HashSet<String> ids = new HashSet<String>();
		for (Node n : getNodes())
			ids.add(n.getId());
		return StringUtils.join(ids, "--");
	}
	
	public int hashCode() {
		return toString().hashCode();
	}

	public int compareTo(Object n2) {
		if (!(n2 instanceof NodeSet))
			throw new ClassCastException("A NodeSet object expected.");
		return toString().compareTo(((NodeSet)n2).toString());  
	}
	
}
