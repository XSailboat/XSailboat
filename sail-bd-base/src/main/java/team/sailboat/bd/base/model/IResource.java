package team.sailboat.bd.base.model;

import java.util.Date;

public interface IResource
{
	String getId() ;
	
	String getName() ;
	boolean setName(String aName) ;
	
	String getDescription() ;
	boolean setDescription(String aDescription) ;
	
	Date getCreateTime() ;
	boolean setCreateTime(Date aCreateTime) ;
	
	String getCreateUserId() ;
	boolean setCreateUserId(String aUserId) ;
	
	Date getLastEditTime() ;
	boolean setLastEditTime(Date aLastEditTime) ;
	
	String getLastEditUserId() ;
	boolean setLastEditUserId(String aUserId) ;
}
