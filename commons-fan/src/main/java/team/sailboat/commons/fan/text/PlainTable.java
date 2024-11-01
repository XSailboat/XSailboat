package team.sailboat.commons.fan.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.FormatException;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.lang.JCommon;

public class PlainTable
{
	static final String sNullCell = "<null>" ;
	static final String sBlankCell = "<blank>" ;
	/**
	 * 表示空格
	 */
	static final String sBlank = "%_" ;
	
	String mTitle ;
	
	List<String> mColNames = new ArrayList<String>() ;
	
	List<List<String>> mBody = new ArrayList<List<String>>() ;
	
	public PlainTable()
	{
	}
	
	public PlainTable(String aTitle)
	{
		mTitle = aTitle ;
	}
	
	/**
	 * 标题
	 * @return
	 */
	public String getTitle()
	{
		return mTitle;
	}
	
	/**
	 * 
	 *@param aCol			必须不为null
	 */
	public void addColumn(String aCol)
	{
		Assert.notBlank(aCol) ;
		mColNames.add(aCol) ;
	}
	
	public void setColumns(String...aColNames)
	{
		mColNames = XC.arrayList(aColNames) ;
	}
	
	public int getColAmount()
	{
		return mColNames.size() ;
	}
	
	public void assertColumns(String...aColumnNames) throws FormatException
	{
		int colAmount = XC.count(aColumnNames) ;
		if(colAmount>mColNames.size())
			throw new FormatException("表格有%1$d列，而断言%2$d列" , mColNames.size() , colAmount) ;
		if(colAmount>0)
		{
			for(int i=0 ; i<colAmount ; i++)
			{
				if(JCommon.unequals(aColumnNames[i] , mColNames.get(i)))
					throw new FormatException("表格的第 %1$d 列名是 %2$s ， 而断言这一列名为 %3$s"
							, i , mColNames.get(i) , aColumnNames[i]) ;	
			}
		}
	}
	
	/**
	 * 如果数组长度大于列数，超出部分将被丢弃，如果小于列数，将补null
	 * @param aCells
	 */
	public void addRow(String...aCells)
	{
		int colNum = getColAmount() ;
		List<String> row = new ArrayList<String>(colNum) ;
		int end = Math.min(colNum, aCells.length) ;
		for(int i=0 ; i<end ; i++)
			row.add(aCells[i]) ;
		for(int i=end ; i<colNum ; i++)
			row.add(null) ;
		mBody.add(row) ;
	}
	
	public String[] getRow(int aRowIndex)
	{
		if(aRowIndex>=0 && aRowIndex<getRowAmount())
			return mBody.get(aRowIndex).subList(0, getColAmount()).toArray(new String[0]) ;
		return null ;
	}
	
	public Iterator<List<String>> getRowIterator()
	{
		return mBody.iterator() ;
	}
	
	/**
	 * 没有算上表头
	 * @return
	 */
	public int getRowAmount()
	{
		return mBody.size() ;
	}
	
	public String[] getColumnByName(String aName , boolean aIgnoreCase)
	{
		int colIndex=0 ;
		for(String name : mColNames)
		{
			if((aIgnoreCase && name.equalsIgnoreCase(aName)) || name.equals(aName))
			{
				String[] col = new String[getRowAmount()] ;
				for(int i=0 ; i<getRowAmount();i++)
					col[i] = mBody.get(i).get(colIndex) ;
				return col ;
			}
			colIndex++ ;
		}
		return null ;
	}
	
	/**
	 * 没有算上表头
	 * @param aColIndex
	 * @return
	 */
	public String[] getColumn(int aColIndex)
	{
		if(aColIndex>=0 && aColIndex<getColAmount())
		{
			String[] col = new String[getRowAmount()] ;
			for(int i=0 ; i<getRowAmount();i++)
				col[i] = mBody.get(i).get(aColIndex) ;
			return col ;
		}
		return null ;
	}
	
	public String[] getColumnNames()
	{
		return mColNames.toArray(new String[0]) ;
	}
	
	public String getColumnName(int aColIndex)
	{
		if(aColIndex>=0 && aColIndex<getColAmount())
			return mColNames.get(aColIndex) ;
		return null ;
	}
	
	public int getColumnIndexByName(String aColName)
	{
		return mColNames.indexOf(aColName) ;
	}
	
	public String getCell(int aRowNum , int aColNum)
	{
		if(aRowNum>=0 && aRowNum<getRowAmount() && aColNum>=0 && aColNum<getColAmount())
			return mBody.get(aRowNum).get(aColNum) ;
		return null ;
	}
	
	/**
	 * 两个西文字符等于一个中文字符宽度
	 */
	@Override
	public String toString()
	{
		StringBuilder strBld = new StringBuilder() ;
		int[] widths = new int[getColAmount()] ;
		for(int i=0 ; i<widths.length ; i++)
		{
			widths[i] = countRowWidth(i, i==widths.length-1?0:5) ;
		}
		
		for(int i=0 ; i<widths.length ; i++)
		{
			append(strBld, getColumnName(i), widths[i]) ;
		}
		
		strBld.append(XString.sLineSeparator) ;
		
		for(int r=0 ; r<getRowAmount() ; r++)
		{
			for(int c=0 ; c<getColAmount() ; c++)
			{
				String cell = getDisplayString(getCell(r, c)) ;
				append(strBld, cell, widths[c]) ;
			}
		}
		
		return strBld.toString() ;
	}
	
	
	
	/**
	 * 计算某一列宽度
	 * @param aBlankWidth 			空格数
	 * @return
	 */
	int countRowWidth(int aColIndex , int aBlankWidth)
	{
		if(aColIndex>=0 && aColIndex<getColAmount())
		{
			String name = getColumnName(aColIndex) ;
			int width = countWidth(name) ;
			String[] cols = getColumn(aColIndex) ;
			for(String col : cols)
			{
				if(col == null)
					col = sNullCell ;
				else if(col.isEmpty())
					col = sBlankCell ;
				width = Math.max(countWidth(col) , width) ;
			}
			return width+aBlankWidth ;
		}
		return -1 ;
	}
	
	static void append(StringBuilder aStrBld , String aText , int aWidth)
	{
		aStrBld.append(aText) ;
		int num = aWidth-countWidth(aText) ;
		if(num>0)
		{
			for(int i=0 ; i<num ; i++)
				aStrBld.append(' ') ;
		}
	}
	
	static String getDisplayString(String aText)
	{
		if(aText == null)
			return sNullCell ;
		else if(aText.trim().isEmpty())
			return sBlankCell ;
		else
			return aText ;
	}
	
	/**
	 * 
	 * @param aText		不能为null
	 * @return
	 */
	static int countWidth(String aText)
	{
		char[] chars = aText.toCharArray() ;
		int width = 0 ;
		for(char ch : chars)
		{
			width += XString.isChinese(ch)?2:1 ;
		}
		return width ;
	}
	
	public static PlainTable build(String aText)
	{
		PlainTable table = new PlainTable() ;
		if(aText != null)
		{
			//先根据换行符，将aText分成一行一行
			String[] lines = aText.split(XString.sLineSeparator) ;
			//总是认为第一行为表头
			XStringReader strReader = new XStringReader(lines[0]) ;
			do
			{
				String colName = strReader.readNextUnblank() ;
				if(colName != null)
					table.addColumn(colName) ;
			}while(!strReader.isDone()) ;
			
			//读正文
			for(int i=1 ; i<lines.length ; i++)
			{
				strReader = new XStringReader(lines[i]) ;
				String[] cells = new String[table.getColAmount()] ;
				int count = 0;
				do
				{
					String cell = strReader.readNextUnblank() ;
					if(count<cells.length)
					{
						cells[count] = cell.replace(sBlank, " ") ;
					}
					else
						break ;
					count++ ;
				}while(!strReader.isDone()) ;
				table.addRow(cells) ;
			}
		}
		return table ;
	}
}
