var _cur_page = 1;//当前页	
var _is_request = false;
/*
		参数:
		template:模板文件key 加密后的path
		parser:模板内容解析器
		container:模板容器,默认$('.data_container')
		callback:回调
		
		
	*/
	function _pfLoad(conf){
		var template_url = '/al/tmp/load_style.do'; 
		var template_key = conf.template || style_template_key;
		if(!template_url || !template_key || !conf.url){
			return;
		}
		var template_body = templates[template_key];
		if(!template_body){
			//加载样式模板
			al.ajax({
				url:template_url,
				data:{path:template_key},
				callback:function(result,data,msg){
					if(result){
						templates[template_key] = unescape(data);
						_pfLoadData(conf);
					}else{
					}
				}
			});
		}else{
			//直接加载数据
			_pfLoadData(conf);
		}
	}
	/**
	* 加载数据
	*/
	function _pfLoadData(conf){
		if(_is_request){
			return;
		}
		_is_request = true;
		
		var data_url = conf.url;
		var template_key = conf.template || style_template_key;
		var template_body = templates[template_key];
		var data = conf.data;
		if(!data){
			data = {};
		}
		if(!data_url){
			return;
		}
		var frm = conf.form;

		if(!frm){
			frm = $('.pageForm')
		}
		if(!frm[0]){
			frm = $('form').first()
		}
		if(frm[0]){
			data = frm.serialize();
		}
		

		if(conf.init){
			_cur_page = 1;
		}
		
		
		data._anyline_page=_cur_page;
		_fpBeforeRequest(conf);
		if(data_url.indexOf('?') > 0){
			data_url = data_url + '&_anyline_page='+_cur_page;
		}else{
			data_url = data_url + '?_anyline_page='+_cur_page;
		}
		al.ajax({
			url:data_url,
			data:data,
			callback:function(result,rtn,msg){
				if(typeof(rtn) == 'string'){
					return;
				}
				_is_request = false;
				if(result){
					if(conf.beforeRender){
						conf.beforeRender(rtn);
					}
					_pfRender(template_body,rtn, conf);
					if(conf.afterRender){
						conf.afterRender(rtn);
					}
					if( typeof cfAfterRender === 'function' ){
						cfAfterRender();
					}
				}else{
					ljs.alert("数据加载失败:"+msg);
				}
			}
		});
	}
	//渲染
	function _pfRender(template_body,data, conf){
		_fpSetRequestNotice(conf,'');
		var container = conf.container;
		if(!container){
			container = $('.renderContainer');
		}
		if(!container[0]){
			container = $('<div class="renderContainer"></div>');
		}
		if(_cur_page == 1){
			container.html('');//初始化或查询时，清空内容,分页时不清空
		}

		//设置分页
		_cur_page = _cur_page + 1;
		var parser = conf.parser;
		if(data){
			if(typeof(data.length) == 'undefined'){
				container.append(_pfReplaceVar(template_body, data, parser));
			}else if(data.length >= 1){
				for ( var i = 0; i < data.length; i++) {
					container.append(_pfReplaceVar(template_body, data[i], parser));
				}
			}else{
				_fpSetRequestNotice(conf,'===没有更多内容了===');
			}
		}else{
			//没有更多内容
			_fpSetRequestNotice(conf,'===没有更多内容了===');
		}
		$('.data_container').append(container);
		if(conf.callback){
			conf.callback();
		}
	}

	//替换点位符
	function _pfReplaceVar(content, data, parser){
			var item = data;
			//根据parser对应
			if(parser){
				for(var k in parser){
					var jk = parser[k];
					var v = item[jk];
					content = content.replace(new RegExp('{'+k+'}','g'),v);
				}
			}
			//自动对应
			for(var k in item){
				var v = item[k];
				content = content.replace(new RegExp('{'+k+'}','g'),v);
				//处理对象属性 
				for(var k0  in v){
					var jk0 = k0;
					var v0 = v[jk0];
					var fullKey = '{'+k+'.'+k0+'}';
					content = content.replace(new RegExp(fullKey,'g'),v0);
				}
			}
			content = content.replace(/{([\w.]+)\}/g,'');
			return content;
	}

	//设置加载提示
	function _fpSetRequestNotice(conf,content){
		var container = conf.container;
		if(!container){
			container = $('.renderContainer');
		}
		if(!container[0]){
			container = $('<div class="renderContainer"></div>');
		}
		
		var _request_notice = $('._request_notice');
		if(!_request_notice[0]){
			_request_notice = $('<div class="_request_notice" style="text-align:center;line-height:46px;color:#8c8c8c"></div>');
		}
		_request_notice.html(content);
		container.append(_request_notice);
		$('.data_container').append(container);
	}
	//加载请求之前 
	function _fpBeforeRequest(conf){
		_fpSetRequestNotice(conf,'数据加载中...');
	}