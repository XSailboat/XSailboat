package team.sailboat.commons.fan.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class FileChecker<T> implements IFileChecker<T>
{
	List<Predicate<T>[]> mFilterStackList ;
	
	protected FileChecker(List<Predicate<T>[]> aFilterStackList)
	{
		mFilterStackList = aFilterStackList ;
	}
	
	@Override
	public boolean haveReachEnd()
	{
		return false;
	}
	
	@Override
	public IFileChecker<T> checkIn(T aFile)
	{
		List<Predicate<T>[]> list = new ArrayList<>() ;
		for(Predicate<T>[] filters : mFilterStackList)
		{
			if(filters[0].test(aFile))
			{
				if(filters.length == 1)
					return IFileChecker.getAcceptAll() ;
				else
					list.add(Arrays.copyOfRange(filters, 1, filters.length)) ;
			}
		}
		if(!list.isEmpty())
			return new FileChecker<>(list) ;
		return null ;
	}
}
