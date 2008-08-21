package org.obd.model.stats;


public class ScoredNode implements Comparable {
	protected String nodeId;
	protected double score;
	protected int count;
	
	public ScoredNode() {
		super();
	}
	
	public ScoredNode(String nodeId) {
		super();
		this.nodeId = nodeId;
	}

	public ScoredNode(String nodeId, double score) {
		super();
		this.nodeId = nodeId;
		this.score = score;
	}

	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public int hashCode() {
		return nodeId.hashCode();
	}

	public String toString() {
		return nodeId + " ["+score+"]";
	}

	public int compareTo(Object sn)  throws ClassCastException  {
	    if (!(sn instanceof ScoredNode))
	        throw new ClassCastException("A ScoredNode object expected.");
		return score > ((ScoredNode)sn).getScore()  ? 1 : -1;
	}
}
