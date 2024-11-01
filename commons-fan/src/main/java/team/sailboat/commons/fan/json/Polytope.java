package team.sailboat.commons.fan.json;

import java.util.function.Function;

import team.sailboat.commons.fan.lang.JCommon;

public abstract class Polytope
{
	protected Object mSource ;
	
	public Polytope(Object aSource) 
	{
		mSource = aSource ;
	}
	
	public Object getSource()
	{
		return mSource;
	}
	
	public abstract Object getFacade() ;
	
	@Override
	public String toString()
	{
		return JCommon.toString(getFacade()) ;
	}
	
	static class FuncPolytope extends Polytope
	{

		Function<Object , Object> mFacadeFunc ;
		
		FuncPolytope(Object aSource , Function<Object , Object> aFacadeFunc)
		{
			super(aSource) ;
			mFacadeFunc = aFacadeFunc ;
		}
		
		@Override
		public Object getFacade()
		{
			return mFacadeFunc.apply(mSource) ;
		}
	}
	
	static class ConstPolytope extends Polytope
	{

		Object mFacade ;
		
		ConstPolytope(Object aSource , Object aFacade)
		{
			super(aSource) ;
			mFacade = aFacade ;
		}
		
		@Override
		public Object getFacade()
		{
			return mFacade ;
		}
	}
	
	public static Polytope of(Object aSource , Object aFacade)
	{
		return new ConstPolytope(aSource, aFacade) ;
	}
	
	public static Polytope of(Object aSource , Function<Object, Object> aFacadeFunc)
	{
		return new FuncPolytope(aSource, aFacadeFunc) ;
	}
}
