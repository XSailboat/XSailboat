package team.sailboat.commons.fan.text;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map.Entry;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.NotFoundException;
import team.sailboat.commons.fan.struct.Tuples;

public class XStringReader extends Reader
{
	char[] mContent ;
	int mPointer = 0 ;
	
	public XStringReader()
	{
		reset(null);
	}
	
	public XStringReader(String aContent)
	{
		reset(aContent);
	}
	
	public XStringReader reset(String aContent)
	{
		if(aContent != null)
			mContent = aContent.toCharArray() ;
		else 
			mContent = new char[0] ;
		mPointer = 0 ;
		return this ;
	}
	
	/**
	 * 
	 * @param aCh
	 * @return		index序号
	 */
	public int next(char aCh)
	{
		while(mPointer<mContent.length && mContent[mPointer] != aCh)
		{
			mPointer++ ;
		}
		if(mPointer<mContent.length)
			return mPointer ;
		else
			return -1 ;
	}
	
	/**
	 * 是否已经读完
	 * @return
	 */
	public boolean isDone()
	{
		return mPointer>=mContent.length ;
	}
	
	public int getPointer()
	{
		return mPointer ;
	}
	
	public XStringReader skipNext(char aCh)
	{
		while(mPointer<mContent.length && mContent[mPointer] == aCh)
			mPointer++ ;
		return this ;
	}
	
	/**
	 * 跳过紧接着的空格和不能显示的字符
	 */
	public XStringReader skipBlank()
	{
		while(mPointer<mContent.length && XString.isBlank(mContent[mPointer]))
			mPointer++ ;
		return this ;
	}
	
	public int getLength()
	{
		return mContent==null?0:mContent.length ;
	}
	
	public int getRemainSize()
	{
		return getLength() - mPointer ;
	}
	
	public int getChar(int aP)
	{
		return aP>=0 && aP<getLength()?mContent[aP]:-1 ;
	}
	
	/**
	 * 当读到末尾时返回0
	 * @return
	 */
	@Override
	public int read()
	{
		if(mPointer<mContent.length)
			return mContent[mPointer++] ;
		return -1 ;
	}
	
	public char readChar() throws EOFException
	{
		if(mPointer<mContent.length)
			return mContent[mPointer++] ;
		throw new EOFException("没有更多的字符") ;
	}
	
	@Override
	public int read(char[] aCbuf, int aOff, int aLen) throws IOException
	{
		 if ((aOff < 0) || (aOff > aCbuf.length) || (aLen < 0) ||
	                ((aOff + aLen) > aCbuf.length) || ((aOff + aLen) < 0))
		 {
			 throw new IndexOutOfBoundsException();
		 }
		 else if (aLen == 0)
		 {
			 return 0;
		 }
		 if (mPointer >= mContent.length)
			 return -1;
		 int n = Math.min(mContent.length - mPointer, aLen);
		 System.arraycopy(mContent, mPointer, aCbuf, aOff , aLen);
	     mPointer += n;
	     return n;
	}
	
	/**
	 * 自动查找后面的数字，跳过非数字字符，并将相连的数字字符组合成float，可以包含一个小数点
	 * @return
	 * @throws Exception 
	 */
	public float readNextFloat() throws NotFoundException
	{
		int i=mPointer ;
		int start = -1 ;
		boolean dot = false ;
		for(;i<mContent.length ; i++)
		{
			if(start<0 && (mContent[i] == '-'||mContent[i]=='+'))
			{
				start = i ;
			}
			else if(isDigit(mContent[i]))
			{
				if(start<0) start = i ;
			}
			else
			{
				if(start>=0)
				{
					if(!dot && mContent[i] == '.')
						dot = true ;
					else break ;
				}
				else if(!dot && mContent[i] == '.')
				{
					dot = true ;
					start = i ;
				}
			}
		}
		mPointer = i ;
		if(start>=0 && i>start)
			return Float.parseFloat(new String(mContent, start , start-i)) ;
		throw new NotFoundException("Not Found") ;
	}
	
	public double readNextDouble() throws NotFoundException
	{
		int i=mPointer ;
		int start = -1 ;
		boolean dot = false ;
		boolean e = false ;
		for(;i<mContent.length ; i++)
		{
			if(start<0 && (mContent[i] == '-'||mContent[i]=='+'))
			{
				start = i ;
			}
			else if(isDigit(mContent[i]) )
			{
				if(start<0) start = i ;
			}
			else
			{
				if(start>=0)
				{
					if(!e &&(mContent[i] == 'E' || mContent[i] == 'e'))
						e = true ;
					else if(e && (mContent[i] == '-' || mContent[i] == '+'))
					{}
					else if(!dot && mContent[i] == '.')
						dot = true ;
					else break ;
				}
				else if(!dot && mContent[i] == '.')
				{
					dot = true ;
					start = i ;
				}
			}
		}
		mPointer = i ;
		if(start>=0 && i>start)
		{
			
			return Double.parseDouble(new String(mContent, start , start-i)) ;
		}
		throw new NotFoundException("Not Found") ;
	}
	/**
	 * 确信下一段非空字符可以解析成浮点数的时候，才可以这么调用，否则会抛出解析异常
	 * @return
	 */
	public float readFloat()
	{
		return Float.parseFloat(readNextUnblank()) ;
	}
	
	/**
	 * 连续的空字符串会被认为是一段，连续的非空字符串也会被认为是一段
	 * @return
	 */
	public String readNextSegment()
	{
		int start = mPointer ;
		skipBlank() ;
		if(mPointer == start)
		{
			int i = mPointer ;
			for(; i<mContent.length ; i++)
			{
				if(mContent[i] == ' ')
					break ;
			}
			mPointer = i ;
		}
		return new String(mContent, start, mPointer-start) ;
	}
	
	/**
	 * 
	 * @param aMark
	 * @param aSkip		在命中的情况下，指针是否跳到下一个字符
	 * @return			没有命中，返回0 ，如果命中且aSkip为true但没有下一个字符，已经达到末尾，返回-1
	 * 			;其它情况下返回1
	 */
	public int skipTo(char aMark , boolean aSkip)
	{
		while(mPointer<mContent.length)
		{
			if(mContent[mPointer] == aMark)
			{
				if(aSkip)
					return ++mPointer<mContent.length?1:-1 ;
				else
					return 1 ;
			}
			mPointer++ ;
		}
		return 0 ;
	}
	
	public boolean skipTo(int aPos)
	{
		if(aPos == mPointer)
			return true ;
		if(aPos<=mContent.length && aPos>=0)
		{
			mPointer = aPos ;
			return true ;
		}
		return false ;
	}
	
	/**
	 * 指针偏移
	 * @param aCount	支持负数
	 */
	public void pointerOffset(int aCount)
	{
		if(aCount<0)
			mPointer = Math.max(0, mPointer+aCount) ;
		else
			mPointer = Math.min(mContent.length , mPointer+aCount) ;
	}
	
	public long skip(long n)
	{
		long newP = mPointer + n ;
		if(newP < 0)
		{
			newP = 0 ;
		}
		else
		{
			long len = getLength() ;
			if(newP > len)
				newP = len ;
			
		}
		long count = Math.abs(newP - mPointer) ;
		mPointer = (int)newP ;
		return count ;
	}
	
	/**
	 * 
	 * @param aMark
	 * @param aSkip			在找到aMark字符的情况下，如果aSkip为true，指针将指向aMark的后一个字符，如果false，则指针将指向aMark字符，
	 * @param aContainMark	返回的结果中是否包含aMark字符
	 * @return			key如果为true表示是找到aMark，值为从此次读取开始到目标位置                <br>
	 * 					false表示没有找到aMark，值为从此次读取开始位置到结束				<br>
	 * 					当此reader已经到达文件末尾时，返回<false , ""> (经慎思，用空字符串比null合适)		<br>
	 */
	public Entry<Boolean, String> readUntil(char aMark , boolean aSkip , boolean aContainMark)
	{
		if(isDone())
			return Tuples.of(false, "") ;
		int start = mPointer ;
		while(mPointer<mContent.length)
		{
			if(mContent[mPointer] == aMark)
			{
				int end = mPointer ;
				if(aSkip)
					mPointer++ ;
				return Tuples.of(true, new String(mContent , start , end+(aContainMark?1:0)-start)) ;
			}
			mPointer++ ;
		}
		return Tuples.of(false, new String(mContent , start , mPointer-start)) ;
	}
	
	/**
	 * 找下一个结束符
	 * @param aEndHalf
	 * @param aSkip					是否跳过结果符
	 * @param aContainMark			返回内容是否包含结束符
	 * @param aStartHalf
	 * @return		返回true表示找到，false表示没有找到，已经到末尾
	 */
	public Entry<Boolean, String> readUntilWithPair(char aEndHalf , boolean aSkip , boolean aContainMark , char aStartHalf)
	{
		if(isDone())
			return Tuples.of(false, "") ;
		int start = mPointer ;
		int pairCount = 0 ;
		while(mPointer<mContent.length)
		{
			if(mContent[mPointer] == aStartHalf)
				pairCount++ ;
			else if(mContent[mPointer] == aEndHalf)
			{
				if(--pairCount < 0)
				{
					int end = mPointer ;
					if(aSkip)
						mPointer++ ;
					return Tuples.of(true, new String(mContent , start , end+(aContainMark?1:0)-start)) ;
				}
			}
			mPointer++ ;
		}
		return Tuples.of(false, new String(mContent , start , mPointer-start)) ;
	}
	
	/**
	 * 
	 * @param aSkip				在找到aMark字符的情况下，如果aSkip为true，指针将指向aMark的后一个字符，如果false，则指针将指向aMark字符，
	 * @param aContainMark		返回的结果中是否包含aMark字符
	 * @param aMarks
	 * @returnkey		如果大于等于0，表示是匹配上的aMarks中字符的序号，值为从此次读取开始到目标位置		<br>
	 * 					-1表示没有找到aMark，值为从此次读取开始位置到结束									<br>
	 * 					当此reader已经到达文件末尾时，返回<-1 , "">(经慎思，用空字符串比null合适) 			<br>
	 */
	public Entry<Integer, String> readUntil(boolean aSkip , boolean aContainMark , char...aMarks)
	{
		if(isDone())
			return Tuples.of(-1, "") ;
		int start = mPointer ;
		int i = -1 ;
		while(mPointer<mContent.length)
		{
			i = XC.indexOf(aMarks, mContent[mPointer]) ; 
			if(i>=0)
			{
				int end = mPointer ;
				if(aSkip)
					mPointer++ ;
				return Tuples.of(i, new String(mContent , start , end+(aContainMark?1:0)-start)) ;
			}
			mPointer++ ;
		}
		return Tuples.of(-1, new String(mContent , start , mPointer-start)) ;
	}
	
	public Entry<Integer, String> readUntil(boolean aSkip , boolean aContainMark , String...aMarks)
	{
		if(isDone())
			return Tuples.of(-1, "") ;
		int start = mPointer ;
		int i=0 ;
		String mark ;
		while(mPointer<mContent.length)
		{
			bp_0802_1707:for(i=0 ; i<aMarks.length ; i++)
			{
				mark = aMarks[i] ;
				if(mContent[mPointer] == mark.charAt(0))
				{
					int strLen = mark.length() ;
					if(start+strLen>mContent.length)
						continue ;
					
					for(int k=mPointer+1 , j=1 ; j<strLen ; j++ , k++)
					{
						if(mContent[k] != mark.charAt(j))
							continue bp_0802_1707 ;
					}
					//能走到这一步说明能匹配上
					int end = mPointer ;
					if(aSkip)
						mPointer += strLen ;
					return Tuples.of(i, new String(mContent , start , end+(aContainMark?strLen:0)-start)) ;
				}
			}
			mPointer++ ;
		}
		return Tuples.of(-1, new String(mContent , start , mPointer-start)) ;
	}
	
	/**
	 * 
	 * @param aMark	
	 * @param aSkip		是否跳过Mark字符串
	 * @return			的返回结果包含aMark
	 */
	public String readUntil(String aMark , boolean aSkip)
	{
		return readUntil(aMark, aSkip, true).getValue() ;
	}
	
	/**
	 * 
	 * @param aMark
	 * @param aSkip
	 * @param aContainMark			在命中的情况下，返回的字符串中是否包含aMark
	 * @return
	 */
	public Entry<Boolean, String> readUntil(String aMark , boolean aSkip , boolean aContainMark)
	{
		if(mPointer<mContent.length)
		{
			int markLen = 0 ;
			if(aMark != null && (markLen=aMark.length())>0 && mPointer+markLen<mContent.length)
			{
				for(int i=mPointer ; i<mContent.length-markLen ; i++)
				{
					if(mContent[i] == aMark.charAt(0))
					{
						boolean fit = true ;
						for(int j=1 ; j<markLen && j+mPointer<mContent.length ; j++)
						{
							if(mContent[j+i] != aMark.charAt(j))
							{
								fit = false ;
								break ;
							}
						}
						if(fit)
						{
							String result = i>mPointer?new String(mContent , mPointer 
									, i-mPointer+(aContainMark?markLen:0)):"" ;
							mPointer = aSkip?i+markLen:i ;
							return Tuples.of(true, result) ;
						}
					}
				}
				
			}
			String result = new String(mContent, mPointer, mContent.length-mPointer) ;
			mPointer = mContent.length ;
			return Tuples.of(false, result) ;
		}
		return Tuples.of(false, null) ;
	}
	
	/**
	 * 会跳过紧接的一段空字符串，读取非空的一段字符串（）
	 * 如果接下来没有非空字符串了，将返回null
	 * @return
	 */
	public String readNextUnblank()
	{
		skipBlank() ;
		int i = mPointer ;
		int start = i ;
		for(; i<mContent.length ; i++)
		{
			if(XString.isBlank(mContent[i]))
				break ;
		}
		mPointer = i ;
		return mPointer>start?new String(mContent, start, mPointer-start):null ;
	}
	
	/**
	 * 到文件末尾都是空白字符的话，返回0，否则返回下一个非空白字符
	 * @return
	 */
	public char readNextUnblankChar()
	{
		skipBlank() ;
		if(mPointer<mContent.length-1)
			return mContent[mPointer++] ;
		return 0 ;
	}
	
	public String readLine()
	{
		if(isDone())
			return null ;
		int i = mPointer ;
		char c = 0 ;
		for(; i<mContent.length ; i++)
		{
			c = mContent[i] ; 
			if(c == '\n' || c == '\r')
				break ;
		}
		String line = new String(mContent, mPointer, i-mPointer) ;
		if(i<mContent.length-1 && c == '\r' && mContent[i+1] == '\n')
			i++ ;
		mPointer = i+1 ;
		return line ;
	}
	
	/**
	 * 读取剩余的
	 * @return
	 */
	public String readRemainder()
	{
		if(!isDone())
		{
			int i = mPointer ;
			mPointer = mContent.length ;
			return new String(mContent, i , mContent.length-i) ;
		}
		return null ;
	}
	
	/**
	 * 读取接下来的aAmount个字符串，如果已经到末尾或者aAmount小于等于0，则返回null
	 * @param aAmount
	 * @return
	 */
	public String readNextN(int aAmount)
	{
		if(!isDone() && aAmount>0)
		{
			int len = Math.min(mContent.length-mPointer, aAmount) ;
			if(len>0)
			{
				mPointer += len ;
				return new String(mContent, mPointer-len, len) ;
			}
		}
		return null ;
	}
	
	static boolean isDigit(char aCh)
	{
		return aCh>='0' && aCh<='9' ;
	}
	
	@Override
	public void close() throws IOException
	{
	}
}
