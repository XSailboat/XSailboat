package team.sailboat.commons.fan.http;

import java.io.DataOutputStream;
import java.io.OutputStream;

public interface IDecoder
{
	
	String decode(String aText) ;
	
	DataOutputStream wrap(OutputStream aOuts) ;
	
	int decode(int aB) ;
}
