package team.sailboat.commons.fan.http;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月26日
 */
public interface IMultiPartConst
{
	public static final String sBoundary = "------------------------hfxxop2PWmOK1lxYuHhDegn8dciw7Mxr" ;
	
	public static final String sLineEnd = "\r\n" ;
	
	default void writeBoundaryBegin(DataOutputStream aOuts) throws IOException
	{
		aOuts.writeBytes("--");
		aOuts.writeBytes(sBoundary) ;
		aOuts.writeBytes(sLineEnd) ;
	}
	
	default void writeBoundaryEnd(DataOutputStream aOuts) throws IOException
	{
		aOuts.writeBytes("--");
		aOuts.writeBytes(sBoundary) ;
		aOuts.writeBytes("--");
		aOuts.writeBytes(sLineEnd) ;
	}
}
