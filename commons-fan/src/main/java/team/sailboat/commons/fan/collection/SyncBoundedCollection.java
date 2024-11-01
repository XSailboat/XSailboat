package team.sailboat.commons.fan.collection;

public class SyncBoundedCollection<E> extends SyncCollection<E> implements BoundedCollection<E>
{

	public SyncBoundedCollection(BoundedCollection<E> aC)
	{
		super(aC);
	}

	@Override
	public boolean isFull()
	{
		synchronized(mMutex)
		{
			return ((BoundedCollection<E>)mC).isFull() ;
		}
	}

	@Override
	public int maxSize()
	{
		return ((BoundedCollection<E>)mC).maxSize() ;
	}
}
