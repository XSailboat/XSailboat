package com.cimstech.sailboat.yarn;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;

public class ClientTest
{
	static final String sCoreSitePath = "/Users/yyl/product/hadoop/etc/hadoop/core-site.xml" ;
	static final String sYarnSitePath = "/Users/yyl/product/hadoop/etc/hadoop/yarn-site.xml" ;
	static final String sHdfsSitePath = "/Users/yyl/product/hadoop/etc/hadoop/hdfs-site.xml" ;

	public static void main(String[] args) throws YarnException, IOException
	{
		YarnClient yarnClient = YarnClient.createYarnClient() ;
		Configuration conf = new Configuration() ;
		conf.addResource(new Path(sCoreSitePath));
		conf.addResource(new Path(sYarnSitePath));
		conf.addResource(new Path(sHdfsSitePath));
		yarnClient.init(conf);
		yarnClient.start(); 
		
		try
		{
			QueueInfo queueInfo = yarnClient.getQueueInfo("default") ;
			
			System.out.println(queueInfo.getQueueConfigurations());
			System.out.println(queueInfo.getCapacity());
		}
		finally
		{
			yarnClient.stop(); 
		}
		
	}

}
