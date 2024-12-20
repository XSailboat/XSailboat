# 全局变量存储单例类
from team.sailboat.py.installer.common.app_util.app_file import YamlFile


class AppVariable:
    _instance = None  # 用于存储唯一的实例

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(AppVariable, cls).__new__(cls)
            cls._instance._data = cls._instance.load_variable()  # 存储状态数据
        return cls._instance

    def __getitem__(self, key):
        return self._data.get_value(key)

    def __setitem__(self, key, value):
        self._data.set_value(key, value)

    def __contains__(self, key):
        return self._data.contains_key(key)

    def __str__(self):
        return str(self._data)

    def get_or_default(self, key, default):
        if not self._data.contains_key(key):
            return default
        return self.__getitem__(key)

    def get(self, key):
        return self.__getitem__(key)

    def load_variable(self):
        # 读取本地存储的变量
        file_path = "../config/py_apps/py_installer/.AppVariable.yaml"
        yaml = YamlFile(file_path)
        return yaml

    def save_variable(self):
        # 保存到本地文件中
        self._data.save()
