package team.sailboat.base.def;

public enum SSLOption
{
	@Deprecated
	USE ,
	
	@Deprecated
	UNUSE ,
	
	/**
	 * 只支持https
	 */
	ONLY ,
	
	/**
	 * https和http都支持
	 */
	ANY ;
}
