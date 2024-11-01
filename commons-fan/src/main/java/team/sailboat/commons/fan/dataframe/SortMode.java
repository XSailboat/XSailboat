package team.sailboat.commons.fan.dataframe;

import java.util.Comparator;

import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.ChineseComparator;

/**
 * 排序方法
 *
 * @author yyl
 * @since 2021 
 */
public enum SortMode
{
	NONE("不排序" , null) ,
	@SuppressWarnings("unchecked")
	ASC("升序" , (Comparator<Object>) Comparator.naturalOrder()),
	@SuppressWarnings("unchecked")
	DESC("降序" , (Comparator<Object>) Comparator.naturalOrder().reversed()) ,
	/**
	 * 需要把字符串转成数值比较
	 */
	NUM_ASC("数值升序" , Comparator.comparingDouble((o)->XClassUtil.toDouble(o))) ,
	/**
	 * 需要把字符串转成数值比较
	 */
	NUM_DESC("数值降序" , Comparator.comparingDouble((o)->XClassUtil.toDouble(o)).reversed()) ,
	CNPinYinASC("中文拼音升序" , ChineseComparator.getInstance()) ,
	CNPinYinDESC("中文拼音降序" , ChineseComparator.getInstance().reversed())
	;
	
	String mDisplayName ;
	Comparator<Object> mComp ;
	
	private SortMode(String aDisplayName , Comparator<Object> aComp)
	{
		mDisplayName = aDisplayName ;
		mComp = aComp ;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
	
	public Comparator<Object> getComparator()
	{
		return mComp ;
	}
}
