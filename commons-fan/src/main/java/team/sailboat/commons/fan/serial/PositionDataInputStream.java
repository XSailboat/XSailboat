package team.sailboat.commons.fan.serial;

import java.io.DataInputStream;
import java.io.InputStream;

public class PositionDataInputStream extends DataInputStream
{

	public PositionDataInputStream(InputStream aIn)
	{
		super(aIn instanceof PositionInputStream?(PositionInputStream)aIn:new PositionInputStream(aIn));
	}
	
	public long getPosition()
	{
		return ((PositionInputStream)in).getPosition() ;
	}

}
