package team.sailboat.ms.base.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import team.sailboat.commons.fan.event.IXListener;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.serial.TLPrintStream;

@Service
public class DebugAviatorService
{
	final Logger mLogger = LoggerFactory.getLogger(getClass()) ;
	
	public JSONObject execAviatorExpr(String aExpr , Map<String, Object> aCtx , String aUserId)
	{
		
		mLogger.info("用户[{}]执行表达式：{} , 参数：{}" , aUserId , aExpr);
		JSONObject resultJo = new JSONObject() ;
		JSONArray outJa = new JSONArray() ;
		IXListener lsn = TLPrintStream.wrapSysOut().addMessageListener((e)->outJa.put(e.getDescription())) ;
		try
		{
			Expression expr = AviatorEvaluator.compile(aExpr , false) ;
			resultJo.put("returnResult" , XClassUtil.toString(expr.execute(aCtx))) ;
		}
		catch(Exception e)
		{
			resultJo.put("exception" , ExceptionAssist.getStackTrace(e)) ;
		}
		finally
		{
			TLPrintStream.removeSysOutListener(lsn) ; 
		}
		return resultJo.put("consoleOut" , outJa) ;
	}
}
