package org.sailboat.ms.crane.test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.ms.crane.bean.HostProfile;

public class Test
{
	static String sYamlFilePath = "/Users/yyl/product/test/host_profiles.yaml" ;

	public static void main(String[] args) throws IOException
	{

		
		Map<String , HostProfile> hostMap = XC.linkedHashMap() ;
		HostProfile host = new HostProfile() ;
		host.setName("XCloud150") ;
		host.setSysPswd("abc") ;
		host.setIp("192.168.0.150") ;
		hostMap.put("XCloud150" , host) ;
		YAMLFactory fac = YAMLFactory.builder().disable(Feature.WRITE_DOC_START_MARKER).build() ;
		ObjectMapper mapper = new ObjectMapper(fac) ;
		String str = mapper.writeValueAsString(hostMap) ;
		System.out.println(str);
		
		MapType mapType =  TypeFactory.defaultInstance().constructMapType(LinkedHashMap.class , String.class , HostProfile.class) ;
		Object obj = mapper.readValue(str , mapType) ;
		System.out.println(obj) ;
		
//		DumperOptions dumpOpt = new DumperOptions() ;
//		dumpOpt.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK) ;
//		Representer representer = new Representer(dumpOpt) ;
//		representer.addClassTag(HostProfile.class , Tag.MAP) ;
//		Yaml yaml = new Yaml(representer ,  dumpOpt) ;
//		try(Writer writer = FileUtils.openWriter(new File(sYamlFilePath), "UTF-8"))
//		{
//			yaml.dump(hostMap , writer) ;
//		}
		
	}

}
