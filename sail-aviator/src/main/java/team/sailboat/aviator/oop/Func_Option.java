package team.sailboat.aviator.oop;

import java.util.Map;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.seq.SeqGetFunction;
import com.googlecode.aviator.runtime.function.seq.SeqNewMapFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class Func_Option extends AbstractFunction
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static final AviatorString sValueKey = new AviatorString("_value") ;
	
	
	final SeqNewMapFunction mapFunc = new SeqNewMapFunction() ;
	
	
	@Override
	public AviatorObject call(Map<String, Object> aEnv , AviatorObject aArg1)
	{
		_Opt opt = new _Opt() ;
		AviatorObject mapAO = mapFunc.call(aEnv , new AviatorString("opt") , opt
				, sValueKey , aArg1) ;
		opt.mMapAO = mapAO ;
		return mapAO ;
	}

	@Override
	public String getName()
	{
		return "Option" ;
	}
	
	static class _Opt extends AbstractFunction
	{
		static final SeqGetFunction sGetter = new SeqGetFunction() ;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		AviatorObject mMapAO ;

		public _Opt()
		{
		}
		
		@Override
		public AviatorObject call(Map<String, Object> aEnv)
		{
			return sGetter.call(aEnv , mMapAO, sValueKey) ;
		}

		@Override
		public String getName()
		{
			return "_opt";
		}
		
	}

}
