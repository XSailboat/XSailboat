package team.sailboat.commons.fan.infc;

import java.io.Closeable;

public interface StatusCloseable extends Closeable
{
	boolean isClosed() ;
}
