package team.sailboat.commons.ms.infc;

public interface IUserSupport extends ICreateUserSupport
{
	
	String getLastEditUserId() ;
	
	String getLastEditUserDisplayName() ;
	void setLastEditUserDisplayName(String aDisplayName) ;
}
