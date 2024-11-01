package team.sailboat.commons.fan.statestore;

public interface IStateStoreBuilder
{
	
	IStateStore build(String aDomainName) throws Exception ;
	
	public static interface IRDBStateStoreBuilder extends IStateStoreBuilder
	{
		IRDBStateStoreBuilder connUrl(String aConnUrl) ;
		
		IRDBStateStoreBuilder username(String aUsername) ;
		
		IRDBStateStoreBuilder password(String aPassword) ;
		
		IRDBStateStoreBuilder tableName(String aTableName) ;
	}
}
