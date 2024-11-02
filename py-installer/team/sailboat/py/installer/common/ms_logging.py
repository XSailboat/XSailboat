import os

from loguru import logger

import sys
import logging
from datetime import datetime

from loguru import logger

# 获取当前时间并生成日志文件路径
from team.sailboat.py.installer.common.app_util.app_path import AppPath

now = datetime.now()  # 获取当前时间
year = now.year  # 获取当前年份
month = now.month  # 获取当前月份
day = now.day  # 获取当前日期

dddd = str(year) + "-" + str(month) + "-" + str(day)
path = AppPath.find_file_top_down(os.getcwd(), "start.sh")

if path is not None:
    path = path.replace("/start.sh", "")
else:
    path = "."
file_path = path + "/log/log-" + dddd + ".txt"


# 配置 logger
def configure_logger(file_path, log_level, max_file_size):
    level = log_level.upper() if log_level else "INFO"
    max_size = max_file_size if max_file_size else "1024 MB"

    # 清空之前的handlers
    logger.remove()

    # 配置控制台输出
    # logger.add(sys.stderr, format="{time:YYYY-MM-DD HH:mm:ss.SSS} | <lvl>{level:8}</>| <lvl>{message}</>", colorize=True, level=level)
    logger.add(sys.stderr, colorize=True, level=level)

    # 配置文件输出
    logger.add(file_path, level=level, rotation=max_size)


# 获取日志配置
log_level = "INFO"
max_file_size = "1024 MB"

# 配置 logger
configure_logger(file_path, log_level, max_file_size)

# 获取 httpx 库的日志记录器
httpx_logger = logging.getLogger("httpx")
# 设置 httpx 日志记录器的级别为 WARNING
httpx_logger.setLevel(logging.WARNING)

# 获取特定库的日志记录器
watchfiles_logger = logging.getLogger("watchfiles")
# 设置日志级别为WARNING或更高，以屏蔽INFO级别的日志消息
watchfiles_logger.setLevel(logging.WARNING)


# 将 loguru 与标准 logging 结合
class InterceptHandler(logging.Handler):
    def emit(self, record):
        loguru_logger = logger.bind(name=record.name)
        level = logger.level(record.levelname).name
        frame, depth = logging.currentframe(), 2
        while frame is not None and frame.f_globals["__name__"] != __name__:
            frame = frame.f_back
            depth += 1
        loguru_logger.opt(depth=depth, exception=record.exc_info).log(level, record.getMessage())


# 将 InterceptHandler 添加到 httpx logger
httpx_logger.addHandler(InterceptHandler())
watchfiles_logger.addHandler(InterceptHandler())

# 导出 logger 供其他模块使用
__all__ = ["logger"]
