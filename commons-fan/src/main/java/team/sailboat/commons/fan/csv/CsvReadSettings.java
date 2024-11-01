package team.sailboat.commons.fan.csv;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lombok.Data;
import team.sailboat.commons.fan.csv.Csv.Letters;
import team.sailboat.commons.fan.text.XString;

@Data
public class CsvReadSettings implements Externalizable
{
	private static final long serialVersionUID = 1L;

	char quoteCharacter = Letters.QUOTE ;

	/**
	 * 去掉头尾的空白字符
	 */
	boolean trimWhitespace = true ;

	/**
	 * 当转义模式为“\”转义时，如果这个值是false，那么表示“\\”表示“\”	
	 * 它总是应该是false才对，不要动它
	 */
	boolean useTextQualifier = false ;

	/**
	 * 单元格定界符
	 */
	char delimiter = Letters.COMMA ;

	/**
	 * 记录行定界符
	 */
	char recordDelimiter = '\n' ;

	/**
	 * 注释字符，缺省是#
	 */
	char comment = Letters.POUND;

	/**
	 * 是否可能有注释
	 */
	boolean allowComments = false ;

	/**
	 * 转义模式
	 */
	EscapeMode escapeMode = EscapeMode.DOUBLED ;
	
	/**
	 * NULL值用什么字符表示。缺省NULL，
	 */
	String nullLiteral = "NULL"  ;

	/**
	 * 防止一行数据量过大。一行数据量过大，很可能是格式有问题
	 */
	boolean safetySwitch = true ;

	/**
	 * 是否跳过空的记录行
	 */
	boolean skipEmptyRecords = true ;

	/**
	 * 是否用一个字符串缓存一下当前的记录行数据.(原始的数据，不拆分，不解析)
	 */
	boolean captureRawRecord = false ;

	public CsvReadSettings()
	{
//		CaseSensitive = true;
		useTextQualifier = true;
		safetySwitch = true;
		skipEmptyRecords = true;
		captureRawRecord = true;
	}
	
	public void updateFrom(CsvReadSettings aOther)
	{
		quoteCharacter = aOther.quoteCharacter ;
		trimWhitespace = aOther.trimWhitespace ;
		useTextQualifier = aOther.useTextQualifier ;
		delimiter = aOther.delimiter ;
		recordDelimiter = aOther.recordDelimiter ;
		comment = aOther.comment ;
		allowComments = aOther.allowComments ;
		escapeMode = aOther.escapeMode ;
		nullLiteral = aOther.nullLiteral ;
		safetySwitch = aOther.safetySwitch ;
		skipEmptyRecords = aOther.skipEmptyRecords ;
		captureRawRecord = aOther.captureRawRecord ;
	}
	
	@Override
	public void readExternal(ObjectInput aIn) throws IOException, ClassNotFoundException
	{
		aIn.readLong() ;			// version
		quoteCharacter = aIn.readChar() ;
		trimWhitespace = aIn.readBoolean() ;
		useTextQualifier = aIn.readBoolean() ;
		delimiter = aIn.readChar() ;
		recordDelimiter = aIn.readChar() ;
		comment = aIn.readChar() ;
		allowComments = aIn.readBoolean() ;
		escapeMode = EscapeMode.valueOf(aIn.readUTF()) ;
		nullLiteral = aIn.readUTF() ;
		safetySwitch = aIn.readBoolean() ;
		skipEmptyRecords = aIn.readBoolean() ;
		captureRawRecord = aIn.readBoolean() ;
	}
	@Override
	public void writeExternal(ObjectOutput aOut) throws IOException
	{
		aOut.writeLong(1L) ;
		aOut.writeChar(quoteCharacter) ;
		aOut.writeBoolean(trimWhitespace) ;
		aOut.writeBoolean(useTextQualifier) ;
		aOut.writeChar(delimiter) ;
		aOut.writeChar(recordDelimiter) ;
		aOut.writeChar(comment) ;
		aOut.writeBoolean(allowComments) ;
		aOut.writeUTF(escapeMode.name()) ;
		aOut.writeUTF(nullLiteral) ;
		aOut.writeBoolean(safetySwitch) ;
		aOut.writeBoolean(skipEmptyRecords) ;
		aOut.writeBoolean(captureRawRecord) ;
	}
	
	public CsvWriteSettings toWriteSettings()
	{
		CsvWriteSettings settings = new CsvWriteSettings() ;
		if(escapeMode != null)
			settings.escapeMode = escapeMode ;
		settings.comment = comment ;
		settings.delimiter = delimiter ;
		settings.forceQualifier = useTextQualifier ;
		settings.supportNULL = XString.isNotEmpty(nullLiteral) ;
		settings.recordDelimiter = recordDelimiter ;
		settings.textQualifier = quoteCharacter ;
		settings.useTextQualifier = useTextQualifier ;
		return settings ;
	}
}
