package team.sailboat.base.dataset;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.json.JSONObject;

/**
 * 数据源描述信息
 *
 * @author yyl
 * @since 2021年12月14日
 */
@Schema(name="DatasetDesc_Api" , description="基于Api的数据集描述")
public class DatasetDesc_Api extends DatasetDescriptor
{

	String mApiId ;
	
	public DatasetDesc_Api()
	{
		super(DatasetSource.Api);
	}
	
	@Schema(description = "Api的Id")
	public String getApiId()
	{
		return mApiId;
	}
	public void setApiId(String aApiId)
	{
		mApiId = aApiId;
	}
	
	@Override
	public DatasetDescriptor clone()
	{
		return initClone(new DatasetDesc_Api());
	}
	
	@Override
	protected DatasetDescriptor initClone(DatasetDescriptor aClone)
	{
		DatasetDesc_Api clone = (DatasetDesc_Api) super.initClone(aClone);
		clone.mApiId = mApiId ;
		return clone ;
	}
	
	public static DatasetDesc_Api build(JSONObject aJo)
	{
		return null ;
	}
}
