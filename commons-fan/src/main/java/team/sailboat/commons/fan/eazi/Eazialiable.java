package team.sailboat.commons.fan.eazi;

import java.io.IOException;
import java.util.Map;

public interface Eazialiable
{
	void write(EntryOutput aOuts) throws IOException ;
	/**
	 * 如果返回的Runnable不为null，XInput将把它压入栈中，待read完以后，再弹出栈执行
	 * 这对于关联的兼容性处理将非常有用
	 * @param aMap
	 * @return		
	 * @throws IOException
	 */
	Runnable read(Map<String , Object> aMap) throws IOException  ;
}	
