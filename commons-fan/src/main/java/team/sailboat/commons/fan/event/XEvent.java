package team.sailboat.commons.fan.event;

public class XEvent
{
	/**
	 * 是否已经有客户已经处理过此事件了.
	 * 事件仍然保持传递，是否再次处理由客户决定
	 */
	public boolean mDid ;
	//描述
	private String mDescription ;
	//源
	private Object mSource ;
	//标记，自定义，自解析
	private int mTag ;
	
	public XEvent(Object aSource , int aTag)
	{
		mSource = aSource ;
		mTag = aTag ;
	}
	
	public XEvent(Object aSource , int aTag , String aDescription)
	{
		this(aSource , aTag) ;
		mDescription = aDescription ;
	}

	public String getDescription()
	{
		return mDescription;
	}

	public void setDescription(String aDescription)
	{
		mDescription = aDescription;
	}

	@SuppressWarnings("unchecked")
	public <T> T getSource()
	{
		return (T)mSource;
	}

	public void setSource(Object aSource)
	{
		mSource = aSource;
	}

	public int getTag()
	{
		return mTag;
	}

	public void setTag(int aTag)
	{
		mTag = aTag;
	}
}
