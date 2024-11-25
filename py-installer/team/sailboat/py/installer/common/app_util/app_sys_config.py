import json
import os
import re
import socket
import time
from pathlib import Path

from team.sailboat.py.installer.common.app_storage import AppStorage
from team.sailboat.py.installer.common.app_util.app_file import AppFile, ConfFile
from team.sailboat.py.installer.common.app_util.app_path import AppPath
from team.sailboat.py.installer.common.app_util.app_sys_command import AppSysCmd
from team.sailboat.py.installer.common.util.ssh_util import SSHConfigurator
from loguru import logger

app_storage = AppStorage()


class AppSysConfig:

    @staticmethod
    def stop_ipV6():
        """关闭IPv6"""
        try:
            # 1.修改/etc/sysctl.conf
            logger.info("【关闭IPV6】- 修改/etc/sysctl.conf文件...")
            optional1, msg = AppFile.update_property_conf("/etc/sysctl.conf", {"net.ipv6.conf.all.disable_ipv6": "1"},
                                                          True)
            # 2.修改/etc/sysconfig/network
            logger.info("【关闭IPV6】- 修改/etc/sysconfig/network文件...")
            optional2, msg = AppFile.update_property_conf("/etc/sysconfig/network",
                                                          {"NETWORKING_IPV6": "no", "NETWORKING": "yes"}, True)
            # 3.修改/etc/sysconfig/network-scripts/网卡名称 文件
            network = AppSysConfig.get_real_network()
            if network == None:
                return False
            logger.info(f"【关闭IPV6】- 修改/etc/sysconfig/network-scripts/{network}文件...")
            optional3, msg = AppFile.update_property_conf(f"/etc/sysconfig/network-scripts/{network}",
                                                          {"IPV6INIT": "no"},
                                                          True)
            # 4.重启网卡
            # logger.info("【关闭IPV6】- 重启网卡")
            # result = AppSysCmd.cmd_run_root("systemctl restart systemd-networkd")
            if optional1 and optional2 and optional3:
                logger.info("【关闭IPV6】- 网卡配置修改成功,麒麟系统需重启生效,Linux系统重启network服务生效")
                return True
            else:
                logger.info("【关闭IPV6】- 网卡重启失败,关闭IPV6操作失败")
                return False

        except Exception as e:
            logger.error(f"【关闭IPV6】- 操作过程中出现异常:{e}", False)
            return False

    @staticmethod
    def stop_virtual_network():
        """关闭虚拟网卡"""
        networks = AppSysConfig.get_virtual_network()
        if len(networks) == 0:
            logger.info("【关闭虚拟网卡】-没有找到虚拟网卡的信息")
            return True
        for info in networks:
            cmd = f"ifconfig {info} down"
            result = AppSysCmd.cmd_run_root(cmd)
            if result.returncode == 0:
                logger.info(f"【关闭虚拟网卡】-虚拟网卡[{info}]成功关闭!")
            else:
                logger.info(f"【关闭虚拟网卡】-虚拟网卡[{info}]关闭失败,状态码:{result.returncode}!")

        # 禁用libvirtd服务开机自启
        cmd = "systemctl disable libvirtd.service"
        result = AppSysCmd.cmd_run_root(cmd)
        if result.returncode == 0:
            logger.info(f"【关闭虚拟网卡】-已禁用libvirtd服务开机自启!")
            return True
        else:
            logger.info(f"【关闭虚拟网卡】-禁用libvirtd服务开机自启失败,状态码:{result.returncode}!")
            return False

    @staticmethod
    def stop_firewall():
        """关闭防火墙自启动"""
        AppSysCmd.cmd_run_root("systemctl stop firewalld")
        result = AppSysCmd.cmd_run_root("systemctl disable firewalld")
        if result.returncode == 0:
            logger.info("【关闭防火墙】-已成功关闭防火墙自启动")
            return True
        else:
            logger.info(f"【关闭防火墙】-关闭防火墙自启动失败!,状态码:{result.returncode}")
            return False

    @staticmethod
    def get_real_network():
        """获取当前配置了IP的非虚拟网卡"""
        """ls /etc/sysconfig/network-scripts | grep ifcfg-*"""
        cmd = "ls /etc/sysconfig/network-scripts | grep ifcfg-*"
        result = AppSysCmd.cmd_run(cmd)
        if result.returncode == 0:
            # 获取所有网卡信息
            networks = str(result.stdout).strip().split("\n")
            for name in networks:
                if name == "ifcfg-lo":
                    # 过滤掉虚拟网卡
                    continue
                # 判断其余网卡中是否配置了IPADDR
                file_content = AppSysCmd.read_file(f"/etc/sysconfig/network-scripts/{name}")
                if "IPADDR" in file_content:
                    logger.info(f"【获取网卡】- 网卡获取成功,网卡名称为:{name.split('-')[1]}")
                    return name
        else:
            logger.info(f"【获取网卡】- 未获取到配置了IP的网卡信息")
            return None

    @staticmethod
    def get_virtual_network():
        """获取所有虚拟网卡的名称"""
        # 只有在"/sys/devices/virtual/net"中有的,并且在ifconfig中也显示的
        nets = AppPath.get_dir_list("/sys/devices/virtual/net")
        nets.remove("lo")
        result = AppSysCmd.cmd_run_root("ifconfig")
        if result.returncode == 0:
            return [v_net for v_net in nets if v_net in result.stdout]

    @staticmethod
    def sys_hostname(content):
        """修改主机名"""
        try:
            result = AppSysCmd.save_file_root(content, "/etc/hostname")
            if result == True:
                logger.info("【修改主机名】- 已完成")
                return True
            else:
                logger.info(f"【修改主机名】- 失败,调用状态码:{result.returncode}")
                return False
        except Exception as e:
            logger.error(f"【修改主机名】- 发生错误:{e}")
            return False

    @staticmethod
    def sys_hosts(content):
        """修改主机名映射"""
        # 获取现有的文件内容

        try:
            logger.info("【修改主机名映射】- 修改/etc/hosts文件...", False)
            hosts = AppSysCmd.read_file_root("/etc/hosts")
            if hosts != None:
                # 查找程序替换的标记位
                content = str(content).strip()
                if len(content.strip().split("\n")) > 1:
                    content = str(content).replace("\"", "")
                else:
                    content = format_string_by_separator(content, "\\s+", " ", 2)
                    if content == None:
                        logger.info(f"【修改主机名映射】- 失败,配置的值个数不是成对的")
                        return False

                new_content = AppFile.replace_text_between_markers(hosts, content)
                result = AppSysCmd.save_file_root(new_content, "/etc/hosts")
                if result:
                    logger.info("【修改主机名映射】- 已完成", False)
                    return True
                else:
                    logger.info(f"【修改主机名映射】- 失败,调用状态码:{result.returncode}")
                    return False
            else:
                return False
        except Exception as e:
            logger.error(f"【修改主机名映射】- 发生错误:{e}")

    @staticmethod
    def set_limit(content):
        """修改文件最大句柄数"""
        content = str(content).strip()
        if len(content.strip().split("\n")) > 1:
            content = str(content).replace("\"", "")
        else:
            content = format_string_by_separator(content, "\\s+", " ", 4)
            if content == None:
                logger.info(f"【修改文件最大句柄数】- 失败,配置某行的个数不是4个值")
                return False
        conf_content = AppSysCmd.read_file_root("/etc/security/limits.conf")
        new_conf_content = AppFile.replace_text_between_markers(conf_content, content)
        result = AppSysCmd.save_file_root(new_conf_content, "/etc/security/limits.conf")
        return result

    @staticmethod
    def close_selinux():
        """关闭SELINUX"""
        conf_content = AppSysCmd.read_file_root("/etc/sysconfig/selinux")
        content = {"SELINUX": "disabled"}
        if conf_content != None:
            conf_file = ConfFile(conf_content)
            for key, value in content.items():
                conf_file[key] = value
            content_str = conf_file.get_content_str()
            result = AppSysCmd.save_file_root(content_str, "/etc/sysconfig/selinux")
            if result:
                logger.info("【关闭SELINUX】- 成功关闭SELINUX！")
            else:
                logger.info("【关闭SELINUX】- 关闭SELINUX失败!")
            return result
        else:
            logger.info("【关闭SELINUX】- /etc/sysconfig/selinux不存在!")

    @staticmethod
    def add_ssh(hostname):
        """添加ssh免密登录"""
        configurator = SSHConfigurator()
        return configurator.configure_hosts(hostname)

    @staticmethod
    def add_env(content):
        """添加环境变量"""
        envs_str = re.split("\\s+", content)
        export_envs = []
        for s in envs_str:
            if not s.startswith("export"):
                export_envs.append(f"export {s}")
            else:
                export_envs.append(s)
        content = "\n".join(export_envs)

        file_content = AppSysCmd.read_file(f"/home/{app_storage['profile']['sysUser']}/.bashrc")
        if file_content != None:
            content = AppFile.replace_text_between_markers(file_content, content, True)
            result = AppSysCmd.save_file(content, f"/home/{app_storage['profile']['sysUser']}/.bashrc")
            AppSysCmd.cmd_run(f"source /home/{app_storage['profile']['sysUser']}/.bashrc")
            return result

    @staticmethod
    def add_env2(content):
        """添加环境变量"""
        envs_str = re.split("\\s+", content)
        export_envs = []
        for s in envs_str:
            if not s.startswith("export"):
                export_envs.append(f"export {s}")
            else:
                export_envs.append(s)
        content = "\n".join(export_envs)

        # /etc/profile
        file_content = AppSysCmd.read_file_root(f"/etc/profile")

        if file_content != None:
            content = AppFile.replace_text_between_markers(file_content, content, True)
            result = AppSysCmd.save_file_root(content, f"/etc/profile")
            return result

        return False

    @staticmethod
    def is_port_open(port, max_attempts=5, attempt_interval=2):
        """
        检查指定端口是否开放。

        :param port: 要检查的端口号
        :param max_attempts: 最大重试次数
        :param attempt_interval: 每次重试之间的间隔时间（秒）
        :return: 如果端口开放则返回 True，否则返回 False
        """
        # 获取主机名
        hostname = app_storage["profile"]["name"]
        for attempt in range(max_attempts):
            try:
                with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
                    result = sock.connect_ex((hostname, port))
                    if result == 0:
                        return True
                    else:
                        pass
            except socket.timeout:
                pass
            except socket.error as e:
                pass

            if attempt < max_attempts - 1:
                time.sleep(attempt_interval)  # 等待一段时间后重试

        return False

    @staticmethod
    def set_telegraf(path, listen_path):
        """
        修改telegraf配置文件

        :param path: 配置文件所在路径
        :param listen_path: 监听的路径
        :return: 成功返回 True，否则返回 False
        """
        data = {}
        root = app_storage["current_user"] == app_storage["profile"]["adminUser"]
        data["[agent].hostname"] = f'"{app_storage["profile"]["name"]}"'  # 设置主机名
        data["[[inputs.net]].interfaces"] = [AppSysConfig.get_real_network().replace("ifcfg-", "")]  # 设置网卡名称
        # 获取监听的目录的挂载盘名称
        mount_point = set()
        ignore_fs_mount_point = set()
        df_result = AppSysCmd.cmd_run_root("df -h")
        # 获取所有挂载信息
        mount_info = str(df_result.stdout).strip().split("\n")[1:]
        for index in range(len(mount_info)):
            # 判断挂载点是否是指定路径的子路径
            info = re.split("\\s+", mount_info[index])
            for p in listen_path:
                mount_name = info[0]

                # 判断 / 路径的情况
                if p == "/" and info[5] == p:
                    mount_point.add(mount_name)
                    continue

                # 创建 Path 对象
                p1 = Path(p).resolve()
                p2 = Path(info[5]).resolve()

                # 比较路径深度
                if len(p1.parts) <= len(p2.parts):
                    # 如果 path1 较浅或相同深度，则认为它是潜在的父路径
                    if p2.is_relative_to(p1) and p != "/":
                        mount_point.add(mount_name)
                        continue
                else:
                    # 如果 path2 较浅，则认为它是潜在的父路径
                    if p1.is_relative_to(p2) and p != "/":
                        mount_point.add(mount_name)
                        continue
                # 否则添加到忽略的挂载点中
                ignore_fs_mount_point.add(mount_name)
        mount_point = list(mount_point)
        ignore_fs_mount_point = list(ignore_fs_mount_point)
        for i in range(len(mount_point)):
            if "/" in mount_point[i]:
                result = AppSysCmd.cmd_run_root(f"readlink {mount_point[i]}")
                if result.returncode == 0 and result.stdout is not None and str(result.stdout).strip() != "":
                    if "/" in str(result.stdout).strip():
                        mount_point[i] = str(result.stdout).strip().split("/")[-1]
                    else:
                        mount_point[i] = str(result.stdout).strip()
                else:
                    mount_point[i] = mount_point[i].split("/")[-1]

        for i in range(len(ignore_fs_mount_point)):
            if "/" in ignore_fs_mount_point[i]:
                result = AppSysCmd.cmd_run_root(f"readlink {ignore_fs_mount_point[i]}")
                if result.returncode == 0 and result.stdout is not None and str(result.stdout).strip() != "":
                    if "/" in str(result.stdout).strip():
                        ignore_fs_mount_point[i] = str(result.stdout).strip().split("/")[-1]
                    else:
                        ignore_fs_mount_point[i] = str(result.stdout).strip()
                else:
                    ignore_fs_mount_point[i] = ignore_fs_mount_point[i].split("/")[-1]

        data["[[inputs.diskio]].devices"] = mount_point
        data["[[inputs.disk]].ignore_fs"] = ignore_fs_mount_point

        result, msg = AppFile.update_property_conf(path, data, root, tag=True)
        return result, msg

    @staticmethod
    def create_user(username, password):
        """
        创建用户并设置密码。

        :param username: 用户名
        :param password: 密码
        :return: 成功返回 None，失败返回错误信息
        """
        # 构建创建用户组的命令
        group_command = f"groupadd {username}"

        # 构建创建用户的命令
        user_command = f"useradd -g {username} -m -s /bin/bash {username}"

        # 构建设置密码的命令
        password_command = f"echo -e \"{username}:{password}\" | chpasswd"

        # 执行创建用户组的命令
        result = AppSysCmd.cmd_run_root(group_command)
        if result.returncode != 0:
            return f"创建用户组失败: {result.stderr}"

        # 执行创建用户的命令
        result = AppSysCmd.cmd_run_root(user_command)
        if result.returncode != 0:
            return f"创建用户失败: {result.stderr}"

        # 执行设置密码的命令
        result = AppSysCmd.cmd_run_root(password_command)
        if result.returncode != 0:
            return f"设置密码失败: {result.stderr}"

        return None

    @staticmethod
    def set_iptables(profile):
        filepath = AppPath.find_file("iptables_config.json", os.path.dirname(os.getcwd()))
        with open(filepath, "w") as f:
            json.dump(profile, f, indent=4)

        # 获取运行脚本的路径
        script_path = AppPath.find_path_with_files(os.getcwd(), ["miniconda"])
        if script_path != None:
            script_path += "/miniconda"
        script_path = script_path + "/bin/python"
        # 获取到辅助脚本的位置
        help_path = AppPath.find_file("iptables_func.py", os.path.dirname(os.getcwd()))
        # 执行脚本
        result = AppSysCmd.cmd_run_root(
            f"{script_path} {help_path} {filepath}")

        return result.returncode == 0, result.stderr


# 添加开机自启脚本
def start_script():
    # 1.判断/etc/systemd/system中是否已经存在了
    result = AppSysCmd.cmd_run_root("test -f /etc/systemd/system/SailInstaller.service && echo 'exists'")
    if result.stdout:
        # 存在
        logger.info("SailInstaller已在开机自启列表中")
        return
    logger.info("正在添加SailInstaller开机自启...")
    start_path = AppPath.find_file_top_down(os.getcwd(), "start.sh")
    if start_path is None:
        logger.info("启动脚本start.sh未找到,自启动设置失败!")
    script = f"""
[Unit]
Description=SailPyInstaller的一键安装服务
After=network.target

[Service]
User={app_storage["profile"]["sysUser"]}
Group={app_storage["profile"]["sysUser"]}
ExecStart=/bin/bash {start_path} --allowed {",".join(app_storage["allowed_ips"])} --mode service
Restart=on-failure
RestartSec=10
# 增加启动限制参数
StartLimitInterval=60
StartLimitBurst=5


[Install]
WantedBy=multi-user.target   
""".strip()
    r1 = AppSysCmd.save_file_root(script, "/etc/systemd/system/SailInstaller.service")
    r2 = AppSysCmd.cmd_run_root("systemctl enable SailInstaller.service")

    if r1 and r2.returncode == 0:
        logger.info("自启动脚本已添加到/etc/systemd/system/SailInstaller.service")


def format_string_by_separator(input_str, separator, join, items_per_line):
    """
    根据指定的分隔符将字符串切分成列表，并按指定数量的元素换行。

    :param input_str: 要处理的原始字符串
    :param separator: 用于分隔字符串的字符
    :param items_per_line: 每行应包含的元素数量
    :return: 处理后的字符串
    """
    # 使用指定的分隔符切分字符串
    elements = re.split(separator, input_str)

    # 判断是否能对items_per_line整除
    if len(elements) % items_per_line != 0:
        return None

    # 初始化结果字符串
    result = ""

    # 遍历元素列表，按指定数量换行
    for i in range(0, len(elements), items_per_line):
        # 将当前行的元素拼接成一行，并添加到结果字符串中
        line_elements = elements[i:i + items_per_line]
        result += join.join(line_elements).strip() + "\n"

    # 返回结果字符串，去掉最后一行的换行符
    return result.strip()