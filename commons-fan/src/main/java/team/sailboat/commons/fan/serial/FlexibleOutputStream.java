package team.sailboat.commons.fan.serial;

import java.io.IOException;
import java.io.OutputStream;

public abstract class FlexibleOutputStream extends OutputStream
{
	
	public abstract long point() throws IOException ;
	
	public void skip(int aN) throws IOException
	{
		skipTo(point()+aN);
	}
	
	public abstract void skipTo(long aPos) throws IOException ;
	
	/**
	 * 指针移到队尾
	 */
	public void skipToEnd() throws IOException
	{
		skipTo(size());
	}
	
	public abstract long size() throws IOException ;
}
