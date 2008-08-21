package org.obd.model.stats;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;


public class AggregateStatisticCollection implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected String id;

	protected HashMap<String,AggregateStatistic> statMap = 
		new HashMap<String,AggregateStatistic>();
	// for Axis2
	protected Collection<AggregateStatistic> stats = 
		new HashSet<AggregateStatistic>();

	public AggregateStatisticCollection() {
		super();
	}

	public void setCount(String type, String filter, Integer c) {
		MeasuredEntity e = getMeasuredEntity(type, filter);
		AggregateStatistic stat = getStat(e);
		((CountStatistic)stat).setCount(c);
	}
	
	public void incrementCount(MeasuredEntity entity) {
		AggregateStatistic stat = getStat(entity);
		((CountStatistic)stat).incrementCount();
	}

	public AggregateStatistic getStat(MeasuredEntity entity) {
		AggregateStatistic stat = statMap.get(entity.toString());
		if (stat == null) {
			System.out.println("creating a new stat for "+entity);
			stat = new CountStatistic(entity);
			System.out.println("creating a new stat for "+entity+" / "+stat);
			statMap.put(entity.toString(), stat);
			stats.add(stat);
		}
		return stat;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Collection<AggregateStatistic> getStats() {
		return stats;
	}
	
	
	public void setStats(Collection<AggregateStatistic> stats) {
		this.stats = stats;
	}
	
	public void combine(AggregateStatisticCollection c) {
		
	}

	// e.g. type=link entity=cell-ont
	public void incrementCount(String type, String filter) {
		MeasuredEntity entity = getMeasuredEntity(type,filter);
		incrementCount(entity);
	}
	public MeasuredEntity getMeasuredEntity(String type, String filter) {
		return new MeasuredEntity(type,filter);
	}

	
		
}
