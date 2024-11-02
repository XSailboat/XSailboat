import ipaddress
import json
import re
from fastapi import Request
from loguru import logger
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.responses import JSONResponse

from team.sailboat.py.installer.api_func.iptables_func import ip_parsing
from team.sailboat.py.installer.common.app_storage import AppStorage

pattern = re.compile(r'File "([^"]*)", line (\d+),')


class HTTPDisableLogHandlerMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        # 要屏蔽输出日志的路径
        paths = ["/app/name"]
        if request.url.path in paths:
            return await call_next(request)
        else:
            response = await call_next(request)
            return response


# 协助存储POST请求体的中间件
class RequestBodyMiddleware(BaseHTTPMiddleware):

    async def dispatch(self, request: Request, call_next):
        # 尝试读取请求体
        if not hasattr(request, 'body_content'):
            body = await request.body()  # 读取原始字节流
            try:
                request.body_content = json.loads(body)  # 尝试解析为 JSON
            except json.JSONDecodeError:
                request.body_content = body.decode()  # 如果不是 JSON，则将其解码为字符串
                print("body", body.decode())

        # 继续处理请求
        response = await call_next(request)
        return response


def is_ip_in_range(ip, start_ip, end_ip):
    """
    检查给定的IP地址是否位于指定的起始和结束IP范围内。

    :param ip: 待检查的IP地址字符串
    :param start_ip: 起始IP地址字符串
    :param end_ip: 结束IP地址字符串
    :return: 如果IP地址位于指定范围内，则返回True，否则返回False
    """
    # 将IP地址转换为整数
    ip_int = int(ipaddress.IPv4Address(ip))
    start_ip_int = int(ipaddress.IPv4Address(start_ip))
    end_ip_int = int(ipaddress.IPv4Address(end_ip))

    # 检查IP地址是否在指定的范围内
    return start_ip_int <= ip_int <= end_ip_int

app_storage = AppStorage()
# 请求拦截,只允许特定的IP访问接口
class IPFilterMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        client_ip = request.client.host
        for ip in app_storage["allowed_ips"]:
            if ip is not None or ip!="":
                ip = ip_parsing(ip)
                if "-" in ip:  # 判断是否在ip范围内
                    range = ip.split("-")
                    if (is_ip_in_range(client_ip, range[0], range[1])):
                        response = await call_next(request)
                        return response
                elif client_ip == ip:
                    response = await call_next(request)
                    return response

        logger.info(f"拦截了未允许的{client_ip}访问!")
        return JSONResponse(status_code=403, content={"detail": "Forbidden"})
