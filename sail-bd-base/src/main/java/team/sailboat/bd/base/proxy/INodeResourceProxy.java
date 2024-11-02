package team.sailboat.bd.base.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface INodeResourceProxy
{

	InputStream openDataReader(String aNodeId) throws IOException ;
	
	OutputStream openDataWriter(String aNodeId) throws IOException ;
	
	String getPath(String aNodeId) throws IOException ;
	
	void delete(String aNodeId) throws IOException ;
}
