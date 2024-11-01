package team.sailboat.commons.fan.infc;

public interface IStringSerializable
{
	/**
	 * 将性质表达成字符串
	 */
	String squash() ;
	/**
	 * 将squash得到的字符串解析并设置成性质值
	 */
	void inflate(String aStr) ;
}
