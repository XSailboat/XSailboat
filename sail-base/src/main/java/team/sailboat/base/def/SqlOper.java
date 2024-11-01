package team.sailboat.base.def;

public enum SqlOper
{
	Equal("等于" , "=") ,
	NotEqual("<>" , "<>") ,
	Like("LIKE" , "LIKE") ,
	NotLike("NOT LIKE" , "NOT LIKE") ,
	In("IN" , "IN") ,
	NotIn("NOT IN" , "NOT IN") ,
	LargeThan(">" , ">") ,
	LargeOrEqualThan(">=" , ">=") ,
	LessThan("<" , "<") ,
	LessOrEqualThan("<=" , "<=") 
	
	;
	
	String mOperator ;
	
	String mDisplayName ;
	
	private SqlOper(String aDisplayName , String aOperator)
	{
		mOperator = aOperator ;
		mDisplayName = aDisplayName ;
	}
	
	public String getOperator()
	{
		return mOperator;
	}
	
	public String getDisplayName()
	{
		return mDisplayName;
	}
}
