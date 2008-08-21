package org.obd.model.stats;

public class CountStatistic extends AggregateStatistic {
	public CountStatistic(MeasuredEntity measuredEntity) {
		super(measuredEntity);
		// TODO Auto-generated constructor stub
	}

	protected int count = 0;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public void incrementCount() {
		count++;
	}
}
