package team.sailboat.commons.fan.eazi;

/**
 * 0xe* 以后的数允许子类自由使用，由开发者保证在使用场景不会出现重复
 *
 * @author yyl
 * @since 2017年8月24日
 */
public interface SerialConstants
{
	public static final String sKEY_ExtraSerialObjects = "##" ;
	public static final String sKEY_Version = "Version" ;
	public static final String sKEY_Head = "Head" ;
	public static final String sKEY_Body = "Body" ;
	
	final static byte TC_Byte = (byte)0x80 ;
	final static byte TC_Int = (byte)0x81 ;
	final static byte TC_Float = (byte)0x82 ;
	final static byte TC_Double = (byte)0x83 ;
	final static byte TC_Boolean = (byte)0x84 ;
	final static byte TC_Long = (byte)0x85 ;
	final static byte TC_Short = (byte)0x86 ;
//	final static byte TC_Handle = (byte)0x87 ;
	final static byte TC_Map = (byte)0x88 ;
	final static byte TC_JavaSerializable = (byte)0x89 ;
	final static byte TC_Set = (byte)0x8a ;
	final static byte TC_Char = (byte)0x90 ;
	final static byte TC_List = (byte)0x91 ;
	
	final static byte TC_ByteArray = (byte)0xa0 ;
	final static byte TC_IntArray = (byte)0xa1 ;
	final static byte TC_FloatArray = (byte)0xa2 ;
	final static byte TC_DoubleArray = (byte)0xa3 ;
	final static byte TC_BooleanArray = (byte)0xa4 ;
	final static byte TC_LongArray = (byte)0xa5 ;
	final static byte TC_ShortArray = (byte)0xa6 ;
	final static byte TC_CharArray = (byte)0xa7 ;
	final static byte TC_StringArray = (byte)0xa8 ;
	
	final static byte TC_StringInternArray = (byte)0xa9 ;
	
	final static byte TC_NULL = (byte)0x70;
	
	/**
     * Reference to an object already written into the stream.
     */
    final static byte TC_Reference =	(byte)0x71;
    
    /**
     * 在一个输出环境中避免大量String重复输出
     */
    final static byte TC_StringIntern = (byte)0x72 ;
	
    /**
     * new Object.
     */
    final static byte TC_Eazialiable = 	(byte)0x73;
    
    /** 
     * new String.
     */
    final static byte TC_String = 	(byte)0x74;
    
    /**
     * new Array.
     */
    final static byte TC_Array = 	(byte)0x75;
    
//    /**
//     * Reference to Class.
//     */
//    final static byte TC_CLASS = 	(byte)0x76;
    
    /**
     * Block of optional data. Byte following tag indicates number
     * of bytes in this block data.
     */
    final static byte TC_BlockData = 	(byte)0x77;
    
    /**
     * 类名，类型为String		<br>
     * 在写入类名以后，紧跟着需要写入TC_ClassId					<br>
     * 如果是引用先前已经用过的类，只需要写入TC_ClassId即可		<br>
     * 只有第一次写入某个类的时候才需要用TC_ClassName
     */
    final static byte TC_ClassName = (byte)0x78 ;
    
    /**
     * 类id，类型为short
     * 
     */
    final static byte TC_ClassId = (byte)0x79 ;
}
