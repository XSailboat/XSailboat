package team.sailboat.commons.fan.json;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class JSONException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Throwable cause;

	String sourceStr;;
	
	public JSONException(String message)
	{
		super(message);
	}

	public JSONException(Throwable t)
	{
		super(t.getMessage());
		this.cause = t;
	}

	public Throwable getCause()
	{
		return this.cause;
	}

	@Override
	public String getMessage()
	{
		if (sourceStr != null)
			return super.getMessage() + " 字符串：" + sourceStr;
		else
			return super.getMessage();
	}

	public void setSourceStr(String aSourceStr)
	{
		sourceStr = aSourceStr;
	}
}
