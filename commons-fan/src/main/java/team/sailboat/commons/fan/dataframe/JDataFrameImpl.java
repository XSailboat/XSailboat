package team.sailboat.commons.fan.dataframe;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;

class JDataFrameImpl implements JDataFrame
{
	JSONObject mColsJo ;
	
	final JSONArray mDataJa = new JSONArray() ;
	
	public JDataFrameImpl(JSONObject aColumns)
	{
		mColsJo = aColumns ;
	}
	
	@Override
	public JDataFrame appendRows(JSONArray aJa)
	{
		mDataJa.merge(aJa) ;
		return this ;
	}
	
	@Override
	public JDataFrame appendRow(JSONArray aJa)
	{
		mDataJa.put(aJa) ;
		return this ;
	}

	@Override
	public JSONArray getData()
	{
		return mDataJa ;
	}
	
	@Override
	public GroupBy group(List<ScalarExp> aColumns)
	{
		Assert.notNull(aColumns, "未指定分组列！") ;
		return new GroupByImpl(this , aColumns) ;
	}
	
	@Override
	public void forEachRow(Consumer<JSONArray> aConsumer)
	{
		mDataJa.forEachJSONArray(aConsumer) ;
	}
	
	@Override
	public int getColumnIndex(String aColumn)
	{
		JSONObject colJo = mColsJo.optJSONObject(aColumn) ;
		Assert.notNull(colJo , "不存在名为%s的列！", aColumn) ;
		return colJo.getInt("index") ;
	}
	
	@Override
	public JDataFrame sort(Collection<? extends Entry<String, ? extends Comparator<Object>>> aColSortMethods)
	{
		if(!mDataJa.isEmpty() && XC.isNotEmpty(aColSortMethods))
		{
			List<ColSorter> sorterList = ColSorter.build(aColSortMethods, (colName)->mColsJo.getJSONObject(colName).getInt("index")) ;
			mDataJa.sort(new ColSortersComp(sorterList)) ;
		}
		return this;
	}
	
	@Override
	public JDataFrame headN(int aNum)
	{
		if(aNum>1 && mDataJa.size() > aNum)
		{
			mDataJa.retain(0 , aNum) ;
		}
		return this ;
	}
	
	@Override
	public JDataFrame handleColumn(ScalarExp aExp)
	{
		if(!mDataJa.isEmpty())
		{
			final int len = mDataJa.size() ;
			int colIndex = getColumnIndex(aExp.getName()) ;
			for(int i=0 ; i<len ; i++)
			{
				JSONArray row = mDataJa.optJSONArray(i) ;
				row.put(colIndex , aExp.eval(row)) ;
			}
		}
		return this ;
	}
	
	@Override
	public JDataFrame handleColumnInTurn(Collection<ScalarExp> aExps)
	{
		if(XC.isNotEmpty(aExps))
		{
			for(ScalarExp exp : aExps)
				handleColumn(exp) ;
		}
		return this ;
	}
	
	@Override
	public JDataFrame filter(FilterExp aExp)
	{
		if(aExp != null)
			mDataJa.retainIf_JSONArray(aExp::eval);
		return this ;
	}
	
}
