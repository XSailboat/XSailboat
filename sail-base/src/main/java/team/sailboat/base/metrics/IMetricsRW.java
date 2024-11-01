package team.sailboat.base.metrics;

public interface IMetricsRW<X extends Metrics , Y extends TimeObject> extends IMetricsReader<Y> , IMetricsWriter<X>
{

}
