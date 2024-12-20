package team.sailboat.commons.fan.tree;

import team.sailboat.commons.fan.infc.EConsumer;

public class BTVisitorAssist
{

	public static void preOrderVisit(BTNode aNode , EConsumer<BTNode , InterruptedException> aVisitor)
	{
		BTNode root = aNode ;
		while(root.getParent() != null)
			root = (BTNode)root.getParent() ; 
		try
		{
			preOrderVisit_0(root, aVisitor) ;
		}
		catch (InterruptedException e)
		{}
	}
	
	static void preOrderVisit_0(BTNode aNode , EConsumer<BTNode , InterruptedException> aVisitor) throws InterruptedException
	{
		if(aNode == null)
			return ;
		aVisitor.accept(aNode);
		preOrderVisit_0(aNode.getLeftChild() , aVisitor);
		preOrderVisit_0(aNode.getRightChild(), aVisitor);
	}

	
	/**
	 * 中序遍历
	 * @param aNode
	 * @param aVisitor
	 */
	public static void midOrderVisit(BTNode aNode , EConsumer<BTNode , InterruptedException> aVisitor)
	{
		BTNode root = aNode ;
		while(root.getParent() != null)
			root = (BTNode)root.getParent() ; 
		try
		{
			midOrderVisit_0(root, aVisitor) ;
		}
		catch (InterruptedException e)
		{}
	}
	
	static void midOrderVisit_0(BTNode aNode , EConsumer<BTNode , InterruptedException> aVisitor) throws InterruptedException
	{
		if(aNode == null)
			return ;
		midOrderVisit_0(aNode.getLeftChild() , aVisitor);
		aVisitor.accept(aNode);
		midOrderVisit_0(aNode.getRightChild(), aVisitor);
	}
}
