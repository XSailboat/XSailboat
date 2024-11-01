package team.sailboat.commons.fan.res;

import team.sailboat.commons.fan.excep.BuildFaildException;
import team.sailboat.commons.fan.infc.IDestroyable;

public interface IResourceBundle<T> extends IDestroyable , ResourceSupplier<T>
{
	boolean prepareForUse() ;
	
	void rebuild() throws BuildFaildException ;
}
