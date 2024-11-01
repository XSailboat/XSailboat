package team.sailboat.commons.fan.text;

public interface ITextOut
{
	
	void setIndent(String aIndent) ;
	/**
	 * 相当于StringBuilder.append([..])
	 * @param aStr
	 * @return
	 */
	ITextOut u(String aStr) ;
	
	ITextOut u(long aVal) ;
	
	ITextOut u(boolean aVal) ;
	
	ITextOut u(float aVal) ;
	
	ITextOut u(double aVal) ;
	
	/**
	 * 当指定参数为true的时候，输出指定的字符串
	 * @param aStr
	 * @param aCnd
	 * @return
	 */
	ITextOut u(String aStr , boolean aCnd) ;
	
	ITextOut u(int aVal) ;
	
	/**
	 * 相当于StringBuilder.append([..]).append([换行]) ;
	 * @param aStr
	 * @return
	 */
	ITextOut n(String aStr) ;
	
	ITextOut n(boolean aVal) ;
	
	ITextOut n(float aVal) ;
	
	ITextOut n(int aVal) ;
	
	ITextOut n(long aVal) ;
	
	/**
	 * 相当于StringBuilder.append([换行]) ;
	 * @return
	 */
	ITextOut n() ;
	
	/**
	 * 当指定参数为true的时候，输出换行
	 * @param aCnd
	 * @return
	 */
	ITextOut n$(boolean aCnd) ;
	
	/**
	 * 相当于StringBuilder.append([缩进]).append(..) ;		<br>
	 * 只有在一行的开头才会被记忆
	 * @return
	 */
	ITextOut t(String aStr) ;
	
	ITextOut t(int aStr) ;
	
	/**
	 * 相当于StringBuilder.append([缩进]) ;					<br>
	 * 只有在一行的开头才会被记忆
	 * @return
	 */
	ITextOut t() ;
	
	/**
	 * 与记忆缩进值相比减少一个缩进，然后输出指定字符串		<br>
	 * 只有在一行的开头才会被记忆
	 * @return
	 */
	ITextOut _t_(String aStr) ;
	
	/**
	 * 与上次比减少一个缩进,输出缩进									<br>
	 * 只有在一行的开头才会被记忆
	 * @return
	 */
	ITextOut _t_() ;
	
	/**
	 * 减少一个记忆缩进，不输出缩进									<br>
	 * 只有在一行的开头才会被记忆
	 * @return
	 */
	ITextOut _t_$() ;
	
	/**
	 * 减少一个记忆缩进，不输出缩进 ，并换行不缩进								<br>
	 * 等价于_t_$().n()
	 * @return
	 */
	ITextOut _t_$n() ;
	
	/**
	 * 与上次比减少一个缩进，然后输出内容，再换行
	 * @param aStr
	 * @return
	 */
	ITextOut _t_n(String aStr) ;
	
	/**
	 * 应用上次记忆的缩进值，然后输出内容		<br>
	 * 只有在一行的开头才会被记忆
	 * @return
	 */
	ITextOut _t(String aStr) ;
	
	/**
	 * 应用上次记忆的缩进值，然后输出内容		<br>
	 * 只有在一行的开头才会被记忆
	 * @return
	 */
	ITextOut _t(int aVal) ;
	
	/**
	 * 应用上次记忆的缩进值，然后输出内容，再换行	<br>
	 * @param aStr
	 * @return
	 */
	ITextOut _tn(String aStr , Object...aArgs) ;
	
	/**
	 * 应用上次记忆的缩进值，然后输出内容，再换行	<br>
	 * @param aVal
	 * @return
	 */
	ITextOut _tn(int aVal) ;
	
	/**
	 * 应用上次记忆的缩进值				<br>
	 * 只有在一行的开头才会被记忆
	 * @return
	 */
	ITextOut _t() ;
	
	/**
	 * 比上次记忆的缩进值增加一个，然后输出内容			<br>
	 * 只有在一行的开头才会被记忆
	 * @param aStr
	 * @return
	 */
	ITextOut __t(String aStr) ;
	
	/**
	 * 比上次记忆的缩进值增加一个，然后输出内容，再换行			<br>
	 * 只有在一行的开头才会被记忆
	 * @param aStr
	 * @return
	 */
	ITextOut __tn(String aStr) ;
	
	/**
	 * 比上次记忆的缩进值增加一个						<br>
	 * 只有在一行开头才会记忆
	 * @param aStr
	 * @return
	 */
	ITextOut __t() ;
	
	/**
	 * 记忆的缩进值增加1，不输出缩进
	 * @return
	 */
	ITextOut __t$() ;
}
