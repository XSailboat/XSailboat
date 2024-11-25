import getpass
import json
import os
import re

from loguru import logger

from team.sailboat.py.installer.common.app_storage import AppStorage
from team.sailboat.py.installer.common.app_util.app_path import AppPath
from team.sailboat.py.installer.common.app_util.app_sys_command import AppSysCmd
from team.sailboat.py.installer.common.app_util.app_sys_config import start_script, AppSysConfig
from team.sailboat.py.installer.common.ms_command import CommandProcessor, custom_cmd


def auth_user(username, password):
    """验证用户和密码"""
    result = AppSysCmd.cmd_run(
        f"echo -n '{password}' | su - {username} -c \"echo -n '{password}' | su - {username} -c 'echo true'\" ")
    if result.returncode == 0 and str(result.stdout).startswith("true"):
        return True
    else:
        return False


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


def storage_status_init():
    app_storage["pre_output"] = ""
    app_storage["pre_output_error"] = ""
    app_storage["current_user"] = app_storage["profile"]["sysUser"]
    app_storage["sql"] = {"mode": False}
    app_storage["pre_output_pOpen"] = {"stdout_lines": [], "stderr_lines": [], "running": False}


@custom_cmd("x_set_host_profile", [])
def host_profile_func(profile):
    """储配置信息,如IP、主机名称、管理员用户名/密码、系统用户名/密码...
    """
    if type(profile) == str:
        profile = json.loads(profile)
    # 验证用户和密码是否正确
    if not auth_user(profile["adminUser"], profile["adminPswd"]):
        logger.info("系统配置中管理员账号或密码错误!")
        return {"code": False, "msg": "系统配置中管理员账号或密码错误!"}

    if not auth_user(profile["sysUser"], profile["sysPswd"]):
        logger.info("系统配置中平台账号或密码错误!")
        return {"code": False, "msg": "系统配置中平台账号或密码错误!"}

    app_storage["profile"] = profile

    # 添加自启动
    start_script()
    with open("./.hostProfile.json", "w") as f:
        json.dump(profile, f, indent=4)
    return {"code": True, "msg": ""}


@custom_cmd("x_create_user", ["u:p:", ["username=", "password="], {"-u": "username", "-p": "password"}])
def create_user_func(info):
    """创建用户,成功返回None,失败则返回失败信息"""
    logger.info(f'正在创建 {info["username"]} 用户...')
    # 验证用户是否存在
    if auth_user(info["username"], info["username"]):
        # 已经存在
        logger.info(f'用户 {info["username"]} 已经存在!请勿重复创建')
        return {"code": True, "msg": ""}
    # 创建用户
    result = AppSysConfig.create_user(info["username"], info["password"])
    if result is None:
        logger.info(f'用户 {info["username"]} 创建成功！')
        # 保存到全局中
        app_storage["profile"]["sysUser"] = info["username"]
        app_storage["profile"]["sysPswd"] = info["password"]
    else:
        logger.info(f'用户 {info["username"]} 创建失败！原因:{result}')
        return {"code": False, "msg": result}

    return {"code": True, "msg": ""}


@custom_cmd("x_chown", [])
def modify_own_func(paths: str):
    """修改指定路径的所有者为平台用户"""
    paths = paths.strip().split()
    for path in paths:
        if getpass.getuser() == "root":
            if AppSysCmd.exist_file(path) or AppSysCmd.exist_dir(path):
                AppSysCmd.cmd_run(f"chmod -R 755 {path}")
    result = AppSysCmd.change_own(app_storage["profile"]["sysUser"], paths)
    return {"code": result is None, "msg": result}


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
        script_path += "/miniconda"
    script_path = script_path + "/bin/python"
    # 获取到辅助脚本的位置
    help_path = AppPath.find_file("restart_help.py", os.path.dirname(os.getcwd()))
    # 执行脚本
    result = AppSysCmd.cmd_run(
        f"{script_path} {help_path} {pid} {app_storage['profile']['adminUser']} {app_storage['profile']['adminPswd']}")
    return {"code": True, "msg": ""}

