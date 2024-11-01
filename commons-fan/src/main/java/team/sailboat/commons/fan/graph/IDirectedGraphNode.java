package team.sailboat.commons.fan.graph;

import java.util.Collection;

/**
 * 有向图节点
 *
 * @author yyl
 * @since 2022年3月31日
 */
public interface IDirectedGraphNode
{
	/**
	 * 节点id
	 * @return
	 */
	String getId() ;
	
	/**
	 * 取得前置节点id
	 * @return
	 */
	Collection<String> getPrecursorIds() ;
	
	/**
	 * 取得后置节点id
	 * @return
	 */
	Collection<String> getFollowerIds() ;
}
