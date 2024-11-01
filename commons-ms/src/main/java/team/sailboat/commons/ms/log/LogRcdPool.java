package team.sailboat.commons.ms.log;

import java.util.function.Consumer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import team.sailboat.commons.fan.collection.CircularFifoQueue;

public class LogRcdPool implements Consumer<ILoggingEvent>
{
	CircularFifoQueue<LogRecord> mLogQueue ;
	long mIndex = 0 ;

	public LogRcdPool(int aCapacity)
	{
		mLogQueue = new CircularFifoQueue<>(aCapacity) ; 
	}
	
	@Override
	public void accept(ILoggingEvent aT)
	{
		mLogQueue.offer(LogRecord.create(mIndex++, aT)) ;
	}
	
	public synchronized LogRecord[] get(long aSeq)
	{
		if(mLogQueue.isEmpty())
			return new LogRecord[0] ;
		LogRecord head =  mLogQueue.peek() ;
		if(head.getSeq()>=aSeq)
			return mLogQueue.toArray(new LogRecord[0]) ;
		else
		{
			int increase = (int)(aSeq-head.getSeq()) ;
			if(increase<mLogQueue.size())
				return mLogQueue.get(increase, mLogQueue.maxSize() , LogRecord.class) ;
			else
				return new LogRecord[0] ;
		}
	}

}
