package team.sailboat.base.metrics;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Metrics_Value extends Metrics
{
	double value ;
	
	/**
	 * 
	 * @param aSource		应用名
	 * @param aCategory		分类
	 * @param aItem			指标项/名，英文名，复合建表规范
	 * @param aName			指标显示名,中文易读形式
	 * @param aTs			时标
	 * @param aValue		值
	 */
	public Metrics_Value(String aSource
			, String aCategory
			, String aItem
			, String aName
			, Date aTs
			, Double aValue)
	{
		super(aSource, aCategory, aItem, aName , aTs) ;
		value = aValue ;
	}
	
	@Override
	public Metrics_Value clone()
	{
		Metrics_Value clone = new Metrics_Value() ;
		initClone(clone) ;
		clone.value = value ;
		return clone ;
	}
}
