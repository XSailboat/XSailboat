package team.sailboat.commons.fan.dpa;

import java.sql.ResultSet;

import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.lang.XClassUtil;

public class DBeanFactory implements IDBeanFactory
{

	@Override
	public DBean create(Class<? extends DBean> aClass, DTableDesc aTblDesc, ResultSet aRs)
	{
		try
		{
			DBean bean = (DBean)XClassUtil.newInstance(aClass) ;
			for(ColumnMeta col : aTblDesc.getColumns())
			{
				Object value = col.getSerDe().reverse(aRs.getObject(col.getAnnotation().name())) ;
				if(value == null)
				{
					String defaultVal = col.getAnnotation().defaultValue() ;
					if(defaultVal.length() > 0)
						value = defaultVal ;
				}
				col.getField().set(bean, XClassUtil.typeAdapt(value , col.getField().getType())) ;
			}
			bean._setLoaded();
			return bean ;
		}
		catch (Exception e)
		{
			WrapException.wrapThrow(e);
			return null ;			//dead code
		}
	}

}
