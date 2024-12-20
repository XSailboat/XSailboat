package team.sailboat.ms.crane.bench;

import team.sailboat.commons.fan.collection.XC;
import team.sailboat.commons.fan.excep.ExceptionAssist;
import team.sailboat.commons.fan.http.HttpClient;
import team.sailboat.commons.fan.http.Request;
import team.sailboat.commons.fan.json.JSONArray;
import team.sailboat.commons.fan.json.JSONObject;
import team.sailboat.commons.fan.text.XString;
import team.sailboat.commons.ms.log.LogPool;
import team.sailboat.ms.crane.IApis_PyInstaller;
import team.sailboat.ms.crane.bean.HostProfile;
import team.sailboat.ms.crane.cmd.ICmd;
import team.sailboat.ms.crane.cmd.LocalCmds;
import team.sailboat.ms.crane.cmd.RestCmd;

/**
 * 
 * 执行命令的操作者
 *
 * @author yyl
 * @since 2024年9月20日
 */
public class Operator_Cmds extends Operator_RemoteHost
{
	
	final String[] mCommands ;
	boolean mFinished = false ;
	
	public Operator_Cmds(LogPool aLogPool
			, String aOperationName
			, HostProfile aHostProfile 
			, String[] aCommands)
	{
		super(aLogPool, aHostProfile , aOperationName) ;
		mCommands = aCommands ;
	}

	@Override
	protected boolean doCmds()
	{
		HttpClient httpClient = HttpClient.of(mHostProfile.getIp() , mHostProfile.getSailPyInstallerPort()) ;
		int startCmdIndex = 0 ;
		try
		{
			int i = 0 ;
			boolean success = true ;
			for(; i<mCommands.length ; i++)
			{
				String cmdName = IOperator.getCmdName(mCommands[i]) ;
				if(LocalCmds.support(cmdName))
				{
					// 先执行之前的远程命令
					if(i>startCmdIndex)
					{
						success = doRemoteCommands(httpClient, XC.copyRange(mCommands , startCmdIndex , i)) ;
						if(!success)
							return false ;
					}
					
					ICmd cmd = LocalCmds.getCmd(cmdName) ;
					if(cmd instanceof RestCmd)
						((RestCmd)cmd).setRestClient(httpClient) ;
					cmd.accept(IOperator.getCmdArgs(mCommands[i]));
					
					startCmdIndex = i+1 ;
				}
			}
			if(startCmdIndex < i)
			{
				success = doRemoteCommands(httpClient, XC.copyRange(mCommands , startCmdIndex , i)) ;
			}
			return success ;
		}
		catch(Exception e)
		{
			logError("命令[{}]执行出现异常。异常消息：{}" , mOperationName , e.getMessage()) ;
			mLogger.error(ExceptionAssist.getStackTrace(e)) ;
			return false ;
		}
	}
	
	@Override
	public boolean isFinished()
	{
		return mFinished ;
	}
	
	boolean doRemoteCommands(HttpClient aHttpClient , String[] aCommands) throws Exception
	{
		JSONArray ja = aHttpClient.askJa(Request.POST().path(IApis_PyInstaller.sPOST_ExecCommand)
				.setJsonEntity(JSONObject.one().put("commands" , aCommands)))  ;
		final int len = ja.size() ;
		boolean success = true ;
		for(int i=0 ; i<len ; i++)
		{
			String result = ja.optString(i) ;
			if(XString.isEmpty(result))
				logInfo("命令执行成功。命令：{}" , aCommands[i]) ;
			else
			{
				logError("命令执行失败。命令：{} 。原因：{}", aCommands[i] , result) ;
				success = false ;
			}
		}
		return success ;
	}
}
