"""
系统级别命令
"""
import os
import subprocess
import threading

from loguru import logger

from team.sailboat.py.installer.common.app_storage import AppStorage

app_storage = AppStorage()
process = None


class ShellResult:
    def __init__(self, stdout, stderr, returncode):
        self.stdout = stdout
        self.stderr = stderr
        self.returncode = returncode


class AppSysCmd():

    @staticmethod
    def cmd_run(command, text=True):
        # 确保每次运行时都获取新的环境变量
        update_envs()
        env = os.environ.copy()
        process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE,
                                   preexec_fn=os.setsid, env=env)
        stdout, stderr = process.communicate()
        # 解码 stdout 和 stderr 为字符串
        stdout = stdout.decode('utf-8').strip()
        stderr = stderr.decode('utf-8').strip()
        if process.returncode == 0:
            stdout += f"\n{stderr}"
            stderr = ""
        # debug
        if stdout.strip() != "" or stderr.strip() != "":
            # logger.info(f"当前命令输出:\n{stdout}\n错误输出:{stderr}")
            pass
        app_storage["pre_output_pOpen"]["stdout_lines"] = [stdout]
        app_storage["pre_output_pOpen"]["stderr_lines"] = [stderr]
        result = ShellResult(stdout, stderr, process.returncode)
        return result

    @staticmethod
    def cmd_run_root(command, text=True):
        # 确保每次运行时都获取新的环境变量
        update_envs()
        env = os.environ.copy()
        env['PYTHONUNBUFFERED'] = '1'
        root_user = app_storage["profile"]["adminUser"]
        password = app_storage["profile"]["adminPswd"]

        process = subprocess.Popen([f'echo -n "{password}" | su - {root_user} -c "{command}"'], shell=True,
                                   stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE,
                                   preexec_fn=os.setsid, env=env)
        stdout, stderr = process.communicate()
        # 解码 stdout 和 stderr 为字符串
        stdout = stdout.decode('utf-8').replace("密码：", "").strip()
        stderr = stderr.decode('utf-8').replace("密码：", "").strip()
        if process.returncode == 0:
            stdout += f"\n{stderr}"
            stderr = ""
        # 将这条的输出结果保存到上下文中
        # debug
        if stdout.strip() != "" or stderr.strip() != "":
            # logger.info(f"当前命令输出:\n{stdout}\n错误输出:{stderr}")
            pass
        app_storage["pre_output_pOpen"]["stdout_lines"] = [stdout]
        app_storage["pre_output_pOpen"]["stderr_lines"] = [stderr]
        result = ShellResult(stdout, stderr, process.returncode)
        return result

    @staticmethod
    def cmd_run_Popen(command):
        process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                   preexec_fn=os.setsid)

        def read_output():
            logger.info(f"开始监听{command}命令的stdout流")
            while True:
                if not app_storage["pre_output_pOpen"]["running"]:
                    logger.info("stdout监听线程接收到停止信号，已停止")
                    break
                line = process.stdout.readline()
                if not line:
                    break
                app_storage["pre_output_pOpen"]["stdout_lines"].append(line.decode('utf-8').strip())
                if len(app_storage["pre_output_pOpen"]["stdout_lines"]) % 100 == 0:
                    logger.info(f' {command} 的stdout条数已经进展到了 {len(app_storage["pre_output_pOpen"]["stdout_lines"])} 条!')

        def read_error():
            logger.info(f"开始监听{command}命令的stderr流")
            while True:
                if not app_storage["pre_output_pOpen"]["running"]:
                    logger.info("stderr监听线程接收到停止信号，已停止")
                    break
                line = process.stderr.readline()
                if not line:
                    break
                app_storage["pre_output_pOpen"]["stderr_lines"].append(line.decode('utf-8').strip())
                if len(app_storage["pre_output_pOpen"]["stderr_lines"]) % 100 == 0:
                    logger.info(f' {command} 的stderr条数已经进展到了 {len(app_storage["pre_output_pOpen"]["stderr_lines"])} 条!')

        app_storage["pre_output_pOpen"]["running"] = True
        # 创建线程读取标准输出和标准错误
        stdout_thread = threading.Thread(target=read_output)
        stderr_thread = threading.Thread(target=read_error)

        stdout_thread.start()
        stderr_thread.start()

    @staticmethod
    def read_file_root(file_path):
        if AppSysCmd.exist_file(file_path):
            result = AppSysCmd.cmd_run_root(f"cat {file_path}")
            return result.stdout
        else:
            return None

    @staticmethod
    def change_own(username: str, paths: list, move_cmds=[]):
        result = []
        # 原记录备份,如果其中有一个文件没有修改成功,则全部回退为原来权限
        back = {}
        # 错误信息
        msg = ""
        for index in range(len(paths)):
            path = paths[index]
            auth = 755  # 默认权限
            if AppSysCmd.exist_file(path) or AppSysCmd.exist_dir(path):
                # 记录原路径的所有者
                r1 = AppSysCmd.cmd_run_root(f"stat -c \'%U\' {path}")
                r2 = AppSysCmd.cmd_run_root(f"stat -c \'%a\' {path}")
                back[path] = (r1.stdout, r2.stdout)
                auth = int(r2.stdout)
            # 如果有需要移动的
            if len(move_cmds) > index:
                AppSysCmd.cmd_run_root(move_cmds[index])

            r3 = AppSysCmd.cmd_run_root(f"chown -R {username}:{username} {path}")
            r4 = AppSysCmd.cmd_run_root(f"chmod -R {auth} {path}")
            if r3.returncode == 0 and r4.returncode == 0:
                result.append(True)
            else:
                msg = r3.stderr + "\n" + r4.stderr
                result.append(False)
                break

        if not all(result) or msg != "":
            # 其中一次有错误,回退
            for path, item in back.items():
                AppSysCmd.cmd_run_root(f"chown -R {item[0]}:{item[0]} {path}")
                AppSysCmd.cmd_run_root(f"chmod -R {int(item[1])} {path}")
            return msg
        else:
            return None

    @staticmethod
    def save_file_root(content, file_path):
        # 使用临时文件来写入内容，然后移动到目标位置
        temp_file_path = f"/home/{app_storage['profile']['sysUser']}/.installer_write.tmp"
        if not AppSysCmd.exist_dir(f"/home/{app_storage['profile']['sysUser']}"):
            temp_file_path = "/root/.installer_write.tmp"
        try:
            # 写入临时文件
            with open(temp_file_path, 'w') as temp_file:
                temp_file.write(content)
            # 使用 su 移动临时文件到目标位置
            move_command = f"su - root -c 'mv {temp_file_path} {file_path}'"
            # 修改为root权限
            AppSysCmd.change_own("root", [file_path], [move_command])
            return True
        except subprocess.CalledProcessError as e:
            logger.error(f"Error: {e}")
            return False
        finally:
            # 清理临时文件
            if os.path.exists(temp_file_path):
                os.remove(temp_file_path)

    @staticmethod
    def read_file(file_path):
        if AppSysCmd.exist_file(file_path):
            cmd1 = f'cat {file_path}'
            result = AppSysCmd.cmd_run(cmd1)
            return result.stdout
        else:
            return None

    @staticmethod
    def save_file(content, file_path):
        # 使用临时文件来写入内容，然后移动到目标位置
        temp_file_path = f"/home/{app_storage['profile']['sysUser']}/.installer_write.tmp"
        if not AppSysCmd.exist_dir(f"/home/{app_storage['profile']['sysUser']}"):
            temp_file_path = "/root/.installer_write.tmp"

        try:
            # 写入临时文件
            with open(temp_file_path, 'w') as temp_file:
                temp_file.write(content)
            # 使用 su 移动临时文件到目标位置
            move_command = f"su - root -c 'mv {temp_file_path} {file_path}'"
            AppSysCmd.change_own(app_storage["profile"]["sysUser"], [file_path], [move_command])
            return True
        except subprocess.CalledProcessError as e:
            logger.error(f"Error: {e}")
            return False
        finally:
            # 清理临时文件
            if os.path.exists(temp_file_path):
                os.remove(temp_file_path)

    @staticmethod
    def exist_file(path):
        """获取指定文件路径是否存在,存在则返回Ture,否则返回False"""
        result = AppSysCmd.cmd_run_root(f"test -f {path} && echo 'exists'")
        return True if result.stdout else False

    @staticmethod
    def exist_dir(path):
        """获取指定文件目录是否存在,存在则返回Ture,否则返回False"""
        result = AppSysCmd.cmd_run_root(f"test -d {path} && echo 'exists'")
        return True if result.stdout else False


def update_envs():
    # 获取当前环境变量
    env = os.environ.copy()

    # 设置 PYTHONUNBUFFERED
    env['PYTHONUNBUFFERED'] = '1'

    # 加载 /etc/profile 中的环境变量
    load_env_command = "source /etc/profile && env"
    load_env_process = subprocess.Popen(load_env_command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                        env=env)
    stdout, stderr = load_env_process.communicate()

    # 更新环境变量
    new_env = {}
    for line in stdout.decode('utf-8').splitlines():
        key, value = line.split('=', 1)
        new_env[key] = value

    # 更新当前环境变量
    os.environ.update(new_env)