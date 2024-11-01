package team.sailboat.aviator;

import com.googlecode.aviator.runtime.function.AbstractFunction;

import team.sailboat.commons.fan.collection.XC;

public abstract class WedgeFunction extends AbstractFunction
{

	private static final long serialVersionUID = 1L;
	
	protected IWedge[] wedges ;
	
	
	public void addWedge(IWedge aWedge)
	{
		if(wedges == null)
			wedges = new IWedge[] {aWedge} ;
		else
		{
			if(XC.findFirstIndex(wedges , wedge->wedge.getId().equals(aWedge.getId()) , 0) != -1)
				return ;
			wedges = XC.merge(wedges , aWedge) ;
		}
	}
	
	public IWedge[] getWedges()
	{
		return wedges;
	}

}
