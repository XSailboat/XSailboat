package team.sailboat.commons.ms.bean;

import java.util.Collection;

/**
 * 
 *
 * @author yyl
 * @since 2024年11月27日
 */
public interface ITreeNodeBean
{
	String getId() ;
	void setId(String aId) ;
	
	String getParentId() ;
	void setParentId(String aParentId) ;
	
	Collection<String> getChildIds() ;
	void setChildIds(Collection<String> aChildIds) ;
	
}
