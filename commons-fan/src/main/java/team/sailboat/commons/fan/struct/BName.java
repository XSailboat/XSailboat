package team.sailboat.commons.fan.struct;

public class BName extends FullName
{
	static final String sJoin = "." ;
	
	String mPrefix ;
	
	String mLocalName ;
	
	String mName ;
	
	public BName()
	{
		super(sJoin) ;
	}
	
	public BName(String aPrefix , String aLocalName)
	{
		super(sJoin , aPrefix, aLocalName) ;
	}
	
	public BName(String aFullName)
	{
		super(sJoin, aFullName) ;
	}
	
	@Override
	protected BName newInstance(String aPrefix, String aLocalName)
	{
		return new BName(aPrefix , aLocalName) ;
	}
	
	@Override
	public BName prefix(String aPrefix)
	{
		return (BName) super.prefix(aPrefix);
	}
}
