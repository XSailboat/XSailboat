package team.sailboat.bd.base.model.dag;

import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;

/**
 * 实例生成方式
 *
 * @author yyl
 * @since 2021年3月10日
 */
public enum InstGenWay
{
	/**
	 * T+1方式生成
	 */
	Tp1 ,
	/**
	 * 提交后10分钟后生成。延迟10分钟是为了防抖动
	 */
	RightNow ;
	
	@BForwardMethod
	public static Object forward(InstGenWay aSource)
	{
		return aSource==null?null:aSource.name() ;
	}
	
	@BReverseMethod
	public static InstGenWay reverse(Object aSource)
	{
		return aSource == null?null:InstGenWay.valueOf(aSource.toString()) ;
	}
}
