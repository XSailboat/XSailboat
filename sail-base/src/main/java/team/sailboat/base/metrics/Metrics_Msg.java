package team.sailboat.base.metrics;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Metrics_Msg extends Metrics
{
	String value ;
	
	
	/**
	 * 
	 * @param aSource
	 * @param aCategory		数据分类。例如：DataSource、XTask
	 * @param aItem
	 * @param aName
	 * @param aValue
	 * @return
	 */
	public static Metrics_Msg ofNow(String aSource
			, String aCategory
			, String aItem
			, String aName
			, String aValue)
	{
		Metrics_Msg msg = new Metrics_Msg() ;
		msg.setTs(new Date()) ;
		msg.setSource(aSource) ;
		msg.setCategory(aCategory);
		msg.setItem(aItem) ;
		msg.setName(aName); ;
		msg.setValue(aValue) ;
		return msg ;
	}
}
