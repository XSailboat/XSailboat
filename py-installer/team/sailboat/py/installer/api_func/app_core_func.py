import getpass
import json
import os
import re

from loguru import logger

from team.sailboat.py.installer.common.app_storage import AppStorage
from team.sailboat.py.installer.common.app_util.app_path import AppPath
from team.sailboat.py.installer.common.app_util.app_sys_command import AppSysCmd
from team.sailboat.py.installer.common.app_util.app_sys_config import start_script
from team.sailboat.py.installer.common.app_variable import AppVariable
from team.sailboat.py.installer.common.ms_command import custom_cmd
from team.sailboat.py.installer.common.util.rsa_key_pair_maker import RSAKeyPairMaker


def get_uvicorn_main_pid():
    """
    获取主进程的 PID。
    """
    current_pid = os.getpid()

    return current_pid


def split_commands(commands):
    new_commands = []
    for cmd in commands:
        if "&&" in str(cmd):
            cmds = re.split("\s*&&\s*", cmd)
            if cmds[0].strip().endswith("&"):
                new_commands += cmds
                continue
        new_commands.append(cmd)
    return new_commands


app_storage = AppStorage()
app_variable = AppVariable()
rsa_maker = RSAKeyPairMaker.getDefault()


def storage_status_init():
    app_storage["pre_output"] = ""
    app_storage["pre_output_error"] = ""
    app_storage["current_user"] = app_storage["profile"]["sysUser"]
    app_storage["sql"] = {"mode": False}
    app_storage["pre_output_pOpen"] = {"stdout_lines": [], "stderr_lines": [], "running": False}


def host_profile_func(profile, codeId):
    """储配置信息,如IP、主机名称、管理员用户名/密码、系统用户名/密码...
    """
    if type(profile) == str:
        profile = json.loads(profile)
    # 解密
    try:
        profile["adminPswd"] = rsa_maker.decrypt(codeId, profile["adminPswd"])
        profile["sysPswd"] = rsa_maker.decrypt(codeId, profile["sysPswd"])
    except Exception as e:
        logger.info("密码解密失败!", e)
        return {"code": False, "msg": f"密码解密失败，{e}"}

    app_storage["profile"] = profile

    # 添加自启动
    start_script()
    host_profile_path = "../config/py_apps/py_installer/.hostProfile.json"
    if not os.path.exists(host_profile_path):
        dirname = os.path.dirname(host_profile_path)
        os.makedirs(dirname, exist_ok=True)
    with open(host_profile_path, "w") as f:
        json.dump(profile, f, indent=4)
    app_variable["host.adminPswd"] = profile["adminPswd"]
    app_variable["host.sysPswd"] = profile["sysPswd"]
    app_variable.save_variable()
    return {"code": True, "msg": ""}


@custom_cmd("x_restart", [])
def restart_service_func(params=None):
    """
    重启服务
    """

    # 获得应用的pid
    pid = get_uvicorn_main_pid()
    logger.info(f"当前进程PID:{pid}")
    # 获取运行脚本的路径
    script_path = AppPath.find_path_with_files(os.getcwd(), ["miniconda"])
    if script_path != None:
        script_path = os.path.join(script_path, "miniconda")
    script_path = os.path.join(script_path, "/bin/python")
    # 获取到辅助脚本的位置
    help_path = AppPath.find_file("restart_help.py", os.path.dirname(os.getcwd()))
    # 执行脚本
    result = AppSysCmd.cmd_run(
        f"{script_path} {help_path} {pid} {app_storage['profile']['adminUser']} {app_storage['profile']['adminPswd']}")
    return {"code": True, "msg": ""}
