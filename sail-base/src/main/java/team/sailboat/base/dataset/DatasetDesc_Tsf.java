package team.sailboat.base.dataset;

import io.swagger.v3.oas.annotations.media.Schema;
import team.sailboat.commons.fan.json.JSONObject;

/**
 * 数据源描述信息		<br />
 * 基于变换
 *
 * @author yyl
 * @since 2021年12月14日
 */
@Schema(name="DatasetDesc_Tsf" , description="基于已有数据集进行变换得到的数据集描述")
public class DatasetDesc_Tsf extends DatasetDescriptor
{
	/**
	 * 上游数据源id
	 */
	String mUpperDatasetId ;
	
	/**
	 * 变换方法
	 */
	String mTransformMethod ;
	
	public DatasetDesc_Tsf()
	{
		super(DatasetSource.Transform);
	}
	
	@Override
	public DatasetDescriptor clone()
	{
		return initClone(new DatasetDesc_Tsf()) ;
	}
	
	@Override
	protected DatasetDescriptor initClone(DatasetDescriptor aClone)
	{
		DatasetDesc_Tsf clone = (DatasetDesc_Tsf) super.initClone(aClone);
		
		return clone ;
	}
	
	public static DatasetDesc_Tsf build(JSONObject aJo)
	{
		return null ;
	}
}
