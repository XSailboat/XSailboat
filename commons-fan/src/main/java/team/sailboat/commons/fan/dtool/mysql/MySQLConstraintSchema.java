package team.sailboat.commons.fan.dtool.mysql;

import team.sailboat.commons.fan.dtool.ConstraintSchema;
import team.sailboat.commons.fan.dtool.DBHelper;
import team.sailboat.commons.fan.text.XString;

public class MySQLConstraintSchema extends ConstraintSchema
{
	public static final String sV_PRIMARY = "PRIMARY" ;
	
	public static final String sPK_REFERENCED_COLUMN_NAME = "REFERENCED_COLUMN_NAME" ;
	public static final String sPK_REFERENCED_TABLE_SCHEMA = "REFERENCED_TABLE_SCHEMA" ;
	public static final String sPK_REFERENCED_TABLE_NAME = "REFERENCED_TABLE_NAME" ;

	public MySQLConstraintSchema()
	{
		super() ;
	}
	
	public MySQLConstraintSchema(String aName)
	{
		super(aName) ;
	}
	
	@Override
	public boolean isForeign()
	{
		return XString.isNotEmpty((String)getOtherProperty(sPK_REFERENCED_COLUMN_NAME)) ;
	}

	@Override
	public boolean isPrimary()
	{
		return sV_PRIMARY.equalsIgnoreCase(getName()) ;
	}

	@Override
	public boolean isUnique()
	{
		return !isPrimary() && !isForeign() ;
	}
	
	public String getReferenceText()
	{
		return new StringBuilder(DBHelper.getTableFullName((String)getOtherProperty(sPK_REFERENCED_TABLE_SCHEMA)
				, (String)getOtherProperty(sPK_REFERENCED_TABLE_NAME)))
				.append("(")
				.append(getOtherProperty(sPK_REFERENCED_COLUMN_NAME))
				.append(")")
				.toString() ;
	}
	
	@Override
	public String getSqlText()
	{
		if(isPrimary())
		{
			return new StringBuilder("PRIMARY KEY(")
					.append(XString.toString(",", getColumnNames()))
					.append(")").toString() ;
		}
		else if(isForeign())
		{
			return new StringBuilder("CONSTRAINT ")
					.append(getName())
					.append("FOREIGN KEY (")
					.append(XString.toString(",", getColumnNames()))
					.append(") REFERENCES ")
					.append(getReferenceText())
					.toString() ;
		}
		else if(isUnique())
		{
			if(getName() == null)
				return new StringBuilder("UNIQUE(")
						.append(XString.toString(",", getColumnNames()))
						.append(")").toString() ;
			else
				return new StringBuilder("CONSTRAINT ")
						.append(getName())
						.append("UNIQUE (")
						.append(XString.toString(",", getColumnNames()))
						.append(')').toString() ;
		}
		return null;
	}

	@Override
	public ConstraintSchema clone()
	{
		MySQLConstraintSchema clone = new MySQLConstraintSchema() ;
		initClone(clone) ;
		return clone ;
	}
	
	public static MySQLConstraintSchema createPrimary()
	{
		return new MySQLConstraintSchema(sV_PRIMARY) ;
	}

}
