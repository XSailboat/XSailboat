package team.sailboat.bd.base.model;

public interface IDevNode extends INode
{
	Position getPosition() ;
	
	boolean setPosition(Position aPosition) ;
	
	boolean setPosition(double aX , double aY) ;
	
	/**
	 * 如果已经提交了，将返回提交的节点版本（即，lastEditTime）
	 * 如果没有提交过，将返回0
	 * @return
	 */
	long getCommittedVersion() ;
	boolean setCommittedVersion(long aVersion) ;
	
	/**
	 * 如果当前的版本（lastEditTime）和 committedVersion一样，那么就返回false
	 * @return
	 */
	default boolean canCommit()
	{
		long commitVersion = getCommittedVersion() ;
		return commitVersion == 0?true:getVersion()>commitVersion ;
	}
}
