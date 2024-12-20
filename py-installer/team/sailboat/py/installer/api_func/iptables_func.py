import ipaddress
import json
import os
import platform
import sys
from datetime import datetime
from json import JSONDecodeError

import iptc

ports_set = set()

ports_map = {}
# 配置文件全局变量
hosts = {}
apps = {}
outer_clients = {}


def ip_parsing(ip_range_str):
    """
    解析 IP 地址范围字符串，返回具体的 IP 地址范围。

    参数:
    ip_range_str (str): 形如 "192.168.1.*"、"192.168.1.1*" 或 "192.168.1.11*" 的 IP 地址范围字符串。

    返回:
    str: 具体的 IP 地址范围字符串，例如 "192.168.1.1-192.168.1.255"。
    """
    if "*" not in ip_range_str:
        return ip_range_str
    parts = ip_range_str.split('.')
    start_ip = ""
    end_ip = ""
    last = parts[3]
    # 判断*出现的位数
    if last == "*":
        start_ip = "1"
        end_ip = "255"
    elif "*" in last and len(last) == 2:

        start_ip = str(int(last.replace("*", "0")))

        if last.startswith("1"):
            end_ip = str(int(last.replace("*", "99")))
        elif last.startswith("2"):
            end_ip = str(int(last.replace("*", "55")))

    elif "*" in last and len(last) == 3:
        start_ip = str(int(last.replace("*", "0")))

        if last.startswith("25"):
            end_ip = str(int(last.replace("*", "5")))
        else:
            end_ip = str(int(last.replace("*", "9")))

    # 构造前缀
    prefix = '.'.join(parts[:-1])
    start_ip = f"{prefix}.{start_ip}"
    end_ip = f"{prefix}.{end_ip}"

    # 验证起始和结束 IP 地址是否有效
    try:
        start_ip = str(ipaddress.IPv4Address(start_ip))
        end_ip = str(ipaddress.IPv4Address(end_ip))
    except ValueError as e:
        raise ValueError(f"无效的 IP 地址范围: {start_ip}-{end_ip}") from e

    return f"{start_ip}-{end_ip}"

    raise ValueError("IP 地址范围字符串必须包含 *")


def create_match(rule, port):
    # 设定匹配1
    match = iptc.Match(rule, "tcp")
    # 目标端口1
    match.dport = str(port)

    return match


def create_iprange_match(rule, ip_range, dst_ip):
    # 使用 iprange 匹配模块
    match = iptc.Match(rule, "iprange")
    match.src_range = ip_range
    match.dst_range = dst_ip
    return match


def open_by_project(apps, chain, hostname, project_name, open, source_ips):
    global hosts
    # 遍历open获取需要开通的app信息
    for name in open:
        if not apps.__contains__(name):
            print(f"没有找到app：{name}")
            continue
        app_info = dict(apps[name])
        # 遍历app_info中的服务器开通
        hosts_ = dict(app_info)["hosts"]
        ports = dict(app_info)["ports"]
        for h in hosts_:
            if h != hostname:
                continue

            for port in ports:
                for _ip in source_ips:
                    ip = _ip

                    if '-' in ip or '*' in ip:
                        if '*' in ip:
                            # 解析为范围
                            ip = ip_parsing(ip)

                        # 处理 IP 范围
                        if ports_map.__contains__(ip):
                            temp = list(ports_map[ip])
                            temp.append(port)
                            ports_map[ip] = temp
                        else:
                            ports_map[ip] = [port]

                        ports_set.add(port)

                        # 添加规则
                        print(f"{project_name}:{ip} 开通了{name}:{h}:{port} 的访问权限")

                        # 创建规则
                        rule = iptc.Rule()
                        rule.protocol = "tcp"
                        match = iptc.Match(rule, "tcp")
                        match.dport = str(port)  # 开放的端口
                        rule.add_match(match)

                        match = create_iprange_match(rule, ip, hosts[h])
                        rule.add_match(match)
                        rule.target = iptc.Target(rule, "ACCEPT")

                        # 设置目标端口

                        # 添加规则到 INPUT 链
                        chain.insert_rule(rule)
                    else:
                        # 单个 IP 地址
                        if ports_map.__contains__(ip):
                            temp = list(ports_map[ip])
                            temp.append(port)
                            ports_map[ip] = temp
                        else:
                            ports_map[ip] = [port]

                        ports_set.add(port)

                        # 添加规则
                        print(f"{project_name}:{ip} 开通了{name}:{h}:{port} 的访问权限")


def clear_chain(table):
    chain = iptc.Chain(table, "INPUT")
    for rule in chain.rules:
        chain.delete_rule(rule)


def load_iptables(config_path):
    global apps, hosts, outer_clients
    config = {}
    # 加载配置文件
    with open(config_path, 'r', encoding='utf-8') as file:
        try:
            # 使用 json.load() 方法将 JSON 文件内容加载到一个字典中
            config = json.load(file)
        except JSONDecodeError as e:
            raise "配置文件解析失败，文件内容为空!"



    apps = config["apps"]
    hosts = config["hosts"]
    outer_clients = config["outer_clients"]

    # 当前主机名
    hostname = platform.node()
    print("当前主机名: " + hostname)

    table = iptc.Table(iptc.Table.FILTER)
    # 关闭自动提交
    table.autocommit = False
    # 清空原有的规则
    print("清空原有的INPUT表规则")
    clear_chain(table)
    chain = iptc.Chain(table, "INPUT")
    print("开启对内部相互之间完全放开安全限制，允许自由访问。")
    # 对内部相互之间完全放开安全限制，允许自由访问。
    for (app_name, app_info) in apps.items():
        hosts_ = dict(app_info)["hosts"]
        ports = dict(app_info)["ports"]
        # 每台ip的每个设定的端口都要对host中的ip进行开放
        for h in hosts_:
            # 如果不是本机,则可以跳过
            if h != hostname:
                continue
            # 添加规则
            for port in ports:
                for (_h, _ip) in hosts.items():
                    ip = _ip
                    # 如果是本机,则可以跳过
                    if h == _h:
                        continue

                    if ports_map.__contains__(_ip):
                        temp = list(ports_map[ip])
                        temp.append(port)
                        ports_map[ip] = temp
                    else:
                        ports_map[ip] = [port]

                    # match = create_match(rule, port)
                    # matchs.add(match)
                    ports_set.add(port)
                    # 添加规则
                    print(f"{_h}:{hosts[_h]} 开通了 {app_name}:{h}:{port}的访问权限")

    # 对外开放表添加对应的权限
    # 1.判断当前运行时间是否在日期区间内
    # 2.在区间内就遍历source_ips,对每个ip开放open内的权限
    # print(apps)
    # print()
    # print(ports_map)
    print()
    print("对外开放表添加对应的权限")
    for (name, info) in outer_clients.items():
        # 获取当前时间是否在区间内
        issue_date = dict(info)["issue_date"]
        expired_date = dict(info)["expired_date"]
        # 获取当前时间
        now = str(datetime.now())
        if issue_date < now < expired_date:
            opens = dict(info)["open"]
            source_ips = dict(info)["source_ips"]
            # 对ip
            open_by_project(apps, chain, hostname, name, opens, source_ips)
        else:
            print(f"项目 {name},当前时间：{now},不在设置区间内：{issue_date}~{expired_date}")
    print()

    if len(ports_map) != 0:
        for (ip, ports) in ports_map.items():
            if "-" in ip:
                continue
            matchs = []
            # 添加规则
            rule = iptc.Rule()
            # 添加源ip地址
            rule.src = ip
            rule.protocol = "tcp"
            for port in set(ports):
                matchs.append(create_match(rule, port))
            # 允许访问
            target = iptc.Target(rule, "ACCEPT")
            for match in matchs:
                rule.add_match(match)
            rule.target = target
            chain.append_rule(rule)

    if len(ports_set) != 0:
        # 添加规则
        drop_rule = iptc.Rule()
        drop_rule.src = "0.0.0.0"
        drop_rule.protocol = "tcp"
        target = iptc.Target(drop_rule, "DROP")
        for port in ports_set:
            # 设定匹配
            match = iptc.Match(drop_rule, "tcp")
            # 目标端口
            match.dport = str(port)
            drop_rule.add_match(match)
        drop_rule.target = target

        chain.append_rule(drop_rule)

    table.commit()
    table.flush()
    table.close()


if __name__ == '__main__':
    argv = sys.argv
    load_iptables(argv[1])
