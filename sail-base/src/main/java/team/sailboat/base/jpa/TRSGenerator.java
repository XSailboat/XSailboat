package team.sailboat.base.jpa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.EnumSet;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.id.factory.spi.CustomIdGeneratorCreationContext;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.text.IDGen;

/**
 * 
 *
 * @author yyl
 * @since 2024年9月30日
 */
public class TRSGenerator implements BeforeExecutionGenerator
{
	private static final long serialVersionUID = 1L;

	String mCategory = "default";

	String mPrefix = "";

	boolean mCanSpecify = false;

	Method mIdGetter;

	boolean mNoIdGetter = false;

	public TRSGenerator(Xid aConfig , Member aAnnotatedMember
			, CustomIdGeneratorCreationContext aContext)
	{
		mCategory = aConfig.category() ;
		mPrefix = aConfig.prefix() ;
		mCanSpecify = aConfig.canSpecify() ;
	}

	@Override
	public EnumSet<EventType> getEventTypes()
	{
		return EnumSet.of(EventType.INSERT) ;
	}

	@Override
	public Object generate(SharedSessionContractImplementor aSession,
			Object aOwner,
			Object aCurrentValue,
			EventType aEventType)
	{
		if (mCanSpecify)
		{
			if (!mNoIdGetter && mIdGetter == null)
			{
				mIdGetter = XClassUtil.getMethod0(aOwner.getClass(), "getId");
				mNoIdGetter = mIdGetter == null;
			}
			if (mIdGetter != null)
			{
				try
				{
					Object id = mIdGetter.invoke(aOwner);
					if (id != null)
						return XClassUtil.toString(id);
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					WrapException.wrapThrow(e);
				}

			}
		}
		return mPrefix + IDGen.newID_ignoreCase(mCategory, 6);
	}
}
