package team.sailboat.bd.base.infc;

import java.util.List;

import org.apache.hadoop.fs.Path;

import team.sailboat.bd.base.ZBDException;

public interface IFunctionResProvider
{
	Path getJarsPath() ;
	
	long getJarResourcesVersion() ;
	
	void setJarResourcesVersion(long aTime) throws ZBDException ;
	
	long getFunctionsVersion() ;
	
	void setFunctionsVersion(long aTime) throws ZBDException ;
	
	List<String> getJarResourcesUrlPaths() throws ZBDException ;
}
