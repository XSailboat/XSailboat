package team.sailboat.base.ds;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.base.def.DataSourceType;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.res.ResHelper;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.text.XString;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME, // Were binding by providing a name
	    include = JsonTypeInfo.As.PROPERTY, // The name is provided in a property
	    property = "type", // Property name is type
	    visible = true // Retain the value of type after deserialisation
	)
	@JsonSubTypes({//Below, we define the names and the binding classes.
	    @JsonSubTypes.Type(value = ConnInfo_RDB.class, name = "ConnInfo_RDB") ,
	    @JsonSubTypes.Type(value = ConnInfo_Hive.class, name = "ConnInfo_Hive") ,
	    @JsonSubTypes.Type(value = ConnInfo_SFTP.class, name = "ConnInfo_SFTP") ,
	    @JsonSubTypes.Type(value = ConnInfo_FTP.class, name = "ConnInfo_FTP") ,
	    @JsonSubTypes.Type(value = ConnInfo_Kafka.class, name = "ConnInfo_Kafka") ,
	    @JsonSubTypes.Type(value = ConnInfo_Http.class, name = "ConnInfo_Http") ,
	    @JsonSubTypes.Type(value = ConnInfo_TDengine.class, name = "ConnInfo_TDengine")
	})
@Schema(name = "ConnInfo" , description="数据源连接信息，这是一个抽象基类，注意根据数据库类型选择" 
	, subTypes = {ConnInfo_RDB.class , ConnInfo_Hive.class , ConnInfo_SFTP.class , ConnInfo_SFTP.class
			, ConnInfo_Kafka.class , ConnInfo_Http.class , ConnInfo_TDengine.class})
public abstract class ConnInfo implements ToJSONObject , Cloneable , AutoCloseable
{	

	boolean mManaged = false  ;
	
	String type ;
	
	String mDescription ;
	
	Object mResource ;
	
	protected ConnInfo(String aType)
	{
		type = aType ;
	}
	
	@Schema(hidden = true)
	@JsonIgnore
	public boolean isManaged()
	{
		return mManaged;
	}
	public void setManaged(boolean aManaged)
	{
		mManaged = aManaged;
		if(!mManaged)
			closeResource();
	}
	
	@Schema(description = "连接信息类型" , allowableValues = {"ConnInfo_RDB","ConnInfo_Hive","ConnInfo_SFTP","ConnInfo_FTP"})
	public String getType()
	{
		return type;
	}
	public void setType(String aType)
	{
		type = aType;
	}
	
	@Schema(description = "此连接的描述信息")
	public String getDescription()
	{
		return mDescription ;
	}
	public void setDescription(String aDescription)
	{
		mDescription = aDescription ;
	}
	
	public abstract void checkForCreate() ;
	
	/**
	 * 如果发生变化，返回true，否则返回false
	 * @param aConnInfo
	 * @return
	 */
	public final boolean copyFrom(ConnInfo aConnInfo)
	{
		return update(aConnInfo, false) ;
	}
	
	public final boolean particalUpdate(ConnInfo aConnInfo)
	{
		return update(aConnInfo, true) ;
	}
	
	protected boolean update(ConnInfo aConnInfo , boolean aPartially)
	{
		boolean changed = false ;
		if((!aPartially || XString.isNotEmpty(aConnInfo.mDescription))
				&& JCommon.unequals(mDescription, aConnInfo.mDescription))
		{
			mDescription = aConnInfo.mDescription ;
			changed = true ;
		}
		return changed ;
	}
	
	public abstract ConnInfo clone() ;
	
	
	@Override
	public boolean equals(Object aObj)
	{
		if(aObj == this)
			return true ;
		if(aObj == null || !getClass().equals(aObj.getClass()))
			return false ;
		ConnInfo other = (ConnInfo)aObj ;
		return JCommon.equals(other.mDescription , mDescription) ;
	}
	
	public static ConnInfo parse(DataSourceType aDSType , String aConnInfo)
	{
		if(aDSType.isRDB())
		{
			if(aDSType == DataSourceType.Hive)
				return ConnInfo_Hive.parse(aConnInfo) ;
			else
				return ConnInfo_RDB.parse(aConnInfo) ;
		}
		else if(aDSType == DataSourceType.SFTP)
			return ConnInfo_SFTP.parse(aConnInfo) ;
		else if(aDSType == DataSourceType.FTP)
			return ConnInfo_FTP.parse(aConnInfo) ;
		else if(aDSType == DataSourceType.Kafka)
			return ConnInfo_Kafka.parse(aConnInfo) ;
		else if(aDSType == DataSourceType.TDengine)
			return ConnInfo_TDengine.parse(aConnInfo) ;
		else
			throw new IllegalStateException("未支持的数据源类型："+aDSType.name()) ;
	}
	
	/**
	 * 如果资源关闭，将返回Null
	 * @return
	 */
	@JsonIgnore
	@Schema(hidden = true)
	public synchronized Object getResource()
	{
		if(mResource != null && ResHelper.isClosed(mResource , false))
		{
			mResource = null ;
		}
		return mResource;
	}
	public synchronized void setResource(Object aResource)
	{
		if(mManaged)
		{
			mResource = aResource;
		}
		else
		{
			StreamAssist.close(mResource) ;
			throw new IllegalStateException("ConnInfo不在管理范围，不能setResource！") ;
		}
	}
	
	@Override
	public synchronized void close()
	{
		closeResource();
		mManaged = false ;
	}
	
	protected synchronized void closeResource()
	{
		if(mResource != null)
		{
			StreamAssist.close(mResource) ;
			mResource = null ;
		}
	}
	
	public abstract String getConnURI(DataSourceType aType) ;
	
	@JsonIgnore
	@Schema(hidden = true)
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("type" , type)
				.put("description", mDescription) ;
	}
}
