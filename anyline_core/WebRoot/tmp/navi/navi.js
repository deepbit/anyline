//标签直接调用
function _navi_init(){
	_navi_go(1,_anyline_navi_conf);
}
//分页标签onclick
function _navi_go(n,conf) {
	if(!conf && "undefined" != typeof _anyline_navi_conf){
		conf = _anyline_navi_conf;
	}
	if (!n) {
		n = document.getElementById('_anyline_go').value;
	}

	if(conf){
		_navi_go_ajax(n,conf);
		return;
	}
	var frm = document.forms["_navi_frm"];
	if (frm) {
		if(frm._anyline_page.value == n){
			return;
		}
		frm._anyline_page.value = n;
		if(!conf){
			frm.submit();
			return;
		}
	}
}
//ajax分页
function _navi_go_ajax(n, conf){
	if (!n) {
		n = $('#_anyline_go').val();
	}
	if (!n) {
		n = 1;
	}
	var _navi_url				= null;	//数据来源
	var _navi_param 			= null;	//参数收集函数
	var _navi_callback 			= null;	//回调
	var _navi_container 		= null;	//内容显示容器
	var _navi_bodyContainer 	= null;	//
	var _navi_pageContainer 	= null;	//
	var _navi_empty 			= null; //空内容显示
	if(conf){
		_navi_url				= conf['url'];
		_navi_param 			= conf['param'];
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
		_navi_data = $("#_navi_frm").serialize();
	}
	_navi_data['_anyline_page'] = n;
	al.ajax({
		url:_navi_url,
		data:_navi_data,
		callback:function(result,data,msg){
			if(result){
				var _body = unescape(data['BODY']);
				var _navi = unescape(data['NAVI']);
				if(_navi_container){
					$('#'+_navi_container).html(_body);
				}else if(_navi_bodyContainer){
					$('#'+_navi_bodyContainer).html(_body);
				}
				if(_navi_pageContainer){
					if(_navi_empty && data['TOTAL_ROW'] == 0){
						$('#'+_navi_pageContainer).html(_navi_empty);
					}else{
						$('#'+_navi_pageContainer).html(_navi);
					}
				}
				if(_navi_callback){
					_navi_callback(result,data,msg);
				}
			}else{
				console.log(msg);
			}
		}
	});
}