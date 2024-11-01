package team.sailboat.base.ds;

import java.util.Date;

import team.sailboat.base.def.DataSourceType;
import team.sailboat.base.def.WorkEnv;

public interface IDataSource
{
	String getId() ;

	String getName() ;
	
	DataSourceType getType() ;
	
	boolean setType(DataSourceType aDSType) ;

	String getDescription(WorkEnv aEnv) ;
	
	ConnInfo getConnInfo(WorkEnv aEnv) ;
	
	ConnInfo cloneConnInfo(WorkEnv aEnv) ;
	
	boolean setProdConnInfo(ConnInfo aConnInfo) ;
	
	boolean setDevConnInfo(ConnInfo aConnInfo) ;
	
	Date getCreateTime() ;
	void setCreateTime(Date aTime) ;
}
