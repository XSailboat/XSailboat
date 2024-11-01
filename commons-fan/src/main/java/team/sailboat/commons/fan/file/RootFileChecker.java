package team.sailboat.commons.fan.file;

import java.util.ArrayList;
import java.util.function.Predicate;

public class RootFileChecker<T> extends FileChecker<T>
{
	public RootFileChecker()
	{
		super(new ArrayList<>()) ;
	}
	
	public RootFileChecker(Predicate<T>[] aFilters)
	{
		super(new ArrayList<>()) ;
		mFilterStackList.add(aFilters) ;
	}
	
	public void addFilters(Predicate<T>[] aFilters)
	{
		mFilterStackList.add(aFilters) ; 
	}
}
