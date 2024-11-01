package team.sailboat.commons.fan.serial;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ExternalizableHelper
{

	public static void writeStringArray(ObjectOutput aOut , String[] aArray) throws IOException
	{
		if(aArray == null)
			aOut.writeInt(-1) ;
		else
		{
			int n = aArray.length ;
			aOut.writeInt(n) ;
			for(int i=0 ; i<n ; i++)
			{
				if(aArray[i] != null)
				{
					aOut.writeBoolean(true) ;
					aOut.writeUTF(aArray[i]) ;
				}
				else
					aOut.writeBoolean(false) ;
			}
		}
	}
	
	public static String[] readStringArray(ObjectInput aIn) throws IOException, ClassNotFoundException
	{
		int n = aIn.readInt() ;
		if(n == -1)
			return null ;
		else
		{
			String[] result = new String[n] ;
			for(int i=0 ; i<n ; i++)
			{
				if(aIn.readBoolean())
					result[i] = aIn.readUTF() ;
			}
			return result ;
		}
	}
	
	public static void writeClassArray(ObjectOutput aOut , Class<?>[] aArray) throws IOException
	{
		if(aArray == null)
			aOut.writeInt(-1) ;
		else
		{
			int n = aArray.length ;
			aOut.writeInt(n) ;
			for(int i=0 ; i<n ; i++)
			{
				if(aArray[i] != null)
				{
					aOut.writeBoolean(true) ;
					aOut.writeUTF(aArray[i].getName()) ;
				}
				else
					aOut.writeBoolean(false) ;
			}
		}
	}
	
	public static Class<?>[] readClassArray(ObjectInput aIn) throws IOException, ClassNotFoundException
	{
		int n = aIn.readInt() ;
		if(n == -1)
			return null ;
		else
		{
			Class<?>[] result = new Class[n] ;
			ClassLoader classLoader = ExternalizableHelper.class.getClassLoader() ;
			for(int i=0 ; i<n ; i++)
			{
				if(aIn.readBoolean())
				{
					result[i] = classLoader.loadClass(aIn.readUTF()) ;
				}
			}
			return result ;
		}
	}
	
	public static void writeString(ObjectOutput aOut , String aStr) throws IOException
	{
		if(aStr == null)
			aOut.writeBoolean(false);
		else
		{
			aOut.writeBoolean(true) ;
			aOut.writeUTF(aStr) ;
		}
	}
	
	public static String readString(ObjectInput aIn) throws IOException, ClassNotFoundException
	{
		if(aIn.readBoolean())
			return aIn.readUTF() ;
		else
			return null ;
	}


}
