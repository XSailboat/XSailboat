package team.sailboat.base.metrics;

public interface IMetricsWriter<T extends Metrics>
{
	
	void store(@SuppressWarnings("unchecked") T... aMetrics) ;
}
