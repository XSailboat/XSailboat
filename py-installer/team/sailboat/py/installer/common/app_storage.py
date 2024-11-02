# 应用全局状态存储单例类
class AppStorage:
    _instance = None  # 用于存储唯一的实例

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(AppStorage, cls).__new__(cls)
            cls._instance._data = {}  # 存储状态数据
        return cls._instance

    def __getitem__(self, key):
        return self._data[key]

    def __setitem__(self, key, value):
        self._data[key] = value

    def __contains__(self, key):
        return key in self._data

    def keys(self):
        return self._data.keys()

    def values(self):
        return self._data.values()

    def items(self):
        return self._data.items()

    def __str__(self):
        return str(self._data)


app_storage = AppStorage()
app_storage["profile"] = {
    "ip": "192.168.64.100",
    "name": "master",
    "adminUser": "root",
    "adminPswd": "123456",
    "sysUser": "hadoop",
    "sysPwsd": "123456"
}
app_storage["current_user"] = app_storage["profile"]["sysUser"]
app_storage["sql"] = {"mode": False, "username": "", "password": "",
                      "database": "", "type": ""}
app_storage["pre_output_pOpen"] = {"stdout_lines": [], "stderr_lines": [], "running": False}
