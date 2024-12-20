package team.sailboat.commons.ms.bean;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.lang.Assert;


/**
 * 
 * 一页数据
 *
 * @author yyl
 * @since 2024年11月26日
 */
@Schema(description = "一页数据")
@Data
public class Page<T>
{
	@Schema(description = "页面序号，从0开始")
	int pageIndex ;
	
	@Schema(description = "页面大小。每页记录数")
	int pageSize ;
	
	@Schema(description = "每页数量")
	int pageAmount ;
	
	@Schema(description = "总数量")
	int totalAmount ;
	
	@Schema(description = "一页数据")
	List<T> data ;

	
	/**
	 * 构造函数，用于创建Page对象
	 * @param aPageSize 每页显示的数据条数
	 * @param aPageIndex 当前页码，从0开始计数
	 * @param aDataList 当前页的数据列表
	 * @param aTotalAmount 总数据条数
	 */
	public Page(int aPageSize , int aPageIndex , List<T> aDataList
			, int aTotalAmount)
	{
		pageSize = aPageSize ;
		pageIndex = aPageIndex ;
		data = aDataList ;
		totalAmount = aTotalAmount ;
		pageAmount = (int)Math.ceil(totalAmount*1d/pageSize) ;
	}
	
	/**
	 * 判断当前Page对象是否为空
	 * @return 如果数据列表为空，则返回true；否则返回false
	 */
	public boolean isEmpty()
	{
		return XC.isEmpty(data) ;
	}
	
	/**
	 * 创建一个空的Page对象
	 * @param <T> 数据类型
	 * @return 一个空的Page对象
	 */
	public static <T> Page<T> emptyPage()
	{
		return new Page<T>(0, 0, XC.emptyList() , 0) ;
	}

	/**
	 * 根据给定的参数创建一个Page对象
	 * @param <T> 数据类型
	 * @param aPageSize 每页显示的数据条数
	 * @param aPage 页码，从0开始计数
	 * @param aDataList 当前页的数据列表
	 * @param aTotalAmount 总数据条数
	 * @return 一个新的Page对象
	 */
	public static <T> Page<T> of(int aPageSize , int aPage , List<T> aDataList
			, int aTotalAmount)
	{
		return new Page<T>(aPageSize, aPage, aDataList, aTotalAmount) ;
	}
	
	/**
	 * 根据给定的参数和全部数据列表创建一个Page对象
	 * @param <T> 数据类型
	 * @param aClass 数据类型Class对象（此参数在当前实现中未使用）
	 * @param aPageSize 每页显示的数据条数
	 * @param aPageIndex 页码，从0开始计数
	 * @param aAllDataList 全部数据列表
	 * @return 一个新的Page对象，包含指定页的数据
	 */
	public static <T> Page<T> of(Class<T> aClass , int aPageSize , int aPageIndex , List<T> aAllDataList)
	{
		if(XC.isEmpty(aAllDataList))
			return emptyPage() ;
		int startIndex = aPageIndex*aPageSize ;
		Assert.isTrue(startIndex<aAllDataList.size() , "指定的页码[%1$d]和页面尺寸[%2$d]超出了总元素个数%3$d" , aPageIndex , aPageSize , aAllDataList.size()) ;
		int endIndex = Math.min(startIndex+aPageSize , aAllDataList.size()) ;
		List<T> subList = XC.arrayList(endIndex-startIndex) ;
		for(int i= startIndex ; i<endIndex ; i++)
			subList.add((T)aAllDataList.get(i)) ;
		
		return new Page<>(aPageSize , aPageIndex
				, subList
				, aAllDataList.size()) ;
	}
	
	/**
	 * 根据给定的参数和全部数据数组创建一个Page对象
	 * @param <T> 数据类型
	 * @param aPageSize 每页显示的数据条数
	 * @param aPageIndex 页码，从0开始计数
	 * @param aAllDataList 全部数据数组
	 * @return 一个新的Page对象，包含指定页的数据
	 */
	public static <T> Page<T> of(int aPageSize , int aPageIndex , T[] aAllDataList)
	{
		if(XC.isEmpty(aAllDataList))
			return emptyPage() ;
		int startIndex = aPageIndex*aPageSize ;
		Assert.isTrue(startIndex<aAllDataList.length , "指定的页码[%1$d]和页面尺寸[%2$d]超出了总元素个数%3$d" , aPageIndex , aPageSize , aAllDataList.length) ;
		int endIndex = Math.min(startIndex+aPageSize , aAllDataList.length) ;
		List<T> subList = XC.arrayList(endIndex-startIndex) ;
		for(int i= startIndex ; i<endIndex ; i++)
			subList.add((T)aAllDataList[i]) ;
		
		return new Page<>(aPageSize , aPageIndex
				, subList
				, aAllDataList.length) ;
	}
}
