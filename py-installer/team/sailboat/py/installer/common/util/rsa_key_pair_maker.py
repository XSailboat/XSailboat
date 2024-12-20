import os
import uuid
from datetime import timedelta
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.backends import default_backend
from collections import OrderedDict
import base64
import time
import threading

# 自动设置环境变量
from loguru import logger

os.environ['CRYPTOGRAPHY_OPENSSL_NO_LEGACY'] = '1'


class AutoCleanHashMap(OrderedDict):
    def __init__(self, expire_time=timedelta(minutes=1)):
        super().__init__()
        self.expire_time = expire_time.total_seconds()
        self.access_times = {}
        self.lock = threading.Lock()
        self.start_cleaning_thread()

    def put(self, key, value):
        with self.lock:
            self[key] = value
            self.access_times[key] = time.time()
            logger.info(f"创建了一个RSA密钥,key {key},时间 {time.strftime('%Y-%m-%d %H:%M:%S')},将在 {self.expire_time} 秒后过期")

    def get(self, key, default=None):
        with self.lock:
            self.clean_expired_items()
            if key in self:
                self.access_times[key] = time.time()
                return super().get(key)
        return default

    def clean_expired_items(self):
        current_time = time.time()
        keys_to_remove = [k for k, v in self.access_times.items() if current_time - v > self.expire_time]
        for key in keys_to_remove:
            del self[key]
            del self.access_times[key]
            logger.info(f"{key} 已过期,时间 {time.strftime('%Y-%m-%d %H:%M:%S')}")

    def start_cleaning_thread(self):
        interval = self.expire_time / 4

        def cleaning_task():
            while True:
                time.sleep(interval)  # 定期检查密钥是否过期
                self.clean_expired_items()

        thread = threading.Thread(target=cleaning_task, daemon=True)
        thread.start()


class RSAKeyPairMaker:
    sDefault = None

    def __init__(self, minutes=1, public_exponent=65537):
        self.minutes = minutes
        self.public_exponent = public_exponent
        self.mRSAKeyCache = AutoCleanHashMap(expire_time=timedelta(minutes=self.minutes))

    @staticmethod
    def getDefault():
        if RSAKeyPairMaker.sDefault is None:
            RSAKeyPairMaker.sDefault = RSAKeyPairMaker()
        return RSAKeyPairMaker.sDefault

    def newOne(self, specified_public_key=None):
        if specified_public_key:
            private_key = self._generate_private_key()
            public_key = self._load_public_key_from_pem(specified_public_key)
        else:
            private_key, public_key = self._generate_key_pair()

        id_ = str(uuid.uuid4())
        self.mRSAKeyCache.put(id_, (private_key, public_key))
        return id_, public_key

    def _generate_private_key(self):
        private_key = rsa.generate_private_key(
            public_exponent=self.public_exponent,
            key_size=2048,
            backend=default_backend()
        )
        return private_key

    def _generate_key_pair(self):
        private_key = rsa.generate_private_key(
            public_exponent=self.public_exponent,
            key_size=2048,
            backend=default_backend()
        )
        public_key = private_key.public_key()
        return private_key, public_key

    def _load_public_key_from_pem(self, pem_string):
        # 确保公钥字符串包含正确的 PEM 头部和尾部标识符
        pem_string = f"""-----BEGIN PUBLIC KEY-----
{pem_string}
-----END PUBLIC KEY-----"""
        public_key = serialization.load_pem_public_key(
            pem_string.encode('utf-8'),
            backend=default_backend()
        )
        return public_key

    def encrypt(self, aId, aPlainText):
        key_tuple = self.mRSAKeyCache.get(aId)
        if key_tuple is None:
            logger.info(f"无效的id[{aId}] 或 密钥已过期")
            raise ValueError(f"无效的id[{aId}] 或 密钥已过期")

        _, public_key = key_tuple  # 确保正确提取公钥

        try:
            # 使用PKCS1填充方案进行加密
            encrypted_bytes = public_key.encrypt(
                aPlainText.encode("utf-8"),
                padding.PKCS1v15()
            )

            # 将加密后的字节数组转换为Base64字符串
            encrypted_base64 = base64.b64encode(encrypted_bytes).decode()
            return encrypted_base64
        except Exception as e:
            logger.info(f"加密失败: {e}")
            raise

    def decrypt(self, aId, aSecretText):
        if not aSecretText:
            return aSecretText
        private_key, _ = self.mRSAKeyCache.get(aId)
        if private_key is None:
            logger.info(f"无效的id[{aId}] 或 密钥已过期")
            raise ValueError(f"无效的id[{aId}] 或 密钥已过期")

        try:
            # 解码Base64字符串
            encrypted_bytes = base64.b64decode(aSecretText)
            logger.info(f"解码后的字节数组长度: {len(encrypted_bytes)} 字节")

            # 使用PKCS1填充方案进行解密
            decrypted_data = private_key.decrypt(
                encrypted_bytes,
                padding.PKCS1v15()
            )
            return decrypted_data.decode()
        except Exception as e:
            logger.info(f"解密失败: {e}")
            raise

    def get_public_key_string(self, aId):
        public_key_tuple = self.mRSAKeyCache.get(aId)
        if public_key_tuple is None:
            logger.info(f"无效的id[{aId}] 或 密钥已过期")
            raise ValueError(f"无效的id[{aId}] 或 密钥已过期")

        _, public_key = public_key_tuple

        public_key_pem = public_key.public_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        )
        return public_key_pem.decode()

    def get_public_key_details(self, aId):
        public_key_tuple = self.mRSAKeyCache.get(aId)
        if public_key_tuple is None:
            logger.info(f"无效的id[{aId}] 或 密钥已过期")
            raise ValueError(f"无效的id[{aId}] 或 密钥已过期")

        _, public_key = public_key_tuple

        public_key_exponent = public_key.public_numbers().e
        public_key_modulus = public_key.public_numbers().n

        return str(hex(public_key_exponent))[2:], str(hex(public_key_modulus))[2:]

