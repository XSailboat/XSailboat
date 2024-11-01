package team.sailboat.commons.fan.eazi;

import java.io.IOException;
import java.util.Map;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.serial.PositionDataInputStream;

public class EaziInput extends EaziInputAdapter
{

	public EaziInput(PositionDataInputStream aDataIns, ClassLoader aClassLoader)
	{
		super(aDataIns, aClassLoader);
	}
	
	public Map<String , Object> construct() throws IOException
	{
		Map<String,Object> map = XC.linkedHashMap() ;
		String key = null ;
		while((key = readKey()) != null)
		{
			Object val = readObject() ;
			if(sKEY_Version.equals(key))
				mVersion = (String)val ;
			else
				map.put(key, val) ;
		}
		while(!mRunnables.isEmpty())
		{
			Runnable run = mRunnables.pop() ;
			try
			{
				run.run() ;
			}
			catch(Exception e)
			{
				e.printStackTrace() ;
			}
		}
		return map ;
	}
	
}
