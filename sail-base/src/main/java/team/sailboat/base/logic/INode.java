package team.sailboat.base.logic;

import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.jfilter.JFilter;
import team.sailboat.commons.fan.jfilter.JFilterBuilder;
import team.sailboat.commons.fan.jfilter.JFilterNodeBuilder;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.XClassUtil;

public interface INode<T extends INode<T>>
{
	@Schema(hidden = true)
	@JsonIgnore
	ILogicJoints<T> getParent() ;
	void setParent(ILogicJoints<T> aParent) ;
	
	public static JSONObject buildJFilter(INode<?> aNode)
	{
		if(aNode == null)
			return null ;
		JFilterBuilder nodeBld = JFilter.builder() ;
		buildJFilter(aNode , nodeBld) ;
		return nodeBld.build() ;
	}
	
	static void buildJFilter(INode<?> aNode , JFilterNodeBuilder aNodeBld)
	{
		JFilterNodeBuilder nodeBld = aNodeBld ;
		if(aNode instanceof LogicJoints)
		{
			switch(((LogicJoints)aNode).getWord())
			{
			case AND:
				nodeBld = nodeBld.must() ;
				break  ;
			case OR:
				nodeBld = nodeBld.should() ;
				break ;
			default:
				throw new IllegalStateException("") ;
			}
			List<Node> items = ((LogicJoints)aNode).getItems() ;
			if(XC.isNotEmpty(items))
			{
				for(INode<?> item : items)
				{
					buildJFilter(item, nodeBld) ;
				}
			}
		}
		else
		{
			Condition cnd = (Condition)aNode ;
			switch(cnd.getOperator())
			{
			case EQUALS:
				nodeBld.term(cnd.getFieldName() ,((EQUALS)cnd).getValue()) ;
				break ;
			case NOT_EQUALS:
				nodeBld.must_not().term(cnd.getFieldName() ,((NOT_EQUALS)cnd).getValue()) ;
				break ;
			case IS_NULL:
				nodeBld.isNull(cnd.getFieldName()) ;
				break ;
			case NOT_NULL:
				nodeBld.must_not().isNull(cnd.getFieldName()) ;
				break ;
			case CONTAINS:
				nodeBld.contains(cnd.getFieldName(), XClassUtil.toString(((CONTAINS)cnd).getValue())) ;
				break ;
			case NOT_CONTAINS:
				nodeBld.must_not().contains(cnd.getFieldName(), XClassUtil.toString(((NOT_CONTAINS)cnd).getValue())) ;
				break ;
			case EMPTY:
				nodeBld.expr(cnd.getFieldName(), "($$ == nil || string.length($$) == 0)") ;
				break ;
			case NOT_EMPTY:
				nodeBld.expr(cnd.getFieldName(), "($$ != nil && string.length($$) > 0)") ;
				break ;
			case IN:
				nodeBld.in(cnd.getFieldName() , ((IN)cnd).getCollection()) ;
				break ;
			case NOT_IN:
				nodeBld.must_not().in(cnd.getFieldName() , ((NOT_IN)cnd).getCollection()) ;
				break ;
			case STARTS_WITH:
				nodeBld.startsWith(cnd.getFieldName() , XClassUtil.toString(((STARTS_WITH)cnd).getValue())) ;
				break ;
			case NOT_STARTS_WITH:
				nodeBld.must_not().startsWith(cnd.getFieldName() , XClassUtil.toString(((NOT_STARTS_WITH)cnd).getValue())) ;
				break ;
			case ENDS_WITH:
				nodeBld.endsWith(cnd.getFieldName() , XClassUtil.toString(((ENDS_WITH)cnd).getValue())) ;
				break ;
			case NOT_ENDS_WITH:
				nodeBld.must_not().endsWith(cnd.getFieldName() , XClassUtil.toString(((NOT_ENDS_WITH)cnd).getValue())) ;
				break ;
			case IN_RANGE:
				nodeBld.range(cnd.getFieldName(), ((IN_RANGE)cnd).getMin() , ((IN_RANGE)cnd).isMinEquals() 
						, ((IN_RANGE)cnd).getMax() , ((IN_RANGE)cnd).isMaxEquals());
				break ;
				
 			default:
				throw new IllegalStateException("") ;
			}
		}
	}
	
	public static <T extends INode<T>> void deepthFirstVisit(INode<T> aNode , Predicate<INode<T>> aVisitor)
	{
		deepthFirstVisit_0(aNode, aVisitor) ;
	}
	
	@SuppressWarnings("rawtypes")
	static <T extends INode<T>> boolean deepthFirstVisit_0(INode<T> aNode , Predicate<INode<T>> aVisitor)
	{
		if(aNode != null)
		{
			if(!aVisitor.test(aNode))
				return false ; 
						
			
			if(aNode instanceof ILogicJoints)
			{
				@SuppressWarnings("unchecked")
				List<INode<T>> nodeList = ((ILogicJoints)aNode).getItems() ;
				if(XC.isNotEmpty(nodeList))
				{
					for(INode<T> node : nodeList)
					{
						if(!deepthFirstVisit_0(node, aVisitor))
							return false ;
					}
				}
			}
		}
		return true ;
	}
}
