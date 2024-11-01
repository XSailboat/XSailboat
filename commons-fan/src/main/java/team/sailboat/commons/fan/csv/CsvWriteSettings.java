package team.sailboat.commons.fan.csv;

import team.sailboat.commons.fan.csv.Csv.Letters;

public class CsvWriteSettings
{
	// having these as publicly accessible members will prevent
	// the overhead of the method call that exists on properties
	public char textQualifier;

	public boolean useTextQualifier;

	public char delimiter ;

	public char recordDelimiter;

	public char comment;

	public EscapeMode escapeMode;

	public boolean forceQualifier;
	
	public boolean supportNULL ;

	public CsvWriteSettings()
	{
		textQualifier = Letters.QUOTE;
		useTextQualifier = true;
		delimiter = Letters.COMMA;
		recordDelimiter = '\n' ;
		comment = Letters.POUND;
		escapeMode = EscapeMode.DOUBLED ;
		forceQualifier = false;
		supportNULL = false ;
	}
	
	public CsvReadSettings toReadSettings()
	{
		CsvReadSettings settings = new CsvReadSettings() ;
		if(escapeMode != null)
			settings.escapeMode = escapeMode ;
		settings.comment = comment ;
		settings.delimiter = delimiter ;
		settings.useTextQualifier = forceQualifier ;
		if(!supportNULL)
			settings.nullLiteral = "" ;
		settings.recordDelimiter = recordDelimiter ;
		settings.quoteCharacter = textQualifier ;
		settings.useTextQualifier = useTextQualifier ;
		settings.allowComments = true ;
		return settings ;
	}
}
