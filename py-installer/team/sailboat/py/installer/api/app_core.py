import traceback

from fastapi import APIRouter, Query
from loguru import logger
from starlette.responses import Response, StreamingResponse

from team.sailboat.py.installer.api_func.app_core_func import split_commands, \
    storage_status_init, host_profile_func, restart_service_func
from team.sailboat.py.installer.common.request_body import HostProfile, Commands
from team.sailboat.py.installer.common.app_storage import AppStorage
from team.sailboat.py.installer.common.ms_command import CommandProcessor

# 服务核心接口
app_core = APIRouter()

app_storage = AppStorage()


# 存储主机配置信息
@app_core.post("/hostProfile/one/_createOrUpdate", name="上传或更新主机配置信息")
async def host_profile(profile: HostProfile, codeId: str = Query("", description="加密ID")):
    """储配置信息,如IP、主机名称、管理员用户名/密码、系统用户名/密码...
    """
    result = host_profile_func(profile.dict(), codeId)
    if result["code"]:
        return Response(content="", media_type="text/plain", headers={"Content-Length": "0"})
    else:
        return Response(content=result["msg"], media_type="text/plain")


@app_core.get("/hostProfile/one", name="获取当前存储的主机系统配置")
async def get_host_profile():
    """
    获取当前存储的系统配置
    """
    return app_storage["profile"]


@app_core.post("/self/_restart", name="重启SailPyInstaller服务")
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
@app_core.post("/command/many/stream/_exec", name="执行命令,流式返回")
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
@app_core.post("/command/many/_exec", name="执行命令")
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
