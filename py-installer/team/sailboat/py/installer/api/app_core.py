import os
import traceback

from fastapi import APIRouter, UploadFile, File
from loguru import logger
from starlette.responses import JSONResponse, Response, StreamingResponse

from aiofiles import open as aopen

from team.sailboat.py.installer.api_func.app_core_func import auth_user, split_commands, \
    storage_status_init, host_profile_func, create_user_func, modify_own_func, restart_service_func
from team.sailboat.py.installer.common.request_body import UserCredentials, HostProfile, Commands, CreateUserInfo, \
    PathList
from team.sailboat.py.installer.common.app_storage import AppStorage
from team.sailboat.py.installer.common.ms_command import CommandProcessor

# 服务核心接口
app_core = APIRouter()

app_storage = AppStorage()


# 存储主机配置信息
@app_core.post("/hostProfile", name="上传或更新配置信息")
async def host_profile(profile: HostProfile):
    """储配置信息,如IP、主机名称、管理员用户名/密码、系统用户名/密码...
    """
    result = host_profile_func(profile.dict())
    if result["code"]:
        return Response(content="", media_type="text/plain", headers={"Content-Length": "0"})
    else:
        return Response(content=result["msg"], media_type="text/plain")


@app_core.get("/getHostProfile", name="获取当前存储的系统配置")
async def get_host_profile():
    """
    获取当前存储的系统配置
    """
    return app_storage["profile"]


@app_core.post("/validation/user", name="验证用户名和密码是否正确")
async def validation(credentials: UserCredentials):
    """
    验证用户名和密码
    """
    if auth_user(credentials.username, credentials.password):
        return Response(content="", media_type="text/plain", headers={"Content-Length": "0"})
    else:
        return Response(content="验证失败!", media_type="text/plain")


@app_core.post("/createUser", name="创建Linux用户")
async def create_user(info: CreateUserInfo):
    """创建用户,成功返回None,失败则返回失败信息"""

    result = create_user_func(info.dict())
    if result["code"]:
        return Response(content="", media_type="text/plain", headers={"Content-Length": "0"})
    else:
        return Response(content=result["msg"], media_type="text/plain")


@app_core.post("/modifyOwn", name="修改指定文件或目录的所有者为平台用户")
async def modify_own(paths: PathList):
    """修改指定路径的所有者为平台用户"""
    result = modify_own_func(" ".join(paths.paths))
    if result["code"]:
        return Response(content="", media_type="text/plain", headers={"Content-Length": "0"})
    else:
        return Response(content=result["msg"], media_type="text/plain")


@app_core.post("/restart", name="重启服务")
async def restart_service():
    """
    通过 API 调用重启服务
    """
    result = restart_service_func()
    if result["code"]:
        return Response(content="", media_type="text/plain", headers={"Content-Length": "0"})
    else:
        return Response(content=result["msg"], media_type="text/plain")

cp = CommandProcessor()


# 执行多个命令
@app_core.post("/exec/commands/stream", name="执行命令")
async def exec_commands_stream(commands: Commands):
    """
    执行多个命令期间出现执行失败则直接返回结果列表
    """
    result = []
    if len(commands.commands) == 0:
        app_storage["current_user"] = app_storage["profile"]["sysUser"]
        result.append("命令列表为空")
        logger.info("命令列表为空")
        return result

    async def generate():
        try:
            # 判断命令中是否有
            cmds = split_commands(commands.commands)
            for command in cmds:
                logger.info(f"当前正在执行命令:{command}")
                try:
                    status = cp.execute_command(command)
                    if status["code"]:
                        result.append("")
                    else:
                        logger.info(f"执行命令失败:{command} 失败,Message:{status['msg']}")
                        result.append(status["msg"])
                        break
                except PermissionError as e:
                    logger.info(f"执行命令失败:{command} 失败,Message:{str(e)},权限不足！")
                    result.append(f'{str(e)},权限不足！')
                    break
        except Exception as e:
            logger.error(f"命令执行时发生异常: {e}")
            logger.error("堆栈信息:")
            logger.error(traceback.format_exc())
        finally:
            storage_status_init()
        yield f'{result}'.encode()

    return StreamingResponse(generate(), media_type="text/event-stream")


# 执行多个命令
@app_core.post("/exec/commands", name="执行命令")
async def exec_commands(commands: Commands):
    """
    执行多个命令期间出现执行失败则直接返回结果列表
    """
    result = []
    if len(commands.commands) == 0:
        app_storage["current_user"] = app_storage["profile"]["sysUser"]
        result.append("命令列表为空")
        logger.info("命令列表为空")
        return result
    try:
        # 判断命令中是否有
        cmds = split_commands(commands.commands)
        for command in cmds:
            logger.info(f"当前正在执行命令:{command}")
            try:
                status = cp.execute_command(command)
                if status["code"]:
                    result.append("")
                else:
                    logger.info(f"执行命令失败:{command} 失败,Message:{status['msg']}")
                    result.append(status["msg"])
                    break
            except PermissionError as e:
                logger.info(f"执行命令失败:{command} 失败,Message:{str(e)},权限不足！")
                result.append(f'{str(e)},权限不足！')
                break
    except Exception as e:
        logger.error(f"命令执行时发生异常: {e}")
        logger.error("堆栈信息:")
        logger.error(traceback.format_exc())
    finally:
        storage_status_init()
    return result


@app_core.post("/upload", name="上传应用软件包")
async def upload_file(path: str, file: UploadFile = File(...)):
    """
    上传压缩包或文件到指定目录，multipart方式上传
    """
    try:
        # 获取文件名
        filename = file.filename

        # 指定保存路径
        save_path = f"{path}/{filename}"

        # 创建目录（如果不存在）
        os.makedirs(os.path.dirname(save_path), exist_ok=True)

        # 异步写入文件
        async with aopen(save_path, mode='wb') as out_file:
            while True:
                chunk = await file.read(1024 * 1024)  # 读取1MB的数据
                if not chunk:
                    break
                await out_file.write(chunk)

    except Exception as e:
        return JSONResponse(status_code=500, content={"code": 500, "msg": str(e)})

    return JSONResponse(status_code=200, content={"code": 200, "msg": "success"})
