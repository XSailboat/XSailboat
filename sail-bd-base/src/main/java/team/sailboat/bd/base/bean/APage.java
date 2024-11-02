package team.sailboat.bd.base.bean;

import java.util.List;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.dpa.DBean;
import team.sailboat.commons.fan.dpa.DPage;
import team.sailboat.commons.fan.lang.Assert;

@Schema(description = "分页")
@Data
public class APage<T>
{
	@SuppressWarnings("rawtypes")
	static final APage sEmptyPage = new APage<>() ;
	
	@Schema(description = "页码，从1开始")
	int pageIndex ;
	
	@Schema(description = "页面尺寸")
	int pageSize ;
	
	@Schema(description = "页数")
	int pageAmount ;
	
	@Schema(description = "总数")
	int totalAmount ;
	
	@Schema(description = "当前页数据行")
	List<T> data ;
	
	public APage()
	{
	}
	
	public APage(List<T> aData , int aPageIndex , int aPageSize , int aTotalAmount)
	{
		data = aData ;
		pageIndex = aPageIndex ;
		pageSize = aPageSize ;
		totalAmount = aTotalAmount ;
		pageAmount = (int)Math.ceil(aTotalAmount*1d/pageSize) ;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> APage<T> emptyPage()
	{
		return (APage<T>) sEmptyPage ;
	}
	
	/**
	 * 页码，序号从0开始
	 * @param <T>
	 * @param aEles
	 * @param aPageSize
	 * @param aPageIndex
	 * @return
	 */
	public static <T> APage<T> of(List<T> aEles , int aPageSize , int aPageIndex)
	{
		if(XC.isEmpty(aEles))
			return emptyPage() ;
		int startIndex = aPageIndex*aPageSize ;
		Assert.isTrue(startIndex<aEles.size() , "指定的页码[%1$d]和页面尺寸[%2$d]超出了总元素个数%3$d" , aPageIndex , aPageSize , aEles.size()) ;
		APage<T> page = new APage<>() ;
		page.setPageIndex(aPageIndex) ; 
		page.setPageSize(aPageSize) ;
		page.setPageAmount((int)Math.ceil(aEles.size()*1d/aPageSize)) ;
		page.setTotalAmount(aEles.size()) ;
		page.setData(aEles.subList(startIndex , Math.min(startIndex+aPageSize , aEles.size()))) ;
		return page ;
	}
	
	public static <T extends DBean , R> APage<R> of(DPage<T> aPage , Function<T, R> aFunc)
	{
		if(aPage == null)
			return null ;
		APage<R> page = new APage<>() ;
		page.setPageAmount(aPage.getPageAmount()) ;
		page.setPageIndex(aPage.getPageIndex()) ;
		page.setPageSize(aPage.getPageSize()) ;
		page.setTotalAmount(aPage.getTotalAmount()) ;
		page.setData(XC.extractAsArrayList(aPage.getData() , aFunc)) ;
		return page ;
	}
}
