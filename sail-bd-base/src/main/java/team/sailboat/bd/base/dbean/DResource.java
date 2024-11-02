package team.sailboat.bd.base.dbean;

import java.util.Date;

import team.sailboat.bd.base.model.IResource;
import team.sailboat.commons.fan.dpa.anno.BColumn;
import team.sailboat.commons.fan.dpa.anno.BDataType;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月29日
 */
@team.sailboat.dplug.anno.DBean
public class DResource implements IResource
{
	@BColumn(name = "id" , primary = true , dataType = @BDataType(name="string" , length = 32) , comment = "唯一性标识，自动生成" , seq = 0)
	protected String mId ;
	
	@BColumn(name = "name" , dataType = @BDataType(name="string" , length = 32) , comment = "节点名称" , seq = 1)
	protected String mName ;
	
	@BColumn(name="description" , dataType = @BDataType(name="string" , length = 512) , comment = "描述" , seq = 2)
	protected String mDescription ;
	
	/**
	 * 创建时间
	 */
	@BColumn(name="create_time" , dataType=@BDataType(name="datetime") , comment = "创建时间" , seq = 3)
	protected Date mCreateTime ;
	
	@BColumn(name="create_userid" , dataType=@BDataType(name="string" , length = 32) , comment = "创建者的id" , seq = 4)
	protected String mCreateUserId ;
	
	/**
	 * 上次编辑时间
	 */
	@BColumn(name="last_edit_time" , dataType=@BDataType(name="datetime") , comment = "最近一次修改时间" , seq = 5)
	protected Date mLastEditTime ;
	
	@BColumn(name="last_edit_userid" , dataType=@BDataType(name="string" , length = 32) , comment = "最近一次修改的用户id" , seq = 6)
	protected String mLastEditUserId ;
	
//	@Override
//	public JSONObject setTo(JSONObject aJSONObj)
//	{
//		return aJSONObj.put("id" , mId)
//				.put("name" , mName)
//				.put("description", mDescription)
//				.put("createTime" , XTime.format$yyyyMMddHHmmssSSS(mCreateTime , null))
//				.put(("createUserId") , mCreateUserId)
//				.put("lastEditTime" , XTime.format$yyyyMMddHHmmssSSS(mLastEditTime , null))
//				.put("lastEditUserId" , mLastEditUserId)
//				;
//	}
}
