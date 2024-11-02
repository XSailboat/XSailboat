package team.sailboat.bd.base.hbase ;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.util.Bytes;

import team.sailboat.bd.base.BdConst;
import team.sailboat.commons.fan.lang.XClassUtil;
import team.sailboat.commons.fan.struct.Bits;

public class HBaseDataTypeSupporter implements Function<Cell, String>
{	
	
	NavigableMap<byte[] , Function<Cell, String>> mDataTypeMap ;
	
	final Bits mBits = new Bits() ;
	
	public HBaseDataTypeSupporter(TableDescriptor aTblDesc)
	{
		for(Entry<Bytes, Bytes> entry : aTblDesc.getValues().entrySet())
		{
			String key = entry.getKey().toString() ;
			if(key.startsWith(BdConst.sHBase_TVK_colDataType))
			{
				if(mDataTypeMap == null)
					mDataTypeMap = new TreeMap<>(Bytes.BYTES_COMPARATOR) ;
				Function<Cell, String> func = null ;
				String dataType = entry.getValue().toString() ;
				switch(dataType)
				{
				case XClassUtil.sCSN_Long:
					func = (cell)->Long.toString(Bytes.toLong(cell.getValueArray() , cell.getValueOffset(), cell.getValueLength())) ;
					break ;
				case XClassUtil.sCSN_Double:
					func = (cell)->Double.toString(Bytes.toDouble(cell.getValueArray() , cell.getValueOffset())) ;
					break ;
				default:
					throw new IllegalStateException("未支持的类型："+dataType) ;
				}
				byte[] col = Bytes.toBytes(key.substring(BdConst.sHBase_TVK_colDataType.length()))  ;
				mDataTypeMap.put(col , func) ;
				mBits.set(col.length, true) ;
			}
		}
	}
	
	@Override
	public String apply(Cell aCell)
	{
		if(aCell == null)
			return null ;
		if(mDataTypeMap != null)
		{
			int byte_len = aCell.getFamilyLength() + 1 + aCell.getQualifierLength() ;
			if(mBits.get(byte_len))
			{
				byte[] array = new byte[byte_len] ;
				System.arraycopy(aCell.getFamilyArray(), aCell.getFamilyOffset() , array, 0, aCell.getFamilyLength()) ;
				array[aCell.getFamilyLength()] = ':' ;
				System.arraycopy(aCell.getQualifierArray(), aCell.getQualifierOffset() , array, aCell.getFamilyLength()+1
						, aCell.getQualifierLength()) ;
				Function<Cell , String> func = mDataTypeMap.get(array) ;
				if(func != null)
					return func.apply(aCell) ;
			}
		}
		return Bytes.toString(aCell.getValueArray() , aCell.getValueOffset() , aCell.getValueLength()) ;
	}
}
