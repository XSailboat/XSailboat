package team.sailboat.commons.fan.http;

import java.io.DataOutputStream;
import java.io.IOException;

import team.sailboat.commons.fan.infc.EConsumer;

public class MultiPartStream implements EConsumer<DataOutputStream , IOException> , IMultiPartConst
{
	
	EntityPart[] mParts ;
	
	
	public MultiPartStream(EntityPart[] aParts)
	{
		mParts = aParts ;
	}

	@Override
	public void accept(DataOutputStream aOuts) throws IOException
	{
		for(EntityPart part : mParts)
		{
			part.accept(aOuts);
		}
		writeBoundaryEnd(aOuts) ;
		aOuts.flush() ;
	}
}
