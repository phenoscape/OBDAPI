package org.obd.model.stats;

import java.io.Serializable;

public class MeasuredEntity implements Serializable {
	protected String type;
	protected String filter;
	
	public MeasuredEntity(String type, String filter) {
		this.type=type;
		this.filter=filter;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public boolean equals(MeasuredEntity e2) {
		return e2.getType().equals(this.type) && e2.getFilter().equals(this.filter);
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}

	public String toString() {
		return type + " by " + filter;
	}
	
}
