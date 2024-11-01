package team.sailboat.commons.fan.file;

public interface IFileChecker<T>
{
	static final IFileChecker<?> sInstance = new AcceptAll<>() ;
	
	@SuppressWarnings("unchecked")
	public static <T> IFileChecker<T>  getAcceptAll()
	{
		return (IFileChecker<T>) sInstance ;
	}
	
	IFileChecker<T> checkIn(T aFile) ;
	
	boolean haveReachEnd() ;
	
	static class AcceptAll<T> implements IFileChecker<T>
	{	
		public AcceptAll()
		{}
		
		@Override
		public IFileChecker<T> checkIn(T aFile)
		{
			return this ;
		}
		
		@Override
		public boolean haveReachEnd()
		{
			return true ;
		}
	}
}
