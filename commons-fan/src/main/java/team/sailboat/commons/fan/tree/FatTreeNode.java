package team.sailboat.commons.fan.tree;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import team.sailboat.commons.fan.collection.XC;

/**
 * <strong>功能：</strong>
 * <p style="text-indent:2em">
 * TreeNode通过setData设置的数据不宜太多，此类的实现省空间，但存取的效率较差			<br>
 * 如果需要高效存取，可以使用TreeDataNode
 *
 * @author yyl
 * @since 2017年6月22日
 */
public class FatTreeNode extends TreeNode implements IDataTreeNode
{
	
	Map<String, Object> mDataMap ;

	public FatTreeNode()
	{
		super();
	}

	public FatTreeNode(String aName, ITreeNode aParent, ITreeNode[] aChildren)
	{
		super(aName, aParent, aChildren);
	}

	public FatTreeNode(String aName, ITreeNode aParent)
	{
		super(aName, aParent);
	}

	public FatTreeNode(String aName, ITreeNode[] aChildren)
	{
		super(aName, aChildren);
	}

	public FatTreeNode(String aName)
	{
		super(aName);
	}

	@Override
	public void forEachDataEntry(BiConsumer<String, Object> aConsumer)
	{
		if(XC.isNotEmpty(mDataMap))
			mDataMap.forEach(aConsumer) ;
	}

	@Override
	public Object getData(String aKey)
	{
		return mDataMap != null?mDataMap.get(aKey):null;
	}

	@Override
	public void setData(String aKey, Object aData)
	{
		if(mDataMap == null)
			mDataMap = new LinkedHashMap<>() ;
		mDataMap.put(aKey, aData) ;
	}

	@Override
	public Object removeData(String aKey)
	{
		return mDataMap != null?mDataMap.remove(aKey):null ;
	}
	
	@Override
	public int getDataEntryAmount()
	{
		return mDataMap.size() ;
	}
}
