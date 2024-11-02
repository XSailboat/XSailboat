# 自动导入 custom_command 包下的所有模块
import pkgutil
import importlib
import os

# 获取当前目录路径
current_dir = os.path.dirname(os.path.abspath(__file__))

# 遍历当前目录下的所有模块
for _, module_name, _ in pkgutil.iter_modules([current_dir]):
    # 动态导入模块
    importlib.import_module(f"{__package__}.{module_name}")