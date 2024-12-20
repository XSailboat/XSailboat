package team.sailboat.commons.fan.dpa;

import java.util.List;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.lang.Assert;

/**
 * 
 * 一页数据
 *
 * @author yyl
 * @since 2024年11月15日
 */
public class DPage<T extends DBean> implements ToJSONObject
{
	Class<T> mClass ;
	
	int mPageIndex ;
	
	int mPageSize ;
	
	int mPageAmount ;
	
	int mTotalAmount ;
	
	List<T> mDataList ;

	
	public DPage(Class<T> aClass , int aPageSize , int aPageIndex , List<T> aDataList
			, int aTotalAmount)
	{
		mClass = aClass ;
		mPageSize = aPageSize ;
		mPageIndex = aPageIndex ;
		mDataList = aDataList ;
		mTotalAmount = aTotalAmount ;
		mPageAmount = (int)Math.ceil(mTotalAmount*1d/mPageSize) ;
	}
	
	public boolean isEmpty()
	{
		return XC.isEmpty(mDataList) ;
	}

	public int getPageIndex()
	{
		return mPageIndex;
	}

	public void setPageIndex(int aPageIndex)
	{
		mPageIndex = aPageIndex;
	}

	public int getPageSize()
	{
		return mPageSize;
	}

	public void setPageSize(int aPageSize)
	{
		mPageSize = aPageSize;
	}

	public int getPageAmount()
	{
		return mPageAmount;
	}

	public void setPageAmount(int aPageAmount)
	{
		mPageAmount = aPageAmount;
	}

	public int getTotalAmount()
	{
		return mTotalAmount;
	}

	public void setTotalAmount(int aTotalAmount)
	{
		mTotalAmount = aTotalAmount;
	}

	public List<T> getData()
	{
		return mDataList;
	}

	public void setData(List<T> aDataList)
	{
		mDataList = aDataList;
	}

	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("data" , JSONArray.of(mDataList))
				.put("pageSize" , mPageSize)
				.put("pageIndex", mPageIndex)
				.put("totalAmount", mTotalAmount)
				.put("pageAmount", mPageAmount)
				.put("meta" , DBean.getTableDesc(mClass).getColumnsMeta()) ;
	}
	
	public static <T extends DBean> DPage<T> emptyPage(Class<T> aClass)
	{
		return new DPage<T>(aClass, 0, 0, XC.emptyList() , 0) ;
	}

	public static <T extends DBean> DPage<T> of(Class<T> aClass , int aPageSize , int aPage , List<T> aDataList
			, int aTotalAmount)
	{
		return new DPage<T>(aClass, aPageSize, aPage, aDataList, aTotalAmount) ;
	}
	
	public static <T extends DBean> DPage<T> of(Class<T> aClass , int aPageSize , int aPageIndex , List<?> aAllDataList)
	{
		if(XC.isEmpty(aAllDataList))
			return emptyPage(aClass) ;
		int startIndex = aPageIndex*aPageSize ;
		Assert.isTrue(startIndex<aAllDataList.size() , "指定的页码[%1$d]和页面尺寸[%2$d]超出了总元素个数%3$d" , aPageIndex , aPageSize , aAllDataList.size()) ;
		int endIndex = Math.min(startIndex+aPageSize , aAllDataList.size()) ;
		List<T> subList = XC.arrayList(endIndex-startIndex) ;
		for(int i= startIndex ; i<endIndex ; i++)
			subList.add((T)aAllDataList.get(i)) ;
		
		return new DPage<>(aClass , aPageSize , aPageIndex
				, subList
				, aAllDataList.size()) ;
	}
	
	public static <T extends DBean> DPage<T> of(Class<T> aClass , int aPageSize , int aPageIndex , T[] aAllDataList)
	{
		if(XC.isEmpty(aAllDataList))
			return emptyPage(aClass) ;
		int startIndex = aPageIndex*aPageSize ;
		Assert.isTrue(startIndex<aAllDataList.length , "指定的页码[%1$d]和页面尺寸[%2$d]超出了总元素个数%3$d" , aPageIndex , aPageSize , aAllDataList.length) ;
		int endIndex = Math.min(startIndex+aPageSize , aAllDataList.length) ;
		List<T> subList = XC.arrayList(endIndex-startIndex) ;
		for(int i= startIndex ; i<endIndex ; i++)
			subList.add((T)aAllDataList[i]) ;
		
		return new DPage<>(aClass , aPageSize , aPageIndex
				, subList
				, aAllDataList.length) ;
	}

}
