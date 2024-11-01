package team.sailboat.commons.fan.es;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.es.agg.AggsDefine;
import team.sailboat.commons.fan.es.index.IndexDefine;
import team.sailboat.commons.fan.es.index.MappingsDefine;
import team.sailboat.commons.fan.es.index.TemplateDefine;
import team.sailboat.commons.fan.es.query.AggSearchResult;
import team.sailboat.commons.fan.es.query.Fields;
import team.sailboat.commons.fan.es.query.PageSearchResult;
import team.sailboat.commons.fan.es.query.QueryDefine;
import team.sailboat.commons.fan.es.query.ScrollSearchResult;
import team.sailboat.commons.fan.es.query.SearchResult;
import team.sailboat.commons.fan.es.query.Sorter;
import team.sailboat.commons.fan.excep.HttpException;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.HttpStatus;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONException;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;

public class ESClient
{
	static String sDefault_Scroll = "1m" ;
	static int sDefautl_PageSize = 500 ;
	
	HttpClient mClient ;
	
	public ESClient(String aUrl) throws MalformedURLException
	{
		this(HttpClient.ofUrl(aUrl)) ;
	}
	
	public ESClient(HttpClient aClient)
	{
		mClient = aClient ;
	}
	
	public JSONObject createOrUpdateIndexTemplate(String aName , TemplateDefine aDefine) throws Exception
	{
		return mClient.askJo(Request.PUT().path("/_index_template/{}" , aName)
				.setJsonEntity(aDefine)) ;
	}
	
	public JSONObject getIndexTempalte(String aName) throws Exception
	{
		try
		{
			return mClient.askJo(Request.GET().path("/_index_template/{}" , aName)) ;
		}
		catch (HttpException e)
		{
			if(e.getStatus() == HttpStatus.NOT_FOUND)
				return null ;
			throw e ;
		}
	}
	
	public boolean existsIndexTemplate(String aName) throws Exception
	{
		try
		{
			mClient.ask(Request.HEAD().path("/_index_template/{}" , aName)) ;
			return true ;
		}
		catch (HttpException e)
		{
			if(e.getStatus() == HttpStatus.NOT_FOUND)
				return false ;
			throw e ;
		}
	}
	
	public JSONObject createIfNotExistsIndexTemplate(String aName , TemplateDefine aDefine) throws Exception
	{
		if(!existsIndexTemplate(aName))
		{
			return mClient.askJo(Request.PUT().path("/_index_template/{}" , aName)
					.setJsonEntity(aDefine)) ;
		}
		return null ;
	}
	
	public JSONObject createIndex(String aName) throws Exception
	{
		return mClient.askJo(Request.PUT().path("/{}" , aName.toLowerCase())) ;
	}
	
	public JSONArray countIndex(String aName) throws Exception
	{
		return mClient.askJa(Request.GET().path("/_cat/count/{}" , aName.toLowerCase())) ;
	}
	
	public JSONObject createIndexIfNotExists(String aName) throws Exception
	{
		if(!existsIndex(aName))
			return createIndex(aName) ;
		else
			return null ;
	}
	
	public JSONObject createIndexIfNotExists(String aName , IndexDefine aIndexDefine) throws Exception
	{
		if(!existsIndex(aName))
			return aIndexDefine==null?createIndex(aName)
					: mClient.askJo(Request.PUT().path("/{}" , aName.toLowerCase())
							.setJsonEntity(aIndexDefine)) ;
		else
			return null ;
	}
	
	public boolean existsIndex(String aName) throws Exception
	{
		try
		{
			mClient.ask(Request.HEAD().path("/"+aName.toLowerCase())) ;
			return true ;
		}
		catch(HttpException e)
		{
			if(e.getStatus() == HttpStatus.NOT_FOUND)
				return false ;
			throw e ;
		}
	}
	
	public JSONObject getIndex(String aIndex) throws Exception
	{
		return mClient.askJo(Request.GET().path("/{}" , aIndex)) ;
	}
	
	/**
	 * 如果指定索引存在删除索引，如果不存在，则返回null
	 * @param aIndex
	 * @return
	 * @throws Exception 
	 */
	public JSONObject deleteIndexIfExists(String aIndex) throws Exception
	{
		try
		{
			return mClient.askJo(Request.DELETE().path("/"+aIndex.toLowerCase())) ;
		}
		catch(HttpException e)
		{
			if(e.getStatus() == HttpStatus.NOT_FOUND)
				return null ;
			throw e ;
		}
	}
	
	/**
	 * 返回结果示例：<br />
	 * [{"health":"yellow","status":"open","index":"test","uuid":"uHTpsmHEQRKvsvNtoboiYg","pri":"1","rep":"1","docs.count":"0","docs.deleted":"0","store.size":"227b","pri.store.size":"227b","dataset.size":"227b"}]
	 * 
	 *
	 * @return
	 * @throws Exception
	 */
	public JSONArray listIndexes() throws Exception
	{
		return mClient.askJa(Request.GET().path("/_cat/indices")) ;
	}
	
	public JSONObject updateMapping(String aIndexName , MappingsDefine aMapping) throws Exception
	{
		return mClient.askJo(Request.PUT().path("/{}/_mapping" , aIndexName.toLowerCase())
				.setJsonEntity(aMapping.getMappingsDefineJo())) ;
	}
	
	public JSONArray listTemplates() throws Exception
	{
		return mClient.askJa(Request.GET().path("/_cat/templates"));
	}
	
	public JSONObject putDoc(String aIndex , String aDocId , JSONObject aJo) throws Exception
	{
		if(XString.isEmpty(aDocId))
			return mClient.askJo(Request.POST().path("/{}/_doc/" , aIndex.toLowerCase())
					.setJsonEntity(aJo)) ;
		else
			return mClient.askJo(Request.PUT().path("/{}/_doc/{}" , aIndex.toLowerCase() , aDocId)
					.setJsonEntity(aJo)) ;
	}
	
	/**
	 * 不存在就创建，存在就替换
	 * @param aIndex
	 * @param aDataMap
	 * @return
	 * @throws Exception
	 */
	public JSONObject putAllDocs(String aIndex, Map<String, JSONObject> aDataMap) throws Exception
	{
		if(XC.isEmpty(aDataMap))
			return null ;
		JSONArray ja = new JSONArray() ;
		aDataMap.forEach((k , v)->{
			ja.put(new JSONObject().put("index" , new JSONObject().put("_id" , k)))
				.put(v) ;
		}) ;
		return mClient.askJo(Request.POST().path("/{}/_bulk" , aIndex.toLowerCase())
				.setJsonEntity(ja)) ;
	}
	
	public JSONObject deleteAllDocs(String aIndex , Collection<String> aIds) throws Exception
	{
		if(XC.isEmpty(aIds))
			return null ;
		JSONArray ja = new JSONArray() ;
		aIds.forEach(id->{
			ja.put(new JSONObject().put("delete" , new JSONObject().put("_id", id))) ;
		});
		return mClient.askJo(Request.POST().path("/{}/_bulk" , aIndex)
				.setJsonEntity(ja)) ;
	}
	
	public JSONObject deleteAllDocs(String aIndex , QueryDefine aQuery) throws Exception
	{
		return mClient.askJo(Request.POST().path("/{}/_delete_by_query" , aIndex.toLowerCase())
				.setJsonEntity(new JSONObject().put("query", aQuery))) ;
	}
	
	/**
	 * 如果指定对象不存在，将返回null
	 * @param aIndex
	 * @param aDocId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getDoc(String aIndex , String aDocId) throws Exception
	{
		try
		{
			return mClient.askJo(Request.GET().path("/{}/_doc/{}" , aIndex.toLowerCase() , aDocId)) ;
		}
		catch(HttpException e)
		{
			if(e.getStatus() == HttpStatus.NOT_FOUND)
				return null ;
			throw e ;
		}
	}
	
	/**
	 * 只返回doc对象的数据部分
	 * @param aIndex
	 * @param aDocId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getDoc_(String aIndex , String aDocId) throws Exception
	{
		JSONObject jo = getDoc(aIndex, aDocId) ;
		return jo == null?null:jo.optJSONObject("_source") ;
	}
	
	public JSONObject getDocs(String aIndex , String... aIds) throws Exception
	{
		try
		{
			String indexName = aIndex.toLowerCase() ;
			return mClient.askJo(Request.GET().path("/{}/_mget" , indexName)
					.setJsonEntity(new JSONObject().put("docs", Stream.of(aIds)
							.map(id->new JSONObject().put("_id", id))
							.collect(Collectors.toList())))) ;
		}
		catch(HttpException e)
		{
			if(e.getStatus() == HttpStatus.NOT_FOUND)
				return null ;
			throw e ;
		}
	}
	
	public SearchResult searchIndex_after(String aIndex
			, QueryDefine aQueryDefine
			, int aSize
			, Sorter aSorter
			, String... aSearchAfterFields)
			throws JSONException, Exception
	{
		Assert.notNull(aSorter , "排序器不能为null!") ;
		return new PageSearchResult(mClient.askJo(Request.GET().path("/{}/_search" , aIndex.toLowerCase())
				.setJsonEntity(new JSONObject().put("query", aQueryDefine)
						.put("size" , aSize)
						.put("sort" , aSorter.toJSONArray())
						.put("search_after" , new JSONArray(aSearchAfterFields))
						))) ;
	}
	
	/**
	 * 
	 * @param aIndex
	 * @param aQueryDefine
	 * @param aFrom			从0开始
	 * @param aSize
	 * @return
	 * @throws JSONException
	 * @throws Exception
	 */
	public SearchResult searchIndex(String aIndex
			, QueryDefine aQueryDefine
			, Integer aFrom
			, Integer aSize)
			throws JSONException, Exception
	{
		return searchIndex(aIndex, aQueryDefine, null, aFrom, aSize) ;
	}
	
	public SearchResult searchIndex_aggs(String aIndex
			, QueryDefine aQueryDefine
			, AggsDefine aAggsDefine)
			throws JSONException, Exception
	{
		return new AggSearchResult(mClient.askJo(Request.GET().path("/{}/_search" , aIndex.toLowerCase())
				.queryParam("size", 0) 
				.setJsonEntity(new JSONObject().put("query", aQueryDefine)
						.put("aggs" , aAggsDefine)
						))
				, aAggsDefine.getAggPath().get(0) , aAggsDefine.getAggPath().get(1)) ;
	}
	
	public SearchResult searchIndex(String aIndex
			, QueryDefine aQueryDefine
			, Sorter aSorter
			, Integer aFrom
			, Integer aSize
			, Float aMinScore)
			throws JSONException, Exception
	{
		return searchIndex(aIndex, aQueryDefine, aSorter, null, aFrom, aSize , aMinScore) ;
	}
	
	public SearchResult searchIndex(String aIndex
			, QueryDefine aQueryDefine
			, Sorter aSorter
			, Integer aFrom
			, Integer aSize)
			throws JSONException, Exception
	{
		return searchIndex(aIndex, aQueryDefine, aSorter, null, aFrom, aSize) ;
	}
	
	public SearchResult searchIndex(String aIndex
			, QueryDefine aQueryDefine
			, Sorter aSorter
			, Fields aReturnFields
			, Integer aFrom
			, Integer aSize)
			throws JSONException, Exception
	{
		return searchIndex(aIndex, aQueryDefine, aSorter, aReturnFields, aFrom, aSize, null) ;
	}
	
	public SearchResult searchIndex(String aIndex
			, QueryDefine aQueryDefine
			, Sorter aSorter
			, Fields aReturnFields
			, Integer aFrom
			, Integer aSize
			, Float aMinScore)
			throws JSONException, Exception
	{
		return new PageSearchResult(mClient.askJo(Request.GET().path("/{}/_search" , aIndex.toLowerCase())
				.queryParam("from" , aFrom)
				.queryParam("size", aSize) 
				.setJsonEntity(new JSONObject().put("query", aQueryDefine)
						.putIf(aSorter != null , "sort" , ()->aSorter.toJSONArray())
						// 如下设置的话，得配合将_source设置为false，不返回_source中的内容。
						// 返回结果放在了fields下面
//						.putIf(aFields != null , "fields" , ()->aFields.toJSONArray())
//						.putIf(aFields != null , "_source" , false)
						.putIf(aReturnFields != null , "_source" , ()->aReturnFields.toJSONArray())
						.putIf(aMinScore != null , "min_score" , aMinScore)
						))) ;
 	}
	
	public SearchResult scrollSearchIndex(String aIndex , QueryDefine aQueryDefine) throws Exception
	{
		return new ScrollSearchResult(mClient.askJo(Request.GET().path("/{}/_search" , aIndex)
				.queryParam("scroll" , sDefault_Scroll)
				.setJsonEntity(new JSONObject().put("query", aQueryDefine)
						.put("size", sDefautl_PageSize))) , this::queryScroll) ;
	}
	
	protected JSONObject queryScroll(String aScrollId) throws Exception
	{
		return mClient.askJo(Request.GET().path("/_search/scroll")
				.setJsonEntity(new JSONObject().put("scroll", sDefault_Scroll)
						.put("scroll_id", aScrollId))) ;
	}
	
	public void scrollSearchIndex(String aIndex, QueryDefine aQueryDefine, BiPredicate<String, JSONObject> aBiPred) throws Exception
	{
		ScrollSearchResult searchResult = (ScrollSearchResult)scrollSearchIndex(aIndex, aQueryDefine) ;
		for(Map.Entry<String, JSONObject> entry : searchResult.sourceObjectEntries())
		{
			if(!aBiPred.test(entry.getKey(), entry.getValue()))
				break ;
		}
	}
	
	public JSONObject getMapping(String aIndex) throws Exception
	{
		return mClient.askJo(Request.GET().path("/{}/_mapping" , aIndex)) ;
	}
	
}
