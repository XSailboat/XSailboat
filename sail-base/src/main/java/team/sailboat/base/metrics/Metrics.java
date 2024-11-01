package team.sailboat.base.metrics;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Metrics implements Cloneable
{
	/**
	 * 数据来源，数据生产者
	 */
	String source ;
	/**
	 * 数据分类。例如：DataSource、XTask
	 */
	String category ;
	/**
	 * 将作为子表表名
	 */
	String item ;
	/**
	 * 名称
	 */
	String name ;
	/**
	 * 时标
	 */
	Date ts ;
	
	protected Metrics(String aSource
			, String aCategory
			, String aItem
			, String aName)
	{
		source = aSource ;
		category = aCategory ;
		item = aItem ;
		name = aName ;
	}
	
	protected Metrics(String aSource
			, String aCategory
			, String aItem
			, String aName
			, Date aTs)
	{
		this(aSource, aCategory, aItem, aName) ;
		ts = aTs ;
	}
	
	protected void initClone(Metrics aClone)
	{
		aClone.source = source ;
		aClone.category = category ;
		aClone.item = item ;
		aClone.name = name ;
		aClone.ts = ts ;
	}
}
