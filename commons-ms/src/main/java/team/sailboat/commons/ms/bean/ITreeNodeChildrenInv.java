package team.sailboat.commons.ms.bean;

import java.util.Collection;
import java.util.function.Function;

/**
 * 
 * 通过父节点关系构建子节点关系(children)
 *
 * @author yyl
 * @since 2024年11月27日
 */
public interface ITreeNodeChildrenInv
{
	void setChildIdProvider(Function<String , Collection<String>> aChildIdPvd) ;
}
