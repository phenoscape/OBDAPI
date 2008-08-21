package org.obd.model;

import java.io.Serializable;

/**
 * modifies the meaning of a Statement in terms of the class the statement is about
 * <p> 
 *  applicable to class nodes only: determines how this statement relates instances of this class
 * @author cjm
 *
 */
public class InstanceQuantifier implements Serializable {

	protected boolean isExistential;
	protected boolean isUniversal;
	protected Integer minCardinality;
	protected Integer maxCardinality;
	
	/**
	 * true if statement has meaning
	 * SubClassOf(restriction(relId,someValuesFrom(targetId))
	 * @return
	 */
	public boolean isExistential() {
		return isExistential;
	}
	public void setExistential(boolean isExistential) {
		this.isExistential = isExistential;
	}
	/**
	 * true if statement has meaning
	 * SubClassOf(restriction(relId,allValuesFrom(targetId))
	 * 
	 * note: can be co-applied with {@link #isExistential()}, in which case
	 * both axioms apply
	 * @return
	 */
	public boolean isUniversal() {
		return isUniversal;
	}
	public void setUniversal(boolean isUniversal) {
		this.isUniversal = isUniversal;
	}
	/**
	 * QCR: statement means
	 * SubClassOf(restriction(relId,maxCard(n,targetId))
	 * note: open world semantics apply
	 * @return
	 */
	public Integer getMaxCardinality() {
		return maxCardinality;
	}
	public void setMaxCardinality(Integer maxCardinality) {
		this.maxCardinality = maxCardinality;
	}
	public Integer getMinCardinality() {
		return minCardinality;
	}
	public void setMinCardinality(Integer minCardinality) {
		this.minCardinality = minCardinality;
	}
	
	
}
