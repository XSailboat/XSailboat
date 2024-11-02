package team.sailboat.bd.base.model;

import team.sailboat.commons.fan.dpa.anno.BForwardMethod;
import team.sailboat.commons.fan.dpa.anno.BReverseMethod;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.json.ToJSONObject;
import team.sailboat.commons.fan.text.XString;

/**
 * 坐标位置
 *
 * @author yyl
 * @since 2021年6月16日
 */
public class Position implements ToJSONObject
{
	final double mX ;
	final double mY ;
	
	public Position(double aX , double aY)
	{
		mX = aX ;
		mY = aY ;
	}
	
	public double getX()
	{
		return mX;
	}
	
	public double getY()
	{
		return mY;
	}
	
	@Override
	public JSONObject setTo(JSONObject aJSONObj)
	{
		return aJSONObj.put("x", mX)
				.put("y" , mY) ;
	}
	
	@Override
	public String toString()
	{
		return toJSONString() ;
	}
	
	@Override
	public boolean equals(Object aObj)
	{
		if(aObj == null || !(aObj instanceof Position))
			return false ;
		Position other = (Position)aObj ;
		return other.mX == mX && other.mY == mY ;
	}
	
	public Position clone()
	{
		return new Position(mX, mY) ;
	}
	
	static Position parse(JSONObject aJobj)
	{
		return new Position(aJobj.optDouble("x", 0d) , aJobj.optDouble("y" , 0d)) ;
	}
	
	@BForwardMethod
	public static Object forward(Position aSource)
	{
		return aSource==null?null:aSource.toJSONString() ;
	}
	
	@BReverseMethod
	public static Position reverse(Object aSource)
	{
		return aSource == null || XString.isEmpty(aSource.toString()) ? null : parse(new JSONObject(aSource.toString())) ;
	}
}
