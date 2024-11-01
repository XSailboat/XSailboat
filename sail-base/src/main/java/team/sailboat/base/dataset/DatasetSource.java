package team.sailboat.base.dataset;

public enum DatasetSource
{
	Api("API接口") ,
	Sql("SQL查询") , 
	Csv("CSV文件") ,
	Transform("数据集变换")
	;
	
	String mDisplayName ;
	
	private DatasetSource(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
