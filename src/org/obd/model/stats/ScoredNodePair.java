package org.obd.model.stats;

import org.obd.model.Node;

// TODO
public class ScoredNodePair {

	private Node baseNode;
	private Node targetNode;
	private int totalNodesInCommon;
	private int totalNodes;
	private double congruence;
	
	public ScoredNodePair() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Node getBaseNode() {
		return baseNode;
	}
	public void setBaseNode(Node baseNode) {
		this.baseNode = baseNode;
	}
	public double getCongruence() {
		return congruence;
	}
	public void setCongruence(double congruence) {
		this.congruence = congruence;
	}
	public Node getTargetNode() {
		return targetNode;
	}
	public void setTargetNode(Node targetNode) {
		this.targetNode = targetNode;
	}
	public int getTotalNodes() {
		return totalNodes;
	}
	public void setTotalNodes(int totalNodes) {
		this.totalNodes = totalNodes;
	}
	public int getTotalNodesInCommon() {
		return totalNodesInCommon;
	}
	public void setTotalNodesInCommon(int totalNodesInCommon) {
		this.totalNodesInCommon = totalNodesInCommon;
	}
	
	public String toString() {
		return baseNode + " <-> " + targetNode + " total:" + totalNodes + " in_common:" + totalNodesInCommon + " congruence:"+congruence;
	}

}
