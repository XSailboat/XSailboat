package team.sailboat.commons.fan.tree;

import java.util.ArrayList;

public class BTNode extends FatTreeNode
{
	public BTNode()
	{
		mChildren = new ArrayList<>(2) ;
		mChildren.add(null) ;
		mChildren.add(null) ;
	}
	
	public BTNode(String aName)
	{
		this() ;
		mName= aName ;
	}
	
	public BTNode getLeftChild()
	{
		return (BTNode) mChildren.get(0) ;
	}
	
	public void setLeftChild(BTNode aChild)
	{
		mChildren.set(0 , aChild) ;
	}
	
	public BTNode getRightChild()
	{
		return (BTNode)mChildren.get(1) ;
	}
	
	public void setRightChild(BTNode aChild)
	{
		mChildren.set(1 , aChild) ;
	}
	
	public BTNode removeRightChild()
	{
		BTNode node = (BTNode)mChildren.get(1) ;
		mChildren.set(1, null) ;
		return node ;
	}
	
	@Override
	public boolean isLeaf()
	{
		return mChildren.get(0) != null && mChildren.get(1) != null ;
	}
	
	@Override
	public boolean hasChildren()
	{
		return !isLeaf() ;
	}
}
