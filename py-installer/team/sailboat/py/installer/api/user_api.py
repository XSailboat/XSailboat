from fastapi import APIRouter

# 用户操作接口
from starlette.responses import Response

from team.sailboat.py.installer.api_func.user_func import create_user_func, auth_user, modify_own_func, \
    get_rsa_public_key
from team.sailboat.py.installer.common.request_body import UserCredentials, CreateUserInfo, PathList

user_api = APIRouter()


@user_api.get("/rsa/public_key", name="获取rsa公钥")
async def rsa_gen():
    return get_rsa_public_key()


@user_api.post("/password/_validate", name="验证用户名和密码是否正确")
async def authentication(credentials: UserCredentials):
    """
    验证用户名和密码
    """
    if auth_user(credentials.username, credentials.password, credentials.codeId):
        return Response(content="", media_type="text/plain", headers={"Content-Length": "0"})
    else:
        return Response(content="验证失败!", media_type="text/plain")


@user_api.post("/one", name="创建Linux用户")
async def create_user(info: CreateUserInfo):
    """创建用户,成功返回None,失败则返回失败信息"""

    result = create_user_func(info.dict())
    if result["code"]:
        return Response(content="", media_type="text/plain", headers={"Content-Length": "0"})
    else:
        return Response(content=result["msg"], media_type="text/plain")


@user_api.post("/file/many/_chown", name="修改指定文件或目录的所有者为平台用户")
async def modify_own(paths: PathList):
    """修改指定路径的所有者为平台用户"""
    result = modify_own_func(" ".join(paths.paths))
    if result["code"]:
        return Response(content="", media_type="text/plain", headers={"Content-Length": "0"})
    else:
        return Response(content=result["msg"], media_type="text/plain")
