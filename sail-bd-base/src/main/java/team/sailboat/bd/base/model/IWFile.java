package team.sailboat.bd.base.model;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.text.XString;

public interface IWFile extends ToJSONObject
{
	String getId() ;
	
	/**
	 * 发生变化，将返回true
	 * @param aName
	 * @return
	 */
	boolean setName(String aName) ;
	String getName() ;
	
	String getDescription() ;
	boolean setDescription(String aDescription) ;
	
	boolean isDirectory() ;
	
	boolean isFile() ;
	
	boolean setCreateTime(Date aTime) ;
	Date getCreateTime() ;
	
	Date getLastEditTime() ;
	boolean setLastEditTime(Date aTime) ;
	
	String getLastEditUserId() ;
	boolean setLastEditUserId(String aUserId) ;
	/**
	 * 取值：f或d
	 */
	boolean setType(String aType) ;
	String getType() ;

	boolean setExtType(String aExtType) ;
	String getExtType() ;
	
	String getCreateUserId() ;
	boolean setCreateUserId(String aUserId) ;
	
	boolean setParentId(String aParentId) ;
	String getParentId() ;
	
	boolean setFilePath(String aPath) ;
	String getFilePath() ;
	
	String getExtAttributes() ;
	boolean setExtAttributes(String aExtAttributes) ;
	
//	String getPathId() ;
//	boolean setPathId(String aPathId) ;
	
	
	public static void setHeaders(IWFile aFile , HttpServletResponse aResp)
	{
		long version = 0 ;
		Date lastEditTime = aFile.getLastEditTime() ;
		if(lastEditTime != null)
		{
			version = lastEditTime.getTime() ;
		}
		aResp.setHeader("contentVersion", Long.toString(version));
		String extAttrsStr = aFile.getExtAttributes() ;
		boolean canCommit = false ;
		if(XString.isEmpty(extAttrsStr))
		{
			canCommit = true ;
		}
		else
		{
			JSONObject jo = new JSONObject(extAttrsStr) ;
			long committedVersion = jo.optLong("committedVersion" , 0L) ;
			canCommit = version > committedVersion ;
		}
		aResp.setHeader("canCommit", Boolean.toString(canCommit));
	}
}
