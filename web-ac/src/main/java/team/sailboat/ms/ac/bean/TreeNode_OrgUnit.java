package team.sailboat.ms.ac.bean;

import java.util.Collection;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.ms.bean.ITreeNodeBean;
import team.sailboat.commons.ms.bean.ITreeNodeChildrenInv;
import team.sailboat.ms.ac.dbean.OrgUnit;

/**
 * 
 * 组织机构的树节点
 *
 * @author yyl
 * @since 2024年11月27日
 */
@Schema(description = "组织机构的树节点")
public class TreeNode_OrgUnit extends OrgUnit.BOrgUnit implements ITreeNodeBean , ITreeNodeChildrenInv
{
	Function<String , Collection<String>> mChildIdPvd ;
	
	@Override
	public void setChildIdProvider(Function<String, Collection<String>> aChildIdPvd)
	{
		mChildIdPvd = aChildIdPvd ;
	}

	@Schema(description = "子节点id集合")
	@Override
	public Collection<String> getChildIds()
	{
		return mChildIdPvd.apply(getId()) ;
	}

	@Override
	public void setChildIds(Collection<String> aChildIds)
	{
		throw new UnsupportedOperationException("不支持的操作") ;
	}

}
