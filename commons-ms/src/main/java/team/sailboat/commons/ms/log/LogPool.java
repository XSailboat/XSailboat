package team.sailboat.commons.ms.log;

import team.sailboat.commons.fan.collection.CircularFifoQueue;

public class LogPool
{
	CircularFifoQueue<LogMsg> mLogQueue ;
	long mIndex = 0 ;

	public LogPool(int aCapacity)
	{
		mLogQueue = new CircularFifoQueue<>(aCapacity) ; 
	}
	
	public void add(String aLogMsg)
	{
		mLogQueue.offer(LogMsg.create(mIndex++, aLogMsg)) ;
	}
	
	public synchronized LogMsg[] get(long aSeq)
	{
		if(mLogQueue.isEmpty())
			return new LogMsg[0] ;
		LogMsg head =  mLogQueue.peek() ;
		if(head.getSeq()>=aSeq)
			return mLogQueue.toArray(new LogMsg[0]) ;
		else
		{
			int increase = (int)(aSeq-head.getSeq()) ;
			if(increase<mLogQueue.size())
				return mLogQueue.get(increase, mLogQueue.maxSize() , LogMsg.class) ;
			else
				return new LogMsg[0] ;
		}
	}

}
