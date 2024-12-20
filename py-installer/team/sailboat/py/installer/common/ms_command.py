import getopt
import json
import re
import shlex
import time

import os
from loguru import logger

from team.sailboat.py.installer.common.app_storage import AppStorage
from team.sailboat.py.installer.common.app_util.app_file import AppFile
from team.sailboat.py.installer.common.app_util.app_path import AppPath
from team.sailboat.py.installer.common.app_util.app_sys_command import AppSysCmd
from team.sailboat.py.installer.common.app_util.app_sys_config import AppSysConfig
from team.sailboat.py.installer.common.app_variable import AppVariable

app_storage = AppStorage()
app_variable = AppVariable()
# 命令行参数解析
command_define = {
    "x_cd": [],
    "x_su": ["u:p:", ["username=", "password="], {"-u": "username", "-p": "password"}],
    "x_set_hostname": [],
    "x_set_limit": [],
    "x_add_hosts": [],
    "x_close": [],
    "x_unzip": ["s:t:e:", ["source=", "target=", "expect="], {"-s": "source", "-t": "target", "-e": "expect"}],
    "x_untar": ["s:t:e:", ["source=", "target=", "expect="], {"-s": "source", "-t": "target", "-e": "expect"}],
    "x_add_ssh": [],
    "x_edit": ["t:p:d:", ["type=", "path=", "data="], {"-t": "type", "-p": "path", "-d": "data"}],
    "x_add_envs": [],
    "x_connect": [],
    "x_contain": [],
    "x_set_telegraf": ["p:l:", ["path=", "listen="], {"-p": "path", "-l": "listen"}],
    "x_flag": [],
    "x_set_iptables": ["h:m:o:", ["hosts=", "modules=", "outer_clients="],
                       {"-h": "hosts", "-m": "modules", "-o": "outer_clients"}],
    "x_sql": ["t:u:p:d:", ["type=", "username=", "password=", "database="],
              {"-u": "username", "-p": "password", "-d": "database", "-t": "type"}],
    "x_expect_std": ["w:s:", ["wait=", "string="], {"-w": "wait", "-s": "string"}],
    "x_expect_err": ["w:s:", ["wait=", "string="], {"-w": "wait", "-s": "string"}]
}
custom_command_define = {}


class CommandProcessor:
    def execute_command(self, command_str, *args, **kwargs):
        # 提取命令名和参数
        # command_parts = self.parse_command(command_str)
        command_parts = None
        try:
            command_str = self.parse_variable_injection(command_str)
            command_parts = shlex.split(command_str)
        except Exception as e:
            return {"code": False, "msg": str(e)}
        command_name = command_parts[0]

        # 判断是否是数据库命令
        result = self.is_exec_sql(command_str)
        if type(result) == dict:
            return result

        method = None
        # 判断是否是系统的自定义方法
        method = getattr(CommandHelper, command_name, None)
        if method is None:
            method = getattr(CustomCommandHelper, command_name, None)

        if command_name not in command_define.keys() and command_name not in custom_command_define.keys():  # 不是自定义命令

            # 判断当前用户，执行对应权限的命令
            if not app_storage.__contains__("current_user"):
                # 未切换用户默认为平台用户
                app_storage["current_user"] = app_storage["profile"]["sysUser"]

            if app_storage["current_user"] != app_storage["profile"]["sysUser"] and app_storage["current_user"] != \
                    app_storage["profile"]["adminUser"]:
                app_storage["current_user"] = app_storage["profile"]["sysUser"]

            if app_storage["current_user"] == app_storage["profile"]["sysUser"]:
                if str(command_str).strip().endswith("&"):
                    AppSysCmd.cmd_run_Popen(command_str)
                    logger.info(f"【命令执行】-用户:{app_storage['current_user']} 命令:{command_str} 执行成功!")
                    return {"code": True, "msg": ""}

                pipe_cmd = self.parse_pipe(command_str)
                if type(pipe_cmd) == list:
                    r = []
                    AppSysCmd.cmd_run(pipe_cmd[0])
                    for cmd in pipe_cmd[1:]:
                        result = self.execute_command(cmd)
                        r.append(result["code"])
                    return {"code": all(r), "msg": "发生错误！"}

                result = AppSysCmd.cmd_run(command_str)
                # 判断是否成功
                if result.returncode == 0:
                    logger.info(f"【命令执行】-用户:{app_storage['current_user']} 命令:{command_str} 执行成功!")
                    return {"code": True, "msg": ""}
                else:
                    return {"code": False,
                            "msg": result.stderr}
            elif app_storage["current_user"] == app_storage["profile"]["adminUser"]:
                if str(command_str).strip().endswith("&"):
                    AppSysCmd.cmd_run_Popen(command_str)
                    logger.info(f"【命令执行】-用户:{app_storage['current_user']} 命令:{command_str} 执行成功!")
                    return {"code": True, "msg": ""}

                pipe_cmd = self.parse_pipe(command_str)
                if type(pipe_cmd) == list:
                    r = []
                    AppSysCmd.cmd_run_root(pipe_cmd[0])
                    for cmd in pipe_cmd[1:]:
                        result = self.execute_command(cmd)
                        r.append(result["code"])
                    return {"code": all(r), "msg": "发生错误！"}
                result = AppSysCmd.cmd_run_root(command_str)
                # 判断是否成功
                if result.returncode == 0:
                    logger.info(f"【命令执行】-用户:{app_storage['current_user']} 命令:{command_str} 执行成功!")
                    return {"code": True, "msg": ""}
                else:
                    return {"code": False,
                            "msg": result.stderr}
            return {"code": True, "msg": f"未知的命令{command_name}"}
        # 调用方法
        # 提取参数
        params = None
        if len(command_parts) > 1:
            if command_name in command_define:
                if len(command_define[command_name]) != 0:
                    params = self.parse_params(command_parts[1:], command_define[command_name])
                else:
                    params = "\n".join(command_parts[1:]).strip()
            elif command_name in custom_command_define:
                if len(custom_command_define[command_name]) != 0:
                    params = self.parse_params(command_parts[1:], custom_command_define[command_name])
                else:
                    params = "\n".join(command_parts[1:]).strip()

        logger.info(f"命令参数解析:{params}")
        params = self.parse_variable_injection(params)
        print(params)
        return method(params)

    def parse_params(self, params_part, rule):
        # 使用 getopt 来解析分割后的参数
        try:
            options, args = getopt.getopt(params_part, rule[0], rule[1])
        except getopt.GetoptError as err:
            # 打印帮助信息并退出
            print(str(err))
            return None
        params = {}
        for opt, arg in options:
            long_opt = rule[2].get(opt, str(opt).replace("-", ""))  # 获取长选项名称
            params[long_opt] = arg
        return params if len(params) != 0 else params_part

    def parse_params_old(self, params_part):
        """参数解析"""
        # 参数分为三种
        # 第一种是命令的,不带参数的例如:stop_ipv6
        # 第二种是一个参数的,不需要带参数名例如:close ipv6
        # 第三种是多个参数的,需要带参数名例如:cmd -p 123 -s 12345
        if len(params_part) == 1:
            return params_part[0]

        if len(params_part) % 2 == 0:
            params = {}
            # 判断个数是否是双数
            # 遍历列表中的元素
            for i in range(0, len(params_part), 2):
                key = params_part[i]
                # 检查是否有对应的值
                if i + 1 < len(params_part):
                    value = params_part[i + 1]
                    params[key] = value
            return params

    def parse_command(self, command_str):
        result = []
        in_quotes = False
        escape_next = False
        current_token = ""

        for char in command_str:
            if escape_next:
                current_token += char
                escape_next = False
                continue

            if char == '\\':
                escape_next = True
                continue

            if char == '"':
                if in_quotes:
                    # 结束双引号内的内容
                    in_quotes = False
                    # 移除当前令牌两侧的双引号
                    current_token = current_token.strip('"')
                    result.append(current_token)
                    current_token = ""
                else:
                    in_quotes = True
            elif char == ' ' and not in_quotes:
                if current_token:
                    result.append(current_token.strip('"'))  # 移除双引号
                    current_token = ""
                continue
            else:
                current_token += char

        if current_token:
            result.append(current_token.strip('"'))  # 移除双引号
        return result

    def filter(self, command_str: str, filter_list: list, method=None):
        """

        """
        command_str = re.sub(r'\s+', command_str.strip())  # 将多余的空格都转换为一个空格
        # 判断是否是属于集合中的命令
        for cmd in filter_list:
            if command_str in cmd:
                if method:  # 自定义的命令过滤方法
                    return method(cmd)
                return True
        return False

    def is_exec_sql(self, cmd):
        """判断是否是执行SQL"""
        if app_storage["sql"]["mode"]:
            exec_sql = ""
            if app_storage["sql"]["type"] == "mysql":
                if app_storage['sql']['database'] != "":
                    exec_sql = f"mysql -u {app_storage['sql']['username']} -p'{app_storage['sql']['password']}' -D {app_storage['sql']['database']} -e \"{cmd}\""
                else:
                    exec_sql = f"mysql -u {app_storage['sql']['username']} -p'{app_storage['sql']['password']}' -e \"{cmd}\""

            elif app_storage["sql"]["type"] == "taos":
                if app_storage['sql']['database'] != "":
                    exec_sql = f"taos -u {app_storage['sql']['username']} -p'{app_storage['sql']['password']}' -d {app_storage['sql']['database']} -s \"{cmd}\""
                else:
                    exec_sql = f"taos -u {app_storage['sql']['username']} -p'{app_storage['sql']['password']}' -s \"{cmd}\""
            elif app_storage["sql"]["type"] == "pgsql":
                os.environ['PGPASSWORD'] = app_storage['sql']['password']
                if app_storage['sql']['database'] != "":
                    exec_sql = f"psql -U {app_storage['sql']['username']} -d {app_storage['sql']['database']} -c \"{cmd}\""
                else:
                    exec_sql = f"psql -U {app_storage['sql']['username']} -c \"{cmd}\""
            else:
                logger.info(f'【SQL命令执行】- 未知的数据库类型{app_storage["sql"]["type"]}')
                return {"code": False,
                        "msg": '未知的数据库类型{app_storage["sql"]["type"]}'}
            result = AppSysCmd.cmd_run(exec_sql)
            # 判断是否成功
            if result.returncode == 0:
                logger.info(f"【SQL命令执行】- 命令:{cmd} 执行成功!")
                return {"code": True, "msg": ""}
            else:
                return {"code": False,
                        "msg": result.stderr}
        return False

    def parse_pipe(self, cmd):
        split = re.split("\s*&&\s*", cmd)
        if len(split) == 1:
            return cmd

        shell = []
        my_shell = []
        for c in split:
            command_parts = shlex.split(c)
            command_name = command_parts[0]
            # 判断是否是系统的自定义方法
            method = getattr(CommandHelper, command_name, getattr(CustomCommandHelper, command_name, None))
            if method is None:
                shell.append(c.strip())
            else:
                my_shell.append(c.strip())

        return [" && ".join(shell)] + my_shell

    def parse_variable_injection(self, params):
        if type(params) == str:
            return self.__replace_placeholders(params, app_variable)
        elif type(params) == dict:
            for key, value in dict(params).items():
                params[key] = self.__replace_placeholders(value, app_variable)
            return params

    def __replace_placeholders(self, template_string, variables):
        # 定义正则表达式模式，匹配 ${...} 格式的占位符
        pattern = r'\$\{([^}]+)\}'

        # 使用 re.sub 进行替换
        def replacer(match):
            key = match.group(1)
            return str(variables.get_or_default(key, match.group(0)))  # 如果键不存在，则返回原字符串

        result_string = re.sub(pattern, replacer, template_string)
        return result_string


# 其他定义命令
class CustomCommandHelper:
    pass


# 系统定义命令
class CommandHelper:

    @staticmethod
    def x_cd(params):
        app_storage["current_location"] = params
        return {"code": True, "msg": ""}

    @staticmethod
    def x_su(params):
        """切换用户"""

        # 切换成管理员用户时需要密码,从管理员切换为系统用户则不需要密码
        username = None
        pswd = None
        current_user = app_storage["current_user"] if app_storage.__contains__("current_user") else \
            app_storage["profile"]["sysUser"]
        if current_user not in [app_storage["profile"]["adminUser"], app_storage["profile"]["sysUser"]]:
            current_user = app_storage["profile"]["sysUser"]
        app_storage["current_user"] = current_user
        logger.info(f"当前用户为:{current_user}")
        if type(params) == dict:
            if "username" not in params:
                return {"code": False, "msg": "缺少参数名 -u"}
            if "password" not in params:
                return {"code": False, "msg": "缺少参数名 -p"}
            username = params["username"]
            pswd = params["password"]
        else:
            if params == None:
                return {"code": False, "msg": "缺少参数"}
            username = params

        if current_user == username:
            return {"code": True, "msg": "用户切换成功!"}

        # 如果是切换普通用户,判断是否是从管理员切换的
        if current_user == app_storage["profile"]["adminUser"] and \
                username == app_storage["profile"]["sysUser"]:
            app_storage["current_user"] = username
            logger.info(f"【命令执行】-切换用户,执行成功！当前用户为:{app_storage['current_user']}")
            return {"code": True, "msg": "用户切换成功!"}

        elif current_user == app_storage["profile"]["sysUser"] and \
                username == app_storage["profile"]["adminUser"]:
            # 如果是想从普通用户切换到管理员用户
            # 判断密码是否正确
            if pswd == app_storage["profile"]["adminPswd"]:
                app_storage["current_user"] = username
                logger.info(f"【命令执行】-切换用户,执行成功！当前用户为:{app_storage['current_user']}")
                return {"code": True, "msg": "用户切换成功!"}
            else:
                return {"code": False, "msg": "用户切换失败,密码错误!"}
        else:
            logger.info(f"【命令执行】-切换用户,执行失败,未知的用户名:{username}!")
            return {"code": False, "msg": f"用户切换失败,未知的用户名:{username}！"}

    @staticmethod
    def stop_virtual_network(params):
        """关闭虚拟网卡"""
        result = AppSysConfig.stop_virtual_network()
        return {"code": result, "msg": "" if result else "关闭虚拟网卡失败！"}

    @staticmethod
    def stop_ipv6(params):
        """关闭IPv6"""
        result = AppSysConfig.stop_ipV6()
        return {"code": result, "msg": "" if result else "关闭IPv6失败！"}

    @staticmethod
    def x_close(params):
        """关闭命令"""
        if params == None:
            return {"code": False, "msg": "close命令没有输入参数!"}

        if str(params).lower() == "ipv6":
            # 关闭ipv6
            result = AppSysConfig.stop_ipV6()
            return {"code": result, "msg": "" if result else "关闭IPv6失败！"}
        elif str(params).lower() == "v_network":
            # 关闭虚拟网卡
            result = AppSysConfig.stop_virtual_network()
            return {"code": result, "msg": "" if result else "关闭虚拟网卡失败！"}
        elif str(params).lower() == "firewall":
            # 关闭防火墙
            result = AppSysConfig.stop_firewall()
            return {"code": result, "msg": "" if result else "关闭防火墙失败！"}
        elif str(params).lower() == "selinux":
            # 关闭SELINUX
            result = AppSysConfig.close_selinux()
            return {"code": result, "msg": "" if result else "关闭SELINUX失败！"}

    @staticmethod
    def x_add_hosts(params):
        """添加主机映射"""
        result = AppSysConfig.sys_hosts(params)
        return {"code": result, "msg": "" if result else "修改主机映射失败!"}

    @staticmethod
    def x_set_hostname(params):
        """修改主机名"""
        result = AppSysConfig.sys_hostname(params)
        return {"code": result, "msg": "" if result else "修改主机名失败!"}

    @staticmethod
    def x_set_limit(params):
        """修改文件最大句柄数"""
        result = AppSysConfig.set_limit(params)
        return {"code": result, "msg": "" if result else "修改文件最大句柄数失败!"}

    @staticmethod
    def x_unzip(params):
        """自定义解压zip文件"""
        source = None
        target = None
        expect = None
        if type(params) == dict:
            if "source" not in params:
                return {"code": False, "msg": "缺少参数名 -s"}
            if "target" not in params:
                return {"code": False, "msg": "缺少参数名 -t"}
            source = params["source"]
            target = params["target"]
            expect = dict(params).get("expect", None)
        else:
            if params == None:
                return {"code": False, "msg": "缺少参数"}
        result = AppPath.unzip(source, target, expect)
        return {"code": result, "msg": "" if result else "文件解压失败!"}

    @staticmethod
    def x_untar(params):
        """自定义解压tar.gz文件"""
        source = None
        target = None
        expect = None
        if type(params) == dict:
            if "source" not in params:
                return {"code": False, "msg": "缺少参数名 -s"}
            if "target" not in params:
                return {"code": False, "msg": "缺少参数名 -t"}
            source = params["source"]
            target = params["target"]
            expect = dict(params).get("expect", None)
        else:
            if params == None:
                return {"code": False, "msg": "缺少参数"}
        try:
            result = AppPath.untar_file(source, target, expect)
            return {"code": result, "msg": "" if result else "文件解压失败!"}
        except FileNotFoundError as e:
            return {"code": False, "msg": str(e)}

    @staticmethod
    def x_add_ssh(params):
        """SSH免密配置"""
        result = AppSysConfig.add_ssh(params)

        return {"code": result, "msg": "" if result else "免密配置失败!"}

    @staticmethod
    def x_add_envs(params):
        result = AppSysConfig.add_env2(params)
        return {"code": result, "msg": "" if result else "环境变量配置失败!"}

    @staticmethod
    def x_edit(params):
        """文件修改
            x_edit -t kv -p path -d data   # key1='hello world' key2=value2
            x_edit -t kv-s -p path -d data   # key1='hello world' key2=value2
            x_edit -t kv-tag -p path -d data   # [tag1].key1='hello world' [tag1].key2=value2
            x_edit -t xml-p -p path -d data # key1=value1 key2=value2
            x_edit -t yaml -p path -d data # xxx.xxx.key1=[1,2,3,4] xxx.xxx.key2='hello world'
        """
        if not type(params) == dict:
            return {"code": False, "msg": "edit命令参数错误"}

        if "type" not in params:
            return {"code": False, "msg": "edit命令未指定文件类型"}

        root = is_root()
        # data = convert_str(params["data"])
        data = convert_str_new(params["data"])
        if params["type"] == "kv":
            flag, msg = AppFile.update_property_conf(params["path"], data, root)
            return {"code": flag, "msg": msg}
        elif params["type"] == "kv-s":
            flag, msg = AppFile.update_property_conf(params["path"], data, root, separator="\\s+")
            return {"code": flag, "msg": msg}
        elif params["type"] == "kv-tag":
            flag, msg = AppFile.update_property_conf(params["path"], data, root, tag=True)
            return {"code": flag, "msg": msg}
        elif params["type"] == "xml-p":
            flag, msg = AppFile.update_property_xml(params["path"], data, root)
            return {"code": flag, "msg": msg}
        elif params["type"] == "yaml":
            flag, msg = AppFile.update_property_yaml(params["path"], data, root)
            return {"code": flag, "msg": msg}
        else:
            return {"code": False, "msg": f'x_edit未知的文件类型 {params["type"]}'}

    @staticmethod
    def x_connect(params):
        """验证给定的端口哪些是正常的"""
        ports = [int(port) for port in re.split("\\s+", params)]
        result = []
        for port in ports:
            result.append(AppSysConfig.is_port_open(port))
        # 如果全部成功了那么就返回True,其中有一个失败则返回False
        if all(result):
            return {"code": True, "msg": ""}
        else:
            return {"code": False, "msg": result}

    @staticmethod
    def x_contain(params):
        """验证上一条命令的输出是否包含给定的字符串"""
        if not app_storage.__contains__("pre_output") or app_storage["pre_output"] == "":
            return {"code": True, "msg": "上一条命令无正确输出结果"}
        if not app_storage.__contains__("pre_output_error"):
            return {"code": False, "msg": "不能找到上一条命令的输出结果!"}

        is_contain = app_storage["pre_output_error"] == ""
        return {"code": is_contain, "msg": "" if is_contain else f"上一条命令结果出现错误!"}

    @staticmethod
    def x_set_telegraf(params):
        path = None
        listen = None
        if type(params) == dict:
            if "path" not in params:
                return {"code": False, "msg": "缺少参数名 -p/--path"}
            if "listen" not in params:
                return {"code": False, "msg": "缺少参数名 -l/--listen"}
            path = params["path"]
            listen = params["listen"]
        else:
            if params == None:
                return {"code": False, "msg": "缺少参数"}
        listen_list = re.split("\\s+", listen)
        result, msg = AppSysConfig.set_telegraf(path, listen_list)
        return {"code": result, "msg": "" if result else f"修改telegraf失败"}

    @staticmethod
    def x_flag(params):
        param_list = str(params).split()
        if len(param_list) != 2:
            return {"code": False, "msg": f"x_flag命令需要接收的参数值为2,实际接收的长度为{len(param_list)}"}

        if param_list[0] not in ["get", "set"]:
            return {"code": False, "msg": f"x_flag命令只支持get/set,未知的{param_list[0]}"}
        flag_path = param_list[1]
        if param_list[0] == "get":
            result = AppSysCmd.exist_file(flag_path)
            return {"code": not result, "msg": "" if not result else f"标志位文件已存在!"}
        elif param_list[0] == 'set':
            result = AppSysCmd.save_file("", flag_path)
            return {"code": result, "msg": "" if result else f"设置标记文件失败!"}

    @staticmethod
    def x_set_iptables(params):
        """设置防火墙配置"""
        hosts = convert_str_new(params["hosts"])
        modules = dict(json.loads(params["modules"]))
        outer_clients = json.loads(params["outer_clients"])
        # 将本服务添加到modules中
        modules["SailPyInstaller"] = {"hosts": [app_storage["profile"]["name"]], "ports": [12205]}

        profile = {"hosts": hosts, "apps": modules, "outer_clients": outer_clients}
        flag, msg = AppSysConfig.set_iptables(profile)
        return {"code": flag, "msg": "" if flag else msg}

    @staticmethod
    def x_sql(params):
        """SQL执行"""
        if type(params) == dict:
            if "type" not in params:
                return {"code": False, "msg": "缺少参数名 -t"}
            if "username" not in params:
                return {"code": False, "msg": "缺少参数名 -u"}
            if params["type"] != "pgsql" and "password" not in params:
                return {"code": False, "msg": "缺少参数名 -p"}
        # 只要运行此条命令后，将会开启SQL命令执行模式
        app_storage["sql"] = {"mode": True, "username": params["username"],
                              "password": dict(params).get("password", ""),
                              "database": dict(params).get("database", ""), "type": params["type"]}

        return {"code": True, "msg": ""}

    @staticmethod
    def x_expect_std(params):
        wait = dict(params).get("wait", 3)
        string = dict(params).get("string", None)
        if string is None:
            return {"code": False, "msg": "缺少参数名 -s"}
        time.sleep(int(wait))  # 等待

        # 取出标准输出流中的字符串
        result = string in ("\n".join(app_storage["pre_output_pOpen"]["stdout_lines"]))
        count = len("\n".join(app_storage["pre_output_pOpen"]["stdout_lines"]))
        # 清空
        app_storage["pre_output_pOpen"]["running"] = False
        app_storage["pre_output_pOpen"]["stdout_lines"] = []
        return {"code": result, "msg": "" if result else f"标准输出流中的输出字符数量为:{count},并未包含指定字符串:{string}"}

    @staticmethod
    def x_expect_err(params):
        wait = dict(params).get("wait", 3)
        string = dict(params).get("string", None)
        if string is None:
            return {"code": False, "msg": "缺少参数名 -s"}
        time.sleep(int(wait))  # 等待
        # 取出错误输出流中的字符串
        result = string in ("\n".join(app_storage["pre_output_pOpen"]["stderr_lines"]))
        count = len("\n".join(app_storage["pre_output_pOpen"]["stderr_lines"]))
        # 清空
        app_storage["pre_output_pOpen"]["running"] = False
        app_storage["pre_output_pOpen"]["stderr_lines"] = []
        return {"code": result, "msg": "" if result else f"错误输出流中的输出字符数量为:{count},并未包含指定字符串:{string}"}


# 自定义命令装饰器
def custom_cmd(name: str, params_rule: list):
    def decorator(func):
        custom_command_define[name] = params_rule
        setattr(CustomCommandHelper, name, staticmethod(func))
        return func

    return decorator


# 判断当前用户是否是管理员用户
def is_root():
    return app_storage["current_user"] == app_storage["profile"]["adminUser"]


def convert_str_to_list(value: str):
    # 去除多余的空格和方括号
    value = value.strip()[1:-1].strip()
    # 分割字符串，使用逗号作为分隔符
    elements = [elem.strip() for elem in value.split(',')]
    return elements


def convert_str_new(data_str: str):
    # 使用正则表达式匹配键值对，包括带引号的值
    pattern = r'''([^\s=]+)\s*=\s*("[^"]*"|'[^']*'|[^&]+)'''
    matches = re.findall(pattern, data_str)
    result = {}

    for match in matches:
        key = match[0].strip()
        value = match[1].strip()
        # 处理列表形式的值
        if value.startswith("[") and value.endswith("]"):
            try:
                result[key] = convert_str_to_list(value)
            except (ValueError, SyntaxError) as e:
                logger.error(f"解析 {key} 失败: {e}")
                result[key] = value  # 如果解析失败，保留原始字符串
        else:
            if "&" in value:
                value = value.strip("'").strip('"')
            result[key] = value
    return result
