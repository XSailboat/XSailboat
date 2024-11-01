package team.sailboat.base.metrics;

import team.sailboat.commons.fan.app.App;
import team.sailboat.commons.fan.lang.XClassUtil;

public interface IMetricsConst
{
	public static final  String sDefaultDBName = XClassUtil.convert(App.instance().getSysEnv() 
			, workEnv->"prod".equals(workEnv)?"":workEnv+"_")+"sys_metrics" ;
		 
	
	public static final String sDefaultTN_status = "status" ;
	
	public static final String sDefaultTN_mvalues = "mvalues" ;
	
	public static final String sDefaultTN_mmsgs = "mmsgs" ;
}
