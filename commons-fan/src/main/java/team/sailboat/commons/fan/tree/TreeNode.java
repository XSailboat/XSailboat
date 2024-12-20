
package team.sailboat.commons.fan.tree;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.infc.IteratorPredicate;

public class TreeNode implements ITreeNode
{
	String mName ;
	ITreeNode mParent ;
	protected List<ITreeNode> mChildren ;

	public TreeNode()
	{
	}
	
	public TreeNode(String aName)
	{
		mName = aName ;
	}
	
	public TreeNode(String aName , ITreeNode[] aChildren)
	{
		this(aName) ;
		addChildren(aChildren) ;
	}
	
	public TreeNode(String aName , ITreeNode aParent , ITreeNode[] aChildren)
	{
		this(aName , aParent) ;
		addChildren(aChildren) ;
	}
	
	public TreeNode(String aName , ITreeNode aParent)
	{
		this(aName) ;
		mParent = aParent ;
		mParent.addChildren(this) ;
	}
	
	@Override
	public String getName()
	{
		return mName ;
	}

	@Override
	public void setName(String aName)
	{
		mName = aName ;
	}

	@Override
	public boolean hasChildren()
	{
		return mChildren != null && mChildren.size()>0 ;
	}
	
	@Override
	public boolean isLeaf()
	{
		return !hasChildren() ;
	}

	@Override
	public synchronized void addChild(int aIndex, ITreeNode aChild)
	{
		checkAndCreateChildrenList();
		if(!mChildren.contains(aChild))
		{
			mChildren.add(aIndex, aChild) ;
			((TreeNode)aChild).mParent = this ;
		}
	}
	
	private void checkAndCreateChildrenList()
	{
		if(mChildren == null)
		{
			synchronized (this)
			{
				if(mChildren == null)
					mChildren = new ArrayList<>() ;
			}
		}
	}

	@Override
	public synchronized void addChild(ITreeNode aChild)
	{
		checkAndCreateChildrenList();
		if(!mChildren.contains(aChild))
		{
			mChildren.add(aChild) ;
			((TreeNode)aChild).mParent = this ;
		}
	}

	@Override
	public synchronized void addChildren(ITreeNode... aChildren)
	{
		if(mChildren == null) mChildren = new ArrayList<>() ;
		for(ITreeNode support : aChildren)
		{
			if(!mChildren.contains(support))
			{
				mChildren.add(support) ;
				((TreeNode)support).mParent = this ;
			}
		}
	}

	@Override
	public synchronized void removeChildren(ITreeNode... aChildren)
	{
		if(aChildren == null || aChildren.length == 0)
			return ;
		if(mChildren != null && mChildren.size()>0)
		{
			Iterator<ITreeNode> it = mChildren.iterator() ;
			while(it.hasNext())
			{
				ITreeNode node = it.next() ;
				if(XC.contains(aChildren, node))
				{
					((TreeNode)node).mParent = null ;
					it.remove() ;
				}
			}
		}
	}
	
	@Override
	public void removeChildren(Collection<ITreeNode> aChildren)
	{
		if(aChildren == null || aChildren.size() == 0)
			return ;
		Collection<ITreeNode> children = aChildren ;
		if(aChildren.size()>3 && mChildren.size()>8 && !(aChildren instanceof Set))
		{
			children = new HashSet<>(aChildren) ;
		}
		if(mChildren != null && mChildren.size()>0)
		{
			mChildren.removeAll(children) ;
			for(ITreeNode child : aChildren)
				((TreeNode)child).mParent = null ;
		}
	}
	
	/**
	 * 为了避免结构性修改，引起快速失败
	 */
	@Override
	public synchronized void iterateChildren(IteratorPredicate<ITreeNode> aItPre)
	{
		if(mChildren != null && mChildren.size()>0)
		{
			Iterator<ITreeNode> it = mChildren.iterator() ;
			bp_0726_1741:while(it.hasNext())
			{
				ITreeNode node = it.next() ;
				switch(aItPre.visit(node))
				{
				case 0:
					break ;
				case 1:
					it.remove();
					break ;
				case 2:
					break bp_0726_1741 ;
				}
			}
		}
	}
	
	@Override
	public synchronized boolean removeChild(ITreeNode aChild)
	{
		if(aChild != null && XC.isNotEmpty(mChildren) && mChildren.remove(aChild))
		{
			((TreeNode)aChild).mParent = null ;
			return true ;
		}
		return false ;
	}

	@Override
	public synchronized void removeAllChildren()
	{
		if(XC.isNotEmpty(mChildren))
		{
			for(ITreeNode child : mChildren)
			{
				((TreeNode)child).mParent = null ;
			}
			mChildren.clear() ;
		}
	}
	
	@Override
	public synchronized ITreeNode[] getChildren()
	{
		return mChildren != null?mChildren.toArray(sEmptyArray):null ;
	}
	
	@Override
	public ITreeNode getChildByName(String aName)
	{
		if(mChildren != null)
		{
			for(ITreeNode node : mChildren)
				if(node.getName().equals(aName))
					return node ;
		}
		return null;
	}
	
	@Override
	public int getChildAmount()
	{
		return XC.count(mChildren) ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> T[] getChildren(Class<T> aClazz)
	{
		if(mChildren != null && mChildren.size()>0)
		{
			List<Object> list = new ArrayList<Object>() ;
			for(ITreeNode support : mChildren)
			{
				if(aClazz.isAssignableFrom(support.getClass()))
					list.add(support) ;
			}
			return list.toArray((T[])Array.newInstance(aClazz, list.size())) ;
		}
		return null ;
	}

	@Override
	public ITreeNode getParent()
	{
		return mParent ;
	}

	@Override
	public void depthFirstVisitDescendant(Consumer<ITreeNode> aConsumer)
	{
		ITreeNode[] children = getChildren() ;
		if(children != null)
		{
			int len = children.length ;
			for(int i=0 ; i<len ; i++)
			{
				aConsumer.accept(children[i]);
				children[i].depthFirstVisitDescendant(aConsumer) ;
			}
		}	
	}
	
	@Override
	public Object depthFirstVisit(BiFunction<Object , ITreeNode, Object> aFunc)
	{
		Object r = aFunc.apply(null, this) ;
		depthFirstVisitDescendant(aFunc, r);
		return r ;
	}
	
	protected void depthFirstVisitDescendant(BiFunction<Object , ITreeNode, Object> aFunc , Object aMapObj)
	{
		ITreeNode[] children = getChildren() ;
		if(children != null)
		{
			int len = children.length ;
			for(int i=0 ; i<len ; i++)
			{
				Object r = aFunc.apply(aMapObj , children[i]);
				((TreeNode)children[i]).depthFirstVisitDescendant(aFunc , r) ;
			}
		}
	}
	
	@Override
	public void depthFirstVisitDescendant_2(BiConsumer<Boolean, ITreeNode> aConsumer)
	{
		ITreeNode[] children = getChildren() ;
		if(children != null)
		{
			int len = children.length ;
			for(int i=0 ; i<len ; i++)
			{
				aConsumer.accept(true , children[i]);
				children[i].depthFirstVisitDescendant_2(aConsumer) ;
				aConsumer.accept(false , children[i]);
			}
		}	
	}

	@Override
	public <X extends Exception> void depthFirstVisitDescendantE(EConsumer<ITreeNode , X> aConsumer) throws X
	{
		ITreeNode[] children = getChildren() ;
		if(children != null)
		{
			int len = children.length ;
			for(int i=0 ; i<len ; i++)
			{
				aConsumer.accept(children[i]);
				children[i].depthFirstVisitDescendantE(aConsumer) ;
			}
		}	
	}

}
