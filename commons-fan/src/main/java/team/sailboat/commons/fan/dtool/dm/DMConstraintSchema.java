package team.sailboat.commons.fan.dtool.dm;

import java.util.regex.Matcher;

import team.sailboat.commons.fan.dtool.ConstraintSchema;
import team.sailboat.commons.fan.lang.Assert;
import team.sailboat.commons.fan.text.XString;

public class DMConstraintSchema extends ConstraintSchema implements DMConst
{
	public static final String sPK_TYPE = "TYPE" ;
	public static final String sPK_CONDITION = "CONDITION" ;
	
	public DMConstraintSchema()
	{
		super() ;
	}
	
	public DMConstraintSchema(String aName , String aType)
	{
		super(aName) ;
		putOtherProperty(sPK_TYPE, aType) ;
	}
	
	public String getType()
	{
		return (String)getOtherProperty(sPK_TYPE) ;
	}

	@Override
	public boolean isPrimary()
	{
		return sConstraintType_P.equals(getType()) ;
	}
	
	@Override
	public boolean isForeign()
	{
		return sConstraintType_R.equals(getType()) ;
	}

	@Override
	public boolean isUnique()
	{
		return sConstraintType_U.equals(getType()) ;
	}
	
	public String getCondition()
	{
		return (String)getOtherProperty(sPK_CONDITION) ;
	}
	
	@Override
	public String getSqlText()
	{
		if(isPrimary())
		{
			return String.format("CONSTRAINT %1$s PRIMARY KEY(%2$s)", getName()
					, XString.toString("," , getColumnNames())) ;
		}
		else if(isUnique())
		{
			return String.format("CONSTRAINT %1$s UNIQUE(%2$s)", getName()
					, XString.toString(",", getColumnNames())) ;
		}
		else if(sConstraintType_C.equals(getType()))
		{
			Matcher matcher = sPtn_CC_NotNull.matcher(getType()) ;
			Assert.isNotTrue(matcher.matches() , "多参数约束不能是NOT NULL约束") ;
			return String.format(" CONSTRAINT %1$s CHECK (%2$s)", getName()
					, getCondition()) ;
		}
		else 
			throw new IllegalStateException(String.format("未预料到的多行约束：%s" , toJSONString())) ;
	}

	@Override
	public ConstraintSchema clone()
	{
		DMConstraintSchema clone = new DMConstraintSchema() ;
		initClone(clone) ;
		return clone ;
	}

}
