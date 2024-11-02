import random
import threading
import time
from collections import defaultdict


class IDGen:
    timestamp_minute = int(time.time() * 1000) // 60_000
    _lock = threading.Lock()
    sSeed = None
    sSeed5 = None
    sCountMap = defaultdict(int)

    # 已经按照ascii码表排序的字符集
    sDigits = ''.join([
        '0123456789',
        '=',
        'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
        '_',
        'abcdefghijklmnopqrstuvwxyz'
    ])

    @classmethod
    def init(cls):
        if cls.sSeed is None:
            cls.sSeed = cls.to_unsigned_string(cls.timestamp_minute, 6) + cls.sDigits[
                random.randint(0, len(cls.sDigits) - 1)]
            cls.sSeed5 = cls.to_unsigned_string(cls.timestamp_minute, 5) + cls.sDigits[
                random.randint(0, len(cls.sDigits) - 1)]

    @classmethod
    def to_unsigned_string(cls, val, shift=6, width=-1):
        mag = 64 - val.bit_length()
        chars = width if width > 0 else max(((mag + (shift - 1)) // shift), 1)
        buf = [cls.sDigits[0]] * chars if width > 0 else [''] * chars
        cls._format_unsigned_long(val, shift, buf, 0, chars)
        return ''.join(buf)

    @classmethod
    def _format_unsigned_long(cls, val, shift, buf, offset, length):
        radix = 1 << shift
        mask = radix - 1
        char_pos = length - 1  # 初始化时先减1，避免在未写入字符时减小char_pos导致的越界
        while val != 0:
            buf[offset + char_pos] = cls.sDigits[int(val & mask)]
            val >>= shift
            char_pos -= 1
        return char_pos + 1

    @classmethod
    def new_id(cls, category=None, width=-1, no_seed=False):
        if category not in cls.sCountMap:
            with cls._lock:
                if category not in cls.sCountMap:
                    cls.sCountMap[category] = 0
        cls.sCountMap[category] += 1
        suffix = cls.to_unsigned_string(cls.sCountMap[category], 6, width)
        seed_to_use = cls.sSeed if width != 5 else cls.sSeed5
        if seed_to_use is None:
            raise ValueError("Seed value is not properly initialized.")

        return suffix if no_seed else seed_to_use + suffix
