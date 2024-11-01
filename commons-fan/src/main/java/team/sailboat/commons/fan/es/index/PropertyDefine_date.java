package team.sailboat.commons.fan.es.index;

import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;

public class PropertyDefine_date extends PropertyDefine
{
	public static final String sFmt_yyyyMMddHHmmssSSS = "yyyy-MM-dd HH:mm:ss.SSS" ;
	public static final String sFmt_yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss" ;
	public static final String sFmt_yyyyMMdd = "yyyy-MM-dd" ;
	public static final String sFmt_epoch_millis = "epoch_millis" ;
	
	public static final String sFmts_cn_common = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis||yyyy-MM-dd HH:mm:ss.SSS" ;
	
	PropertyDefine_date(MappingsDefine aUp , JSONObject aPropertyDefine)
	{
		super(aUp , aPropertyDefine) ;
	}
	
	public PropertyDefine_date format(String aFormats)
	{
		mPropertyDefine.put("format" , aFormats) ;
		return this ;
	}
	
	public PropertyDefine_date appendFormat(String aFormat)
	{
		String formats = mPropertyDefine.optString("format") ;
		if(XString.isEmpty(formats))
			return format(aFormat) ;
		else
		{
			mPropertyDefine.put("format" , formats + "||"+aFormat) ;
			return this ;
		}
	}
	
	
}
