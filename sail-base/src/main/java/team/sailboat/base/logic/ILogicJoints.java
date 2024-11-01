package team.sailboat.base.logic;

import java.util.List;

public interface ILogicJoints<T extends INode<T>> extends INode<T>
{
	
	List<T> getItems() ;
	
	void setItems(List<T> aItems) ;
	
	void removeItem(T aItem) ;
}
