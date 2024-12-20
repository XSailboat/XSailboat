import os
from fastapi import APIRouter, UploadFile, File
from aiofiles import open as aopen
from starlette.responses import JSONResponse
# 文件操作接口
file_api = APIRouter()

@file_api.post("/one", name="上传文件到指定目录")
async def upload_file(path: str, file: UploadFile = File(...)):
    """
    上传压缩包或文件到指定目录，multipart方式上传
    """
    try:
        # 获取文件名
        filename = file.filename

        # 指定保存路径
        save_path = f"{path}/{filename}"

        # 创建目录（如果不存在）
        os.makedirs(os.path.dirname(save_path), exist_ok=True)

        # 异步写入文件
        async with aopen(save_path, mode='wb') as out_file:
            while True:
                chunk = await file.read(1024 * 1024)  # 读取1MB的数据
                if not chunk:
                    break
                await out_file.write(chunk)

    except Exception as e:
        return JSONResponse(status_code=500, content={"code": 500, "msg": str(e)})

    return JSONResponse(status_code=200, content={"code": 200, "msg": "success"})