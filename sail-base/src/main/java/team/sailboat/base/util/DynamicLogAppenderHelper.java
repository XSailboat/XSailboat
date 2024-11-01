package team.sailboat.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.danielwegener.logback.kafka.KafkaAppender;
import com.github.danielwegener.logback.kafka.keying.KeyingStrategy;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ScanException;
import ch.qos.logback.core.util.OptionHelper;
import team.sailboat.base.ZKSysProxy;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.struct.Bytes;
import team.sailboat.commons.fan.sys.JEnvKit;

public class DynamicLogAppenderHelper
{
	static final Logger sLogger = LoggerFactory.getLogger(DynamicLogAppenderHelper.class) ;
	
	public static void buildAndStartKafkaAppender(String aTopicName , String aPattern
			, Filter<ILoggingEvent> aFilter) throws Exception
	{
		buildAndStartKafkaAppender(aTopicName, aPattern, aFilter, null);
	}
	
	public static void buildAndStartKafkaAppender(String aTopicName , String aPattern
			, Filter<ILoggingEvent> aFilter
			, KeyingStrategy<ILoggingEvent> aKeyStrategy) throws Exception
	{
		Context loggerCtx = (LoggerContext)LoggerFactory.getILoggerFactory() ;
		KafkaAppender<ILoggingEvent> appender = new KafkaAppender<>();
        // 这里设置级别过滤器
        appender.addFilter(aFilter) ;
        // 设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        // 但可以使用<scope="context">设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        appender.setContext(loggerCtx);
        // appender的name属性
        appender.setName("kafka_"+aTopicName);
        String bootServers = ZKSysProxy.getSysDefault().getKafkaBootstrapServers() ;
        appender.addProducerConfigValue("bootstrap.servers" , bootServers) ;
        appender.setTopic(aTopicName) ;
        if(aKeyStrategy == null)
        {
	        appender.setKeyingStrategy(new KeyingStrategy<ILoggingEvent>() {
	        	byte[] pid ;
				@Override
				public byte[] createKey(ILoggingEvent e) {
					if(pid == null)
						pid = Bytes.get(JEnvKit.getPID()) ;
					return pid ;
				}
			});
        }
        else
        	appender.setKeyingStrategy(aKeyStrategy) ;
        appender.setEncoder(createEncoder(loggerCtx , aPattern));
        appender.start();
        
        ((LoggerContext)loggerCtx).getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
        sLogger.info("日志写入Kafka集群：{}" , bootServers) ;
	}
	
	private static PatternLayoutEncoder createEncoder(Context aLoggerCtx , String aPattern) throws ScanException
	{
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		// 设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
		// 但可以使用<scope="context">设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
		encoder.setContext(aLoggerCtx);
		// 设置格式
		String pattern = OptionHelper.substVars(aPattern , aLoggerCtx);
		encoder.setPattern(pattern);
		encoder.setCharset(AppContext.sUTF8);
		encoder.start();
		return encoder;
	}
}
