package team.sailboat.commons.fan.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.infc.EConsumer;
import team.sailboat.commons.fan.serial.StreamAssist;
import team.sailboat.commons.fan.text.XString;

public class EntityPart implements EConsumer<DataOutputStream , IOException> , IMultiPartConst
{
	String mContentType ;
	
	String mName ;
	
	LinkedHashMap<String, String> mProperties = XC.linkedHashMap() ;
	
	Object mContent ;
	
	public EntityPart(String aName)
	{
		mName = aName ;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setProperty(String aName , String aValue)
	{
		mProperties.put(aName, aValue) ;
	}
	
	public Map<String, String> getProperties()
	{
		return mProperties;
	}
	
	public String getContentType()
	{
		return mContentType;
	}
	public void setContentType(String aContentType)
	{
		this.mContentType = aContentType;
	}
	
	/**
	 * 
	 * @param aContent		可以是InputStream
	 */
	public void setContent(Object aContent)
	{
		mContent = aContent;
	}

	@Override
	public void accept(DataOutputStream douts) throws IOException
	{
		writeBoundaryBegin(douts) ;
		StringBuilder strBld = new StringBuilder() ;
		strBld.append("Content-Disposition: form-data; name=\"").append(mName)
				.append('"') ;
		if(XC.isNotEmpty(mProperties))
		{
			for(Entry<String, String> entry : mProperties.entrySet())
			{
				strBld.append("; ").append(entry.getKey()).append("=\"").append(entry.getValue()).append('"') ;
			}
		}
		strBld.append(sLineEnd)
			.append("Content-Transfer-Encoding: binary").append(sLineEnd);
		if(XString.isNotEmpty(mContentType))
		{
			strBld.append("Content-Type: ").append(mContentType).append(sLineEnd) ;
		}
		strBld.append(sLineEnd) ;
		douts.write(strBld.toString().getBytes(AppContext.sUTF8)) ;
		// 写入内容
		if(mContent != null)
		{
			if(mContent instanceof InputStream)
			{
				// 写出的数据会在内存里，flush是没有用的。jdk21的PosterOutputStream继承ByteArrayOutputStream
				StreamAssist.transfer_cn((InputStream)mContent , douts) ;
			}
			else
				throw new IllegalStateException("为支持的MultiPart的Content类型："+mContent.getClass().getName()) ;
		}
		douts.writeBytes(sLineEnd) ;
	}
	
	public static EntityPart build(String aFileName , InputStream aIns , String aContentType)
	{
		EntityPart part = new EntityPart(aFileName) ;
		part.setProperty("name" , "file");
		part.setProperty("filename" , aFileName) ;
		part.setContent(aIns) ;
		part.setContentType(aContentType) ;
		return part ;
	}
}
