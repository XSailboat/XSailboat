package team.sailboat.commons.fan.tree;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.infc.IteratorPredicate;

public interface ITreeNode
{	
	public static final ITreeNode[] sEmptyArray = new ITreeNode[0] ;
	
	
	String getName() ;
	
	void setName(String aName) ;
	
	boolean hasChildren() ;
	
	boolean isLeaf() ;
	
	void addChild(int aIndex , ITreeNode aChild) ;
	
	void addChild(ITreeNode aChild) ;
	
	void addChildren(ITreeNode...aChildren) ;
	
	void removeChildren(ITreeNode...aChildren) ;
	
	void removeChildren(Collection<ITreeNode> aChildren) ;
	
	boolean removeChild(ITreeNode aChild) ;
	
	void iterateChildren(IteratorPredicate<ITreeNode> aItPre) ;
	
	void removeAllChildren() ;
	
	ITreeNode[] getChildren() ;
	
	ITreeNode getChildByName(String aName) ;
	
	int getChildAmount() ;
	
	<T> T[] getChildren(Class<T> aClazz) ;
	
	ITreeNode getParent() ;
	
	/**
	 * 深度优先遍历子孙节点（不包括自身）
	 * @param aVisitor
	 */
	void depthFirstVisitDescendant(Consumer<ITreeNode> aConsumer) ;
	
	/**
	 * 深度优先遍历子孙节点（包括自身）
	 * @param aFunc
	 */
	Object depthFirstVisit(BiFunction<Object , ITreeNode , Object> aFunc) ;
	
	/**
	 * 深度优先，双向遍历。即正向深入一次，反向退出一次		<br>
	 * 叶子节点也会被两次访问到
	 * @param aConsumer		正向深入时，第一个Boolean值为true，反向退出时，boolean值为false
	 */
	void depthFirstVisitDescendant_2(BiConsumer<Boolean , ITreeNode> aConsumer) ;
	
	<X extends Exception>void depthFirstVisitDescendantE(EConsumer<ITreeNode , X> aConsumer) throws X ;
}
