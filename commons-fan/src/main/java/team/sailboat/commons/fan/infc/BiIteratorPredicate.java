package team.sailboat.commons.fan.infc;

@FunctionalInterface
public interface BiIteratorPredicate<K , V> extends IterateOpCode
{
	
	/**
	 * 
	 * @param aEle
	 * @return			0表示继续，1表示删除，2表示中断，3表示删除当前的且中断遍历
	 */
	int visit(K aKey , V aVal) ;
}
