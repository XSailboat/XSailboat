# rsa加密
import base64
import getpass

from cryptography.hazmat.primitives.asymmetric import padding
from loguru import logger

from team.sailboat.py.installer.common.app_storage import AppStorage
from team.sailboat.py.installer.common.app_util.app_sys_command import AppSysCmd
from team.sailboat.py.installer.common.app_util.app_sys_config import AppSysConfig
from team.sailboat.py.installer.common.ms_command import custom_cmd
from team.sailboat.py.installer.common.util.rsa_key_pair_maker import RSAKeyPairMaker

rsa_maker = RSAKeyPairMaker.getDefault()
app_storage = AppStorage()


def auth_user(username, password,key_id=""):
    """验证用户和密码"""
    # 解密
    if key_id!="":
        try:
            password = rsa_maker.decrypt(key_id, password)
        except Exception as e:
            logger.info("验证用户时解密失败!",e)
            return False

    result = AppSysCmd.cmd_run(
        f"echo -n '{password}' | su - {username} -c \"echo -n '{password}' | su - {username} -c 'echo true'\" ")
    if result.returncode == 0 and str(result.stdout).startswith("true"):
        return True
    else:
        return False


def get_rsa_public_key():
    key_id, public_key = rsa_maker.newOne()
    public_key_exponent, public_key_modulus = rsa_maker.get_public_key_details(key_id)
    result = {
        "codeId": key_id,
        "publicKeyExponent": public_key_exponent,
        "publicKeyModulus": public_key_modulus
    }
    return result


@custom_cmd("x_create_user", ["u:p:", ["username=", "password="], {"-u": "username", "-p": "password"}])
def create_user_func(info):
    """创建用户,成功返回None,失败则返回失败信息"""
    logger.info(f'正在创建 {info["username"]} 用户...')
    # 验证用户是否存在
    if auth_user(info["username"], info["password"],""):
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

