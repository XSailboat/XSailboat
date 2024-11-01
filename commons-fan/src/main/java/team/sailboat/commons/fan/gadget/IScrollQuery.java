package team.sailboat.commons.fan.gadget;

import java.io.Closeable;

import team.sailboat.commons.fan.json.JSONObject;

public interface IScrollQuery<T> extends Closeable
{
	JSONObject scrollNext(int aMaxSize) ;
	
	String getHandle() ;
}
