/*
 * Java CSV is a stream based library for reading and writing
 * CSV and other delimited data.
 *   
 * Copyright (C) Bruce Dunwiddie bruce@csvreader.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package team.sailboat.commons.fan.csv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.NumberFormat;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import team.sailboat.commons.fan.file.FileUtils;
import team.sailboat.commons.fan.lang.Assert;

/**
 * A stream based parser for parsing delimited text data from a file or a
 * stream.
 * 对其进行了修改，以支持NULL值输出。用0x00表示NULL
 */
public class CsvReader implements Closeable , Csv
{
	static final int sReady = 0 ;
	static final int sReaded = 1 ;
	static final int sClosed = 2 ;
	
	private Reader inputStream = null;

	private String fileName = null;

	final CsvReadSettings mSettings = new CsvReadSettings() ;

	private Charset charset = null;

	private boolean useCustomRecordDelimiter = false;

	// this will be our working buffer to hold data chunks
	// read in from the data file

	private final DataBuffer dataBuffer = new DataBuffer();

	private final ColumnBuffer columnBuffer = new ColumnBuffer();

	private final RawRecordBuffer rawBuffer = new RawRecordBuffer();

	private String rawRecord = "";

	private HeadersHolder headersHolder = new HeadersHolder();

	// these are all more or less global loop variables
	// to keep from needing to pass them all into various
	// methods during parsing

	private boolean startedColumn = false;

	private boolean startedWithQualifier = false;

	private boolean hasMoreData = true;

	private char lastLetter = '\0';

	private boolean hasReadNextLine = false;

	private int columnsCount = 0;

	private long currentRecord = 0;

	private String[] values = new String[StaticSettings.INITIAL_COLUMN_COUNT];
	private boolean[] isQualified = new boolean[values.length];

	private boolean initialized = false;

	private int mState ;

	/**
	 * Creates a {@link com.cimstech.xfront.common.csv.CsvReader CsvReader} object using a file
	 * as the data source.
	 * 
	 * @param fileName
	 *            The path to the file to use as the data source.
	 * @param delimiter
	 *            The character to use as the column delimiter.
	 * @param charset
	 *            The {@link java.nio.charset.Charset Charset} to use while
	 *            parsing the data.
	 */
	public CsvReader(String fileName, char delimiter, Charset charset)
			throws FileNotFoundException {
		if (fileName == null) {
			throw new IllegalArgumentException(
					"Parameter fileName can not be null.");
		}

		if (charset == null) {
			throw new IllegalArgumentException(
					"Parameter charset can not be null.");
		}

		if (!new File(fileName).exists()) {
			throw new FileNotFoundException("File " + fileName
					+ " does not exist.");
		}

		this.fileName = fileName;
		mSettings.setDelimiter(delimiter);
		this.charset = charset;

		
	}

	/**
	 * Creates a {@link com.cimstech.xfront.common.csv.CsvReader CsvReader} object using a file
	 * as the data source.&nbsp;Uses ISO-8859-1 as the
	 * {@link java.nio.charset.Charset Charset}.
	 * 
	 * @param fileName
	 *            The path to the file to use as the data source.
	 * @param delimiter
	 *            The character to use as the column delimiter.
	 */
	public CsvReader(String fileName, char delimiter)
			throws FileNotFoundException {
		this(fileName, delimiter, Charset.forName("ISO-8859-1"));
	}

	/**
	 * Creates a {@link com.cimstech.xfront.common.csv.CsvReader CsvReader} object using a file
	 * as the data source.&nbsp;Uses a comma as the column delimiter and
	 * ISO-8859-1 as the {@link java.nio.charset.Charset Charset}.
	 * 
	 * @param fileName
	 *            The path to the file to use as the data source.
	 */
	public CsvReader(String fileName) throws FileNotFoundException {
		this(fileName, Letters.COMMA);
	}

	/**
	 * Constructs a {@link com.cimstech.xfront.common.csv.CsvReader CsvReader} object using a
	 * {@link java.io.Reader Reader} object as the data source.
	 * 
	 * @param inputStream
	 *            The stream to use as the data source.
	 * @param delimiter
	 *            The character to use as the column delimiter.
	 */
	public CsvReader(Reader inputStream, char delimiter) {
		if (inputStream == null) {
			throw new IllegalArgumentException(
					"Parameter inputStream can not be null.");
		}

		this.inputStream = inputStream;
		mSettings.setDelimiter(delimiter) ;
		initialized = true;
	}

	/**
	 * Constructs a {@link com.cimstech.xfront.common.csv.CsvReader CsvReader} object using a
	 * {@link java.io.Reader Reader} object as the data source.&nbsp;Uses a
	 * comma as the column delimiter.
	 * 
	 * @param inputStream
	 *            The stream to use as the data source.
	 */
	public CsvReader(Reader inputStream) {
		this(inputStream, Letters.COMMA);
	}
	
	public CsvReader()
	{
	}
	
	public CsvReader(CsvReadSettings aSettings)
	{
		if(aSettings != null)
			mSettings.updateFrom(aSettings) ;
	}

	/**
	 * Constructs a {@link com.cimstech.xfront.common.csv.CsvReader CsvReader} object using an
	 * {@link java.io.InputStream InputStream} object as the data source.
	 * 
	 * @param inputStream
	 *            The stream to use as the data source.
	 * @param delimiter
	 *            The character to use as the column delimiter.
	 * @param charset
	 *            The {@link java.nio.charset.Charset Charset} to use while
	 *            parsing the data.
	 */
	public CsvReader(InputStream inputStream, char delimiter, Charset charset) {
		this(new InputStreamReader(inputStream, charset), delimiter);
	}

	/**
	 * Constructs a {@link com.cimstech.xfront.common.csv.CsvReader CsvReader} object using an
	 * {@link java.io.InputStream InputStream} object as the data
	 * source.&nbsp;Uses a comma as the column delimiter.
	 * 
	 * @param inputStream
	 *            The stream to use as the data source.
	 * @param charset
	 *            The {@link java.nio.charset.Charset Charset} to use while
	 *            parsing the data.
	 */
	public CsvReader(InputStream inputStream, Charset charset) {
		this(new InputStreamReader(inputStream, charset));
	}
	
	public void reset(InputStream aInputStream , Charset aCharset)
	{
		this.charset = aCharset ;
		reset(new InputStreamReader(aInputStream, aCharset)) ;
	}
	
	public void reset(Reader aReader)
	{
		this.inputStream = aReader ;
		reset();
	}
	
	public void reset()
	{
		this.fileName = null;
		this.dataBuffer.clear();
		this.columnBuffer.clear() ;
		this.rawBuffer.clear();
		this.initialized = true ;
		this.hasMoreData = true;
		this.lastLetter = '\0';
		this.hasReadNextLine = false;
		this.currentRecord = 0 ;
		this.mState = sReady ;
	}

//	public boolean getCaptureRawRecord()
//	{
//		return mSettings.isCaptureRawRecord() ;
//	}
//
//	public void setCaptureRawRecord(boolean captureRawRecord)
//	{
//		mSettings.setCaptureRawRecord(captureRawRecord) ;
//	}

	public String getRawRecord() {
		return rawRecord;
	}

	/**
	 * Sets the character to use as the record delimiter.
	 * 
	 * @param recordDelimiter
	 *            The character to use as the record delimiter. Default is
	 *            combination of standard end of line characters for Windows,
	 *            Unix, or Mac.
	 */
	public void setRecordDelimiter(char recordDelimiter)
	{
		useCustomRecordDelimiter = true;
		mSettings.setRecordDelimiter(recordDelimiter) ;
	}
	
	public void setEscapeMode(EscapeMode aEscapeMode) throws IllegalArgumentException
	{
		if(aEscapeMode != null)
			mSettings.escapeMode = aEscapeMode ;
	}
	
	public void updateSettings(CsvReadSettings aSettings)
	{
		Assert.notNull(aSettings) ;
		setRecordDelimiter(aSettings.getRecordDelimiter()) ; 
		mSettings.setEscapeMode(aSettings.getEscapeMode()) ;
		mSettings.setAllowComments(aSettings.isAllowComments()) ;
		mSettings.setEscapeMode(aSettings.getEscapeMode()) ;
		mSettings.setNullLiteral(aSettings.getNullLiteral()) ;
		mSettings.setCaptureRawRecord(aSettings.isCaptureRawRecord()) ;
		mSettings.setComment(aSettings.getComment()) ;
		mSettings.setQuoteCharacter(aSettings.getQuoteCharacter()) ;
		mSettings.setSafetySwitch(aSettings.isSafetySwitch()) ;
		mSettings.setSkipEmptyRecords(aSettings.isSkipEmptyRecords());
		mSettings.setTrimWhitespace(aSettings.trimWhitespace) ;
		mSettings.setUseTextQualifier(aSettings.isUseTextQualifier()) ;
		mSettings.setDelimiter(aSettings.getDelimiter()) ;
	}

	/**
	 * Gets the count of columns found in this record.
	 * 
	 * @return The count of columns found in this record.
	 */
	public int getColumnCount() {
		return columnsCount;
	}

	/**
	 * Gets the index of the current record.
	 * 
	 * @return The index of the current record.
	 */
	public long getCurrentLineNumber() {
		return currentRecord - 1;
	}

	/**
	 * Gets the count of headers read in by a previous call to
	 * {@link com.cimstech.xfront.common.csv.CsvReader#readHeaders readHeaders()}.
	 * 
	 * @return The count of headers read in by a previous call to
	 *         {@link com.cimstech.xfront.common.csv.CsvReader#readHeaders readHeaders()}.
	 */
	public int getHeaderCount() {
		return headersHolder.Length;
	}

	/**
	 * Returns the header values as a string array.
	 * 
	 * @return The header values as a String array.
	 * @exception IOException
	 *                Thrown if this object has already been closed.
	 */
	public String[] getHeaders() throws IOException {
		checkForGet();

		if (headersHolder.Headers == null) {
			return null;
		} else {
			// use clone here to prevent the outside code from
			// setting values on the array directly, which would
			// throw off the index lookup based on header name
			String[] clone = new String[headersHolder.Length];
			System.arraycopy(headersHolder.Headers, 0, clone, 0,
					headersHolder.Length);
			return clone;
		}
	}

	public void setHeaders(String[] headers) {
		headersHolder.Headers = headers;

		headersHolder.mIndexByName.clear();

		if (headers != null) {
			headersHolder.Length = headers.length;
		} else {
			headersHolder.Length = 0;
		}

		// use headersHolder.Length here in case headers is null
		for (int i = 0; i < headersHolder.Length; i++) {
			headersHolder.mIndexByName.put(headers[i], i);
		}
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String[] getValues() throws IOException
	{
		checkForGet();

		// need to return a clone, and can't use clone because values.Length
		// might be greater than columnsCount
		String[] clone = new String[columnsCount];
		System.arraycopy(values, 0, clone, 0, columnsCount);
		return clone;
	}

	/**
	 * Returns the current column value for a given column index.
	 * 
	 * @param columnIndex
	 *            The index of the column.
	 * @return The current column value.无此列将返回null
	 */
	public String get(int columnIndex)
	{
		checkForGet();
		return columnIndex > -1 && columnIndex < columnsCount ? values[columnIndex] : null;
	}

	/**
	 * Returns the current column value for a given column header name.
	 * 
	 * @param headerName
	 *            The header name of the column.
	 * @return The current column value.无此列将返回null
	 */
	public String get(String headerName)
	{
		checkForGet();
		return get(headersHolder.mIndexByName.get(headerName));
	}

	/**
	 * Creates a {@link com.cimstech.xfront.common.csv.CsvReader CsvReader} object using a string
	 * of data as the source.&nbsp;Uses ISO-8859-1 as the
	 * {@link java.nio.charset.Charset Charset}.
	 * 
	 * @param data
	 *            The String of data to use as the source.
	 * @return A {@link com.cimstech.xfront.common.csv.CsvReader CsvReader} object using the
	 *         String of data as the source.
	 */
	public static CsvReader parse(String data) {
		if (data == null) {
			throw new IllegalArgumentException(
					"Parameter data can not be null.");
		}

		return new CsvReader(new StringReader(data));
	}

	/**
	 * 是否还有没读完的数据
	 * @return
	 */
	public boolean hasMore()
	{
		return hasMoreData ;
	}
	
	/**
	 * Reads another record.
	 * 
	 * @return Whether another record was successfully read or not.
	 * @exception IOException
	 *                Thrown if an error occurs while reading data from the
	 *                source stream.
	 */
	public boolean readRecord() throws IOException {
		checkClosed();

		columnsCount = 0;
		rawBuffer.Position = 0;

		dataBuffer.LineStart = dataBuffer.Position;

		hasReadNextLine = false;

		// check to see if we've already found the end of data

		if (hasMoreData) {
			// loop over the data stream until the end of data is found
			// or the end of the record is found

			do {
				if (dataBuffer.Position == dataBuffer.Count) {
					checkDataLength();
				} else {
					startedWithQualifier = false;

					// grab the current letter as a char

					char currentLetter = dataBuffer.Buffer[dataBuffer.Position];

					if (mSettings.isUseTextQualifier()
							&& currentLetter == mSettings.getQuoteCharacter()) {
						// this will be a text qualified column, so
						// we need to set startedWithQualifier to make it
						// enter the seperate branch to handle text
						// qualified columns

						lastLetter = currentLetter;

						// read qualified
						startedColumn = true;
						dataBuffer.ColumnStart = dataBuffer.Position + 1;
						startedWithQualifier = true;
						boolean lastLetterWasQualifier = false;

						char escapeChar = mSettings.getQuoteCharacter() ;

						if (mSettings.getEscapeMode() == EscapeMode.BACKSLASH)
							escapeChar = Letters.BACKSLASH;

						boolean eatingTrailingJunk = false;
						boolean lastLetterWasEscape = false;
						boolean readingComplexEscape = false;
						int escape = ComplexEscape.UNICODE;
						int escapeLength = 0;
						char escapeValue = (char) 0;

						dataBuffer.Position++;

						do {
							if (dataBuffer.Position == dataBuffer.Count) {
								checkDataLength();
							} else {
								// grab the current letter as a char

								currentLetter = dataBuffer.Buffer[dataBuffer.Position];

								if (eatingTrailingJunk) {
									dataBuffer.ColumnStart = dataBuffer.Position + 1;

									if (currentLetter == mSettings.getDelimiter())
									{
										endColumn();
									}
									else if ((!useCustomRecordDelimiter && (currentLetter == Letters.CR || currentLetter == Letters.LF))
											|| (useCustomRecordDelimiter && currentLetter == mSettings.getRecordDelimiter()))
									{
										endColumn();

										endRecord();
									}
								} else if (readingComplexEscape) {
									escapeLength++;

									switch (escape) {
									case ComplexEscape.UNICODE:
										escapeValue *= (char) 16;
										escapeValue += hexToDec(currentLetter);

										if (escapeLength == 4) {
											readingComplexEscape = false;
										}

										break;
									case ComplexEscape.OCTAL:
										escapeValue *= (char) 8;
										escapeValue += (char) (currentLetter - '0');

										if (escapeLength == 3) {
											readingComplexEscape = false;
										}

										break;
									case ComplexEscape.DECIMAL:
										escapeValue *= (char) 10;
										escapeValue += (char) (currentLetter - '0');

										if (escapeLength == 3) {
											readingComplexEscape = false;
										}

										break;
									case ComplexEscape.HEX:
										escapeValue *= (char) 16;
										escapeValue += hexToDec(currentLetter);

										if (escapeLength == 2) {
											readingComplexEscape = false;
										}

										break;
									}

									if (!readingComplexEscape) {
										appendLetter(escapeValue);
									} else {
										dataBuffer.ColumnStart = dataBuffer.Position + 1;
									}
								} else if (currentLetter == mSettings.getQuoteCharacter())
								{
									if (lastLetterWasEscape) {
										lastLetterWasEscape = false;
										lastLetterWasQualifier = false;
									} else {
										updateCurrentValue();

										if (mSettings.getEscapeMode() == EscapeMode.DOUBLED)
										{
											lastLetterWasEscape = true;
										}

										lastLetterWasQualifier = true;
									}
								} else if (mSettings.getEscapeMode() == EscapeMode.BACKSLASH
										&& lastLetterWasEscape) {
									switch (currentLetter) {
									case 'n':
										appendLetter(Letters.LF);
										break;
									case 'r':
										appendLetter(Letters.CR);
										break;
									case 't':
										appendLetter(Letters.TAB);
										break;
									case 'b':
										appendLetter(Letters.BACKSPACE);
										break;
									case 'f':
										appendLetter(Letters.FORM_FEED);
										break;
									case 'e':
										appendLetter(Letters.ESCAPE);
										break;
									case 'v':
										appendLetter(Letters.VERTICAL_TAB);
										break;
									case 'a':
										appendLetter(Letters.ALERT);
										break;
									case '0':
									case '1':
									case '2':
									case '3':
									case '4':
									case '5':
									case '6':
									case '7':
										escape = ComplexEscape.OCTAL;
										readingComplexEscape = true;
										escapeLength = 1;
										escapeValue = (char) (currentLetter - '0');
										dataBuffer.ColumnStart = dataBuffer.Position + 1;
										break;
									case 'u':
									case 'x':
									case 'o':
									case 'd':
									case 'U':
									case 'X':
									case 'O':
									case 'D':
										switch (currentLetter) {
										case 'u':
										case 'U':
											escape = ComplexEscape.UNICODE;
											break;
										case 'x':
										case 'X':
											escape = ComplexEscape.HEX;
											break;
										case 'o':
										case 'O':
											escape = ComplexEscape.OCTAL;
											break;
										case 'd':
										case 'D':
											escape = ComplexEscape.DECIMAL;
											break;
										}

										readingComplexEscape = true;
										escapeLength = 0;
										escapeValue = (char) 0;
										dataBuffer.ColumnStart = dataBuffer.Position + 1;

										break;
									default:
										break;
									}

									lastLetterWasEscape = false;

									// can only happen for ESCAPE_MODE_BACKSLASH
								} else if (currentLetter == escapeChar) {
									updateCurrentValue();
									lastLetterWasEscape = true;
								} else {
									if (lastLetterWasQualifier) {
										if (currentLetter == mSettings.getDelimiter())
										{
											endColumn();
										} else if ((!useCustomRecordDelimiter && (currentLetter == Letters.CR || currentLetter == Letters.LF))
												|| (useCustomRecordDelimiter && currentLetter == mSettings.getRecordDelimiter()))
										{
											endColumn();

											endRecord();
										} else {
											dataBuffer.ColumnStart = dataBuffer.Position + 1;

											eatingTrailingJunk = true;
										}

										// make sure to clear the flag for next
										// run of the loop

										lastLetterWasQualifier = false;
									}
								}

								// keep track of the last letter because we need
								// it for several key decisions

								lastLetter = currentLetter;

								if (startedColumn) {
									dataBuffer.Position++;

									if (mSettings.isSafetySwitch()
											&& dataBuffer.Position
													- dataBuffer.ColumnStart
													+ columnBuffer.Position > 100000) {
										close();

										throw new IOException(
												"Maximum column length of 100,000 exceeded in column "
														+ NumberFormat
																.getIntegerInstance()
																.format(
																		columnsCount)
														+ " in record "
														+ NumberFormat
																.getIntegerInstance()
																.format(
																		currentRecord)
														+ ". Set the SafetySwitch property to false"
														+ " if you're expecting column lengths greater than 100,000 characters to"
														+ " avoid this error.");
									}
								}
							} // end else

						} while (hasMoreData && startedColumn);
					} else if (currentLetter == mSettings.getDelimiter())
					{
						// we encountered a column with no data, so
						// just send the end column

						lastLetter = currentLetter;

						endColumn();
					} else if (useCustomRecordDelimiter
							&& currentLetter == mSettings.getRecordDelimiter())
					{
						// this will skip blank lines
						if (startedColumn || columnsCount > 0
								|| !mSettings.skipEmptyRecords)
						{
							endColumn();

							endRecord();
						} else {
							dataBuffer.LineStart = dataBuffer.Position + 1;
						}

						lastLetter = currentLetter;
					} else if (!useCustomRecordDelimiter
							&& (currentLetter == Letters.CR || currentLetter == Letters.LF)) {
						// this will skip blank lines
						if (startedColumn
								|| columnsCount > 0
								|| (!mSettings.skipEmptyRecords && (currentLetter == Letters.CR || lastLetter != Letters.CR))) {
							endColumn();

							endRecord();
						} else {
							dataBuffer.LineStart = dataBuffer.Position + 1;
						}

						lastLetter = currentLetter;
					}
					else if (mSettings.isAllowComments() && columnsCount == 0
							&& currentLetter == mSettings.getComment())
					{
						// encountered a comment character at the beginning of
						// the line so just ignore the rest of the line

						lastLetter = currentLetter;

						skipLine();
					}
					else if (mSettings.isTrimWhitespace()
							&& (currentLetter == Letters.SPACE || currentLetter == Letters.TAB 
							|| currentLetter == 65279/*add by yyl.一个看不见不占位的字符*/))
					{
						// do nothing, this will trim leading whitespace
						// for both text qualified columns and non

						startedColumn = true;
						dataBuffer.ColumnStart = dataBuffer.Position + 1;
					} else {
						// since the letter wasn't a special letter, this
						// will be the first letter of our current column

						startedColumn = true;
						dataBuffer.ColumnStart = dataBuffer.Position;
						boolean lastLetterWasBackslash = false;
						boolean readingComplexEscape = false;
						int escape = ComplexEscape.UNICODE;
						int escapeLength = 0;
						char escapeValue = (char) 0;

						boolean firstLoop = true;

						do {
							if (!firstLoop
									&& dataBuffer.Position == dataBuffer.Count) {
								checkDataLength();
							} else {
								if (!firstLoop) {
									// grab the current letter as a char
									currentLetter = dataBuffer.Buffer[dataBuffer.Position];
								}

								if (!mSettings.useTextQualifier
										&& mSettings.getEscapeMode() == EscapeMode.BACKSLASH
										&& currentLetter == Letters.BACKSLASH) {
									if (lastLetterWasBackslash) {
										lastLetterWasBackslash = false;
									} else {
										updateCurrentValue();
										lastLetterWasBackslash = true;
									}
								} else if (readingComplexEscape) {
									escapeLength++;

									switch (escape) {
									case ComplexEscape.UNICODE:
										escapeValue *= (char) 16;
										escapeValue += hexToDec(currentLetter);

										if (escapeLength == 4) {
											readingComplexEscape = false;
										}

										break;
									case ComplexEscape.OCTAL:
										escapeValue *= (char) 8;
										escapeValue += (char) (currentLetter - '0');

										if (escapeLength == 3) {
											readingComplexEscape = false;
										}

										break;
									case ComplexEscape.DECIMAL:
										escapeValue *= (char) 10;
										escapeValue += (char) (currentLetter - '0');

										if (escapeLength == 3) {
											readingComplexEscape = false;
										}

										break;
									case ComplexEscape.HEX:
										escapeValue *= (char) 16;
										escapeValue += hexToDec(currentLetter);

										if (escapeLength == 2) {
											readingComplexEscape = false;
										}

										break;
									}

									if (!readingComplexEscape) {
										appendLetter(escapeValue);
									} else {
										dataBuffer.ColumnStart = dataBuffer.Position + 1;
									}
								} else if (mSettings.getEscapeMode() == EscapeMode.BACKSLASH
										&& lastLetterWasBackslash) {
									switch (currentLetter) {
									case 'n':
										appendLetter(Letters.LF);
										break;
									case 'r':
										appendLetter(Letters.CR);
										break;
									case 't':
										appendLetter(Letters.TAB);
										break;
									case 'b':
										appendLetter(Letters.BACKSPACE);
										break;
									case 'f':
										appendLetter(Letters.FORM_FEED);
										break;
									case 'e':
										appendLetter(Letters.ESCAPE);
										break;
									case 'v':
										appendLetter(Letters.VERTICAL_TAB);
										break;
									case 'a':
										appendLetter(Letters.ALERT);
										break;
									case '0':
									case '1':
									case '2':
									case '3':
									case '4':
									case '5':
									case '6':
									case '7':
										escape = ComplexEscape.OCTAL;
										readingComplexEscape = true;
										escapeLength = 1;
										escapeValue = (char) (currentLetter - '0');
										dataBuffer.ColumnStart = dataBuffer.Position + 1;
										break;
									case 'u':
									case 'x':
									case 'o':
									case 'd':
									case 'U':
									case 'X':
									case 'O':
									case 'D':
										switch (currentLetter) {
										case 'u':
										case 'U':
											escape = ComplexEscape.UNICODE;
											break;
										case 'x':
										case 'X':
											escape = ComplexEscape.HEX;
											break;
										case 'o':
										case 'O':
											escape = ComplexEscape.OCTAL;
											break;
										case 'd':
										case 'D':
											escape = ComplexEscape.DECIMAL;
											break;
										}

										readingComplexEscape = true;
										escapeLength = 0;
										escapeValue = (char) 0;
										dataBuffer.ColumnStart = dataBuffer.Position + 1;

										break;
									default:
										break;
									}

									lastLetterWasBackslash = false;
								} else {
									if (currentLetter == mSettings.getDelimiter())
									{
										endColumn();
									}
									else if ((!useCustomRecordDelimiter && (currentLetter == Letters.CR || currentLetter == Letters.LF))
											|| (useCustomRecordDelimiter && currentLetter == mSettings.getRecordDelimiter()))
									{
										endColumn();

										endRecord();
									}
								}

								// keep track of the last letter because we need
								// it for several key decisions

								lastLetter = currentLetter;
								firstLoop = false;

								if (startedColumn) {
									dataBuffer.Position++;

									if (mSettings.isSafetySwitch()
											&& dataBuffer.Position
													- dataBuffer.ColumnStart
													+ columnBuffer.Position > 100000) {
										close();

										throw new IOException(
												"Maximum column length of 100,000 exceeded in column "
														+ NumberFormat
																.getIntegerInstance()
																.format(
																		columnsCount)
														+ " in record "
														+ NumberFormat
																.getIntegerInstance()
																.format(
																		currentRecord)
														+ ". Set the SafetySwitch property to false"
														+ " if you're expecting column lengths greater than 100,000 characters to"
														+ " avoid this error.");
									}
								}
							} // end else
						} while (hasMoreData && startedColumn);
					}

					if (hasMoreData) {
						dataBuffer.Position++;
					}
				} // end else
			} while (hasMoreData && !hasReadNextLine);

			// check to see if we hit the end of the file
			// without processing the current record

			if (startedColumn || lastLetter == mSettings.getDelimiter())
			{
				endColumn();

				endRecord();
			}
		}

		if (mSettings.captureRawRecord) {
			if (hasMoreData) {
				if (rawBuffer.Position == 0) {
					rawRecord = new String(dataBuffer.Buffer,
							dataBuffer.LineStart, dataBuffer.Position
									- dataBuffer.LineStart - 1);
				} else {
					rawRecord = new String(rawBuffer.Buffer, 0,
							rawBuffer.Position)
							+ new String(dataBuffer.Buffer,
									dataBuffer.LineStart, dataBuffer.Position
											- dataBuffer.LineStart - 1);
				}
			} else {
				// for hasMoreData to ever be false, all data would have had to
				// have been
				// copied to the raw buffer
				rawRecord = new String(rawBuffer.Buffer, 0, rawBuffer.Position);
			}
		} else {
			rawRecord = "";
		}

		return hasReadNextLine;
	}

	/**
	 * @exception IOException
	 *                Thrown if an error occurs while reading data from the
	 *                source stream.
	 */
	private void checkDataLength() throws IOException {
		if (!initialized) {
			if (fileName != null) {
				inputStream = new BufferedReader(new InputStreamReader(
						new FileInputStream(fileName), charset),
						StaticSettings.MAX_FILE_BUFFER_SIZE);
			}

			charset = null;
			initialized = true;
		}

		updateCurrentValue();

		if (mSettings.captureRawRecord && dataBuffer.Count > 0) {
			if (rawBuffer.Buffer.length - rawBuffer.Position < dataBuffer.Count
					- dataBuffer.LineStart) {
				int newLength = rawBuffer.Buffer.length
						+ Math.max(dataBuffer.Count - dataBuffer.LineStart,
								rawBuffer.Buffer.length);

				char[] holder = new char[newLength];

				System.arraycopy(rawBuffer.Buffer, 0, holder, 0,
						rawBuffer.Position);

				rawBuffer.Buffer = holder;
			}

			System.arraycopy(dataBuffer.Buffer, dataBuffer.LineStart,
					rawBuffer.Buffer, rawBuffer.Position, dataBuffer.Count
							- dataBuffer.LineStart);

			rawBuffer.Position += dataBuffer.Count - dataBuffer.LineStart;
		}

		try {
			dataBuffer.Count = inputStream.read(dataBuffer.Buffer, 0,
					dataBuffer.Buffer.length);
		} catch (IOException ex) {
			close();

			throw ex;
		}

		// if no more data could be found, set flag stating that
		// the end of the data was found

		if (dataBuffer.Count == -1) {
			hasMoreData = false;
		}

		dataBuffer.Position = 0;
		dataBuffer.LineStart = 0;
		dataBuffer.ColumnStart = 0;
	}

	/**
	 * Read the first record of data as column headers.
	 * 
	 * @return Whether the header record was successfully read or not.
	 * @exception IOException
	 *                Thrown if an error occurs while reading data from the
	 *                source stream.
	 */
	public boolean readHeaders() throws IOException {
		boolean result = readRecord();

		// copy the header data from the column array
		// to the header string array

		headersHolder.Length = columnsCount;

		headersHolder.Headers = new String[columnsCount];

		for (int i = 0; i < headersHolder.Length; i++) {
			String columnValue = get(i);

			headersHolder.Headers[i] = columnValue;

			// if there are duplicate header names, we will save the last one
			headersHolder.mIndexByName.put(columnValue, i);
		}

//		if (result) {
//			currentRecord--;
//		}
//
//		columnsCount = 0;

		return result;
	}
	
	/**
	 * 读取并返回CSV的所有列名
	 * @return
	 * @throws IOException
	 */
	public String[] nextHeaders() throws IOException
	{
		return readHeaders()?getHeaders():null ;
	}
	
	/**
	 * 没有更多数据时，返回null
	 * @return
	 * @throws IOException
	 */
	public String[] nextLine() throws IOException
	{
		return readRecord()?getValues():null ;
	}

	/**
	 * Returns the column header value for a given column index.
	 * 
	 * @param columnIndex
	 *            The index of the header column being requested.
	 * @return The value of the column header at the given column index.
	 * @exception IOException
	 *                Thrown if this object has already been closed.
	 */
	public String getHeader(int columnIndex) throws IOException {
		checkForGet();
		return columnIndex > -1 && columnIndex < headersHolder.Length
				?headersHolder.Headers[columnIndex]:null ;
	}

	public boolean isQualified(int columnIndex) throws IOException {
		checkForGet();

		if (columnIndex < columnsCount && columnIndex > -1) {
			return isQualified[columnIndex];
		} else {
			return false;
		}
	}

	/**
	 * @exception IOException
	 *                Thrown if a very rare extreme exception occurs during
	 *                parsing, normally resulting from improper data format.
	 */
	private void endColumn() throws IOException {
		String currentValue = "";

		// must be called before setting startedColumn = false
		if (startedColumn) {
			if (columnBuffer.Position == 0) {
				if (dataBuffer.ColumnStart < dataBuffer.Position) {
					int lastLetter = dataBuffer.Position - 1;

					if (mSettings.isTrimWhitespace() && !startedWithQualifier)
					{
						while (lastLetter >= dataBuffer.ColumnStart
								&& (dataBuffer.Buffer[lastLetter] == Letters.SPACE || dataBuffer.Buffer[lastLetter] == Letters.TAB)) {
							lastLetter--;
						}
					}

					currentValue = new String(dataBuffer.Buffer,
							dataBuffer.ColumnStart, lastLetter
									- dataBuffer.ColumnStart + 1);
				}
			} else {
				updateCurrentValue();

				int lastLetter = columnBuffer.Position - 1;

				if (mSettings.isTrimWhitespace() && !startedWithQualifier)
				{
					while (lastLetter >= 0
							&& (columnBuffer.Buffer[lastLetter] == Letters.SPACE || columnBuffer.Buffer[lastLetter] == Letters.SPACE)) {
						lastLetter--;
					}
				}

				currentValue = new String(columnBuffer.Buffer, 0,
						lastLetter + 1);
			}
		}

		columnBuffer.Position = 0;

		startedColumn = false;

		if (columnsCount >= 100000 && mSettings.isSafetySwitch()) {
			close();

			throw new IOException(
					"Maximum column count of 100,000 exceeded in record "
							+ NumberFormat.getIntegerInstance().format(
									currentRecord)
							+ ". Set the SafetySwitch property to false"
							+ " if you're expecting more than 100,000 columns per record to"
							+ " avoid this error.");
		}

		// check to see if our current holder array for
		// column chunks is still big enough to handle another
		// column chunk

		if (columnsCount == values.length) {
			// holder array needs to grow to be able to hold another column
			int newLength = values.length * 2;

			String[] holder = new String[newLength];

			System.arraycopy(values, 0, holder, 0, values.length);

			values = holder;

			boolean[] qualifiedHolder = new boolean[newLength];

			System.arraycopy(isQualified, 0, qualifiedHolder, 0,
					isQualified.length);

			isQualified = qualifiedHolder;
		}

		values[columnsCount] = Csv.isNull(currentValue)?null:currentValue;

		isQualified[columnsCount] = startedWithQualifier;

		currentValue = "";

		columnsCount++;
	}

	private void appendLetter(char letter) {
		if (columnBuffer.Position == columnBuffer.Buffer.length) {
			int newLength = columnBuffer.Buffer.length * 2;

			char[] holder = new char[newLength];

			System.arraycopy(columnBuffer.Buffer, 0, holder, 0,
					columnBuffer.Position);

			columnBuffer.Buffer = holder;
		}
		columnBuffer.Buffer[columnBuffer.Position++] = letter;
		dataBuffer.ColumnStart = dataBuffer.Position + 1;
	}

	private void updateCurrentValue() {
		if (startedColumn && dataBuffer.ColumnStart < dataBuffer.Position) {
			if (columnBuffer.Buffer.length - columnBuffer.Position < dataBuffer.Position
					- dataBuffer.ColumnStart) {
				int newLength = columnBuffer.Buffer.length
						+ Math.max(
								dataBuffer.Position - dataBuffer.ColumnStart,
								columnBuffer.Buffer.length);

				char[] holder = new char[newLength];

				System.arraycopy(columnBuffer.Buffer, 0, holder, 0,
						columnBuffer.Position);

				columnBuffer.Buffer = holder;
			}

			System.arraycopy(dataBuffer.Buffer, dataBuffer.ColumnStart,
					columnBuffer.Buffer, columnBuffer.Position,
					dataBuffer.Position - dataBuffer.ColumnStart);

			columnBuffer.Position += dataBuffer.Position
					- dataBuffer.ColumnStart;
		}

		dataBuffer.ColumnStart = dataBuffer.Position + 1;
	}

	/**
	 * @exception IOException
	 *                Thrown if an error occurs while reading data from the
	 *                source stream.
	 */
	private void endRecord() throws IOException {
		// this flag is used as a loop exit condition
		// during parsing

		hasReadNextLine = true;

		currentRecord++;
		mState = sReaded ;
	}

	/**
	 * Gets the corresponding column index for a given column header name.
	 * 
	 * @param headerName
	 *            The header name of the column.
	 * @return The column index for the given column header name.&nbsp;Returns
	 *         -1 if not found.
	 * @exception IOException
	 *                Thrown if this object has already been closed.
	 */
	public int getIndex(String headerName) throws IOException
	{
		checkForGet();
		return headersHolder.mIndexByName.get(headerName);
	}
	
	/**
	 * 跳过x行
	 * @param aLineAmount
	 * @return
	 * @throws IOException
	 */
	public int skip(final long aLineAmount) throws IOException
	{
		checkClosed();
		columnsCount = 0;
		int n = 0 ;
		for(; n<aLineAmount ; n++,currentRecord++)
		{
			boolean foundEol = false;

			do {
				if (dataBuffer.Position == dataBuffer.Count) {
					checkDataLength();
				} else {
					char currentLetter = dataBuffer.Buffer[dataBuffer.Position++];

					switch(currentLetter)
					{
					case Letters.CR:
						if(dataBuffer.Position == dataBuffer.Count && hasMoreData)
							checkDataLength();
						if(dataBuffer.Position<dataBuffer.Count)
						{
							char nextLetter = dataBuffer.Buffer[dataBuffer.Position] ;
							//\r\n
							if (nextLetter == Letters.LF)
							{
								dataBuffer.Position++;
								currentLetter = nextLetter ;
							}
						}
					case Letters.LF:
						foundEol = true;
						dataBuffer.LineStart = dataBuffer.Position ;
						break ;
					}

					// keep track of the last letter because we need
					// it for several key decisions

					lastLetter = currentLetter;

				} // end else
			} while (hasMoreData && !foundEol);
		}
		columnBuffer.Position = 0;
		rawBuffer.Position = 0;
		rawRecord = "";
		if(n>0)
			mState = sReady ;
		return n ;
	}

	/**
	 * Skips the next line of data using the standard end of line characters and
	 * does not do any column delimited parsing.
	 * 
	 * @return Whether a line was successfully skipped or not.
	 * @exception IOException
	 *                Thrown if an error occurs while reading data from the
	 *                source stream.
	 */
	private boolean skipLine() throws IOException {
		checkClosed();

		// clear public column values for current line

		columnsCount = 0;

		boolean skippedLine = false;

		if (hasMoreData) {
			boolean foundEol = false;

			do {
				if (dataBuffer.Position == dataBuffer.Count) {
					checkDataLength();
				} else {
					skippedLine = true;

					// grab the current letter as a char

					char currentLetter = dataBuffer.Buffer[dataBuffer.Position];

					if (currentLetter == Letters.CR
							|| currentLetter == Letters.LF) {
						foundEol = true;
						currentRecord++ ;
					}

					// keep track of the last letter because we need
					// it for several key decisions

					lastLetter = currentLetter;

					if (!foundEol) {
						dataBuffer.Position++;
					}

				} // end else
			} while (hasMoreData && !foundEol);

			columnBuffer.Position = 0;

			dataBuffer.LineStart = dataBuffer.Position + 1;
		}

		rawBuffer.Position = 0;
		rawRecord = "";
		return skippedLine;
	}

	/**
	 * Closes and releases all related resources.
	 */
	@Override
	public void close() {
		if (mState != sClosed) {
			close(true);
		}
	}

	/**
	 * 
	 */
	private void close(boolean closing)
	{
		if (closing) {
			charset = null;
			headersHolder.Headers = null;
			headersHolder.mIndexByName = null;
			dataBuffer.Buffer = null;
			columnBuffer.Buffer = null;
			rawBuffer.Buffer = null;
		}

		try {
			if (initialized) {
				inputStream.close();
			}
		} catch (Exception e) {
			// just eat the exception
		}

		inputStream = null;

		mState = sClosed ;
	}
	
	private void checkForGet()
	{
		Assert.isTrue(mState == sReaded , "尚未读取一行") ;
	}
	
	

	/**
	 * @exception IOException
	 *                Thrown if this object has already been closed.
	 */
	private void checkClosed() throws IOException {
		if (mState == sClosed) {
			throw new IOException(
					"This instance of the CsvReader class has already been closed.");
		}
	}

	/**
	 * 
	 */
	protected void finalize()
	{
		close(false);
	}

	private class ComplexEscape {
		private static final int UNICODE = 1;

		private static final int OCTAL = 2;

		private static final int DECIMAL = 3;

		private static final int HEX = 4;
	}

	private static char hexToDec(char hex) {
		char result;

		if (hex >= 'a') {
			result = (char) (hex - 'a' + 10);
		} else if (hex >= 'A') {
			result = (char) (hex - 'A' + 10);
		} else {
			result = (char) (hex - '0');
		}

		return result;
	}

	private class DataBuffer {
		public char[] Buffer;

		public int Position;

		// / <summary>
		// / How much usable data has been read into the stream,
		// / which will not always be as long as Buffer.Length.
		// / </summary>
		public int Count;

		// / <summary>
		// / The position of the cursor in the buffer when the
		// / current column was started or the last time data
		// / was moved out to the column buffer.
		// / </summary>
		public int ColumnStart;

		public int LineStart;

		public DataBuffer() {
			Buffer = new char[StaticSettings.MAX_BUFFER_SIZE];
			Position = 0;
			Count = 0;
			ColumnStart = 0;
			LineStart = 0;
		}
		
		void clear()
		{
			Position = 0;
			Count = 0;
			ColumnStart = 0;
			LineStart = 0;
		}
	}

	private class ColumnBuffer {
		public char[] Buffer;

		public int Position;

		public ColumnBuffer() {
			Buffer = new char[StaticSettings.INITIAL_COLUMN_BUFFER_SIZE];
			Position = 0;
		}
		
		void clear()
		{
			Position = 0 ;
		}
	}

	private static class RawRecordBuffer {
		public char[] Buffer;

		public int Position;

		public RawRecordBuffer() {
			Buffer = new char[StaticSettings.INITIAL_COLUMN_BUFFER_SIZE
					* StaticSettings.INITIAL_COLUMN_COUNT];
			Position = 0;
		}
		
		void clear()
		{
			Position = 0 ;
		}
	}

	private class HeadersHolder {
		public String[] Headers;

		public int Length;

		public TObjectIntMap<String> mIndexByName;

		public HeadersHolder() {
			Headers = null;
			Length = 0;
			mIndexByName = TObjectIntHashMap.create(-1) ;
		}
	}

	private class StaticSettings {
		// these are static instead of final so they can be changed in unit test
		// isn't visible outside this class and is only accessed once during
		// CsvReader construction
		public static final int MAX_BUFFER_SIZE = 1024_000 ;

		public static final int MAX_FILE_BUFFER_SIZE = 4 * 1024;

		public static final int INITIAL_COLUMN_COUNT = 10;

		public static final int INITIAL_COLUMN_BUFFER_SIZE = 50;
	}
	
	 public static CsvReader create(File aFile , String aEncoding) throws IOException
	    {
	    	return new CsvReader(FileUtils.openBufferedReader(aFile, aEncoding)) ;
	    }
}