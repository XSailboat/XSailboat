import json
import os
import platform

from fastapi import APIRouter, UploadFile, File

from starlette.responses import FileResponse

from team.sailboat.py.installer.common.app_util.app_sys_command import AppSysCmd
from team.sailboat.py.installer.common.request_body import IptablesConfig
from team.sailboat.py.installer.common.app_util.app_path import AppPath

iptables = APIRouter()


@iptables.post("/conf/_exec", name="执行防火墙配置接口")
async def load_ipables():
    """
    执行防火墙程序
    """
    try:
        filepath = "../config/py_app/py_installer/iptables_config.json"
        if AppPath.check_path_exists(filepath):
            # 获取运行脚本的路径
            script_path = AppPath.find_path_with_files(os.getcwd(), ["miniconda"])
            if script_path != None:
                script_path += "/miniconda"
            script_path = script_path + "/bin/python"
            # 获取到辅助脚本的位置
            help_path = AppPath.find_file("iptables_func.py", os.path.dirname(os.getcwd()))
            # 获取配置文件的绝对路径
            filepath = os.path.abspath(filepath)
            # 执行脚本
            result = AppSysCmd.cmd_run_root(
                f"{script_path} {help_path} {filepath}")
            if result.returncode == 0:
                return {"code": 200, "msg": ""}
            else:
                return {"code": 500, "msg": result.stderr}
        else:
            return {"code": 500, "msg": "没有找到配置文件"}
    except Exception as e:
        return {"code": 500, "msg": e}


@iptables.post("/conf/file/one/_createOrUpdate", name="上传防火墙配置文件方式接口")
async def upload_config_file(file: UploadFile = File(...)):
    """
    上传配置文件
    """
    try:
        # 读取文件内容
        contents = await file.read()

        # 将内容转换为 JSON 字典
        data = json.loads(contents.decode('utf-8'))
        filepath = "../config/py_app/py_installer/iptables_config.json"
        # 写入 JSON 数据
        with open(filepath, "w") as f:
            json.dump(data, f, indent=4)
    except Exception as e:
        return {"code": 500, "msg": e}
    return {"code": 200, "msg": "success"}


@iptables.post("/conf/one/_createOrUpdate", name="上传防火墙JSON方式接口")
async def upload_config_json(data: IptablesConfig):
    """
    传入JSON覆盖配置文件内容
    {
        "hosts":[],
        "apps":{},
        "outer_clients":{}
    }
    """
    try:

        config_json = data.dict()

        filepath = "../config/py_app/py_installer/iptables_config.json"
        # 写入 JSON 数据
        with open(filepath, "w") as f:
            json.dump(config_json, f, indent=4)
    except Exception as e:
        return {"code": 500, "msg": e}
    return {"code": 200, "msg": "success"}


@iptables.get("/conf/content", name="获取当前配置文件内容JSON接口")
async def get_config_file_content():
    """
    获取配置文件的内容
    """
    try:
        filepath = "../config/py_app/py_installer/iptables_config.json"
        if AppSysCmd.exist_file(filepath):
            json_str = ""
            with open(filepath, "r") as f:
                json_str = json.loads(f.read())
            return {"code": 200, "msg": "success", "data": json_str}
        else:
            return {"code": 200, "msg": "success", "data": ""}
    except Exception as e:
        return {"code": 500, "msg": e}


@iptables.get("/conf/file/one", name="下载当前文件配置接口")
async def read_file():
    """
    下载配置文件
    """
    filepath = "../config/py_app/py_installer/iptables_config.json"
    # 检查文件是否存在
    if not AppSysCmd.exist_file(filepath):
        return {"code": -1, "error": "文件不存在"}

    return FileResponse(filepath, media_type='application/octet-stream',
                        filename=platform.node() + '_iptables_config.json')
