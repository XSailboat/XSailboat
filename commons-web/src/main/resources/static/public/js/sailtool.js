class SailTool {
	sysEnv = '';
	authes = [];
	
	static {
		this.stackData = {};
		this.chars = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
			'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z'];
		this.isChrome = (/chrome/.test(navigator.userAgent.toLowerCase()));
	};
	
	static hasAuthority(auth) {
		var flag = false;
		this.authes.forEach(item => {
			if(item.authority == auth){
				flag = true;
			}
		})
		return flag;
	}

	/**
	 * 复制文本到剪切板
	 */
	static copyToClipboard(text) {
		var textarea = document.createElement("textarea"); //创建input对象
		var currentFocus = document.activeElement; //当前获得焦点的元素
		var toolBoxwrap = document.getElementById('app'); //将文本框插入到NewsToolBox这个之后
		toolBoxwrap.appendChild(textarea); //添加元素
		textarea.value = text;
		textarea.focus();
		if (textarea.setSelectionRange) {
			textarea.setSelectionRange(0, textarea.value.length); //获取光标起始位置到结束位置
		} else {
			textarea.select();
		}
		try {
			var flag = document.execCommand("copy"); //执行复制
		} catch (eo) {
			var flag = false;
		}
		toolBoxwrap.removeChild(textarea); //删除元素
		currentFocus.focus();
		if (flag)
			ElementPlus.ElMessage({
				message: '已复制到剪切板！',
				type: 'success'
			})
		else {
			ElementPlus.ElMessage({
				message: '不支持，请手动复制！',
				type: 'info'
			})
			textarea.select();
		}
		return flag;
	};
	static urlHash(value) {
		let hash = window.location.hash;
		if (value) {
			value = "#" + value;
			var newUrl;
			if (hash) {
				newUrl = window.location.href.replace(hash, value);
			} else {
				newUrl = window.location.href + value;
			}
			if (window.location.href != newUrl)
				window.history.replaceState({ path: newUrl }, '', newUrl);
		} else {
			return hash ? hash.substring(1) : '';
		}
	};
	/**
	 * 向URL追加k-v
	 */
	static urlPush(key, value) {
		if (!value)
			this.delUrlParam(key);
		else {
			var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");

			var search = window.location.search, newUrl = '';
			var separator = search.length > 0 ? "&" : "?";
			if (search.match(re)) {
				newUrl = search.replace(re, '$1' + key + "=" + value + '$2');
			} else if (value) {
				newUrl = search + separator + key + "=" + value;
			}
			newUrl = window.location.origin + window.location.pathname + newUrl + window.location.hash;
			if (window.location.href != newUrl)
				window.history.replaceState({ path: newUrl }, '', newUrl);
		}
	};
	/**
	 * 删除URL中的参数k-v
	 */
	static delUrlParam(paramKey) {
		var url = window.location.href, newUrl = '';    //页面url
		var urlParam = window.location.search.substring(1);   //页面参数
		var beforeUrl = url.substr(0, url.indexOf("?"));   //页面主地址（参数之前地址）
		var nextUrl = "";

		var arr = new Array();
		if (urlParam != "") {
			var urlParamArr = urlParam.split("&"); //将参数按照&符分成数组
			for (var i = 0; i < urlParamArr.length; i++) {
				var paramArr = urlParamArr[i].split("="); //将参数键，值拆开
				//如果键雨要删除的不一致，则加入到参数中
				if (paramArr[0] != paramKey) {
					arr.push(urlParamArr[i]);
				}
			}
		}
		if (arr.length > 0) {
			nextUrl = "?" + arr.join("&");
		}
		newUrl = beforeUrl + nextUrl + window.location.hash;
		window.history.replaceState({ path: newUrl }, '', newUrl);
	};
	/**
	 * 获取URL参数k-v
	 */
	static urlParams() {
		var params = { has: false };
		var query = window.location.search.substring(1);
		if (query) {
			params['has'] = true;
			var vars = query.split("&");
			for (var i = 0; i < vars.length; i++) {
				var pair = vars[i].split("=");
				var value = decodeURIComponent(pair[1]);
				if (value === 'true')
					params[pair[0]] = true;
				else if (value === 'false')
					params[pair[0]] = false;
				else
					params[pair[0]] = value;
			}
		}
		return params;
	};
	/**
	 * 文件大小格式化
	 *
	 * bytes： 文件大小
	 *
	 */
	static bytesToSize(bytes) {
		if (bytes === 0) return '0 B';
		if (!bytes) return '';
		var k = 1024, // or 1024
			sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
			i = Math.floor(Math.log(bytes) / Math.log(k));
		var num = parseFloat(bytes / Math.pow(k, i))
		return Math.round(num * 100) / 100 + ' ' + sizes[i];
	};
	/**
	 * 时间格式化
	 *
	 * timestamp： 时间戳
	 *
	 */
	static timestampToTime(timestamp) {
		if (!timestamp) return '';
		var date = new Date(timestamp);//时间戳为10位需*1000，时间戳为13位的话不需乘1000
		var Y = date.getFullYear() + '-';
		var M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
		var D = (date.getDate() < 10 ? '0' + date.getDate() : date.getDate()) + ' ';
		var h = (date.getHours() < 10 ? '0' + date.getHours() : date.getHours()) + ':';
		var m = (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes()) + ':';
		var s = date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds();
		return Y + M + D + h + m + s;
	};
	/**
	 * 随机字符串【0-9、a-z、A-Z】
	 *
	 * length： 长度
	 *
	 */
	static randomNumber(length) {
		if (!length) length = 6;
		var number = '';
		for (var i = 0; i < length; i++) {
			number += this.chars[Math.round(Math.random() * 61)]
		}
		return number;
	};
	/**
	 * 右键菜单位置坐标
	 *
	 * MouseEvent
	 * H: 菜单面板高度
	 * W: 菜单面板宽度
	 *
	 */
	static contextmenuPos(MouseEvent, H, W) {
		let X = MouseEvent.clientX, Y = MouseEvent.clientY - 6;
		if (MouseEvent.clientX > document.body.clientWidth - W)
			X = X - W + 10;
		if (MouseEvent.clientY > document.body.clientHeight - H)
			Y = Y - H + 30;
		return { x: X, y: Y };
	};
	static UUID() {
		var d = new Date().getTime();
		if (window.performance && typeof window.performance.now === "function") {
			d += performance.now(); //use high-precision timer if available
		}
		var uuid = 'xxxxxxxxxxxxxxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			var r = (d + Math.random() * 16) % 16 | 0;
			d = Math.floor(d / 16);
			return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
		});
		return uuid.toUpperCase();
	};
	static latestDate(type) {
		var date = new Date();
		var year = date.getFullYear(),
			month = date.getMonth() + 1,
			day = date.getDate(),
			houre = date.getHours(),
			minute = date.getMinutes();
		second = date.getSeconds();
		month = month < 10 ? ('0' + month) : month;
		day = day < 10 ? ('0' + day) : day;
		houre = houre < 10 ? ('0' + houre) : houre;
		minute = minute < 10 ? ('0' + minute) : minute;
		second = second < 10 ? ('0' + second) : second;
		if (type == '年') {
			return year + '';
		} else if (type == '月') {
			return year + '-' + month;
		} else if (type == '日') {
			return year + '-' + month + '-' + day;
		} else if (type == '时') {
			return year + '-' + month + '-' + day + ' ' + houre;
		} else if (type == '分') {
			return year + '-' + month + '-' + day + ' ' + houre + ':' + minute;
		} else {
			return year + '-' + month + '-' + day + ' ' + houre + ':' + minute + ':' + second;
		}
	};
	static downloadFile(url, args, name) {
		axios({
			method: 'post',
			url: url,
			data: args || {},
			responseType: 'blob'
		}).then(res => {
			let blob = new Blob([res.data], { type: res.data.type })
			let downloadElement = document.createElement('a')
			let href = window.URL.createObjectURL(blob); //创建下载的链接
			downloadElement.href = href;
			downloadElement.download = name || decodeURIComponent(res.headers["content-disposition"].split("filename=")[1]); //下载后文件名
			document.body.appendChild(downloadElement);
			downloadElement.click(); //点击下载
			document.body.removeChild(downloadElement); //下载完成移除元素
			window.URL.revokeObjectURL(href); //释放blob
		}).catch(error => {
			this.message('error', error);
		})
	};
	static clone(val) {
		return JSON.parse(JSON.stringify(val));
	};
	static localStorage(key, val) {
		let _this = this;
		while (_this.$parent) {
			_this = _this.$parent;
		}
		let sKey = _this.userInfo.id + "_" + key;
		if (val) {
			if (val == '_DEL') {
				localStorage.removeItem(sKey)
			} else {
				localStorage.setItem(sKey, val)
			}
		} else {
			return localStorage.getItem(sKey)
		}
	};
	/**
	 * 处理axios异常
	 *
	 * obj
	 *
	 */
	static exception(obj) {
		if (obj.status == 504) {
			ElementPlus.ElMessageBox.alert('服务器连接超时！', '警告', {
				confirmButtonText: '确定',
				type: 'error'
			});
		} else if (obj.status == 403) {
			ElementPlus.ElMessageBox.alert('你还没有权限访问，请联系管理员！', '警告', {
				confirmButtonText: '确定',
				type: 'warning'
			});
		} else if (obj && obj.isAxiosError) {
			const data = obj.response.data;
			if (data.status == 400) {
				ElementPlus.ElMessageBox.alert(data.message, '请求错误', {
					confirmButtonText: '确定',
					type: 'error'
				});
			} else if (this.sysEnv != 'prod') {
				ElementPlus.ElNotification({
					message: '<div class="msg-body"><label class="msg">' + (data.message || data.error) + '【' + data.path + '】</label></div>',
					dangerouslyUseHTMLString: true,
					position: 'bottom-left',
					customClass: 'bottom-notify bottom-notify-error',
					duration: 20000
				});
			}
		} else if (this.sysEnv != 'prod' && obj && obj.data) {
			var _uuid = this.UUID();
			this.stackData[_uuid] = obj.data;
			let btnHtml = obj.data.stackTrace ? '<div class="btn" onclick="SailTool.stackView(\'' + _uuid + '\')">堆栈信息</div>' : '';
			ElementPlus.ElNotification({
				message: '<div class="msg-body"><label class="msg">' + obj.data.message + '</label>' + btnHtml + '</div>',
				dangerouslyUseHTMLString: true,
				position: 'bottom-left',
				customClass: 'bottom-notify bottom-notify-error',
				duration: 20000
			});
		} else {
			console.error(obj)
		}
	};
	static stackView(stackId) {
		const stack = this.stackData[stackId];
		var htmlBody = '<div class="stack-body""><div>异常信息：' + stack.stackTrace + '</div></div>';
		ElementPlus.ElMessageBox.alert(htmlBody, "异常信息", {
			dangerouslyUseHTMLString: true,
			customClass: 'alert-dialog',
			center: true,
			callback: _ => {
				delete this.stackData[stackId];
			}
		});
	}
}
