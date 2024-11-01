package team.sailboat.commons.fan.csv;

public interface Csv
{
	
	public static class Letters
	{
		public static final char LF = '\n';

		public static final char CR = '\r';

		public static final char QUOTE = '"';

		/**
		 * 逗号
		 */
		public static final char COMMA = ',';

		public static final char SPACE = ' ';

		public static final char TAB = '\t';

		public static final char POUND = '#';

		public static final char BACKSLASH = '\\';

		public static final char NULL = '\0';
		
		public static final char BACKSPACE = '\b';

		public static final char FORM_FEED = '\f';

		public static final char ESCAPE = '\u001B'; // ASCII/ANSI escape

		public static final char VERTICAL_TAB = '\u000B';

		public static final char ALERT = '\u0007';
	}
	
	public static boolean isNull(String aText)
	{
		return aText == null || (aText.length() == 1 && aText.charAt(0) == Letters.NULL) ;
	}
}
