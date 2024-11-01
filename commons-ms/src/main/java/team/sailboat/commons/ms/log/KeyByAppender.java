package team.sailboat.commons.ms.log;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import team.sailboat.commons.fan.text.XString;

public class KeyByAppender extends AppenderBase<ILoggingEvent>
{
	
	String mKeyPattern ;
	
	final PatternLayout mKeyLayout = new PatternLayout();
	
	@Override
	public void start()
	{
		super.start();
		mKeyLayout.setPattern(mKeyPattern) ;
		mKeyLayout.setContext(getContext()) ;
		mKeyLayout.start() ;
	}

	@Override
	protected void append(ILoggingEvent aEventObject)
	{
		String key = mKeyLayout.doLayout(aEventObject) ;
		if(XString.isEmpty(key))
			return ;
		KeyByLogEventSite.consume(name , key , aEventObject) ;
		
	}

	public void setKeyPattern(String aKeyPattern)
	{
		mKeyPattern = aKeyPattern;
	}
	public String getKeyPattern()
	{
		return mKeyPattern;
	}
}
