package team.sailboat.commons.fan.eazi;

import java.io.IOException;

import team.sailboat.commons.fan.serial.FlexibleDataOutputStream;

public class EaziOutput extends EaziOutputAdapter implements SerialConstants 
{
	public EaziOutput(FlexibleDataOutputStream aFOuts)
	{
		super(aFOuts) ;
	}
	
	public void close() throws IOException
	{
		if(mFDOStream != null) 
			mFDOStream.close();
	}
}
