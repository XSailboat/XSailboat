package team.sailboat.commons.fan.adapter;

import java.sql.Blob;
import java.sql.SQLException;

import team.sailboat.commons.fan.excep.WrapException;

public class TA_Bytes implements ITypeAdapter<byte[]>
{

	@Override
	public byte[] apply(Object aT)
	{
		if(aT == null)
			return null ;
		if(aT instanceof byte[])
			return (byte[]) aT ;
		if(aT instanceof Blob)
		{
			try
			{
				long len = ((Blob) aT).length() ;
				return ((Blob) aT).getBytes(0, (int)len) ;
			}
			catch (SQLException e)
			{
				WrapException.wrapThrow(e);
			}
		}
		throw new IllegalArgumentException("不支持的转成byte[]的类型："+aT.getClass().getName()) ;
		
	}

	@Override
	public Class<byte[]> getType()
	{
		return byte[].class ;
	}

}