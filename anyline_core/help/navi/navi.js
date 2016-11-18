//标签直接调用
function _navi_init(conf){
	_navi_go(1,conf);
}
function _navi_jump(conf){
	var frm = $(event.target).closest("form");
	var n = frm.find('._anyline_jump_txt').val();
	_navi_go(n,conf);
}
//分页标签onclick
function _navi_go(n,conf) {
	if(!n){
		n = 1;
	}
	if(typeof(conf) != "undefined" && conf){
		_navi_go_ajax(n,conf);
		return;
	}
	var frm = $(event.target).closest("form");
	if(frm){
		if(frm.find('._anyline_navi_cur_page').val() == n){
			return;
		}
		frm.find('._anyline_navi_cur_page').val(n);
		frm.submit();
	}else{
		console.log('分页异常');
	}
}
//ajax分页
function _navi_go_ajax(n, conf){
	if (!n) {
		n = 1;
	}
	var _navi_url				= null;	//数据来源
	var _navi_param 			= null;	//参数收集函数
	var _navi_callback 			= null;	//回调
	var _navi_before			= null;	//渲染前调用
	var _navi_after				= null; //渲染后调用
	var _navi_container 		= null;	//内容显示容器
	var _navi_bodyContainer 	= null;	//
	var _navi_pageContainer 	= null;	//
	var _navi_empty 			= null; //空内容显示
	if(conf){
		_navi_url				= conf['url'];
		_navi_param 			= conf['param'];
		_navi_before 			= conf['before'];
		_navi_after 			= conf['after'];
		_navi_callback 			= conf['callback'];
		_navi_container 		= conf['container'];
		_navi_bodyContainer		= conf['bodyContainer'];
		_navi_pageContainer 	= conf['naviContainer'];
		_navi_empty 			= conf['empty'];
		
	}
	var _navi_data = {};
	if(typeof _navi_param === 'function' ){
		_navi_data = _navi_param();
	}else{
		//_navi_data = $("#_navi_frm").serialize();
	}
	_navi_data['_anyline_page'] = n;
	_navi_data['_anyline_navi_conf_'] = conf['_anyline_navi_conf_'];
	al.ajax({
		url:_navi_url,
		data:_navi_data,
		callback:function(result,data,msg){
			if(result){
				var _body = unescape(data['BODY']);
				var _navi = unescape(data['NAVI']);

				if(_navi_before && typeof _navi_before == 'function'){
					_navi_before();
				}

				if(_navi_callback && typeof _navi_callback == 'function'){
					_navi_callback(result,data,msg);
				}else{
					if(_navi_container){
						$('#'+_navi_container).html(_body);
					}else if(_navi_bodyContainer){
						$('#'+_navi_bodyContainer).html(_body);
					}
					if(_navi_pageContainer){
						if(_navi_empty != null && data['TOTAL_ROW'] == 0){
							$('#'+_navi_pageContainer).html(_navi_empty);
						}else{
							$('#'+_navi_pageContainer).html(_navi);
						}
					}
				}
				if(_navi_after && typeof _navi_after == 'function'){
					_navi_after();
				}
			}else{
				console.log(msg);
			}
		}
	});
}