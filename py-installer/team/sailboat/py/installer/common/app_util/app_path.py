import os.path
import shutil
import sys
import tarfile
import zipfile
from pathlib import Path

from loguru import logger

from team.sailboat.py.installer.common.app_util.app_sys_command import AppSysCmd


class AppPath():
    """
        应用目录定位
    """

    @staticmethod
    def find_project_root():
        """定位到main.py所在的路径"""
        script_name = os.path.basename(sys.argv[0])
        if script_name == 'main.py':
            return os.path.dirname(os.path.abspath(sys.argv[0]))
        else:
            path = AppPath.find_file("main.py", os.path.dirname(os.getcwd()))

            if path:
                return path
        raise FileNotFoundError("无法找到主文件脚本路径")

    @staticmethod
    def get_data_file():
        """"获取data目录所在的相对路径"""
        return AppPath.find_directory_by_name("data")

    @staticmethod
    def find_directory_by_name(directory_name):
        """在项目中查找指定文件名对于main的相对路径"""

        start_dir = os.path.dirname(AppPath.find_project_root())
        for root, dirs, files in os.walk(start_dir):
            if directory_name in dirs:
                return os.path.relpath(os.path.join(root, directory_name), start=AppPath.find_project_root())
        return None

    @staticmethod
    def get_directory_size(directory):
        total_size = 0
        for dirpath, dirnames, filenames in os.walk(directory):
            for filename in filenames:
                filepath = os.path.join(dirpath, filename)
                total_size += os.path.getsize(filepath)
        return total_size

    @staticmethod
    def get_file_size(file_path):
        """获取本地指定路径文件的大小"""
        if os.path.exists(file_path):
            return os.path.getsize(file_path)
        return 0

    @staticmethod
    def format_file_size(byte_count):
        """Format the file size in human-readable form."""
        units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
        index = 0
        while byte_count >= 1024 and index < len(units) - 1:
            byte_count /= 1024
            index += 1

        return f"{round(byte_count, 2)} {units[index]}"

    @staticmethod
    def find_file_top_down(starting_path, target_filename):
        """
        从指定路径开始，逐层向上查找指定的文件，直到根目录。

        :param starting_path: 开始查找的路径
        :param target_filename: 要查找的文件名
        :return: 如果找到文件，则返回其绝对路径；否则返回 None
        """
        # 检查当前目录
        for entry in os.listdir(starting_path):
            full_path = os.path.join(starting_path, entry)
            if entry == target_filename:
                return os.path.abspath(full_path)

        # 获取父目录
        parent_dir = os.path.dirname(starting_path)

        # 如果当前目录已经是根目录，则停止查找
        if parent_dir == starting_path:
            return None

        # 递归查找父目录
        return AppPath.find_file_top_down(parent_dir, target_filename)

    @staticmethod
    def find_path_with_files(base_path, items):
        """
        递归查找指定路径中包含所有指定文件或目录的路径。

        :param base_path: 根路径
        :param items: 包含文件或目录名称的列表
        :return: 包含所有指定文件或目录的路径，如果没有找到则返回 None
        """
        for root, dirs, files in os.walk(base_path):
            all = set(dirs + files)
            items = set(items)
            if items.issubset(all):
                return root

        return None

    @staticmethod
    def check_path_exists(path):
        return os.path.exists(path)

    # 移动目录或文件到指定目录下
    @staticmethod
    def move(source, target):
        """移动文件或目录到目标位置"""
        shutil.move(source, target)

    # 复制目录或文件到指定目录下
    @staticmethod
    def copy(source, target):
        """复制文件或目录到目标位置"""
        if os.path.isdir(source):
            shutil.copytree(source, os.path.join(target, os.path.basename(source)))
        else:
            shutil.copy2(source, target)

    # 从指定目录查找指定文件在什么位置
    @staticmethod
    def find_file(filename, directory=None):
        """在指定目录及其子目录下查找指定文件"""
        if directory is None:
            directory = AppPath.find_project_root()

        for root, dirs, files in os.walk(directory):
            if filename in files:
                return os.path.join(root, filename)
        return None

    # 创建目录，可以递归创建(绝对路径)
    @staticmethod
    def create_dir(path):
        """创建目录，如果父目录不存在则递归创建"""
        os.makedirs(path, exist_ok=True)

    @staticmethod
    def get_dir_list(directory_path):
        """获取指定路径下的所有文件名(不包括子目录)"""
        # 检查路径是否存在
        if not os.path.exists(directory_path):
            logger.info(f"路径{directory_path}不存在!")
            return []

        # 获取目录下的所有文件
        return os.listdir(directory_path)

    @staticmethod
    def unzip(zip_filepath, dest_directory, expect=None):
        # 创建目标目录如果它不存在
        if not os.path.exists(dest_directory):
            os.makedirs(dest_directory)

        with zipfile.ZipFile(zip_filepath, 'r') as zip_ref:
            # 解压所有内容到指定的目录
            zip_ref.extractall(dest_directory)

        # 获取解压后的文件路径
        listdir = os.listdir(dest_directory)
        if expect is None:
            path_split = str(zip_filepath).split("/")
            if path_split[-1] in listdir:
                listdir.remove(path_split[-1])

            if len(listdir) == 1:
                if os.path.isdir(f"{dest_directory}/{listdir[0]}"):
                    # 移动
                    AppSysCmd.cmd_run_root(f"mv {dest_directory}/{listdir[0]}/* {dest_directory}")
                    AppSysCmd.cmd_run_root(f"rm -rf {dest_directory}/{listdir[0]}")
                else:
                    # 移动
                    AppSysCmd.cmd_run_root(f"mv {dest_directory}/{listdir[0]} {dest_directory}")
                    AppSysCmd.cmd_run_root(f"rm -rf {dest_directory}/{listdir[0]}")
        else:
            dir_names = [dir.strip() for dir in str(expect).split(",")]
            path = AppPath.find_path_with_files(dest_directory, dir_names)
            print(dir_names, path)
            # 移动
            if path is not None:
                AppSysCmd.cmd_run_root(f"mv {path}/* {dest_directory}")
                AppSysCmd.cmd_run_root(f"rm -rf {dest_directory}/{listdir[0]}")
            else:
                # 没有找到预期文件，删除已经解压的
                AppSysCmd.cmd_run_root(f"rm -rf {dest_directory}")
                return False
        return True

    @staticmethod
    def untar_file(tar_path, dest_directory, expect=None):
        # 创建目标目录如果它不存在
        # 可以解压.tar, .tar.bz2, .tar.xz .tar.gz等
        if not os.path.exists(dest_directory):
            os.makedirs(dest_directory)
        # 使用with语句打开tar文件
        with tarfile.open(tar_path, 'r:*') as tar:
            # 解压所有内容到指定的目录
            tar.extractall(path=dest_directory)

        # 获取解压后的文件路径
        listdir = os.listdir(dest_directory)
        if expect is None:
            path_split = str(tar_path).split("/")
            if path_split[-1] in listdir:
                listdir.remove(path_split[-1])

            if len(listdir) == 1:
                if os.path.isdir(f"{dest_directory}/{listdir[0]}"):
                    # 移动
                    AppSysCmd.cmd_run_root(f"mv {dest_directory}/{listdir[0]}/* {dest_directory}")
                    AppSysCmd.cmd_run_root(f"rm -rf {dest_directory}/{listdir[0]}")
                else:
                    # 移动
                    AppSysCmd.cmd_run_root(f"mv {dest_directory}/{listdir[0]} {dest_directory}")
                    AppSysCmd.cmd_run_root(f"rm -rf {dest_directory}/{listdir[0]}")
        else:
            dir_names = [dir.strip() for dir in str(expect).split(",")]
            path = AppPath.find_path_with_files(dest_directory, dir_names)
            # 移动
            if path is not None:
                AppSysCmd.cmd_run_root(f"mv {path}/* {dest_directory}")
                AppSysCmd.cmd_run_root(f"rm -rf {dest_directory}/{listdir[0]}")
            else:
                # 没有找到预期文件，删除已经解压的
                AppSysCmd.cmd_run_root(f"rm -rf {dest_directory}")
                return False
        return True


if __name__ == '__main__':
    pass
