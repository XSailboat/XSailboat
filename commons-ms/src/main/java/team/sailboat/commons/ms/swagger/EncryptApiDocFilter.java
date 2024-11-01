package team.sailboat.commons.ms.swagger;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.WriteListener;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import team.sailboat.commons.fan.lang.JCommon;
import team.sailboat.commons.fan.serial.FlexibleBArrayDataOStream;

@Order(0)
@WebFilter(filterName="EncryptApiDocFilter" , urlPatterns="/v3/api-docs")
public class EncryptApiDocFilter implements Filter
{
	
	public EncryptApiDocFilter()
	{
	}

	@Override
	public void doFilter(ServletRequest aRequest, ServletResponse aResponse, FilterChain aChain)
			throws IOException, ServletException
	{
		String path = ((HttpServletRequest)aRequest).getServletPath() ;
		if("/v3/api-docs".equals(path))
		{
			((HttpServletResponse)aResponse).setHeader("X-Encrypt", "Common");
			FlexibleBArrayDataOStream bouts = new FlexibleBArrayDataOStream(102400) ;
			aChain.doFilter(aRequest , new OSResponseWrapper((HttpServletResponse)aResponse , bouts)) ;
			OutputStream outs = aResponse.getOutputStream() ;
			outs.write(JCommon.encrypt_0(bouts.toByteArray() , 0 , (int)bouts.size())) ;
			outs.flush();
		}
		else
		{
			aChain.doFilter(aRequest , aResponse) ;
		}
	}
	
	static class OSResponseWrapper extends HttpServletResponseWrapper
	{
		
		OutputStream outs ;
		ServletOutputStream sos ;

		public OSResponseWrapper(HttpServletResponse aResponse , OutputStream aOuts)
		{
			super(aResponse);
			outs = aOuts ;
			sos = new DelegatingServletOutputStream(outs) ;
		}
		
		@Override
		public ServletOutputStream getOutputStream() throws IOException
		{
			return sos ;
		}
		
	}
	
	static class DelegatingServletOutputStream extends ServletOutputStream {

		private final OutputStream targetStream;


		/**
		 * Create a DelegatingServletOutputStream for the given target stream.
		 * @param targetStream the target stream (never {@code null})
		 */
		public DelegatingServletOutputStream(OutputStream targetStream) {
			Assert.notNull(targetStream, "Target OutputStream must not be null");
			this.targetStream = targetStream;
		}

		/**
		 * Return the underlying target stream (never {@code null}).
		 */
		public final OutputStream getTargetStream() {
			return this.targetStream;
		}


		@Override
		public void write(int b) throws IOException {
			this.targetStream.write(b);
		}

		@Override
		public void flush() throws IOException {
			super.flush();
			this.targetStream.flush();
		}

		@Override
		public void close() throws IOException {
			super.close();
			this.targetStream.close();
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			throw new UnsupportedOperationException();
		}

	}


}
