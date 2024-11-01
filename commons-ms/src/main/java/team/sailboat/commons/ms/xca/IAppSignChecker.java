package team.sailboat.commons.ms.xca;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import team.sailboat.commons.fan.excep.HttpException;

public interface IAppSignChecker
{
	public AppCertificate check(HttpServletRequest aReq) throws HttpException , IOException ;
}
