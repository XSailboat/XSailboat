package team.sailboat.commons.ms.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Stack;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.json.DupDetector;
import com.fasterxml.jackson.core.json.JsonWriteContext;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;

class JObjGenerator extends JsonGenerator
{
	
	static final int sFeature = Feature.STRICT_DUPLICATE_DETECTION.getMask() ;
	
	final Stack<Object> mStack = new Stack<Object>() ;
	
	Object mCurrent ;
	
	String mFieldName ;
	
	boolean mClosed = false ;
	
	JsonWriteContext mJCtx ;

	public JObjGenerator()
	{
		mJCtx = JsonWriteContext.createRootContext(DupDetector.rootDetector(this)) ;
	}

	@Override
	public void flush() throws IOException
	{
	}
	
	public JSONObject getJSONObject()
	{
		return (JSONObject)(mStack.isEmpty()?mCurrent:mStack.firstElement()) ;
	}
	
	public JSONArray getJSONArray()
	{
		return (JSONArray)(mStack.isEmpty()?mCurrent:mStack.firstElement()) ;
	}

	@Override
	public void writeStartArray() throws IOException
	{
		setCurrent(new JSONArray()) ;
		mJCtx = mJCtx.createChildArrayContext() ;
	}

	@Override
	public void writeEndArray() throws IOException
	{
		mStack.pop() ;
		if(!mStack.isEmpty())
			mCurrent = mStack.peek() ;
		mJCtx = mJCtx.getParent() ;
	}

	@Override
	public void writeStartObject() throws IOException
	{
		setCurrent(new JSONObject()) ;
		mJCtx = mJCtx.createChildObjectContext() ;
	}
	
	protected void setCurrent(Object aCurrent)
	{
		if(mCurrent != null)
		{
			if(mFieldName == null)
				((JSONArray)mCurrent).put(aCurrent) ;
			else
				((JSONObject)mCurrent).put(mFieldName , aCurrent) ;
		}
		mCurrent = aCurrent ;
		mStack.push(mCurrent) ;
		mFieldName = null ;
	}
	
	@Override
	public void setCurrentValue(Object v)
	{
		if (mJCtx != null)
		{
			mJCtx.setCurrentValue(v);
		}
	}
	
	@Override
	public Object getCurrentValue()
	{
		return mJCtx.getCurrentValue() ;
	}

	@Override
	public void writeEndObject() throws IOException
	{
		mStack.pop() ;
		if(!mStack.isEmpty())
			mCurrent = mStack.peek() ;
		mJCtx = mJCtx.getParent() ;
	}

	@Override
	public void writeFieldName(String aName) throws IOException
	{
		mFieldName = aName ;
		mJCtx.writeFieldName(aName) ;
	}

	@Override
	public void writeString(String aText) throws IOException
	{
		if(mFieldName == null)
			((JSONArray)mCurrent).put(aText) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , aText) ;
			mFieldName = null ;
		}
	}

	@Override
	public void writeString(char[] aText, int aOffset, int aLen) throws IOException
	{
		String text = new String(aText , aOffset , aLen) ;
		if(mFieldName == null)
			((JSONArray)mCurrent).put(text) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , text) ;
			mFieldName = null ;
		}
	}

	@Override
	public void writeRawUTF8String(byte[] aText, int aOffset, int aLength) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void writeUTF8String(byte[] aText, int aOffset, int aLength) throws IOException
	{
		String text = new String(aText , aOffset , aLength , AppContext.sUTF8) ;
		if(mFieldName == null)
			((JSONArray)mCurrent).put(text) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , text) ;
			mFieldName = null ;
		}
	}

	@Override
	public void writeRaw(String aText) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void writeRaw(String aText, int aOffset, int aLen) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void writeRaw(char[] aText, int aOffset, int aLen) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void writeRaw(char aC) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void writeBinary(Base64Variant aBv, byte[] aData, int aOffset, int aLen) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void writeNumber(int aV) throws IOException
	{
		if(mFieldName == null)
			((JSONArray)mCurrent).put(aV) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , aV) ;
			mFieldName = null ;
		}
	}

	@Override
	public void writeNumber(long aV) throws IOException
	{
		if(mFieldName == null)
			((JSONArray)mCurrent).put(aV) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , aV) ;
			mFieldName = null ;
		}
	}

	@Override
	public void writeNumber(BigInteger aV) throws IOException
	{
		if(mFieldName == null)
			((JSONArray)mCurrent).put(aV) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , aV) ;
			mFieldName = null ;
		}
	}

	@Override
	public void writeNumber(double aV) throws IOException
	{
		if(mFieldName == null)
			((JSONArray)mCurrent).put(aV) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , aV) ;
			mFieldName = null ;
		}
	}

	@Override
	public void writeNumber(float aV) throws IOException
	{
		if(mFieldName == null)
			((JSONArray)mCurrent).put(aV) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , aV) ;
			mFieldName = null ;
		}
	}

	@Override
	public void writeNumber(BigDecimal aV) throws IOException
	{
		if(mFieldName == null)
			((JSONArray)mCurrent).put(aV) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , aV) ;
			mFieldName = null ;
		}
	}

	@Override
	public void writeNumber(String aEncodedValue) throws IOException
	{
	}

	@Override
	public void writeBoolean(boolean aState) throws IOException
	{
		if(mFieldName == null)
			((JSONArray)mCurrent).put(aState) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , aState) ;
			mFieldName = null ;
		}
	}

	@Override
	public void writeNull() throws IOException
	{
		if(mFieldName == null)
			((JSONArray)mCurrent).put((Object)null) ;
		else
		{
			((JSONObject)mCurrent).put(mFieldName , null , true) ;
			mFieldName = null ;
		}
	}

	@Override
	public JsonGenerator setCodec(ObjectCodec aOc)
	{
		return this ;
	}

	@Override
	public ObjectCodec getCodec()
	{
		return null;
	}

	@Override
	public Version version()
	{
		return null;
	}

	@Override
	public JsonGenerator enable(Feature aF)
	{
		return this ;
	}

	@Override
	public JsonGenerator disable(Feature aF)
	{
		return this ;
	}

	@Override
	public boolean isEnabled(Feature aF)
	{
		return false;
	}

	@Override
	public int getFeatureMask()
	{
		return 0;
	}

	@Override
	public JsonGenerator setFeatureMask(int aValues)
	{
		return this ;
	}

	@Override
	public JsonGenerator useDefaultPrettyPrinter()
	{
		return this ;
	}

	@Override
	public void writeFieldName(SerializableString aName) throws IOException
	{
		mFieldName = aName.getValue() ;
	}

	@Override
	public void writeString(SerializableString aText) throws IOException
	{
		writeString(aText.getValue()) ;
	}

	@Override
	public void writeRawValue(String aText) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void writeRawValue(String aText, int aOffset, int aLen) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void writeRawValue(char[] aText, int aOffset, int aLen) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public int writeBinary(Base64Variant aBv, InputStream aData, int aDataLength) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void writeObject(Object aPojo) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public void writeTree(TreeNode aRootNode) throws IOException
	{
		throw new UnsupportedOperationException() ;
	}

	@Override
	public JsonStreamContext getOutputContext()
	{
		return mJCtx ;
	}

	@Override
	public boolean isClosed()
	{
		return mClosed ;
	}

	@Override
	public void close() throws IOException
	{
		mClosed = true ;
	}

	
}
