package team.sailboat.commons.fan.struct;

public interface IXDataBag
{
	public void setData(Object aData) ;
	public Object getData() ;
	
	/**
	 * aKey==null时，相当于setData(Object aData)
	 * @param aKey
	 * @param aData
	 */
	public void setData(Object aKey , Object aData) ;
	
	/**
	 * aKey==null时，相当于getData()
	 * @param aKey
	 * @return
	 */
	public Object getData(Object aKey) ;
	
	/**
	 * 包含aKey==null的情形
	 * @return
	 */
	public int getDataEntryAmount() ;
}
