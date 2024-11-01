package team.sailboat.commons.ms.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import ch.qos.logback.core.net.SyslogOutputStream;
import team.sailboat.commons.fan.collection.BlockingArrayQueue;
import team.sailboat.commons.fan.exec.CommonExecutor;
import team.sailboat.commons.fan.serial.StreamAssist;

public class SyslogOutputStream_TCP extends SyslogOutputStream
{
	static final int sBufPerfectSize = 102400;
	static final int sSendBufSize = 1024000;
	static final int sSendFaildLogItemSizeLimit = 2000 ;
	private InetAddress address;
	private Socket mSocket;
	private ByteArrayOutputStream mBouts = new ByteArrayOutputStream();
	private int port;

	final BlockingQueue<byte[]> mMsgDataCache = new BlockingArrayQueue<byte[]>(sSendFaildLogItemSizeLimit , true) ;
	boolean mClosed = false ;

	public SyslogOutputStream_TCP(String aSyslogHost, int aPort) throws UnknownHostException, SocketException
	{
		super(aSyslogHost, aPort);
		this.address = InetAddress.getByName(aSyslogHost);
		this.port = aPort;
		CommonExecutor.execInSelfThread(()->{
			while(!mClosed)
			{
				try
				{
					byte[] data = mMsgDataCache.take() ;
					sendData(data) ;
				}
				catch (InterruptedException | IOException e)
				{
					e.printStackTrace();
				}
			}
			
		} , "SysLog数据提交");
	}
	
	protected void sendData(byte[] aData) throws IOException
	{
		if(mSocket == null || !mSocket.isConnected())
		{
			StreamAssist.close(mSocket) ;
			mSocket = new Socket() ;
			mSocket.setSendBufferSize(sSendBufSize);
			mSocket.setSoTimeout(500);
			mSocket.connect(new InetSocketAddress(address, port), 500);
		}
		OutputStream outs = mSocket.getOutputStream() ;
		for (int i = 0, len = 0; i < aData.length; i += len)
		{
			len = Math.min(aData.length - i, sSendBufSize);
			outs.write(aData, i, len);					// 这个地方有可能会挂住。
			outs.flush();
		}
	}
	

	public void write(byte[] byteArray, int offset, int len) throws IOException
	{
		synchronized (mBouts)
		{
			//把里面的\n 替换成 \r
			final byte nc = (byte)'\n' ;
			final byte rc = (byte)'\r' ;
			final byte vc = (byte)0x0b ;
			final byte sc = (byte)' ' ;
			int i = offset + len-1 ;
			while(i-->offset)
			{
				if(byteArray[i] == nc)
					byteArray[i] = vc ;
				else if(byteArray[i] == rc)
					byteArray[i] = sc ;
			}
			mBouts.write(byteArray, offset, len);
		}
	}
	
	@Override
	public void write(int b) throws IOException
	{
		synchronized (mBouts)
		{
			mBouts.write(b);
		}	
	}

	@Override
	public synchronized void flush() throws IOException
	{
		mMsgDataCache.offer(mBouts.toByteArray()) ;
		// clean up for next round
		if (mBouts.size() > sBufPerfectSize)
		{
			mBouts = new ByteArrayOutputStream();
		}
		else
		{
			mBouts.reset();
		}
	}

	public synchronized void close()
	{
		mClosed = true ;
		super.close();
		address = null;
		StreamAssist.close(mSocket) ;
		mSocket = null ;
	}
}
