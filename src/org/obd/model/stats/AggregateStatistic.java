package org.obd.model.stats;

import java.io.Serializable;

public class AggregateStatistic implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum AggregateType { COUNT, AVERAGE, MIN, MAX }
	
	protected AggregateType type;
	protected MeasuredEntity measuredEntity;
	
	public AggregateStatistic(MeasuredEntity measuredEntity) {
		super();
		this.measuredEntity = measuredEntity;
	}

	public MeasuredEntity getMeasuredEntity() {
		return measuredEntity;
	}

	public void setMeasuredEntity(MeasuredEntity measuredEntity) {
		this.measuredEntity = measuredEntity;
	}	
	
}
