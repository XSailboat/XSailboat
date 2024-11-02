from team.sailboat.py.installer.common.ms_command import custom_cmd

@custom_cmd("x_test",params_rule=[])
def x_test(params):
    print(params)
    return {"code":True,"msg":"1234"}
