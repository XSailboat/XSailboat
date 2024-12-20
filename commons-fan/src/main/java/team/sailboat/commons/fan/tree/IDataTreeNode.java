package team.sailboat.commons.fan.tree;

import java.util.function.BiConsumer;

public interface IDataTreeNode extends ITreeNode
{
	Object getData(String aKey) ;
	
	void setData(String aKey , Object aData) ;
	
	Object removeData(String aKey) ;

	int getDataEntryAmount() ;
	
	void forEachDataEntry(BiConsumer<String , Object> aBiConsumer) ;
}
