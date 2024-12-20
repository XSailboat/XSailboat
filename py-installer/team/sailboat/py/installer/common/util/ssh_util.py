import os
import re

import paramiko
from pathlib import Path

from loguru import logger

from team.sailboat.py.installer.common.app_storage import AppStorage

app_storage = AppStorage()


class SSHConfigurator:
    def __init__(self):
        # 获取本机用户名
        self.local_username = app_storage["profile"]["sysUser"]
        # 生成SSH密钥对的路径

        self.private_key_path = f'/home/{self.local_username}/.ssh/id_rsa'
        self.public_key_path = f'/home/{self.local_username}/.ssh/id_rsa.pub'
        self.public_key = self.load_ssh_key_pair()

    def generate_ssh_key_pair(self):
        """生成SSH密钥对"""
        # 确保父目录存在
        parent_dir = os.path.dirname(self.private_key_path)
        Path(parent_dir).mkdir(parents=True, exist_ok=True)
        private_key = paramiko.RSAKey.generate(2048)
        private_key.write_private_key_file(self.private_key_path)
        public_key = f'ssh-rsa {private_key.get_base64()}'
        with open(self.public_key_path, 'w') as pub_key_file:
            pub_key_file.write(public_key)
        return public_key

    def load_ssh_key_pair(self):
        """读取现有的SSH密钥对"""
        if os.path.exists(self.private_key_path) and os.path.exists(self.public_key_path):
            with open(self.public_key_path, 'r') as pub_key_file:
                public_key = pub_key_file.read().strip()
            return public_key
        return None

    def ensure_ssh_directory_exists(self, client, username):
        """确保远程用户的 .ssh 目录存在"""
        home_dir = client.exec_command("echo ~")[1].read().decode().strip()
        ssh_dir = os.path.join(home_dir, ".ssh")

        stdin, stdout, stderr = client.exec_command(f"mkdir -p {ssh_dir}")
        error_output = stderr.read().decode().strip()
        if error_output:
            logger.info(f"创建 SSH 目录失败: {error_output}")
            return False
        else:
            logger.info(f"创建 SSH 目录成功: {ssh_dir}")
            return True

    def ensure_authorized_keys_exists(self, client, username):
        """确保远程用户的 authorized_keys 文件存在"""
        home_dir = client.exec_command("echo ~")[1].read().decode().strip()
        authorized_keys_path = os.path.join(home_dir, ".ssh", "authorized_keys")

        stdin, stdout, stderr = client.exec_command(f"touch {authorized_keys_path}")
        error_output = stderr.read().decode().strip()
        if error_output:
            logger.info(f"创建 authorized_keys 文件失败: {error_output}")
            return False
        else:
            logger.info(f"创建 authorized_keys 文件成功: {authorized_keys_path}")
            self.set_permissions(client, username)
            return True

    def set_permissions(self, client, username):
        """设置 .ssh 目录和 authorized_keys 文件的权限"""
        home_dir = client.exec_command("echo ~")[1].read().decode().strip()
        ssh_dir = os.path.join(home_dir, ".ssh")
        authorized_keys_path = os.path.join(home_dir, ".ssh", "authorized_keys")

        stdin, stdout, stderr = client.exec_command(f"chmod 700 {ssh_dir}")
        error_output = stderr.read().decode().strip()
        if error_output:
            logger.info(f"设置 SSH 目录权限失败: {error_output}")
            return False
        else:
            logger.info(f"设置 SSH 目录权限成功: {ssh_dir}")

        stdin, stdout, stderr = client.exec_command(f"chmod 600 {authorized_keys_path}")
        error_output = stderr.read().decode().strip()
        if error_output:
            logger.info(f"设置 authorized_keys 文件权限失败: {error_output}")
            return False
        else:
            logger.info(f"设置 authorized_keys 文件权限成功: {authorized_keys_path}")
            return True

    def check_and_add_public_key(self, host, username, password, public_key):
        """检查远程服务器上的 authorized_keys 文件，如果没有则添加"""
        client = paramiko.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

        try:
            # 使用密码认证连接远程服务器
            client.connect(host, username=username, password=password)
            logger.info(f"已连接到 {host} 用户为 {username} 使用密码")

            # 检查 .ssh 目录是否存在
            _, stdout_ssh_dir, stderr_ssh_dir = client.exec_command("test -d ~/.ssh && echo 'exists'")
            ssh_dir_exists = stdout_ssh_dir.read().decode().strip()
            error_output_ssh_dir = stderr_ssh_dir.read().decode().strip()
            if not ssh_dir_exists:
                logger.info(f"{host}上不存在.ssh目录,准备创建...")
                is_success = self.ensure_ssh_directory_exists(client, None)
                if is_success:
                    is_success = self.ensure_authorized_keys_exists(client, None)
                    logger.info(f"{host}创建.ssh文件成功!")
            else:
                # 检查 authorized_keys 文件是否存在
                _, stdout_auth_file, stderr_auth_file = client.exec_command(
                    "test -f ~/.ssh/authorized_keys && echo 'exists'")
                auth_file_exists = stdout_auth_file.read().decode().strip()
                error_output_auth_file = stderr_auth_file.read().decode().strip()
                if not auth_file_exists:
                    logger.info(f"{host}上不存在authorized_keys,准备创建...")
                    is_success = self.ensure_authorized_keys_exists(client, None)
                    if is_success:
                        logger.info(f"{host}创建authorized_keys文件成功!")

            # 检查远程服务器上的 authorized_keys 文件是否已经包含公钥
            _, stdout, stderr = client.exec_command(f"grep -Fx '{public_key}' ~/.ssh/authorized_keys")
            existing_key = stdout.read().decode().strip()
            error_output = stderr.read().decode().strip()
            if error_output:
                logger.info(f"执行命令失败: {error_output}")
                return False
            if existing_key:
                logger.info(f"公钥已存在于 {host} 上")
                return True
            else:
                # 公钥不存在，追加公钥
                client.exec_command(f"echo '{public_key}' >> ~/.ssh/authorized_keys")
                logger.info(f"公钥已添加到 {host} 的 ~/.ssh/authorized_keys 文件中")
                return True
        except Exception as e:
            logger.info(f"连接或检查/添加公钥失败: {e}")
            return False
        finally:
            client.close()

    def append_public_key_to_authorized_keys(self, host, username, public_key):
        """将公钥追加到远程服务器的 authorized_keys 文件中"""
        client = paramiko.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

        try:
            # 使用本地生成的私钥连接远程服务器
            local_private_key = paramiko.RSAKey.from_private_key_file(self.private_key_path)
            logger.info(f"从 {self.private_key_path} 加载私钥")

            client.connect(host, username=username, pkey=local_private_key)
            logger.info(f"已连接到 {host} 作为 {username} 使用私钥")

            # 检查远程服务器上的 authorized_keys 文件是否已经包含公钥
            _, stdout, stderr = client.exec_command(f"grep -Fx '{public_key}' ~/.ssh/authorized_keys")
            existing_key = stdout.read().decode().strip()
            error_output = stderr.read().decode().strip()
            if error_output:
                logger.info(f"执行命令失败: {error_output}")
                return False
            if existing_key:
                logger.info(f"公钥已存在于 {host} 上")
                return True
            else:
                # 公钥不存在，追加公钥
                client.exec_command(f"echo '{public_key}' >> ~/.ssh/authorized_keys")
                logger.info(f"公钥已添加到 {host} 的 ~/.ssh/authorized_keys 文件中")
                return True
        except Exception as e:
            logger.info(f"连接或添加公钥失败: {e}")
            return False
        finally:
            client.close()

    def configure_hosts(self, hosts_info):
        """配置多个远程主机"""
        if self.public_key is None:
            self.public_key = self.generate_ssh_key_pair()

        results = []
        for host_info in re.split('\\s+', hosts_info):
            parts = host_info.strip().split('@')
            if len(parts) != 3:
                logger.info(f"{host_info} 格式错误，期望格式为 username@hostname@password")
                results.append(False)
                continue

            remote_username, remote_host, remote_password = parts
            result_check_and_add = self.check_and_add_public_key(remote_host, remote_username, remote_password,
                                                                 self.public_key)
            result_append_public_key = self.append_public_key_to_authorized_keys(remote_host, remote_username,
                                                                                 self.public_key)
            results.append(result_check_and_add and result_append_public_key)
        return all(results)
