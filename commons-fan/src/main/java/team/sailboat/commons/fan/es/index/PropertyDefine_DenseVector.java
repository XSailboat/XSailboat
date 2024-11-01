package team.sailboat.commons.fan.es.index;

import team.sailboat.commons.fan.json.JSONObject;

public class PropertyDefine_DenseVector extends PropertyDefine
{
	/**
	 * 欧式距离
	 */
	public static final String sSimilarity_l2_norm = "l2_norm" ;
	/**
	 * 点积
	 */
	public static final String sSimilarity_dot_product = "dot_product" ; 
	
	/**
	 * 余弦
	 */
	public static final String sSimilarity_cosine = "cosine" ;
	
	/**
	 * 向量最大内积
	 */
	public static final String sSimilarity_max_inner_product = "max_inner_product" ;
	
	PropertyDefine_DenseVector(MappingsDefine aUp , JSONObject aPropertyDefine)
	{
		super(aUp , aPropertyDefine) ;
	}
	
	public PropertyDefine_DenseVector dims(int aDims)
	{
		mPropertyDefine.put("dims" , aDims) ;
		return this ;
	}
	
	public PropertyDefine_DenseVector similarity(String aSimilarity)
	{
		mPropertyDefine.put("similarity" , aSimilarity) ;
		return this ;
	}
	
	public PropertyDefine_DenseVector indexOptions(String aType)
	{
		mPropertyDefine.put("index_options" , new JSONObject().put("type", aType)) ;
		return this ;
	}
	
}
