package team.sailboat.bd.base.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import team.sailboat.commons.fan.app.AppContext;
import team.sailboat.commons.fan.collection.PropertiesEx;
import team.sailboat.commons.fan.excep.WrapException;
import team.sailboat.commons.fan.text.XString;

public class FlinkYAMLConfUtils
{
	static Logger sLogger = LoggerFactory.getLogger(FlinkYAMLConfUtils.class);

	public static PropertiesEx load(InputStream aIns)
	{
		PropertiesEx props = new PropertiesEx();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(aIns, AppContext.sUTF8)))
		{
			String line;
			int lineNo = 0;
			while ((line = reader.readLine()) != null)
			{
				lineNo++;
				// 1. check for comments
				String[] comments = line.split("#", 2);
				String conf = comments[0].trim();

				// 2. get key and value
				if (conf.length() > 0)
				{
					String[] kv = conf.split(": ", 2);

					// skip line with no valid key-value pair
					if (kv.length == 1)
					{
						sLogger.warn("Error while trying to split key and value in configuration file {}: “{}“",
								lineNo,
								line);
						continue;
					}

					String key = kv[0].trim();
					String value = kv[1].trim();

					// sanity check
					if (key.length() == 0 || value.length() == 0)
					{

						sLogger.warn(
								"Error after splitting key and value in configuration file {}: “{}“",
								lineNo,
								line);
						continue;
					}
					props.put(key, value);
				}
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error parsing YAML configuration.", e);
		}

		return props;
	}
	
	public static void store(OutputStream aOuts , Properties aProps)
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(aOuts, AppContext.sUTF8)))
		{
			Set<?> enu = aProps.keySet() ;
			for(Object key : enu)
			{
				String key_1 = key.toString() ;
				writer.append(key_1).append(": ").append(aProps.getProperty(key_1)).append(XString.sLineSeparator) ;
			}
		}
		catch (IOException e)
		{
			WrapException.wrapThrow(e) ;
		}
	}

}
