from typing import List, Dict

from pydantic import BaseModel


class UserCredentials(BaseModel):
    username: str
    password: str


class HostProfile(BaseModel):
    ip: str
    name: str
    adminUser: str
    adminPswd: str
    sysUser: str
    sysPswd: str
    deployModuleNames: List


class Commands(BaseModel):
    commands: List


class IptablesConfig(BaseModel):
    hosts: Dict
    apps: Dict
    outer_clients: Dict


class CreateUserInfo(BaseModel):
    username:str
    password:str

class PathList(BaseModel):
    paths:List