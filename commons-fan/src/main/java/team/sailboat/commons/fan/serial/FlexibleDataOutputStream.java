package team.sailboat.commons.fan.serial;

import java.io.DataOutput;
import java.io.IOException;

import team.sailboat.commons.fan.struct.Bytes;

public abstract class FlexibleDataOutputStream extends FlexibleOutputStream
		implements DataOutput
{
	
	public final void writeBoolean(boolean v) throws IOException
	{
		write(v ? 1 : 0);
	}

	public final void writeByte(int aByte) throws IOException
	{
		write(aByte);
	}

	public final void writeShort(int v) throws IOException
	{
		Bytes.writeShort(this, v);
	}

	public final void writeChar(int v) throws IOException
	{
		write((v >>> 8) & 0xFF);
		write((v >>> 0) & 0xFF);
	}

	public final void writeInt(int v) throws IOException
	{
		Bytes.writeInt(this, v);
	}
	
	public final void writePackedInt(int v) throws IOException
	{
		int shift = (v & ~0x7F); //reuse variable
        if (shift != 0) {
            //$DELAY$
            shift = 31-Integer.numberOfLeadingZeros(v);
            shift -= shift%7; // round down to nearest multiple of 7
            while(shift!=0){
                write((((v>>>shift) & 0x7F) | 0x80));
                //$DELAY$
                shift-=7;
            }
        }
        //$DELAY$
        write((v & 0x7F));
	}

	public final void writeLong(long v) throws IOException
	{
		Bytes.writeLong(this, v);
	}

	public final void writeFloat(float v) throws IOException
	{
		Bytes.writeFloat(this, v);
	}

	public final void writeDouble(double v) throws IOException
	{
		writeLong(Double.doubleToLongBits(v));
	}

	public final void writeChars(String s) throws IOException
	{
		Bytes.writeChars(this , s);
	}

	/**
	 * 字符串用UTF-8编码长度不能超过65535
	 * @param aStr
	 * @throws IOException
	 */
	public final void writeUTF(String aStr) throws IOException
	{
		Bytes.writeUTF(this, aStr);
	}
	
	@Override
	public void writeBytes(String aS) throws IOException
	{
		int len = aS.length();
        for (int i = 0 ; i < len ; i++)
            write((byte)aS.charAt(i));
	}
}
