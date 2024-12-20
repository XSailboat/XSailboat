package team.sailboat.commons.ms.bean;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;

@Schema(description = "树形结构数据")
@Data
@NoArgsConstructor
public class TreeBean<T extends ITreeNodeBean>
{
	@Schema(description = "根节点id")
	Collection<String> rootNodeIds = XC.linkedHashSet() ;
	
	@Schema(description = "节点数据，键是节点id，值是节点")
	Map<String , T> data = XC.hashMap() ;
	
	@Getter(value = AccessLevel.NONE)
	@Setter(value = AccessLevel.NONE)
	final Map<String , Collection<String>> mChildIdPvd = XC.hashMap() ;
	
	Function<String, T> mSourceGetter ;
	
	public TreeBean(Function<String, T> aSourceGetter)
	{
		mSourceGetter = aSourceGetter ;
	}
	
	public boolean addNode(T aNode)
	{
		if(data.putIfAbsent(aNode.getId(), aNode) != null)
			return false ;
		boolean isRoot = XString.isEmpty(aNode.getParentId()) ;
		if(isRoot)
			rootNodeIds.add(aNode.getId()) ;
		else if(mSourceGetter != null && data.get(aNode.getParentId()) == null)
		{
			// 把祖先节点，自动加进来
			T parent = mSourceGetter.apply(aNode.getParentId()) ;
			Assert.notNull(parent , "找不到id为 %s 的树节点！" , aNode.getParentId()) ;
			addNode(parent) ;
		}
		if(aNode instanceof ITreeNodeChildrenInv n)
		{
			n.setChildIdProvider(mChildIdPvd::get) ;
			if(!isRoot)
				XC.getOrPut(mChildIdPvd, aNode.getParentId() , XC::linkedHashSet)
					.add(aNode.getId()) ;
		}
		return true ;
	}
}
