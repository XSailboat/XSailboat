package team.sailboat.bd.base.model;

import java.util.LinkedHashSet;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.base.util.JacksonUtils;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

@Schema(description = "内容")
@Data
public class Content
{
	@Schema(description = "内容类型")
	ContentType type ;
	
	@Schema(description = "数据")
	String data ;
	
	@Schema(description = "版本")
	long version ;
	
	@Schema(description = "引用的表")
	Set<String> referenceTableNames ;
	
	@Schema(description = "此内容创建的表")
	Set<String> createTableNames ;
	
	public Content()
	{
	}
	
	public Content(ContentType aType , String aData , long aVersion)
	{
		type = aType ;
		data = aData ;
		version = aVersion ;
	}
	
	public Content clone()
	{
		Content clone = new Content(type, data, version) ;
		if(createTableNames != null)
			clone.createTableNames = new LinkedHashSet<String>(createTableNames) ;
		if(referenceTableNames != null)
			clone.referenceTableNames = new LinkedHashSet<String>(referenceTableNames) ;
		return clone ;
	}
	
	public static Content parse(JSONObject aJObj)
	{
		String type = aJObj.optString("type") ;
		Content content = new Content(ContentType.valueOf(type), aJObj.optString("data")
				, aJObj.optLong("version")) ;
		JSONArray ja = aJObj.optJSONArray("createTableNames") ;
		if(ja != null)
			content.createTableNames = XC.hashSet(ja.toStringArray()) ;
		ja = aJObj.optJSONArray("referenceTableNames") ;
		if(ja != null)
			content.referenceTableNames = XC.hashSet(ja.toStringArray()) ;
		return content ;
	}
	
//	@Override
//	public boolean equals(Object aObj)
//	{
//		if(aObj == null || !(aObj instanceof Content))
//			return false ;
//		Content other = (Content)aObj ;
//		return other.type == type && JCommon.equals(other.type , type)
//				&& other.version == version
//				&& JCommon.equals(other.createTableNames , createTableNames)
//				&& JCommon.equals(other.referenceTableNames , referenceTableNames) ;
//	}
	
	public static class SerDe
	{
		@BForwardMethod
		public static Object forward(Content aSource)
		{
			return JacksonUtils.toString(aSource) ;
		}
		
		@BReverseMethod
		public static Content reverse(Object aSource)
		{
			return aSource == null?null:JacksonUtils.asBean(aSource.toString() , Content.class) ;
		}
	}
	
	public static Content ofHdfs(String aPath , long aVersion)
	{
		return new Content(ContentType.hdfs , aPath , aVersion) ;
	}
	
	public static Content ofHBase(String aRowKey , long aVersion)
	{
		return new Content(ContentType.hbase , aRowKey , aVersion) ;
	}
}
