package team.sailboat.base.dataset;

import team.sailboat.base.def.DataSourceType;
import team.sailboat.base.def.WorkEnv;

public interface IDataset
{
	
	String getName() ;
	
	DataSourceType getDataSourceType() ;
	boolean setDataSourceType(DataSourceType aDataSourceType) ;
	
	String getDataSourceId() ;
	boolean setDataSourceId(String aDataSourceId) ;
	
	DatasetDescriptor getDatasetDescriptor() ;
	boolean setDatasetDescriptor(DatasetDescriptor aDatasetDescriptor) ;
	
	WorkEnv getWorkEnv() ;
}
