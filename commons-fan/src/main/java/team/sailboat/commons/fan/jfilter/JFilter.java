package team.sailboat.commons.fan.jfilter;

import java.util.List;

import team.sailboat.commons.fan.jfilter.JFilterNodeBuilder.Logic;
import team.sailboat.commons.fan.jfilter.JFilterNodeBuilder.Must;
import team.sailboat.commons.fan.jfilter.JFilterNodeBuilder.MustNot;
import team.sailboat.commons.fan.jfilter.JFilterNodeBuilder.Should;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.Assert;

public class JFilter implements FilterConstant
{
	public static JFilterBuilder builder()
	{
		return new InnerJFilterBuilder() ;
	}
	
	static class InnerJFilterBuilder implements JFilterBuilder
	{
		JSONObject mFinalResult = new JSONObject() ;

		@Override
		public Must must()
		{
			JSONArray obj = new JSONArray() ;
			mFinalResult.put(sUN_must , obj) ;
			return new InnerMust(this , obj) ;
		}

		@Override
		public Should should()
		{
			JSONArray obj = new JSONArray() ;
			mFinalResult.put(sUN_should , obj) ;
			return new InnerShould(this , obj) ;
		}

		@Override
		public MustNot must_not()
		{
			JSONArray obj = new JSONArray() ;
			mFinalResult.put(sUN_must_not, obj) ;
			return new InnerMustNot(this , obj) ;
		}

		@Override
		public JFilterBuilder term(String aField, Object aValue)
		{
			mFinalResult.put(sTag_term , new JSONObject().put(aField, aValue)) ;
			return this ;
		}
		
		@Override
		public JFilterBuilder contains(String aField, String aValue)
		{
			mFinalResult.put(sTag_contains, new JSONObject().put(aField, aValue)) ;
			return this ;
		}

		@Override
		public JFilterBuilder startsWith(String aField, String aValue)
		{
			mFinalResult.put(sTag_startsWith ,  new JSONObject().put(aField, aValue)) ;
			return this ;
		}
		
		@Override
		public JFilterBuilder endsWith(String aField, String aValue)
		{
			mFinalResult.put(sTag_endsWith ,  new JSONObject().put(aField, aValue)) ;
			return this ;
		}
		
		@Override
		public JFilterBuilder in(String aField, List<Object> aCollection)
		{
			Assert.notNull(aCollection) ;
			mFinalResult.put(sTag_in ,  new JSONObject().put(aField, new JSONArray(aCollection))) ;
			return this ;
		}
		
		@Override
		public JFilterBuilder expr(String aField, String aExpr)
		{
			mFinalResult.put(sTag_expr ,  new JSONObject().put(aField, aExpr)) ;
			return this ;
		}

		@Override
		public JFilterBuilder range(String aField, Object aMinVal , boolean aMinEquals , Object aMaxVal , boolean aMaxEquals)
		{
			JSONObject jobj = new JSONObject() ;
			if(aMinVal != null)
				jobj.put(aMinEquals?sTag_gte:sTag_gt , aMinVal) ;
			if(aMaxVal != null)
				jobj.put(aMaxEquals?sTag_lte:sTag_lt , aMaxVal) ;
			mFinalResult.put(sTag_range, jobj) ;
			return this ;
		}
		
		@Override
		public JFilterBuilder isNull(String aField)
		{
			mFinalResult.put("null", aField) ;
			return this ;
		}
		
		@Override
		public JSONObject build()
		{
			return mFinalResult ;
		}
	}
	
	static abstract class InnerLogic implements Logic
	{
		protected Object mParent ;
		protected JSONArray mResult ;
		
		public InnerLogic(Object aParent , JSONArray aObj)
		{
			mParent = aParent ;
			mResult = aObj ;
		}
		
		@Override
		public final JFilterBuilder back_root()
		{
			if(mParent instanceof JFilterBuilder)
				return ((JFilterBuilder)mParent) ;
			else
				return ((Logic)mParent).back_root() ;
		}

		public Logic term(String aField, Object aValue)
		{
			mResult.put(new JSONObject().put(sTag_term , new JSONObject().put(aField, aValue)));
			return this ;
		}
		
		@Override
		public Logic contains(String aField, String aValue)
		{
			mResult.put(new JSONObject().put(sTag_contains, new JSONObject().put(aField, aValue))) ;
			return this ;
		}

		@Override
		public Logic startsWith(String aField, String aValue)
		{
			mResult.put(new JSONObject().put(sTag_startsWith ,  new JSONObject().put(aField, aValue))) ;
			return this ;
		}
		
		@Override
		public Logic endsWith(String aField, String aValue)
		{
			mResult.put(new JSONObject().put(sTag_endsWith ,  new JSONObject().put(aField, aValue))) ;
			return this ;
		}
		
		@Override
		public Logic in(String aField, List<Object> aCollection)
		{
			Assert.notNull(aCollection) ;
			mResult.put(new JSONObject().put(sTag_in ,  new JSONObject().put(aField, new JSONArray(aCollection)))) ;
			return this ;
		}
		
		@Override
		public Logic expr(String aField, String aExpr)
		{
			mResult.put(new JSONObject().put(sTag_expr ,  new JSONObject().put(aField, aExpr))) ;
			return this ;
		}

		public Logic range(String aField, Object aMinVal, boolean aMinEquals, Object aMaxVal, boolean aMaxEquals)
		{
			JSONObject jobj = new JSONObject() ;
			if(aMinVal != null)
				jobj.put(aMinEquals?sTag_gte:sTag_gt , aMinVal) ;
			if(aMaxVal != null)
				jobj.put(aMaxEquals?sTag_lte:sTag_lt , aMaxVal) ;
			mResult.put(new JSONObject().put(sTag_range, new JSONObject().put(aField , jobj))) ;
			return this ;
		}
		
		public Logic isNull(String aField)
		{
			mResult.put(new JSONObject().put("null", aField)) ;
			return this ;
		}
	}
	
	static class InnerMust extends InnerLogic implements Must
	{
		public InnerMust(Object aParent , JSONArray aObj)
		{
			super(aParent , aObj) ;
		}

		@Override
		public Should back_should()
		{
			return (Should)mParent ;
		}

		@Override
		public MustNot back_must_not()
		{
			return (MustNot)mParent;
		}

		@Override
		public Should should()
		{
			JSONArray obj = new JSONArray() ;
			mResult.put(new JSONObject().put(sUN_should , obj)) ;
			return new InnerShould(this , obj) ;
		}

		@Override
		public MustNot must_not()
		{
			JSONArray obj = new JSONArray() ;
			mResult.put(new JSONObject().put(sUN_must_not , obj)) ;
			return new InnerMustNot(this , obj) ;
		}
		
		@Override
		public Must term(String aField, Object aValue)
		{
			return (Must)super.term(aField, aValue);
		}
		
		
		@Override
		public Must range(String aField, Object aMinVal, boolean aMinEquals, Object aMaxVal, boolean aMaxEquals)
		{
			return (Must)super.range(aField, aMinVal, aMinEquals, aMaxVal, aMaxEquals);
		}
		
		@Override
		public Must isNull(String aField)
		{
			return (Must)super.isNull(aField);
		}
		
		@Override
		public Must contains(String aField, String aValue)
		{
			return (Must)super.contains(aField, aValue) ;
		}

		@Override
		public Must startsWith(String aField, String aValue)
		{
			return (Must)super.startsWith(aField, aValue) ;
		}

		@Override
		public Must endsWith(String aField, String aValue)
		{
			return (Must)super.endsWith(aField, aValue) ;
		}

		@Override
		public Must in(String aField, List<Object> aCollection)
		{
			return (Must)super.in(aField, aCollection) ;
		}

		@Override
		public Must expr(String aField, String aExpr)
		{
			return (Must)super.expr(aField, aExpr) ;
		}

		@Override
		public Must must()
		{
			return this ;
		}
	}
	
	static class InnerShould extends InnerLogic implements Should
	{
		
		public InnerShould(Object aParent , JSONArray aResult)
		{
			super(aParent , aResult) ;
		}

		@Override
		public Must back_must()
		{
			return (Must)mParent ;
		}

		@Override
		public MustNot back_must_not()
		{
			return (MustNot)mParent ;
		}

		@Override
		public Must must()
		{
			JSONArray obj = new JSONArray() ;
			mResult.put(new JSONObject().put(sUN_must , obj)) ;
			return new InnerMust(this , obj) ;
		}

		@Override
		public MustNot must_not()
		{
			JSONArray obj = new JSONArray() ;
			mResult.put(new JSONObject().put(sUN_must_not , obj)) ;
			return new InnerMustNot(this , obj) ;
		}
		
		@Override
		public Should term(String aField, Object aValue)
		{
			return (Should)super.term(aField, aValue);
		}
		
		@Override
		public Should contains(String aField, String aValue)
		{
			return (Should)super.contains(aField, aValue) ;
		}

		@Override
		public Should startsWith(String aField, String aValue)
		{
			return (Should)super.startsWith(aField, aValue) ;
		}

		@Override
		public Should endsWith(String aField, String aValue)
		{
			return (Should)super.endsWith(aField, aValue) ;
		}

		@Override
		public Should in(String aField, List<Object> aCollection)
		{
			return (Should)super.in(aField, aCollection) ;
		}

		@Override
		public Should expr(String aField, String aExpr)
		{
			return (Should)super.expr(aField, aExpr) ;
		}
		
		@Override
		public Should range(String aField, Object aMinVal, boolean aMinEquals, Object aMaxVal, boolean aMaxEquals)
		{
			return (Should)super.range(aField, aMinVal, aMinEquals, aMaxVal, aMaxEquals);
		}
		
		@Override
		public Should isNull(String aField)
		{
			return (Should)super.isNull(aField);
		}

		@Override
		public Should should()
		{
			return this ;
		}
	}
	
	static class InnerMustNot extends InnerLogic implements MustNot
	{
		
		InnerMustNot(Object aParent , JSONArray aObj)
		{
			super(aParent, aObj) ;
		}

		@Override
		public Must back_must()
		{
			return (Must)mParent ;
		}

		@Override
		public Should back_should()
		{
			return (Should)mParent ;
		}

		@Override
		public Must must()
		{
			JSONArray obj = new JSONArray() ;
			mResult.put(new JSONObject().put(sUN_must , obj)) ;
			return new InnerMust(this , obj) ;
		}

		@Override
		public Should should()
		{
			JSONArray obj = new JSONArray() ;
			mResult.put(new JSONObject().put(sUN_should , obj)) ;
			return new InnerShould(this , obj) ;
		}

		@Override
		public MustNot term(String aField, Object aValue)
		{
			return (MustNot)super.term(aField, aValue);
		}
		
		@Override
		public MustNot contains(String aField, String aValue)
		{
			return (MustNot)super.contains(aField, aValue) ;
		}

		@Override
		public MustNot startsWith(String aField, String aValue)
		{
			return (MustNot)super.startsWith(aField, aValue) ;
		}

		@Override
		public MustNot endsWith(String aField, String aValue)
		{
			return (MustNot)super.endsWith(aField, aValue) ;
		}

		@Override
		public MustNot in(String aField, List<Object> aCollection)
		{
			return (MustNot)super.in(aField, aCollection) ;
		}

		@Override
		public MustNot expr(String aField, String aExpr)
		{
			return (MustNot)super.expr(aField, aExpr) ;
		}
		
		@Override
		public MustNot range(String aField, Object aMinVal, boolean aMinEquals, Object aMaxVal, boolean aMaxEquals)
		{
			return (MustNot)super.range(aField, aMinVal, aMinEquals, aMaxVal, aMaxEquals);
		}
		
		@Override
		public MustNot isNull(String aField)
		{
			return (MustNot)super.isNull(aField);
		}

		@Override
		public MustNot must_not()
		{
			return this ;
		}
	}
}
