/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package team.sailboat.commons.fan.cli;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import team.sailboat.commons.fan.collection.XC;

/**
 * A class that implements the <code>CommandLineParser</code> interface
 * can parse a String array according to the {@link Options} specified
 * and return a {@link CommandLine}.
 *
 * @version $Id: CommandLineParser.java 1443102 2013-02-06 18:12:16Z tn $
 */
public interface CommandLineParser
{
	static final Pattern sPattern = Pattern.compile("\"([^\"]*)\"|'([^']*)'|`([^`]*)`|([^\\s]+)");
	
    /**
     * Parse the arguments according to the specified options.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @return the list of atomic option and value tokens
     *
     * @throws ParseException if there are any problems encountered
     * while parsing the command line tokens.
     */
    CommandLine parse(Options options, String[] arguments) throws ParseException;

    /**
     * Parse the arguments according to the specified options.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @param stopAtNonOption if <tt>true</tt> an unrecognized argument stops
     *     the parsing and the remaining arguments are added to the 
     *     {@link CommandLine}s args list. If <tt>false</tt> an unrecognized
     *     argument triggers a ParseException.
     *
     * @return the list of atomic option and value tokens
     * @throws ParseException if there are any problems encountered
     * while parsing the command line tokens.
     */
    CommandLine parse(Options options, String[] arguments, boolean stopAtNonOption) throws ParseException;
    
    public static List<String> splitLine(String aCmdLine)
    {
		List<String> tokens = XC.arrayList() ;
		// 正则表达式匹配带引号的参数或者非空格分隔的参数
		Matcher matcher = sPattern.matcher(aCmdLine);
		while (matcher.find())
		{
			if (matcher.group(1) != null)
			{ // 匹配到 "..."
				tokens.add(matcher.group(1));
			}
			else if (matcher.group(2) != null)
			{ // 匹配到 '...'
				tokens.add(matcher.group(2));
			}
			else if (matcher.group(3) != null)
			{ // 匹配到 `...`
				tokens.add(matcher.group(3));
			}
			else if (matcher.group(4) != null)
			{ // 匹配到其他没有空格分隔的部分
				tokens.add(matcher.group(4));
			}
		}
		return tokens;
    }
}
