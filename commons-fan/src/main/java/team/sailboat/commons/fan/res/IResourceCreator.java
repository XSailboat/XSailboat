package team.sailboat.commons.fan.res;

public interface IResourceCreator<T>
{
	T create() throws Exception ;
	
	/**
	 * 资源是否可用
	 * @param aRes
	 * @return
	 */
	default boolean isAvailable(T aRes)
	{
		return true ;
	}
}
