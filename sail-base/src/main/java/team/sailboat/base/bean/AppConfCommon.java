package team.sailboat.base.bean;

import org.springframework.beans.factory.annotation.Value;

import lombok.Data;
import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.text.XString;

@Data
public class AppConfCommon
{
	@Value("${sys.rdb.conn.url:}")
	String sysRdbConnUrl;
	
	@Value("${sys.rdb.conn.key:}")
	String sysRdbConnKey;
	
	@Value("${sys.rdb.conn.secret:}")
	String sysRdbConnSecret ;
	
	@Value("${sys.rdb.conn.schema:zbd}")
	String sysRdbConnSchema ;
	
	@Value("${sys.tdengine.driver}")
	String sysTDengineDriver;
	
	@Value("${sys.tdengine.conn.url:}")
	String sysTDengineConnUrl;
	
	@Value("${sys.tdengine.conn.key:}")
	String sysTDengineConnKey;
	
	@Value("${sys.tdengine.conn.secret:}")
	String sysTDengineConnSecret ;
	
	@Value("${sys.tdengine.conn.schema:}")
	String sysTDengineConnSchema ;
	
	@Value("${sys.hadoop.user:hadoop}")
	String sysHadoopUser ;
	
	@Value("${sys.authcenter.base_url:}")
	String sysAuthCenterBaseUrl ;
	
	@Value("${sys.es.url:}")
	String sysEsUrl ;
	
	@Value("${sys.es.apiKey:}")
	String sysEsApiKey ;
	
	@Value("${sys.graphdb.conn.url:}")
	String sysGraphDBConnUrl ;
	
	@Value("${sys.graphdb.conn.key:}")
	String sysGraphDBConnKey ;
	
	@Value("${sys.graphdb.conn.secret:}")
	String sysGraphDBConnSecret ;
	
	@Value("${ha.zookeeper.quorum:}")
	String zookeeperQuorum ;
	
	String msgProducerId ;
	
	public String getSysRdbConnUrl()
	{
		return XString.msgFmt(sysRdbConnUrl , sysRdbConnSchema) ;
	}
	
	public String getSysTDengineConnUrl()
	{
		return XString.msgFmt(sysTDengineConnUrl , sysTDengineConnSchema) ;
	}
	
	@Value("${sys.msg.producer.id:}")
	public void setMsgProducerId(String aMsgProducerId)
	{
		msgProducerId = aMsgProducerId ;
		AppContext.set("sys.msg.producer.id", msgProducerId);
	}
}
