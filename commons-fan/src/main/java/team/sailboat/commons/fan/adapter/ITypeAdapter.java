package team.sailboat.commons.fan.adapter;

import java.util.function.Function;

public interface ITypeAdapter<T> extends Function<Object, T>
{
	Class<T> getType() ;
}
