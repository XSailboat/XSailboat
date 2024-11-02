package team.sailboat.bd.base.model;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.bd.base.infc.IWorkspace;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.lang.JCommon;

@Schema(description = "工作空间")
@Data
public class Workspace implements IWorkspace
{
	@Schema(description = "工作空间id")
	String id ;
	
	@Schema(description = "工作空间名称，英文名，只能是大小写字母、数字、下划线（_），不能以数字和下划线开头")
	String name ;
	
	@Schema(description = "显示名，可以是中文字符、字母、数字、下划线")
	String displayName ;
	
	@Schema(description = "工作空间描述")
	String description ;
	
	@Schema(description = "工作空间创建时间，格式：yyyy-MM-dd HH:mm:ss.SSS")
	String createTime ;
	
	@Schema(description = "创建者的id")
	String createUserId ;
	
	@Schema(description = "工作空间是否已经关闭")
	boolean closed ;
	
	@Schema(description = "性质")
	PropertiesEx conf ;
	
	@JsonProperty("conf")
	public Map<String, String> getConfMap()
	{
		return conf==null?Collections.emptyMap():conf.toStringMap() ;
	}
	public void setConfMap(Map<String, String> aConf)
	{
		if(aConf == null)
			conf = null ;
		else
			conf = new PropertiesEx(aConf) ;
	}
	
	@JsonIgnore
	@Schema(hidden = true)
	@Override
	public PropertiesEx getConf()
	{
		return conf ;
	}
	@Override
	public boolean setConf(PropertiesEx aConf)
	{
		if(conf == null)
		{
			if(aConf != null)
			{
				conf = new PropertiesEx(aConf) ;
				return true ;
			}
			else
				return false ;
		}
		else if(aConf == null)
		{
			if(conf.isEmpty())
				return false ;
			else
			{
				conf.clear();
				return true ;
			}
		}
		else
		{
			boolean changed = false ;
			for(Entry<Object , Object> entry : aConf.entrySet())
			{
				Object oldValue = conf.put(entry.getKey(), entry.getValue()) ;
				if(!changed && JCommon.unequals(oldValue, entry.getValue()))
					changed = true ;
			}
			for(Object key : conf.keySet())
			{
				if(!aConf.containsKey(key))
				{
					conf.remove(key) ;
					changed = true ;
				}
			}
			return changed ;
		}
	}
}
