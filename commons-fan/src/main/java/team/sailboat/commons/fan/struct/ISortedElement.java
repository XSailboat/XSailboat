package team.sailboat.commons.fan.struct;

import java.beans.PropertyChangeListener;

public interface ISortedElement<T>
{
	void setPropertyChangeListener(PropertyChangeListener aLsn) ;
	T getValue() ;
	int getIndex() ;
	void setIndex(int aIndex) ;
}
