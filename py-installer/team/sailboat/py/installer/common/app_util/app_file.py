"""
文件修改工具类
"""
import configparser
import os
import re
from html import unescape

from loguru import logger

from team.sailboat.py.installer.common.app_util.app_sys_command import AppSysCmd


class AppFile():

    # 在指定目录下创建空文件
    @staticmethod
    def create_file(directory, file_name):
        """在指定目录下创建一个空文件"""
        file_path = os.path.join(directory, file_name)
        with open(file_path, 'w') as f:
            pass  # 创建空文件
        logger.info(f"【文件创建】- 已完成,路径为:{directory}/{file_name}", False)

    @staticmethod
    def read_conf(data):
        config = configparser.ConfigParser()
        config.read_string(data)
        return config

    @staticmethod
    def update_property_conf(file_path: str, data: dict, root=False, separator="=", tag=False):
        """修改或新增键值对类型文件,可以是conf、ini文件,格式为 key=value"""
        if AppSysCmd.exist_file(file_path):
            content = None
            conf_file = None
            if root:
                content = AppSysCmd.read_file_root(file_path)
                if tag:
                    conf_file = TagConfFile(content, separator)
                else:
                    conf_file = ConfFile(content, separator)
            else:
                content = AppSysCmd.read_file(file_path)
                if tag:
                    conf_file = TagConfFile(content, separator)
                else:
                    conf_file = ConfFile(content, separator)
            for key, value in data.items():
                conf_file[key] = value

            result = AppSysCmd.save_file_root(conf_file.get_content_str(), file_path) if root else AppSysCmd.save_file(
                conf_file.get_content_str(), file_path)
            if result:
                return (True, f"{file_path}更新成功!")
            else:
                return (False, f"{file_path}更新失败!原因:{result}")
        else:
            return (False, f"{file_path}不存在!")

    # 更新或创建Xml-property类型文件
    @staticmethod
    def update_property_xml(file_path: str, data: dict, root=False):
        if AppSysCmd.exist_file(file_path):
            try:
                xml = XmlPropertyFile(file_path)
                for key, value in data.items():
                    key = re.sub(r"\s*\.\s*", ".", key)
                    value = unescape(value)
                    # 判断是描述还是value值
                    if "description" in key:
                        xml.add_property(str(key).replace(".description", "").strip(), None, value)
                    else:
                        xml.add_property(key.strip(), value, None)
                xml.save()
                return (True, None)
            except Exception as e:
                return (False, f"{file_path}修改出现错误{e}")
        else:
            return (False, f"{file_path}不存在")

    # 更新或创建yaml类型文件
    @staticmethod
    def update_property_yaml(file_path: str, data: dict, root=False):
        if AppSysCmd.exist_file(file_path):
            try:
                yaml = YamlFile(file_path)
                for key, value in data.items():
                    yaml.add_or_update_value(key.strip(), value)
                yaml.save()
                return (True, None)
            except Exception as e:
                return (False, f"{file_path}修改出现错误{e}")
        else:
            return (False, f"{file_path}不存在")

    # 获取标记区间的文本内容
    @staticmethod
    def get_markers_text(text):
        """
        获取文本中以 '# start' 开头和 '# end' 结尾的中间内容为指定文本。
        如果找不到指定标记，则返回提示信息。
        """
        pattern = r'# start(.*?)# end'
        match = re.search(pattern, text, re.DOTALL)

        if match:
            # 获取匹配组
            matched_content = match.group(0)
            return matched_content
        else:
            return None

    # 文本文件标记
    @staticmethod
    def replace_text_between_markers(text, replacement_text, update=False):
        """
        获取文本中以 '# start' 开头和 '# end' 结尾的中间内容为指定文本。
        如果找不到指定标记，则返回提示信息。
        """
        pattern = r'# start(.*?)# end'
        match = re.search(pattern, text, re.DOTALL)
        replacement_text = replacement_text if replacement_text.endswith("\n") else replacement_text + "\n"
        if match:
            # 获取匹配组
            matched_content = match.group(0)
            # 替换匹配到的内容，保留标记
            # 是否更新
            if update:
                content = matched_content.strip().split("\n")
                # 原数据
                d1 = {}
                for s in content:
                    if s.startswith("#") or s == "":
                        continue
                    item = s.split("=", 1)
                    if len(item) != 2:
                        continue
                    key = item[0].strip()
                    value = item[1].strip()
                    d1[key] = value
                data = str(replacement_text).split("\n")
                # 待添加或修改数据
                for s in data:
                    if s == "":
                        continue
                    item = s.split("=", 1)
                    key = item[0].strip()
                    value = item[1].strip()
                    # 如果是添加环境变量,如果包含export PATH则追加
                    if key.strip() == "export PATH":
                        envs_list = value.split(":")
                        if "$PATH" in envs_list:
                            envs_list.remove("$PATH")
                        if d1.__contains__(key.strip()):
                            # 去重
                            envs = set((d1[key.strip()].split(":") + envs_list))
                            if "$PATH" in envs:
                                envs.remove("$PATH")
                            d1[key] = "$PATH:" + (":".join(envs))
                        else:
                            d1[key] = "$PATH:" + (":".join(envs_list))
                        continue

                    d1[key] = value
                new_text = []
                # 确保export PATH在最后被定义
                path_str = ""
                for key, value in d1.items():
                    if "export PATH" in key:
                        path_str = f"{key}={value}"
                        continue
                    new_text.append(f"{key}={value}")
                new_text.append(path_str)
                replacement_text = "\n".join(new_text)
            replaced_text = text.replace(matched_content, '# start\n' + replacement_text + '\n# end')
            return replaced_text.strip()
        else:
            return text + "\n# start\n" + replacement_text + "\n# end".strip()


# 自定义.conf文件解析类
class ConfFile:
    def __init__(self, data: str, separator="="):
        # 将配置字符串分割成行
        self.lines = data.split('\n')
        self.separator = separator
        self.separator_str = " " if self.separator == "\\s+" else self.separator
        # 创建两个字典来存储配置
        self.common_config = {}  # 公共配置
        self.custom_config = {}  # 自定义配置
        # 存储配置项的索引
        self.common_indices = {}
        self.custom_indices = {}
        # 标记是否处于自定义配置块中
        self.in_custom_block = False
        # 记录自定义配置块的开始和结束索引
        self.custom_block_start = None
        self.custom_block_end = None

        # 逐行解析配置
        for index, line in enumerate(self.lines):
            line = line.strip()
            if not line or line.startswith('#') \
                    and not (line.startswith('# start') or line.startswith('# end')):
                continue
            if line.startswith('# start'):
                self.in_custom_block = True
                self.custom_block_start = index
                continue
            if line.startswith('# end'):
                self.in_custom_block = False
                self.custom_block_end = index
                continue
            parts = re.split(self.separator, line, 1)
            if len(parts) == 2:
                # parts[1] = f"\"{parts[1]}\""
                key, value = parts
                if self.in_custom_block:
                    self.custom_config[key.strip()] = value.strip()
                    self.custom_indices[key.strip()] = index
                else:
                    self.common_config[key.strip()] = value.strip()
                    self.common_indices[key.strip()] = index

    def __contains__(self, key):
        return key in self.common_config or key in self.custom_config

    def __getitem__(self, item):
        if item in self.common_config:
            return self.common_config[item]
        elif item in self.custom_config:
            return self.custom_config[item]
        raise KeyError(f"Key '{item}' not found.")

    def __delitem__(self, key):
        if key in self.common_config:
            index = self.common_indices[key]
            del self.common_config[key]
            del self.common_indices[key]
            del self.lines[index]
        elif key in self.custom_config:
            index = self.custom_indices[key]
            del self.custom_config[key]
            del self.custom_indices[key]
            del self.lines[index]

    def __setitem__(self, key, value):
        if key in self.common_config:
            index = self.common_indices[key]
            # 判断是纯字符串还是其他类型
            self.lines[index] = f"{key}{self.separator_str}{str(value)}"
            value = str(value).replace("'", '"')
            self.common_config[key] = f"{value}"
            self.lines[index] = f"{key}{self.separator_str}{str(value)}"


        elif key in self.custom_config:
            index = self.custom_indices[key]
            self.lines[index] = f"{key}{self.separator_str}{value}"
            self.custom_config[key] = f"{value}"

        else:
            # 如果没有自定义配置块，则创建一个新的
            if self.custom_block_start is None:
                self.create_custom_block()
            # 如果不存在，则认为是自定义配置
            self.custom_config[key] = value

            self.lines.insert(self.custom_block_end, f'{key}{self.separator_str}{str(value)}')
            # 插入到自定义配置块的末尾
            self.custom_indices[key] = self.custom_block_end

    def create_custom_block(self):
        # 创建一个新的自定义配置块
        self.lines.append('# start')
        self.lines.append('# end')
        self.custom_block_start = len(self.lines) - 2
        self.custom_block_end = len(self.lines) - 1

    def get_content_str(self):
        # 重新构建配置字符串
        content = '\n'.join(self.lines)
        content = re.sub(r"# end\s*\n\s*# start", "", content, flags=re.DOTALL | re.MULTILINE)
        return content.strip()


class TagConfFile:
    def __init__(self, data: str, separator="="):
        # 将配置字符串分割成行
        self.lines = data.split('\n')
        self.separator = separator
        self.separator_str = " " if self.separator == "\\s+" else self.separator
        # 创建字典来存储配置
        self.config = {}
        # 存储配置项的索引
        self.indices = {}
        # 当前标签
        self.current_tag = None

        # 逐行解析配置
        for index, line in enumerate(self.lines):
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            if line.startswith('[') and line.endswith(']'):
                self.current_tag = line.strip()
                continue
            parts = re.split(self.separator, line, maxsplit=1)
            if len(parts) == 2:
                key, value = parts
                key = key.strip()
                value = value.strip()
                if '.' in key:
                    tag, key = key.split('.', 1)
                else:
                    tag = self.current_tag
                if tag not in self.config:
                    self.config[tag] = {}
                self.config[tag][key] = value
                self.indices[(tag, key)] = index

    def __contains__(self, key):
        tag, key = self._split_key(key)
        return tag in self.config and key in self.config[tag]

    def __getitem__(self, item):
        tag, key = self._split_key(item)
        if tag in self.config and key in self.config[tag]:
            return self.config[tag][key]
        raise KeyError(f"Key '{item}' not found.")

    def __delitem__(self, key):
        tag, key = self._split_key(key)
        if tag in self.config and key in self.config[tag]:
            index = self.indices[(tag, key)]
            del self.config[tag][key]
            del self.indices[(tag, key)]
            del self.lines[index]

    def __setitem__(self, key, value):
        tag, key = self._split_key(key)
        if tag not in self.config:
            self.config[tag] = {}
        self.config[tag][key] = str(value)
        key_with_tag = f"{tag}.{key}" if '.' in key else key
        if (tag, key) in self.indices:
            index = self.indices[(tag, key)]
            self.lines[index] = f"{key_with_tag}{self.separator_str}{str(value)}"
        else:
            # 找到标签所在的位置
            tag_index = None
            for i, line in enumerate(self.lines):
                if line.strip() == f"{tag}":
                    tag_index = i
                    break
            if tag_index is None:
                # 如果标签不存在，添加标签和键值对
                self.lines.append(f"{tag}")
                self.lines.append(f"{key_with_tag}{self.separator_str}{str(value)}")
                tag_index = len(self.lines) - 2
            else:
                # 在标签后添加键值对
                self.lines.insert(tag_index + 1, f"{key_with_tag}{self.separator_str}{str(value)}")
            self.indices[(tag, key)] = tag_index + 1

    def get_content_str(self):
        # 重新构建配置字符串
        return '\n'.join(self.lines).strip()

    def _split_key(self, key):
        # 尝试按点号分割键名
        if '.' in key:
            tag, key = key.split('].', 1)
            tag = f'{tag}]'
        return tag, key


import xml.etree.ElementTree as ET


def indent(elem, level=0):
    """
    格式化XML
    """
    i = "\n" + level * "  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level + 1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i


# 封装xml-property文件解析类
class XmlPropertyFile:
    def __init__(self, file_path):
        """
        初始化 XMLPropertyHandler 对象
        :param file_path: XML 文件的路径
        """
        self.file_path = file_path
        self.root = self._load_xml()

    def _load_xml(self):
        """
        加载 XML 文件
        :return: ElementTree 的根节点
        """
        tree = ET.parse(self.file_path)
        root = tree.getroot()
        return root

    def get_value(self, key):
        """
        获取指定键对应的值
        :param key: 键名
        :return: 键对应的值
        """
        for property_element in self.root.findall('.//property'):
            name_element = property_element.find('name')
            if name_element is not None and name_element.text == key:
                value_element = property_element.find('value')
                if value_element is not None:
                    return value_element.text
        return None

    def set_value(self, key, value, description=None):
        """
        设置指定键的值
        :param key: 键名
        :param value: 值
        """
        # 直接调用 add_property 方法来处理设置或添加
        self.add_property(key, value, description)

    def add_property(self, key, value, description=None):
        """
        添加一个新的键值对或修改已有的键值对
        :param key: 键名
        :param value: 值
        :param description: 描述
        """
        # 检查是否存在相同的键名
        for property_element in self.root.findall('.//property'):
            name_element = property_element.find('name')
            if name_element is not None and name_element.text == key:
                value_element = property_element.find('value')
                description_element = property_element.find('description')
                if value is not None:
                    if value_element is not None:
                        value_element.text = value
                    else:
                        value_element = ET.SubElement(property_element, 'value')
                        value_element.text = value
                if description is not None:
                    if description_element is not None:
                        description_element.text = description
                    else:
                        description_element = ET.SubElement(property_element, 'description')
                        description_element.text = description
                return

        # 如果没有找到，则添加新的键值对
        property_element = ET.SubElement(self.root, 'property')
        name_element = ET.SubElement(property_element, 'name')
        name_element.text = key
        if value is not None:
            value_element = ET.SubElement(property_element, 'value')
            value_element.text = value
        if description is not None:
            description_element = ET.SubElement(property_element, 'description')
            description_element.text = description

    def remove_property(self, key):
        """
        移除指定的键值对
        :param key: 键名
        """
        for property_element in self.root.findall('.//property'):
            name_element = property_element.find('name')
            if name_element is not None and name_element.text == key:
                self.root.remove(property_element)
                break

    def save(self):
        """
        将修改后的 XML 文件保存回磁盘，并格式化输出
        """
        # 重新构造 XML 文档，并插入必要的换行符和缩进
        indent(self.root)  # 递归地缩进所有子元素
        tree = ET.ElementTree(self.root)
        tree.write(self.file_path, encoding='utf-8', xml_declaration=True)


import yaml


# 封装的yaml解析
class YamlFile:
    def __init__(self, file_path):
        """
        初始化 YAMLTool 对象
        :param file_path: YAML 文件的路径
        """
        self.file_path = file_path
        self.data = self._load_yaml()

    def _load_yaml(self):
        """
        加载 YAML 文件
        :return: YAML 文件中的数据字典
        """
        with open(self.file_path, 'r', encoding='utf-8') as file:
            return yaml.safe_load(file)

    def save(self):
        """
        将修改后的 YAML 文件保存回磁盘
        """
        with open(self.file_path, 'w', encoding='utf-8') as file:
            yaml.dump(self.data, file, default_flow_style=False, allow_unicode=True)

    def _navigate_to(self, data, path, create_if_missing=False):
        """
        导航到指定的路径，并返回最后一个键对应的值
        :param data: 当前层级的数据字典
        :param path: 键的列表
        :param create_if_missing: 如果路径中的键不存在是否创建
        :return: 导航到的位置的数据
        """
        if not path:
            return data
        current_key = path.pop(0)
        if current_key not in data:
            if create_if_missing:
                data[current_key] = {}
            else:
                raise KeyError(f"Key '{current_key}' does not exist.")
        return self._navigate_to(data[current_key], path, create_if_missing)

    def get_value(self, key):
        """
        获取指定键对应的值，支持带点号的键名表示嵌套关系
        :param key: 键名，可以是带点号的字符串（例如 "section.items"）
        :return: 键对应的值
        """
        keys = key.split('.')
        return self._navigate_to(self.data, list(keys))

    def set_value(self, key, value):
        """
        设置指定键的值，支持带点号的键名表示嵌套关系
        :param key: 键名，可以是带点号的字符串（例如 "section.items"）
        :param value: 值
        """
        keys = key.split('.')
        final_key = keys.pop(-1)
        container = self._navigate_to(self.data, list(keys), create_if_missing=True)
        container[final_key] = value

    def add_or_update_value(self, key, value):
        """
        添加一个新的键值对，如果键已存在，则更新其值，支持带点号的键名表示嵌套关系
        :param key: 键名，可以是带点号的字符串（例如 "section.items"）
        :param value: 值
        """
        self.set_value(key, value)

    def remove_value(self, key):
        """
        移除指定的键值对，支持带点号的键名表示嵌套关系
        :param key: 键名，可以是带点号的字符串（例如 "section.items"）
        """
        keys = key.split('.')
        final_key = keys.pop(-1)
        container = self._navigate_to(self.data, list(keys))
        if final_key not in container:
            raise KeyError(f"Key '{final_key}' does not exist.")
        del container[final_key]

    def append_list_item(self, key, item):
        """
        在指定键对应的列表末尾追加一项，支持带点号的键名表示嵌套关系
        :param key: 列表键名，可以是带点号的字符串（例如 "section.items"）
        :param item: 要追加的项
        """
        keys = key.split('.')
        final_key = keys.pop(-1)
        container = self._navigate_to(self.data, list(keys), create_if_missing=True)
        if final_key not in container:
            container[final_key] = []
        elif not isinstance(container[final_key], list):
            raise ValueError(f"Key '{key}' is not a list.")
        container[final_key].append(item)

    def remove_list_item(self, key, item):
        """
        从指定键对应的列表中移除一项，支持带点号的键名表示嵌套关系
        :param key: 列表键名，可以是带点号的字符串（例如 "section.items"）
        :param item: 要移除的项
        """
        keys = key.split('.')
        final_key = keys.pop(-1)
        container = self._navigate_to(self.data, list(keys))
        if final_key not in container:
            raise KeyError(f"Key '{key}' does not exist.")
        if not isinstance(container[final_key], list):
            raise ValueError(f"Key '{key}' is not a list.")
        if item not in container[final_key]:
            raise ValueError(f"Item '{item}' does not exist in the list.")
        container[final_key].remove(item)
