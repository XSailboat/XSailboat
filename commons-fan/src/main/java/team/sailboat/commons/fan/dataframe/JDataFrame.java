package team.sailboat.commons.fan.dataframe;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

public interface JDataFrame
{
	public static JDataFrame newFrame(JSONObject aColumns)
	{
		return new JDataFrameImpl(aColumns) ;
	}
	
	JSONArray getData() ;
	
	JDataFrame appendRows(JSONArray aJa) ;
	
	JDataFrame appendRow(JSONArray aJa) ;
	
	JDataFrame handleColumn(ScalarExp aExp) ;
	
	JDataFrame handleColumnInTurn(Collection<ScalarExp> aExp) ;
	
	JDataFrame sort(Collection<? extends Entry<String , ? extends Comparator<Object>>> aColSortMethods) ;
	
	JDataFrame filter(FilterExp aExp) ;
	
	JDataFrame headN(int aNum) ;
	
	GroupBy group(List<ScalarExp> aColumns) ;
	
	void forEachRow(Consumer<JSONArray> aConsumer) ;
	
	int getColumnIndex(String aColumn) ;
}
