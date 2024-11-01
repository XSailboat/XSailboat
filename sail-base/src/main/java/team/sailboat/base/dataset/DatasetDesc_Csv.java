package team.sailboat.base.dataset;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.json.JSONObject;

/**
 * 数据源描述信息
 *
 * @author yyl
 * @since 2021年12月14日
 */
@Schema(name = "DatasetDesc_Csv" , description="基于Csv文件的数据集描述")
public class DatasetDesc_Csv extends DatasetDescriptor
{

	public DatasetDesc_Csv()
	{
		super(DatasetSource.Csv) ;
	}
	
	@Override
	public DatasetDescriptor clone()
	{
		return initClone(new DatasetDesc_Csv()) ;
	}
	
	@Override
	protected DatasetDescriptor initClone(DatasetDescriptor aClone)
	{
		DatasetDesc_Csv clone = (DatasetDesc_Csv) super.initClone(aClone);
		
		return clone ;
	}
	
	public static DatasetDesc_Csv build(JSONObject aJo)
	{
		return null ;
	}
}
