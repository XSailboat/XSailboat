package team.sailboat.commons.fan.infc;

import java.io.Closeable;
import java.io.IOException;

public interface IDestroyable extends Closeable
{
	default void destroy()
	{
		try
		{
			close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		} 
	}
}
