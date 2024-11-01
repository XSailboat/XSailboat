package team.sailboat.base.logic;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.collection.XC;

@Schema(name="LogicJoints" , description="逻辑连接词")
public class LogicJoints extends Node implements ILogicJoints<Node>
{
	LogicWord mWord ;
	
	List<Node> mItems ;
	
	public LogicJoints()
	{
		super() ;
	}
	
	public LogicJoints(LogicWord aWord)
	{
		this() ;
		mWord = aWord ;
	}
	
	@Schema(description = "逻辑词")
	public LogicWord getWord()
	{
		return mWord;
	}
	public void setWord(LogicWord aWord)
	{
		mWord = aWord;
	}
	
	@Schema(description = "应用逻辑词的条目")
	public List<Node> getItems()
	{
		return mItems;
	}
	public void setItems(List<Node> aItems)
	{
		setParentOfItems(mItems , null);
		mItems = aItems;
		setParentOfItems(mItems , this) ;
	}
	
	@Override
	public void removeItem(Node aItem)
	{
		if(mItems != null)
		{
			mItems.remove(aItem) ;
			if(aItem.getParent() == this)
				aItem.setParent(null) ;
		}
	}
	
	private void setParentOfItems(List<Node> aItems , LogicJoints aParent)
	{
		if(XC.isNotEmpty(aItems))
		{
			for(Node node : aItems)
			{
				node.setParent(aParent) ;
			}
		}
	}
	
	public static LogicJoints ofAnd()
	{
		return new LogicJoints(LogicWord.AND) ;
	}
	
	public static LogicJoints ofOr()
	{
		return new LogicJoints(LogicWord.OR) ;
	}
	
}
