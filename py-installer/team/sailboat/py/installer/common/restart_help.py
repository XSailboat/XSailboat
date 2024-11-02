"""
重启脚本
"""
import os
import sys

from team.sailboat.py.installer.common.app_storage import AppStorage
from team.sailboat.py.installer.common.app_util.app_path import AppPath
from team.sailboat.py.installer.common.app_util.app_sys_command import AppSysCmd


def restart(pid, adminUser, adminPswd):
    app_storage = AppStorage()

    app_storage["profile"] = {"adminUser": adminUser, "adminPswd": adminPswd}
    if pid is not None or pid != "None":
        result = AppSysCmd.cmd_run_root(f"kill {pid}")
        if result.returncode != 0:
            return
    # 判断是否设置了自启动服务
    result = AppSysCmd.cmd_run_root("test -f /etc/systemd/system/SailInstaller.service && echo 'exists'")
    if result.stdout:
        # 直接restart服务
        result = AppSysCmd.cmd_run_root("systemctl restart SailInstaller")
        print("systemctl",result.returncode, result.stdout, result.stderr)
        print(f"通过systemctl服务重启成功!")
    else:
        # 找到脚本的路径
        start_path = AppPath.find_file_top_down(os.getcwd(), "start.sh")
        # 使用脚本的方式启动服务
        AppSysCmd.cmd_run(f"/bin/bash {start_path} daemon")
        print(f"通过{start_path}脚本重启成功!")


if __name__ == '__main__':
    args = sys.argv
    restart(args[1], args[2], args[3])
