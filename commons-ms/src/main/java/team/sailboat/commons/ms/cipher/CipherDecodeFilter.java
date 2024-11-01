package team.sailboat.commons.ms.cipher ;
/**
 * Copyright 2012-2013 eBay Software Foundation, All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


import java.io.IOException;

import org.springframework.core.annotation.Order;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.http.CipherCoder;
import team.sailboat.commons.fan.http.CipherDecoder;
import team.sailboat.commons.fan.http.ICoder;
import team.sailboat.commons.fan.http.URLCoder;
import team.sailboat.commons.fan.text.XString;


@Order(0)
@WebFilter(filterName="CipherDecodeFilter" , urlPatterns="/*")
public final class CipherDecodeFilter implements Filter
{
	
	public CipherDecodeFilter()
	{
	}

	@Override
	public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aChain)
			throws IOException, ServletException
	{
		if(aRequest instanceof HttpServletRequest)
		{
			HttpServletRequest httpReq = (HttpServletRequest)aRequest ;
			String algo = httpReq.getHeader(ICoder.sHeaderName_CipherAlgo) ;
			if(XString.isNotEmpty(algo) && !URLCoder.sName.equals(algo))
			{
				if(algo.startsWith(CipherCoder.sNamePrefix))
				{
					int diff = Integer.parseInt(algo.substring(CipherCoder.sNamePrefix.length())) ;
					CipherDecoder decoder = new CipherDecoder(diff) ;
					aRequest = new CipherRequestWrapper(httpReq, decoder) ;
 				}
			}
		}
		aChain.doFilter(aRequest, aResponse) ;
	}
    
}