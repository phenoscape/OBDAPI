package org.obd.query;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A {@link QueryTerm} that performs a boolean operation over 1 or more sub-terms
 * @author cjm
 *
 */
public class NodeSetQueryTerm extends QueryTerm {

	protected boolean isConjunctive = false;
	protected boolean isInternalIdentifiers = false;
	protected Collection<String> nodeIds = new LinkedList<String>();

	public NodeSetQueryTerm() {
		super();
	}
	
	public NodeSetQueryTerm(Collection<String> nodeIds) {
		super();
		this.nodeIds = nodeIds;
	}

	public void setInternalNodeIds(Collection<Integer> ids) {
		this.setInternalIdentifiers(true);
		for (Integer id : ids)
			nodeIds.add(id.toString());
	}

	public boolean isConjunctive() {
		return isConjunctive;
	}

	public void setConjunctive(boolean isConjunctive) {
		this.isConjunctive = isConjunctive;
	}
	
	

	public boolean isInternalIdentifiers() {
		return isInternalIdentifiers;
	}

	public void setInternalIdentifiers(boolean isInternalIdentifiers) {
		this.isInternalIdentifiers = isInternalIdentifiers;
	}

	public Collection<String> getNodeIds() {
		return nodeIds;
	}

	public void setNodeIds(Collection<String> nodeIds) {
		this.nodeIds = nodeIds;
	}
	
	
	public String toString() {
		return "[nodes: "+nodeIds.toString()+"]";
	}

}
