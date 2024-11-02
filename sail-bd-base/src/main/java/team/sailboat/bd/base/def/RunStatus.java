package team.sailboat.bd.base.def;

public enum RunStatus
{
	/**
	 * 未运行
	 */
	norun
	/**
	 * 启动过程中
	 */
	, starting
	/**
	 * 已经进去执行队列，构建出任务实例，正在等待执行中
	 */
	,waiting
	/**
	 * 正在执行执行
	 */
	,running
	/**
	 * 执行失败
	 */
	,failure
	/**
	 * 执行成功
	 */
	,success ;
}
