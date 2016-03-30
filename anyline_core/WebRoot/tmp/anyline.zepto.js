function al(){}
al.url = null;					//请求的URL
al.value = 'CD';				//键值对时 KEY
al.label = 'NM';				//键值对时 VALUE
al.async = true;				//是否异步请求
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

function init(config){
	if(typeof config['async'] == "undefined") {config['async'] = true;}
	if(!config['success']){config['success']=function(data){};}
	if(!config['callback']){config['callback']=function(result,data,msg){};}
	if(!config['fail']){config['fail']=function(data,msg){};}
	return config;
}
al.submit = function(frm,config){
	if(!frm){return;}
	config = config.init(config);
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
	config = init(config);
	$.ajax({
	   async: config.async,
	   type: 'post',
	   url: config.url,
	   data: config.data,
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
	if(result){
		//函数回调
		var success = config.success;
		if(success){
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
	if(callback){
		callback(result,data,message);
	}
};
function _ajax_error(XMLHttpRequest, textStatus, errorThrown){
	//config.lock();
//	if(typeof(art) != "undefined"){
//		art.dialog({content:XMLHttpRequest.responseText});
//	}else{
//		alert("状态:"+textStatus+"\n消息:"+XMLHttpRequest.responseText);
//	}
};