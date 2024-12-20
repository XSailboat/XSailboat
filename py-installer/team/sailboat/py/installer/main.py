import argparse
import asyncio
import getpass
import json

from fastapi import FastAPI

# 1. 构建微服务
from loguru import logger

from team.sailboat.py.installer.api.app_core import app_core
from team.sailboat.py.installer.api.file_api import file_api
from team.sailboat.py.installer.api.iptables import iptables
from team.sailboat.py.installer.api.user_api import user_api
from team.sailboat.py.installer.common.app_storage import AppStorage
from team.sailboat.py.installer.common.app_util.app_path import AppPath
from team.sailboat.py.installer.common.app_util.app_sys_command import AppSysCmd
from team.sailboat.py.installer.common.app_variable import AppVariable
from team.sailboat.py.installer.common.handler.http_handler import IPFilterMiddleware
from team.sailboat.py.installer.custom_command import *  # 引入自定义命令包

app_storage = AppStorage()
app_variable = AppVariable()


def close_firewalld():
    username = getpass.getuser()
    if username == "root":
        result = AppSysCmd.cmd_run("systemctl stop firewalld && systemctl disable firewalld")
        if result.returncode == 0:
            logger.info(f"当前用户为:{username},自动关闭并禁用firewalld")


# 加载主机配置
def load_default():
    filepath = "../config/py_apps/py_installer/.hostProfile.json"
    if not os.path.exists(filepath):
        return
    with open(filepath, 'r', encoding='utf-8') as file:
        host_profile = json.load(file)
        app_storage["profile"] = host_profile
    app_variable.load_variable()
    logger.info("缓存主机配置加载成功!")


app = FastAPI(
    title="SailPyInstaller",
    version="1.0.0",
    description="Sailboat的一键安装微服务"
)
# 注册中间件
app.add_middleware(IPFilterMiddleware)
# 添加路由
app.include_router(router=iptables, prefix="/iptables", tags=["防火墙相关接口"])
app.include_router(router=app_core, prefix="/core", tags=["系统核心接口"])
app.include_router(router=user_api, prefix="/user", tags=["用户相关接口"])
app.include_router(router=file_api, prefix="/file", tags=["文件相关接口"])
close_firewalld()
load_default()
if __name__ == '__main__':
    import uvicorn

    parser = argparse.ArgumentParser(description="SailPyInstaller运行启动参数")
    parser.add_argument('--host', type=str, default="0.0.0.0", help='IP')
    parser.add_argument('--port', type=int, default=12205, help='端口')
    parser.add_argument('--allowed', type=str, default="", help="运行访问的IP列表")
    args = parser.parse_args()
    app_storage["allowed_ips"] = [str(ip).strip() for ip in args.allowed.split(",")]
    uvicorn.run(app, host=args.host, port=args.port)
