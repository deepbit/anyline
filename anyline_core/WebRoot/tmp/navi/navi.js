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
	var data_url		= null;	//数据来源
	var param 			= null;	//参数收集函数
	var callback 		= null;	//回调
	var container 		= null;	//内容显示容器
	var bodyContainer 	= null;	//
	var naviContainer 	= null;	//
	var empty 			= null; //空内容显示
	if(conf){
		data_url		= conf['url'];
		param 			= conf['param'];
		callback 		= conf['callback'];
		container 		= conf['container'];
		bodyContainer	= conf['bodyContainer'];
		naviContainer 	= conf['naviContainer'];
		empty 			= conf['empty'];
	}
	data = {};
	if(typeof param === 'function' ){
		data = param();
	}else{
		data = $("#_navi_frm").serialize();
	}
	data['_anyline_page'] = n;
	al.ajax({
		url:data_url,
		data:data,
		callback:function(result,data,msg){
			if(result){
				if(container){
					$('#'+container).html(data['BODY']);
				}
				if(naviContainer){
					if(empty && data['TOTAL_ROW'] == 0){
						$('#'+naviContainer).html(empty);
					}else{
						$('#'+naviContainer).html(data['NAVI']);
					}
				}
				if(callback){
					callback(result,data,msg);
				}
			}else{
				console.log(msg);
			}
		}
	});
}
/*
onload调用
下标调用
条件切换调用
============

function changePage(n){
	if(!n){
		n = $('#_anyline_page').val();
}
if(!n){
	n = 1;
}
var cons = '-1';
$('.investNav .current').each(function(){
	var val = $(this).attr('data-value');
	if(val >0){
		cons += ','+ val;
	}
});
al.ajax({
	url:'/web/hm/bor/l.do',
	data:{_anyline_page:n, cons:cons},
	callback:function(result,data,msg){
		var to = new Date();
		if(result){
			$('.caseList').html(data['HTML']);
			$('.caseNavi').html(data['NAVI']);
			if($('.caseList .caseDetail').length<1){
				$('.caseNavi').hide();
			}else{
				$('.caseNavi').show();
				}
				init();
			}else{
			}
		}
	});
}
*/