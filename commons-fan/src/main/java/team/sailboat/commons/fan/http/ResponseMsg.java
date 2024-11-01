package team.sailboat.commons.fan.http;

public class ResponseMsg<T>
{
	T mContent ;
	
	int mStatus ;
	
	public ResponseMsg()
	{
		
	}
	
	public ResponseMsg(int aStatus , T aContent)
	{
		mStatus = aStatus ;
		mContent = aContent ;
	}
	
	public T getContent()
	{
		return mContent;
	}
	public void setContent(T aContent)
	{
		mContent = aContent;
	}
	
	public int getStatus()
	{
		return mStatus;
	}
	public void setStatus(int aStatus)
	{
		mStatus = aStatus;
	}
	
	public static <T> ResponseMsg<T> ofNormal(T aContent)
	{
		return new ResponseMsg<>(200 , aContent) ;
	}
}
