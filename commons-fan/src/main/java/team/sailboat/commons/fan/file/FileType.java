package team.sailboat.commons.fan.file;

public enum FileType
{
	RegFile("常规文件"),
	Directory("目录") ;
	
	String mDisplayName ;
	
	private FileType(String aDisplayName)
	{
		mDisplayName = aDisplayName ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
