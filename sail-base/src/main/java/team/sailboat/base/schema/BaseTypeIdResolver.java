package team.sailboat.base.schema;

import com.fasterxml.jackson.annotation.ObjectIdGenerator.IdKey;
import com.fasterxml.jackson.annotation.ObjectIdResolver;

public class BaseTypeIdResolver implements ObjectIdResolver
{
	
	@Override
	public void bindItem(IdKey aId, Object aPojo)
	{
	}

	@Override
	public Object resolveId(IdKey aId)
	{
		return BaseType.of(aId.key.toString()) ;
	}

	@Override
	public ObjectIdResolver newForDeserialization(Object aContext)
	{
		return this ;
	}

	@Override
	public boolean canUseFor(ObjectIdResolver aResolverType)
	{
		return aResolverType.getClass() == getClass() ;
	}

}
