function al(){}
al.url = null;					//请求的URL
al.value = 'CD';				//键值对时 KEY
al.label = 'NM';				//键值对时 VALUE
al.async = true;				//是否异步请求
al.cache = false;				//是否缓存数据(result=true时缓存data)
al.data = null;					//请求参数
al.user = null;					//用户名
al.password = null;				//密码
al.success = function(o){};		//请求成功时的回调函数 与callback相同
al.callback = function(o){};	//请求成功时的回调函数 与success相同
al.fail = function(data,msg){};	//请求失败的回调函数
al.type = 'POST';				//请求方法类型
al.form = null;					//提交的form
al.xhr = null;					//xhr
al.dataType = 'json';			//返回的数据类型
al.json = null;					//返回的数据
var _anyline_ajax_cache_data = {};
al.init = function(config){
	if(typeof config['async'] == "undefined") {config['async'] = true;}
	if(!config['success']){config['success']=function(data){};}
	if(!config['callback']){config['callback']=function(result,data,msg){};}
	if(!config['fail']){config['fail']=function(data,msg){};}
	return config;
}
al.submit = function(frm,config){
	if(!frm){return;}
	config = al.init(config);
	$(frm).ajaxSubmit({
		type:'post',
		dataType: 'json',
		success:function(json){
			var result = json['result'];
			var message = json['message'];
			var data;
			var type = json['type'];
			config['json'] = json;
			_ajax_success(config);
		},
	   error:function(XMLHttpRequest, textStatus, errorThrown) {
	   		_ajax_error(XMLHttpRequest, textStatus, errorThrown);
	   }
	});
	
};
al.ajax = function(config){
	config = al.init(config);
	var _data = config.data;
	
	if(typeof _data == 'function'){
		_data = _data();
	}
	config.data = _data;
	if(config['cache']){
		var _request_key = _fn_pack_rquest_key(config.url, _data);
		var cache_data = _anyline_ajax_cache_data[_request_key];
		if(cache_data){
			var success = config.success;
			if(typeof success == 'function'){
				success(cache_data,'');
			}
			var callback = config.callback;
			if(typeof callback == 'function'){
				callback(true,cache_data,'');
			}
			return;
		}
	}
	
	$.ajax({
	   async: config.async,
	   type: 'post',
	   url: config.url,
	   data: _data,
	   dataType: 'json',
	   success: function(json){
	   		config.json = json;
			_ajax_success(config);
	   },
	   error:function(XMLHttpRequest, textStatus, errorThrown) {
	   		_ajax_error(XMLHttpRequest, textStatus, errorThrown);
	   }
	});
};
/**
 * 加载服务器端文件
 * path必须以密文提交 <al:des>/WEB-INF/template/a.jsp</al:des>
 * 以WEB-INF为相对目录根目录
 * al.template('/WEB-INF/template/a.jsp',function(result,data,msg){alert(data)});
 * al.template({path:'template/a.jsp', id:'1'},function(result,data,msg){});
 * 模板文件中以${param.id}的形式接收参数
 * 
 * 对于复杂模板(如解析前需要查询数据)需要自行实现解析方法js中 通过指定解析器{parser:'/al/tmp/load1.do'}形式实现
 *controller中通过 WebUtil.parseJsp(request, response, file)解析JSP
 *注意 parsejsp后需要对html编码(以避免双引号等字符在json中被转码) js接收到数据后解码
 *escape unescape
 */
var _anyline_template_file= {};
al.template = function(config, fn){
	if(typeof config == 'string'){
		config = {path:config};
	}
	var parser_url = '/al/tmp/load.do';
	if(config['parser']){
		parser_url = config['parser'];
	}
	var cache = true;
	if(config['cache'] == false){
		cache = false;
	}
	al.ajax({
		url:parser_url,
		data:config,
		cache:cache,
		callback:function(result,data,msg){
			data = unescape(data);
			fn(result,data,msg);
		}
	});
}
function _ajax_success(config){
	var result = config.json['result'];
	var message = config.json['message'];
	var data;
	var type = config.json['type'];
	
	//解析数据
	if(type=='string' || type=='number'){
		data = config.json['data'];
	}else if(type=='map'){
		data = config.json['data'];
	}else if(type == 'list'){
		var tmp = config.json['data'];
		data = tmp;
	}
	//附加操作方法
	if(data){
		data.get=function(idx,key){
			if(!key){
				key = idx;
				idx = 0;
			}
			if(data.length && idx<data.length){
				return data[idx][key];
			}else{
				return null;
			}
		};
	}

	if(config['cache']){
		var _request_key = _fn_pack_rquest_key(config.url, config.data);
		_anyline_ajax_cache_data[_request_key] = data;
	}
	if(result){
		//函数回调
		var success = config.success;
		if(typeof success == 'function'){
			success(data,message);
		}
	}else{
		var fail = config.fail;
		if(fail){
			fail(data,message);
		}
		if(config.json['url']){
			window.document.location.target = '_top';
			window.document.location.href = json['url'];
		}
		
	}
	
	var callback = config.callback;
	if(typeof callback == 'function'){
		callback(result,data,message);
	}
};

function _ajax_error(XMLHttpRequest, textStatus, errorThrown){
	//config.lock();
//	if(typeof(art) != "undefined"){
//		art.dialog({content:XMLHttpRequest.responseText});
//	}else{
		console.log("状态:"+textStatus+"\n消息:"+XMLHttpRequest.responseText);
//	}
};

function _fn_pack_rquest_key(url,param){
	var result = url;
	for(var i in param){
		result += '[' + i + '=' + param[i] + ']';
	}
	return result;
}