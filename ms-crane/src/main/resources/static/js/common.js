const chars = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
			'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' , 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z'];

app.config.globalProperties.reLogioning = false;
app.config.globalProperties.loginSuccess = false;
app.config.globalProperties.dataRequestConfig = null;

app.config.globalProperties.confirm = function(){
	return new Promise(resolve => {
		this.$confirm('您的登录已超时，您是否要在新窗口中重新登录？', "提示", {
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

app.config.globalProperties.checkLoginStatusAsync = function(){
	return new Promise(resolve => {
		setTimeout(() => {
			axios.get('api/status').then(() => {
				this.$message.success("登录成功！");
				resolve(true);
			}).catch(() => {
				resolve(false);
			})
		}, 1500)
	})
}

app.config.globalProperties.isLoginSuccessAsync = function(){
	return new Promise(resolve => {
		setTimeout(() => {
			resolve(app.config.globalProperties.loginSuccess);
		}, 1000)
	})
}

if(axios){
	axios.defaults.headers["X-Requested-With"] = "XMLHttpRequest"; 
	axios.interceptors.request.use(config => {
		if(store.state.csrf.enable && config.method !='get'){
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
		if(error.response && error.response.status == 401 && error.response.config.url.indexOf("api/status") == -1){
			if(!app.config.globalProperties.reLogioning){
				app.config.globalProperties.reLogioning = true;
				app.config.globalProperties.dataRequestConfig = error.response.config;
				
				var confirm = await app.config.globalProperties.confirm();
				if(confirm){
					var url = JSON.parse(error.response.data.message).path;
		    		window.open(url);
					app.config.globalProperties.loginSuccess = false;
					for(var i = 0; i < 60; i++){
						let _loginSuccess = await app.config.globalProperties.checkLoginStatusAsync();
						if(_loginSuccess){
							app.config.globalProperties.loginSuccess = _loginSuccess;
							app.config.globalProperties.reLogioning = false;
							return new Promise(resolve => {
								resolve(axios(app.config.globalProperties.dataRequestConfig));
							})
						}
					}
					app.config.globalProperties.reLogioning = false;
					return Promise.reject(error);
				}else {
					return Promise.reject(error);
				}
			}else{
				for(var i = 0; i < 120; i++){
					let _loginSuccess = await app.config.globalProperties.isLoginSuccessAsync();
					if(_loginSuccess){
						return new Promise(resolve => {
							resolve(axios(app.config.globalProperties.dataRequestConfig));
						})
					}
				}
			}
		}else{
			try{
				var data = JSON.parse(error.response.data);
				return Promise.reject({...error.response, data: data});
			}catch(e){
				return Promise.reject({...error.response, data: error.response.data});
			}
		}
	});
}
if(typeof $ === 'function'){
	$.ajaxSetup({
		contentType:"application/json",
		dataType: "json",
		complete:function(xhr,textStatus){
			 if (xhr.getResponseHeader("sessionstatus") == "timeout") {
			        window.location.reload();
			 }
		},
		beforeSend: function(xhr,settings){
			if(store.state.csrf.enable && settings.type.toLowerCase != 'get'){
				if(settings.url.indexOf("?")>0)
					settings.url = settings.url+"&_csrf="+store.state.csrf.value;
				else
					settings.url = settings.url+"?_csrf="+store.state.csrf.value;
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

String.prototype.contains = function(s) {
	if (s == null) {
		return false;
	}
	if (this.indexOf(s) > -1) return true;
}


app.config.globalProperties.getSvgPathBys = function(s) {
	let iconClass = "default";
	
	if (!s) return "/3rds/icon-svg/" + iconClass + '.svg';;
	
	s = s.toLowerCase();
	
	if (s.contains("jdk") || s.contains("java")) {
		 iconClass = "java";
	} else if (s.contains("zookeeper")) {
		 iconClass = "zookeeper";
	} else if (s.contains("hdfs") || s.contains("hadoop") || s.contains('yarn')) {
		 iconClass = "hadoop";
	} else if (s.contains("hbase")) {
		iconClass = "hbase";
	} else if (s.contains("tdengine")) {
		iconClass = "tdengine";
	} else if (s.contains("flume")) {
		iconClass = "flume";
	} else if (s.contains("flink")) {
		iconClass = "flink";
	} else if (s.contains("mysql")) {
		iconClass = "mysql";
	} else if (s.contains("postgresql")) {
		iconClass = "postgresql";
	} else if (s.contains("kafka")) {
		iconClass = "kafka";
	} else if (s.contains("hive")) {
		iconClass = "hive";
	} else if (s.contains("redis")) {
		iconClass = "redis";
	}
	
	return "/3rds/icon-svg/" + iconClass + '.svg';
}

app.config.globalProperties.formatBytes = function(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
}

String.prototype.startWith = function(s) {
	if (s == null || s == "" || this.length == 0 || s.length > this.length)
		return false;
	if (this.substr(0, s.length) == s)
		return true;
	else
		return false;
}

Date.prototype.format = function(fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substring(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substring(("" + o[k]).length)));
        }
    }
    return fmt;
};

/**
 * 当前浏览器是否为谷歌浏览器
 */	
app.config.globalProperties.isChrome = (/chrome/.test(navigator.userAgent.toLowerCase()));

/**
 * el标签默认尺寸
 */	
app.config.globalProperties.$ELEMENT = { size: 'mini'};

/**
 * 复制文本到剪切板
 */
app.config.globalProperties.copyToClipboard = function copyToClipboard(text) {
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
    if(flag)
    	this.$message.success('已复制到剪切板！');
	else{
		this.$message('不支持，请手动复制！');
		textarea.select();
	}
    return flag;
}

/**
 * el-table列值有中文排序（静态）
 */
app.config.globalProperties.sortZhColumn = function(str1, str2, type) {
   return str1[type].localeCompare(str2[type]);
}

app.config.globalProperties.clone = function(val){
	return JSON.parse(JSON.stringify(val));
}

/**
 * 向URL追加k-v
 */
app.config.globalProperties.urlPush = function(key, value) {
	if(!value)
		this.delUrlParam(key);
	else{
		var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
		
		var search = window.location.search, newUrl = '';
		var separator = search.length > 0 ? "&" : "?";
		if (search.match(re)) {
			newUrl = search.replace(re, '$1' + key + "=" + value + '$2');
		} else if(value){
			newUrl = search + separator + key + "=" + value;
		}
		newUrl = window.location.origin + window.location.pathname + newUrl + window.location.hash;
		if(window.location.href != newUrl)
			window.history.replaceState({path: newUrl}, '', newUrl);
	}
};

/**
 * 删除url中的锚点信息
 */
app.config.globalProperties.delUrlAnchor = function() {
	var newUrl = window.location.origin + window.location.pathname +  window.location.search;
	window.history.replaceState({path: newUrl}, '', newUrl);
};

/**
 * 删除URL中的参数k-v
 */
app.config.globalProperties.delUrlParam = function(paramKey) {
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
    newUrl = beforeUrl + nextUrl;
    window.history.replaceState({path: newUrl}, '', newUrl);
};

/**
 * 获取URL参数k-v
 */
app.config.globalProperties.urlParams = function(){
	var params = {has: false};
	var query = window.location.search.substring(1);
	if(query){
		params['has'] = true;
	   	var vars = query.split("&");
       	for (var i=0;i<vars.length;i++) {
           	var pair = vars[i].split("=");
           	var value = decodeURIComponent(pair[1]);
           	if(value === 'true')
           		params[pair[0]] = true;
           	else if(value === 'false')
        	   	params[pair[0]] = false;
			else
        	   	params[pair[0]] = value;
		}
	}
	return params;
}

/**
 * 通知信息弹窗
 *
 * type： 类型，包括【success, warn, error】
 *
 * msg： 通知信息
 *
 */
app.config.globalProperties.message = function(type, obj){
	if(obj.status == 504){
		this.$alert('服务器连接超时！', '警告', {
			confirmButtonText: '确定',
			type: 'error'
	   	});
	}else if(obj.status == 403){
		this.$alert('你还没有权限访问，请联系管理员！', '警告', {
			confirmButtonText: '确定',
			type: 'warning'
	   	});
	}else if(obj.data){
		if(vue.$data.runModel == 'dev'){
			vue.$data.stack = obj.data.stackTrace;
			this.$notify({
		        message: '<div class="msg-body"><label class="msg">'+obj.data.message+'</label><label class="btn" onclick="vue.stackView()">堆栈信息</label></div>',
		        dangerouslyUseHTMLString: true,
		        position: 'bottom-left',
		        customClass: 'bottom-notify bottom-notify-' + type,
		        duration: 20000
		    });
	    }else{
	    	console.log(obj.data)
	    }
    }else{
    	throw obj;
    }
}
/**
 * 文件大小格式化
 *
 * bytes： 文件大小
 *
 */
app.config.globalProperties.bytesToSize = function(bytes) {
    if (bytes === 0) return '0 B';
    if(!bytes) return '';
    var k = 1024, // or 1024
        sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
        i = Math.floor(Math.log(bytes) / Math.log(k));
 	var num = parseFloat(bytes / Math.pow(k, i))
   	return Math.round(num * 100) / 100 + ' ' + sizes[i];
}

/**
 * 毫秒转易读
 *
 * ms： 毫秒 long
 *
 * showMs： 是否显示毫秒信息
 */
app.config.globalProperties.msToTimeStr = function(ms, showMs=false){
	var text = [];
	var msStr = ms % 1000, aSeconds = ms / 1000;
	if(aSeconds || aSeconds === 0){
		if (aSeconds >= 60) {
		    var minutes = Math.floor(aSeconds / 60);
		    aSeconds %= 60;
		    if (minutes >= 60) {
		        var hours = Math.floor(minutes / 60);
		        minutes %= 60;
		        if (hours >= 24) {
		            var day = Math.floor(hours / 24);
		            hours %= 24;
		            text.push(day)
		            text.push("天");
		        }
		        text.push(hours);
		        text.push("小时");
		    }
		    text.push(minutes);
		    text.push("分");
		}
		text.push(Math.floor(aSeconds));
		text.push("秒");
		if(showMs && msStr != 0){
			text.push(msStr);
			text.push('毫秒');
		}
	}else{
		text.push("-");
	}
	return text.join('');
}

/**
 * 时间格式化
 *
 * timestamp： 时间戳小
 *
 */
app.config.globalProperties.timestampToTime = function(timestamp) {
	if(!timestamp) return '';
    var date = new Date(timestamp);//时间戳为10位需*1000，时间戳为13位的话不需乘1000
    var Y = date.getFullYear() + '-';
    var M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1):date.getMonth()+1) + '-';
    var D = (date.getDate()< 10 ? '0'+date.getDate():date.getDate())+ ' ';
    var h = (date.getHours() < 10 ? '0'+date.getHours():date.getHours())+ ':';
    var m = (date.getMinutes() < 10 ? '0'+date.getMinutes():date.getMinutes()) + ':';
    var s = date.getSeconds() < 10 ? '0'+date.getSeconds():date.getSeconds();
    return Y+M+D+h+m+s;
}

app.config.globalProperties.toMs = function(val, unit) {
	if(val){
		if(unit == 's')
			return val * 1000;
		if(unit == 'm')
			return val * 60000;
		if(unit == 'h')
			return val * 3600000;
	}
	return val;
}

app.config.globalProperties.toUnit = function(val, unit) {
	if(val){
		if(unit == 's' || !unit)
			return val / 1000;
		if(unit == 'm')
			return val / 60000;
		if(unit == 'h')
			return val / 3600000;
	}
	return val || 0;
}

/**
 * 随机字符串【0-9、a-z、A-Z】
 *
 * length： 长度
 *
 */
app.config.globalProperties.randomNumber = function(length) {
	if(!length) length = 6;
	var number = '';
	for (var i = 0; i < length; i++) {
		number += chars[Math.round(Math.random() * 61)]
	}
	return number;
} 

/**
 * 右键菜单位置坐标
 *
 * MouseEvent
 * H: 菜单面板高度
 * W: 菜单面板宽度
 *
 */
app.config.globalProperties.contextmenuPos = function(MouseEvent, H, W) {
	let X = MouseEvent.clientX, Y = MouseEvent.clientY - 6;
	if(MouseEvent.clientX > document.body.clientWidth - W)
		X = X - W + 10;
	if(MouseEvent.clientY > document.body.clientHeight - H)
		Y = Y - H + 30;
	return {x: X, y: Y};
} 

/**
 * 生成UUID
 *
 */
app.config.globalProperties.UUID = function() {
    var d = new Date().getTime();
    if (window.performance && typeof window.performance.now === "function") {
        d += performance.now(); //use high-precision timer if available
    }
    var uuid = 'xxxxxxxxxxxxxxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
    return uuid.toUpperCase();
}

app.config.globalProperties.Base64Encode = function(text){
	return Base64.encode(text).split("").reverse().join("");
}

app.config.globalProperties.Base64Decode = function(text){
	return Base64.decode(text.split("").reverse().join(""));
}

/**
 * 时间  YY-MM-dd HH:mm:ss
 *
 */
app.config.globalProperties.latestDate = function(type1) {
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
	if (type1 == '年') {
		return year+'';
	} else if (type1 == '月') {
		return year + '-' + month;
	} else if(type1 == '日'){
		return year + '-' + month + '-' + day;
	}else if(type1 == '时'){
		return year + '-' + month + '-' + day + ' ' + houre;
	}else if(type1 == '分'){
		return year + '-' + month + '-' + day + ' ' + houre + ':' + minute;
	}else{
		return year + '-' + month + '-' + day + ' ' + houre + ':' + minute + ':' + second;
	}
}

/**
 * 等待
 *
 */
app.config.globalProperties.sleep = async function(time) {
	await this.sleepTime(time);
}

app.config.globalProperties.sleepTime = function(time) {
	if(isNaN(time)) {
		return;
	}
	return new Promise((resolve) => setTimeout(resolve, time));
}

/**
 * 下载文件，
 * url： 地址
 * args： 参数
 * name： 文件名
 *
 */
app.config.globalProperties.downloadFile = function(url, args, name, callback){
	axios({
        method: 'post',
        url: url,
        data: args || {},
        responseType:'blob'
	}).then(res=>{
		let blob = new Blob([res.data], {type: res.data.type})
        let downloadElement = document.createElement('a')
        let href = window.URL.createObjectURL(blob); //创建下载的链接
        downloadElement.href = href;
        downloadElement.download = decodeURIComponent(res.headers["content-disposition"].split("filename=")[1]) || name; //下载后文件名
        document.body.appendChild(downloadElement);
        downloadElement.click(); //点击下载
        document.body.removeChild(downloadElement); //下载完成移除元素
        window.URL.revokeObjectURL(href); //释放blob
        callback && callback(true)
	}).catch(error=>{
		callback && callback(error)
	})
}

/**
 * 下载临时文件，
 * fileName： 文件名称
 * tempFileId: 临时文件ID
 */
app.config.globalProperties.downloadTempFile = function(fileName, tempFileId){
	window.location.href = "/sailworks/api/downloadTempFile/"+ tempFileId +"?fileName=" + encodeURIComponent(encodeURIComponent(fileName));
}

/**
 * 下载字符串到文件
 * fileName: 文件名
 * content: 文件内容
 */
app.config.globalProperties.downloadStringToFile = function(fileName, content){
	var eleLink = document.createElement('a')
	  // 设置a标签 download 属性，以及文件名
	eleLink.download = fileName
	  // a标签不显示
	eleLink.style.display = 'none'
	  // 获取字符内容，转为blob地址
	var blob = new Blob([content])
	  // blob地址转为URL
	eleLink.href = URL.createObjectURL(blob)
	  // a标签添加到body
	document.body.appendChild(eleLink)
	  // 触发a标签点击事件，触发下载
	eleLink.click()
	  // a标签从body移除
	document.body.removeChild(eleLink)
}

app.config.globalProperties.clone = function(val){
	return JSON.parse(JSON.stringify(val));
}

app.config.globalProperties.localStorage = function(key, val){
	let _this = this;
	while(_this.$parent){
		_this = _this.$parent;
	}
	let sKey = _this.user.id + "_" + key;
	if(val){
		if(val == '_DEL'){
			localStorage.removeItem(sKey) 
		}else{
			localStorage.setItem(sKey, val)
		}
	}else{
		return localStorage.getItem(sKey)
	}
}

app.config.globalProperties.validForbid = function (value, number = 512) {
  value = value.replace(/[`~!@#$%^&*()\-+=<>?:"{}|,./;'\\[\]·~！@#￥%……&*（）——\-+={}|《》？：“”【】、；‘’，。、]/g, '').replace(/\s/g, "");
  if (value.length >= number) {
    this.$message({
      type: "warning",
      message: `输入内容不能超过${number}个字符`
    });
  }
  return value;
}

app.config.globalProperties.base64 = function(input) {
	var keyStr = "ABCDEFGHIJKLMNOP" + "QRSTUVWXYZabcdef" + "ghijklmnopqrstuvwxyz0123456789+/" + "=";

	var output = "";
	var chr1, chr2, chr3 = "";
	var enc1, enc2, enc3, enc4 = "";
	var i = 0;
	do {
		chr1 = input.charCodeAt(i++);
		chr2 = input.charCodeAt(i++);
		chr3 = input.charCodeAt(i++);
		enc1 = chr1 >> 2;
		enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
		enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
		enc4 = chr3 & 63;
		if (isNaN(chr2)) {
			enc3 = enc4 = 64;
		} else if (isNaN(chr3)) {
			enc4 = 64;
		}
		output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2)
			+ keyStr.charAt(enc3) + keyStr.charAt(enc4);
		chr1 = chr2 = chr3 = "";
		enc1 = enc2 = enc3 = enc4 = "";
	} while (i < input.length);

	return output;
}

app.config.globalProperties.hasAuth = function(authName, subspaceId){
	if(subspaceId){
		authName = subspaceId + ':' + authName;
	}
	return this.authes.indexOf(authName) != -1;
}

app.config.globalProperties.shareChart = function(option, url, callback){
	axios.post('api/chart/share', {
		id: url.split('xz_c_id=')[1],
		option: JSON.stringify({...option, dataset: {}})
	}).then(() => {
		callback(true);
	}).catch(() => {
		callback(false);
	})
}

app.config.globalProperties.xzChart = function(chart, option, url){
	var myButtons = {
		myDownload: {
			show: this.isShowDownloadAll,
			title: '保存为图片',
			icon : 'path://M76.4773 958.962751c-6.930853 0-12.515035-5.584182-12.515035-12.44852 0-6.903224 5.584182-12.519128 12.447497-12.519128l871.03312 0c6.935969 0 12.536524 5.634324 12.549827 12.56006 0 6.823406-5.586229 12.407588-12.452613 12.407588L76.4773 958.962751zM511.233032 829.193199c-4.739954-0.014326-7.961322-2.543939-9.462512-4.054339L128.229988 451.40083c-2.435469-2.372024-3.833305-5.743818-3.833305-9.251711 0.013303-7.287987 5.989411-13.248745 13.320377-13.248745 3.571339 0 6.915503 1.384533 9.417487 3.89675l350.888626 350.924441L498.023172 77.354785c0-7.414877 5.976108-13.404288 13.32447-13.417591 7.249101 0 13.179161 5.990434 13.179161 13.352099l0.106424 706.494693 350.845647-350.921371c1.794879-1.793856 4.850471-3.934612 9.211802-3.934612l0.239454 0c7.325849 0 13.284561 5.962805 13.284561 13.291724 0 4.652973-2.548032 7.897877-4.068665 9.417487L520.594237 825.25961C518.86178 827.040163 515.830747 829.164546 511.233032 829.193199z',
			onclick : () => {
				var imgUrl = chart.getDataURL({
					pixelRatio: 5,
					backgroundColor: '#fff',
					excludeComponents: ['toolbox']
				});
			    var a = document.createElement("a");
			    a.setAttribute("href", imgUrl);
			    a.setAttribute("download", chart.getOption().title[0].text);
			    a.setAttribute("target","_blank");
			    let clickEvent = document.createEvent("MouseEvents");
			    clickEvent.initEvent("click", true, true);  
			    a.dispatchEvent(clickEvent);
			    $(a).remove();
			},
		},
		myShare : {
			show : true,
			title : '分享',
			icon : 'path://M853.578 661.715c-65.68 0-119.381 35.158-144.39 89.736L391.713 616.41c18.518-28.693 28.692-63.85 28.692-101.764 0-15.711-1.854-31.448-5.563-46.258l202.703-157.269c26.864 20.348 61.12 32.376 99.059 32.376 91.641 0 160.128-68.46 160.128-159.097 0-90.663-68.512-159.124-160.128-159.124-91.641 0-159.2 68.46-159.2 159.124 0 23.129 4.636 44.404 12.028 62.897L382.44 390.68c-37.012-55.505-98.106-94.372-167.52-94.372A204.97 204.97 0 0 0 9.438 501.691a204.97 204.97 0 0 0 205.485 205.356c42.575 0 82.369-11.076 114.77-31.423l364.711 155.388c4.636 86.027 71.268 149.851 159.175 149.851 91.667 0 159.226-68.435 159.226-159.097 0-90.663-68.512-160.05-159.2-160.05z',
			onclick : () => {
				this.shareChart(option, url, flag => {
					if(flag){
				        this.$prompt('作为<b>&lt;iframe&gt;</b>中显示的文档的 URL', '', {
				        	dangerouslyUseHTMLString: true,
				          	confirmButtonText: '复制到剪切板',
				          	inputValue: url,
				          	showCancelButton: false,
				          	closeOnClickModal: false,
				          	closeOnHashChange: false,
				          	customClass: 'share-pane'
				        }).then(({ value }) => {
							this.copyToClipboard(value);	        
				        });
			        }else{
			        	this.$alert('图表分享异常！', '警告', {
					 		confirmButtonText: '确定',
					 		type: 'warning'
					  	});
			        }
				});
			},
		},
	}
	chart.setOption({
		toolbox : {
			showTitle : false,
			feature : myButtons,
			tooltip: {
	            show: true,
	            confine: true,
	            enterable: false,
	            formatter: (param) => {
            		return '<div>' + param.title + '</div>';
	            },                                                                                                                                                                                         
	            textStyle: {
	                fontSize: 12,
	                color: '#3FA7DC'
	            },
	            borderWidth : 1,
	            bordeColor: '#409eff',
	            extraCssText: 'box-shadow: 2px 2px 10px #ccc;'
	        }
		}
	})
}

const successMessage = app.config.globalProperties.$message.success,
	infoMessage = app.config.globalProperties.$message.info,
	errorMessage = app.config.globalProperties.$message.error,
	warnMessage = app.config.globalProperties.$message.warning;

app.config.globalProperties.$message.success = function (msg) {
	if(typeof msg == 'string')
		successMessage({
			customClass: 'fixed',
			offset: 4,
		  	showClose: true,
	    	message: msg,
		})
	else
		successMessage(msg);
}

app.config.globalProperties.$message.info = function (msg) {
	if(typeof msg == 'string')
		infoMessage({
			customClass: 'fixed',
			offset: 4,
		  	showClose: true,
	    	message: msg,
		})
	else
		infoMessage(msg);
}

app.config.globalProperties.$message.error = function (msg) {
	if(typeof msg == 'string')
		errorMessage({
			customClass: 'fixed',
			offset: 4,
		  	showClose: true,
	    	message: msg,
		})
	else
		errorMessage(msg);
}

app.config.globalProperties.$message.warning = function (msg) {
	if(typeof msg == 'string')
		warnMessage({
			customClass: 'fixed',
			offset: 4,
		  	showClose: true,
	    	message: msg,
		})
	else
		warnMessage(msg);
}

app.config.globalProperties.format$yyyyMMddHHmmss = function (timestamp) {
	let date = new Date(timestamp)
	let year = date.getFullYear()
	let month = date.getMonth() + 1
	month = month < 10 ? '0' + month : month
	let day = date.getDate()
	day = day < 10 ? '0' + day : day
	let HH = date.getHours()
	HH = HH < 10 ? '0' + HH : HH
	let mm = date.getMinutes()
	mm = mm < 10 ? '0' + mm : mm
	let ss = date.getSeconds()
	ss = ss < 10 ? '0' + ss : ss
	return  year + '-' + month + '-' + day + ' ' + HH + ':' + mm + ':' + ss
}

app.config.globalProperties.format$yyyyMMdd = function (timestamp) {
	let date =  this.format$yyyyMMddHHmmss(timestamp)
	// yyyy-MM-dd
	return date.substring(0, 10)
}


app.config.globalProperties.parse$timestamp = function (dateStr) {
	return new Date(dateStr).getTime()
}

app.config.globalProperties.lastDateStr = function(dateStr) {
	if(!dateStr || (typeof dateStr !== 'string')) return "";

	var newDate = new Date();

	var fmt = 'yyyy-MM-dd HH:mm:ss.SSS';
	var o = {
        "M+": newDate.getMonth() + 1, //月份
        "d+": newDate.getDate(), //日
        "H+": newDate.getHours(), //小时
        "m+": newDate.getMinutes(), //分
        "s+": newDate.getSeconds(), //秒
        "q+": Math.floor((newDate.getMonth() + 3) / 3), //季度
        "S": newDate.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (newDate.getFullYear() + "").substring(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substring(("" + o[k]).length)));
        }
    }
    var currDateStr = fmt;

    var currDate = newDate.getTime();
	var editDate = new Date(dateStr).getTime();

	var offset = (currDate - editDate) / 1000;
	if(offset < 60)
		return '1分钟内';
	else if(offset < 300)
		return '5分钟内';
	else if(offset < 6000)
		return '10分钟内';
	else if(offset < 1800)
		return '半小时内';
	else if(offset < 3600)
		return '1小时内';
	else if(dateStr.substring(0, 11) == currDateStr.substring(0, 11))
		return dateStr.substring(11, 19);
	else if(dateStr.substring(0, 4) == currDateStr.substring(0, 4))
		return dateStr.substring(5, 19);
	else
		return dateStr.substring(0, 19);
}


app.config.globalProperties.success = function(message) {
	ElementPlus.ElMessage({
	    showClose: true,
	    message,
	    type: 'success'
	});
}

app.config.globalProperties.error = function(message) {
	ElementPlus.ElMessage({
	    showClose: true,
	    message,
	    type: 'error'
	});
}

app.config.globalProperties.warning = function(message) {
	ElementPlus.ElMessage({
	    showClose: true,
	    message,
	    type: 'warning'
	});
}

app.config.globalProperties.info = function(message) {
	ElementPlus.ElMessage({
	    showClose: true,
	    message
	});
}
