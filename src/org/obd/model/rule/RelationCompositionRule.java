package org.obd.model.rule;

public class RelationCompositionRule implements InferenceRule {
	protected String impliedRelation;
	protected String leftRelation;
	protected String rightRelation;
	protected boolean isLeftInverted;
	protected boolean isRightInverted;
	
	public RelationCompositionRule(String impliedRelation, String leftRelation, String rightRelation, boolean isLeftInverted, boolean isRightInverted) {
		super();
		this.impliedRelation = impliedRelation;
		this.leftRelation = leftRelation;
		this.rightRelation = rightRelation;
		this.isLeftInverted = isLeftInverted;
		this.isRightInverted = isRightInverted;
	}
	
	public String getImpliedRelation() {
		return impliedRelation;
	}
	public void setImpliedRelation(String impliedRelation) {
		this.impliedRelation = impliedRelation;
	}
	public boolean isLeftInverted() {
		return isLeftInverted;
	}
	public void setLeftInverted(boolean isLeftInverted) {
		this.isLeftInverted = isLeftInverted;
	}
	public boolean isRightInverted() {
		return isRightInverted;
	}
	public void setRightInverted(boolean isRightInverted) {
		this.isRightInverted = isRightInverted;
	}
	public String getLeftRelation() {
		return leftRelation;
	}
	public void setLeftRelation(String leftRelation) {
		this.leftRelation = leftRelation;
	}
	public String getRightRelation() {
		return rightRelation;
	}
	public void setRightRelation(String rightRelation) {
		this.rightRelation = rightRelation;
	}
	
	

}
