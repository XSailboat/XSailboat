package team.sailboat.commons.fan.eazi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;

import team.sailboat.commons.fan.serial.FlexibleBArrayDataOStream;
import team.sailboat.commons.fan.serial.FlexibleDataOutputStream;
import team.sailboat.commons.fan.serial.PositionDataInputStream;
import team.sailboat.commons.fan.serial.PositionInputStream;

public class EaziTool
{
	public static byte[] toBytes(Eazialiable aOutObj) throws IOException
	{
		FlexibleBArrayDataOStream fouts = new FlexibleBArrayDataOStream(1024) ;
		EaziOutput output = new EaziOutput(fouts) ;
		output.writeEazialiable(aOutObj);
		output.close(); 
		return fouts.toByteArray() ;
	}
	
	public static void write(FlexibleDataOutputStream aOuts , Eazialiable aObj) throws IOException
	{
		new EaziOutput(aOuts).writeEazialiable(aObj) ;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void write(FlexibleDataOutputStream aOuts , Map aObj) throws IOException
	{
		new EaziOutput(aOuts).writeMap(aObj); ;
	}
	
	public static void write(FlexibleDataOutputStream aOuts , Eazialiable[] aObjs) throws IOException
	{
		new EaziOutput(aOuts).writeArray(aObjs) ;
	}
	
	public static Object toObject(byte[] aData , ClassLoader aClassLoader) throws IOException
	{
		EaziInputAdapter input = new EaziInput(new PositionDataInputStream(new PositionInputStream(new ByteArrayInputStream(aData))), aClassLoader) ;
		return input.readObject() ;
	}
	
	public static Object read(InputStream aIns , ClassLoader aClassLoader) throws IOException
	{
		return read(aIns, aClassLoader, null) ;
	}
	
	public static Object read(InputStream aIns , ClassLoader aClassLoader , Function<String , Class<?>> aClassProvider) throws IOException
	{
		EaziInput input = new EaziInput(new PositionDataInputStream(new PositionInputStream(aIns)), aClassLoader) ;
		input.setClassProvider(aClassProvider);
		return input.readObject() ;
	}
}
