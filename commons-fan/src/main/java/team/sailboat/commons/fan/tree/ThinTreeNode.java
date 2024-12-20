package team.sailboat.commons.fan.tree;

import java.util.function.BiConsumer;

import team.sailboat.commons.fan.struct.XDataBag;

/**
 * 
 *
 * @author yyl
 * @since 2024年11月27日
 */
public class ThinTreeNode extends TreeNode implements IDataTreeNode
{
	XDataBag mDataBag ;
	
	public ThinTreeNode()
	{
		super();
	}

	public ThinTreeNode(String aName, ITreeNode aParent, ITreeNode[] aChildren)
	{
		super(aName, aParent, aChildren);
	}

	public ThinTreeNode(String aName, ITreeNode aParent)
	{
		super(aName, aParent);
	}

	public ThinTreeNode(String aName, ITreeNode[] aChildren)
	{
		super(aName, aChildren);
	}

	public ThinTreeNode(String aName)
	{
		super(aName);
	}

	@Override
	public Object getData(String aKey)
	{
		return mDataBag != null?mDataBag.getData(aKey):null ;
	}

	@Override
	public void setData(String aKey, Object aData)
	{
		if(mDataBag == null)
			mDataBag = new XDataBag() ;
		mDataBag.setData(aKey, aData) ;
	}

	@Override
	public Object removeData(String aKey)
	{
		return mDataBag != null?mDataBag.removeData(aKey):null;
	}

	@Override
	public int getDataEntryAmount()
	{
		return mDataBag != null?mDataBag.getDataEntryAmount():0 ;
	}

	@Override
	public void forEachDataEntry(BiConsumer<String, Object> aBiConsumer)
	{
		if(mDataBag != null)
			mDataBag.forEach((key , val)->aBiConsumer.accept((String)key, val)) ;
	}

}
