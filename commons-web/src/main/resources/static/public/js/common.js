var reLogioning = false, loginSuccess = false, dataRequestConfig = {};

const confirm = function() {
	return new Promise(resolve => {
		ElementPlus.ElMessageBox.confirm('您的登录已超时，您是否要在新窗口中重新登录？', "提示", {
			showClose: false,
			type: 'info',
			confirmButtonText: '确定',
			cancelButtonClass: 'concel-right el-button--normal is-plain  el-button--mini',
			confirmButtonClass: 'el-button--normal  el-button--mini'
		}).then(_ => {
			resolve(true);
		}).catch(_ => {
			resolve(false);
		});
	})
}

const checkLoginStatusAsync = function() {
	return new Promise(resolve => {
		setTimeout(() => {
			axios.get('api/status').then(() => {
				ElementPlus.ElMessage({
					message: '登录成功！',
					type: 'success',
				})
				resolve(true);
			}).catch(() => {
				resolve(false);
			})
		}, 1500)
	})
}

const isLoginSuccessAsync = function() {
	return new Promise(resolve => {
		setTimeout(() => {
			resolve(loginSuccess);
		}, 1000)
	})
}

if (axios) {
	window.$proxy = axios.create(axios.defaults);
	$proxy.defaults.baseURL = "/proxy";
	axios.defaults.headers["X-Requested-With"] = "XMLHttpRequest";
	axios.interceptors.request.use(config => {
		if (store.state.csrf.enable && config.method != 'get') {
			config.params = {
				"_csrf": store.state.csrf.value,
				...config.params
			}
		}
		return config;
	});
	axios.interceptors.response.use(async response => {
		return response;
	}, async error => {
		if (error.response && error.response.status == 401 && error.response.config.url.indexOf("api/status") == -1) {

			var requestId = Date.now();
			dataRequestConfig[requestId] = error.response.config;

			if (!reLogioning) {
				reLogioning = true;
				loginSuccess = false;

				var confirm_status = await confirm();
				if (confirm_status) {
					if (error.response.data && error.response.data.message) {
						var url = JSON.parse(error.response.data.message).path;
						window.open(url);
						for (var i = 0; i < 60; i++) {
							let _loginSuccess = await checkLoginStatusAsync();
							if (_loginSuccess) {
								loginSuccess = _loginSuccess;
								reLogioning = false;
								return new Promise(resolve => {
									resolve(axios(dataRequestConfig[requestId]));
								})
							}
						}
						reLogioning = false;
						return Promise.reject(error);
					}else{
						window.location.reload();
					}
				} else {
					return Promise.reject(error);
				}
			} else {
				for (var i = 0; i < 120; i++) {
					let _loginSuccess = await isLoginSuccessAsync();
					if (_loginSuccess) {
						return new Promise(resolve => {
							resolve(axios(dataRequestConfig[requestId]));
						})
					}
				}
			}
		} else {
			try {
				var data = JSON.parse(error.response.data);
				return Promise.reject({ ...error.response, data: data });
			} catch (err) {
				console.log(error);
				return Promise.reject(error);
			}
		}
	});
}
if (typeof $ === 'function') {
	$.ajaxSetup({
		contentType: "application/json",
		dataType: "json",
		complete: function(xhr, textStatus) {
			if (xhr.getResponseHeader("sessionstatus") == "timeout") {
				window.location.reload();
			}
		},
		beforeSend: function(xhr, settings) {
			if (store.state.csrf.enable && settings.type.toLowerCase != 'get') {
				if (settings.url.indexOf("?") > 0)
					settings.url = settings.url + "&_csrf=" + store.state.csrf.value;
				else
					settings.url = settings.url + "?_csrf=" + store.state.csrf.value;
			}
		}
	});
}

String.prototype.endWith = function(s) {
	if (s == null || s == "" || this.length == 0 || s.length > this.length)
		return false;
	if (this.substring(this.length - s.length) == s)
		return true;
	else
		return false;
}


String.prototype.startWith = function(s) {
	if (s == null || s == "" || this.length == 0 || s.length > this.length)
		return false;
	if (this.substr(0, s.length) == s)
		return true;
	else
		return false;
}

Array.prototype.multiKeySort = function(keys) {
	return this.sort((a, b) => {
		for (let key of keys) {
			var ak = a[key], bk = b[key];
			if (ak < bk) {
				return -1;
			}
			if (ak > bk) {
				return 1;
			}
		}
		return 0;
	});
}

ElementPlus.ElMessage.$success = msg => {
	ElementPlus.ElMessage({
		showClose: true,
		message: msg,
		type: 'success',
		grouping: true,
		dangerouslyUseHTMLString: true,
	})
}

ElementPlus.ElMessage.$error = msg => {
	ElementPlus.ElMessage({
		showClose: true,
		message: msg,
		type: 'error',
		grouping: true,
		dangerouslyUseHTMLString: true,
	})
}


