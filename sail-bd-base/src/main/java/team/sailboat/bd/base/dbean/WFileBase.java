package team.sailboat.bd.base.dbean;

import java.util.Date;

import team.sailboat.bd.base.model.IWFile;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;
import team.sailboat.commons.fan.dpa.anno.BLazy;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.fan.time.XTime;

@team.sailboat.dplug.anno.DBean
public class WFileBase implements IWFile
{
	
	@BColumn(name = "id" , primary = true , dataType = @BDataType(name="string" , length = 32) , comment = "唯一性标识，自动生成" , seq = 0)
	String mId ;
	
	@BColumn(name = "name" , dataType = @BDataType(name="string" , length = 32) , comment = "名称" , seq = 1)
	String mName ;
	
	@BColumn(name = "parentId" , dataType = @BDataType(name="string" , length = 32) , comment = "父节点id" , seq = 2)
	String mParentId ;
	
	@BColumn(name = "description" , dataType = @BDataType(name="string" , length = 256) , comment = "描述" , seq = 3)
	String mDescription ;
	
	@BColumn(name = "type" , dataType = @BDataType(name="string" , length = 16) , comment = "文件类型,d表示目录，f表示常规文件" , seq = 4)
	String mType ;
	
	@BColumn(name = "extType" , dataType = @BDataType(name="string" , length = 32) , comment = "文件内容的类型,sql,txt,py等" , seq = 5)
	String mExtType ;
	
	@BColumn(name = "createUserId" , dataType = @BDataType(name="string" , length = 32) , comment = "创建者的用户id" , seq = 6)
	String mCreateUserId ;
	
	@BColumn(name = "filePath" , dataType = @BDataType(name="string" , length = 256) , comment = "hdfs上相对于约定目录的文件路径,不以“/”开头" , seq = 7)
	String mFilePath ;
	
	@BLazy
	@BColumn(name = "extAttributes" , dataType = @BDataType(name="string" , length = 5120) , comment = "扩展信息，JSONObject格式" , seq = 8)
	protected String mExtAttributes ;
	
	@BColumn(name="createTime" , dataType = @BDataType(name="datetime") , comment = "创建时间" , seq = 9)
	Date mCreateTime ;
	
	@BColumn(name="lastEditTime" , dataType = @BDataType(name="datetime") , comment = "最近一次修改时间" , seq = 10)
	Date mLastEditTime ;
	
	@BColumn(name = "lastEditUserId" , dataType = @BDataType(name="string" , length = 32) , comment = "最近一次更新的用户id" , seq = 11)
	String mLastEditUserId ;
	
//	@BColumn(name="pathId" , dataType = @BDataType(name="string" , length = 512) , comment = "路径id" , seq = 12)
//	String mPathId ;
	
	public WFileBase()
	{
		super() ;
	}
	
	@Override
	public boolean isDirectory()
	{
		return "d".equals(mType) ;
	}
	
	@Override
	public boolean isFile()
	{
		return "f".equals(mType) ;
	}
	
	@Override
	public boolean setType(String aType)
	{
		switch(aType)
		{
		case "d":
		case "f":
			if(!aType.equals(mType))
			{
				String oldVal = mType ;
				mType = aType ;
				setChanged("mType", mType, oldVal);
				return true ;
			}
			break ;
		default:
			throw new IllegalArgumentException("不合法的type："+aType) ;
		}
		return false ;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("id", mId)
				.put("name", mName)
				.put("parentId", mParentId)
				.put("description", mDescription)
				.put("type", mType)
				.put("extType" , mExtType)
				.put("createUserId", mCreateUserId)
				.put("createTime", XTime.format$yyyyMMddHHmmssSSS(mCreateTime , null))
				.put("lastEditUserId" , mLastEditUserId)
				.put("lastEditTime" , XTime.format$yyyyMMddHHmmssSSS(mLastEditTime, null))
				.put("extAttributes", XString.isNotEmpty(mExtAttributes)?new JSONObject(mExtAttributes):null) ;
				
	}
}
