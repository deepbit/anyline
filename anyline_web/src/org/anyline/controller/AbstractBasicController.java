/* 
 * Copyright 2006-2015 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.controller;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.anyline.config.ConfigParser;
import org.anyline.config.ParseResult;
import org.anyline.config.db.impl.PageNaviImpl;
import org.anyline.config.http.ConfigStore;
import org.anyline.config.http.impl.ConfigStoreImpl;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.tag.Navi;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.Constant;
import org.anyline.util.WebUtil;
import org.apache.log4j.Logger;

public class AbstractBasicController{
	protected Logger log = Logger.getLogger(this.getClass());
	protected String dir;				// <result>文件默认目录
	protected final String FAIL = "fail";
	protected final String JSON = "json";
	protected final String SUCCESS = "success";

	
	
	/******************************************************************************************************************
	 * 
	 * 封装参数
	 * 
	 *******************************************************************************************************************/

	/**
	 * 从封装request参数到实体类对象
	 * 
	 * @param <T>
	 * @param clazz
	 * @param encrypt
	 *            参数名是否经过加密
	 * @return
	 */
	public <T> T entity(HttpServletRequest request, Class<T> clazz, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		T entity = null;
		if (null == clazz) {
			return entity;
		}
		try {
			entity = (T) clazz.newInstance();
			/* 属性赋值 */
			if (null != params && params.length > 0) {
				/* 根据指定的属性与request参数对应关系 */

				for (String param : params) {
					/* 解析属性与request参数对应关系 */

					ParseResult parser = ConfigParser.parse(param,true);
					Object value = ConfigParser.getValues(request, parser);//getParam(request,parser.getKey(), parser.isKeyEncrypt(), parser.isValueEncrypt());
					BeanUtil.setFieldValue(entity, parser.getId(), value);
				}// end for
			} else {// end指定属性与request参数对应关系
				/* 未指定属性与request参数对应关系 */
				/* 读取类属性 */
				List<Field> fields = BeanUtil.getFields(clazz);
				for (Field field : fields) {
					/* 取request参数值 */
					String fieldName = field.getName();
					Object value = getParam(request,fieldName, keyEncrypt, valueEncrypt);
					/* 属性赋值 */
					BeanUtil.setFieldValue(entity, field, value);
				}
			}// end 未指定属性与request参数对应关系
				// 其他初始化工作("regCd","regTime","uptTime","uptCd","isActive")
//			Object client = request
//					.getAttribute(Constant.REQUEST_ATTR_HTTP_CLIENT);
			// BeanUtil.setValue(entity, "clientTrace", client);
			//BeanUtil.setFieldValue(entity, "clientTrace", client);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entity;
	}
	
	public <T> T entity(HttpServletRequest request, Class<T> clazz, boolean keyEncrypt, String... params) {
		return entity(request,clazz, keyEncrypt, false, params);
	}

	public <T> T entity(HttpServletRequest request, Class<T> clazz, String... params) {
		return entity(request, clazz, false, false, params);
	}

	public DataRow entityRow(HttpServletRequest request, DataRow row, boolean keyEncrypt, boolean valueEncrypt, String... params) {

		/**
		 * 
			1 数据库查询             clear  X
			2 entityRow  X      X
			3 new        X      X
			4 null       X      X
		 */
		
//		boolean clearUpdateColumns = true;
		if (null == row) {
			row = new DataRow();
		}
//		if(row.getUpdateColumns().size() == 0){
//			
//		}
		
		if (null != params && params.length > 0) {
			for (String param : params) {
				ParseResult parser = ConfigParser.parse(param,true);
				Object value = ConfigParser.getValue(request, parser);
				row.put(parser.getId(), value);
			}
		}else{
			Enumeration<String> names = request.getParameterNames();
			while(names.hasMoreElements()){
				String name = names.nextElement();
				String value = request.getParameter(name);
				row.put(name, value);
		    }
		}
//		if(clearUpdateColumns){
//			row.clearUpdateColumns();
//		}
		return row;
	}
	
	public DataRow entityRow(HttpServletRequest request, DataRow row, boolean keyEncrypt, String... params) {
		return entityRow(request, row, keyEncrypt, false, params);
	}
	public DataRow entityRow(HttpServletRequest request, DataRow row, String... params) {
		return entityRow(request, row, false, false, params);
	}

	public DataRow entityRow(HttpServletRequest request, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		return entityRow(request,null, keyEncrypt, valueEncrypt, params);
	}
	public DataRow entityRow(HttpServletRequest request, boolean keyEncrypt, String... params) {
		return entityRow(request,null, keyEncrypt, false, params);
	}

	public DataRow entityRow(HttpServletRequest request, String... params) {
		return entityRow(request, null, false, false, params);
	}

	public DataSet entitySet(HttpServletRequest request, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		DataSet set = new DataSet();
		
		if (null != params && params.length > 0) {

			Map<String,List<Object>> map = new HashMap<String,List<Object>>();
			int size = 0;
			for (String param : params) {
				ParseResult parser = ConfigParser.parse(param,true);
				List<Object> values = ConfigParser.getValues(request, parser);
				map.put(parser.getId(), values);
				if(size <= values.size()){
					size = values.size();
				}
			}
			
			for(int i=0; i<size; i++){
				DataRow row = new DataRow();
				for (String param : params) {
					ParseResult parser = ConfigParser.parse(param,true);
					List<Object> values = map.get(parser.getId());
					if(values.size() > i){
						row.put(parser.getId(), values.get(i));
					}else{
						List<ParseResult> defs = parser.getDefs();
						if(null != defs && defs.size()>0){
							ParseResult def = defs.get(0);
							String key = def.getKey();
							if(null != key && key.startsWith("{") && key.endsWith("}")){
								// col:value
								key = key.substring(1, key.length()-1);
							}
							row.put(parser.getId(), key);
						}
					}
				}

//				Object client = request.getAttribute(Constant.REQUEST_ATTR_HTTP_CLIENT);
//				if (null == client) {
//					client = new ClientTrace(request);
//				}

				row.clearUpdateColumns();
//				row.setClientTrace(client);
				set.addRow(row);
			}
		}
		return set;
	}
	

	public DataSet entitySet(HttpServletRequest request, boolean keyEncrypt, String... params) {
		return entitySet(request,keyEncrypt, false, params);
	}

	public DataSet entitySet(HttpServletRequest request, String... params) {
		return entitySet(request, false, false, params);
	}

	/**
	 * 解析参数
	 * 
	 * @param navi
	 *            是否分页
	 * @param conditions
	 *            参数
	 */
	protected ConfigStore parseConfig(HttpServletRequest request, boolean navi, String... configs) {
		ConfigStore store = new ConfigStoreImpl(configs);
		if (navi) {
			PageNavi pageNavi = parsePageNavi(request);
			store.setPageNavi(pageNavi);
		}
		store.setValue(request);
		return store;
	}
	/**
	 * 解析参数
	 * 
	 * @param vol 
	 *            每页多少条记录 vol不大于0时不分页
	 * @param configs
	 *            参数
	 * @return
	 */
	protected ConfigStore parseConfig(HttpServletRequest request, int vol, String... configs) {
		ConfigStore store = new ConfigStoreImpl(configs);
		if(vol >0){
			PageNavi pageNavi = parsePageNavi(request);
			pageNavi.setPageRows(vol);
			store.setPageNavi(pageNavi);
		}
		store.setValue(request);
		return store;
	}

	/**
	 * 解析参数
	 * 
	 * @param fr
	 *            开始行(下标从1开始)
	 * @param to
	 *            结束行
	 * @param configs
	 *            参数
	 * @return
	 */
	protected ConfigStore parseConfig(HttpServletRequest request, int fr, int to, String... configs) {
		ConfigStore store = new ConfigStoreImpl(configs);
		PageNavi navi = new PageNaviImpl();
		navi.setCalType(1);
		navi.setFirstRow(fr);
		navi.setLastRow(to);
		store.setPageNavi(navi);
		store.setValue(request);
		return store;
	}
	protected ConfigStore parseConfig(HttpServletRequest request, String... conditions) {
		return parseConfig(request, false, conditions);
	}
	/**
	 * rquest.getParameter
	 * 
	 * @param key
	 * @param keyEncrypt
	 * @param valueEncrypt
	 * @return
	 */
	protected String getParam(HttpServletRequest request, String key, boolean keyEncrypt, boolean valueEncrypt) {
		return (String) WebUtil.getHttpRequestParam(request, key,keyEncrypt, valueEncrypt);
	}

	protected String getParam(HttpServletRequest request, String key, boolean valueEncrypt) {
		return getParam(request,key, false,valueEncrypt);
	}

	protected String getParam(HttpServletRequest request, String key) {
		return getParam(request, key, false, false);
	}
	protected List<Object> getParams(HttpServletRequest request, String key, boolean keyEncrypt, boolean valueEncrypt) {
		return WebUtil.getHttpRequestParams(request, key, keyEncrypt, valueEncrypt);
	}
	
	protected List<Object> getParams(HttpServletRequest request, String key, boolean valueEncrypt) {
		return getParams(request, key, false, valueEncrypt);
	}
	protected List<Object> getParams(HttpServletRequest request, String key) {
		return getParams(request,key, false, false);
	}

	/**
	 * 按PageNavi预订格式<br/>
	 * 从HttpServletRequest中提取分布数据
	 * 
	 * @param request
	 * @return
	 */
	private  PageNavi parsePageNavi(HttpServletRequest request) {
		if (null == request)
			return null;
		int pageNo = 1; // 当前页数 默认1
		int pageVol = ConfigTable.getInt("PAGE_DEFAULT_VOL"); // 每页多少条 默认10
		// 提取request中请求参数
		pageVol = BasicUtil.parseInt(request.getAttribute(PageNavi.PAGE_ROWS),pageVol);
		pageVol = BasicUtil.parseInt(request.getParameter(PageNavi.PAGE_ROWS),pageVol);
		pageNo = BasicUtil.parseInt(request.getParameter(PageNavi.PAGE_NO),pageNo);
		String uri = null;
		if (null == uri) {
			uri = request.getRequestURI();
		}
		PageNavi navi = (PageNavi) request.getAttribute("navi");
		if (null == navi) {
			navi = new PageNaviImpl(pageNo, pageVol, uri);
		} else {
			navi.setPageRows(pageVol);
			navi.setCurPage(pageNo);
			navi.setBaseLink(uri);
		}
		String flag = (String)request.getAttribute(Navi.CONFIG_FLAG_KEY);
		if(null == flag){
			flag = request.getParameter(Navi.CONFIG_FLAG_KEY);
		}
		if(null != flag){
			flag = flag.replace("'", "").replace("\"", "");
		}
		navi.setFlag(flag);
		boolean showStat = ConfigTable.getBoolean("NAVI_SHOW_STAT");
		showStat = BasicUtil.parseBoolean(request.getParameter(PageNavi.SHOW_STAT), showStat);
		navi.setShowStat(showStat);
		boolean showJump = ConfigTable.getBoolean("NAVI_SHOW_JUMP");
		showJump = BasicUtil.parseBoolean(request.getParameter(PageNavi.SHOW_JUMP), showJump);
		navi.setShowJump(showJump);
		request.setAttribute("navi", navi);
		
		return navi;
	}

	/******************************************************************************************************************
	 * 
	 * 数据验证
	 * 
	 *******************************************************************************************************************/
	/**
	 * 验证必须参数
	 * 
	 * @param encrypt
	 *            KEY是否加密
	 */
	protected boolean checkRequired(HttpServletRequest request, boolean keyEncrypt, boolean valueEncrypt, String... params) {
		params = BasicUtil.compressionSpace(params);
		boolean result = true;
		for (String param : params) {
			param = param.trim();
			if(BasicUtil.isEmpty(param)){
				continue;
			}

			ParseResult parser = ConfigParser.parse(param, true);
			if (BasicUtil.isEmpty(getParam(request, parser.getKey(), parser.isKeyEncrypt(), parser.isValueEncrypt()))) {
				setMessage(request,"请提供必须参数:" + parser.getId());
				result = false;
			}
		}
		return result;
	}
	protected boolean checkRequired(HttpServletRequest request, String... params) {
		return checkRequired(request, false, false, params);
	}

	/******************************************************************************************************************
	 * 
	 * 数据库操作
	 * 
	 *******************************************************************************************************************/

	/**
	 * 是否是ajax请求
	 * 
	 * @return
	 */
	protected boolean isAjaxRequest(HttpServletRequest request) {
		String header = request.getHeader("x-requested-with");
		if (header != null && "XMLHttpRequest".equals(header))
			return true;
		else
			return false;
	}
	/**
	 * request参数保存到分页中
	 * 
	 * @param single
	 *            是否取单个值(否则取数组)
	 * @param request
	 * @param keys
	 * @return
	 */
	private PageNavi saveParamToNavi(HttpServletRequest request, boolean single, boolean keyEncrypt, boolean valueEncrypt, String... keys) {
		PageNavi navi = (PageNavi) request.getAttribute("navi");
		if (null == navi) {
			navi = new PageNaviImpl();
			request.setAttribute("navi", navi);
		}
		if (null != keys) {
			for (String key : keys) {
				if (single) {
					navi.addParam(key, WebUtil.getHttpRequestParam(request, key, keyEncrypt, valueEncrypt));
				} else {
					navi.addParam(key, WebUtil.getHttpRequestParams(request, key, keyEncrypt, valueEncrypt));
				}
			}
		}
		return navi;
	}
//	/**
//	 * 当前操作客户端
//	 * 
//	 * @return
//	 */
//	protected ClientTrace currentClient(HttpServletRequest request) {
//		ClientTrace client = null;
//		client = (ClientTrace) request.getAttribute(Constant.REQUEST_ATTR_HTTP_CLIENT);
//		if (null == client) {
//			client = new ClientTrace(request);
//			//service.save(ConfigTable.getString("CLIENT_TRACE_TABLE"), client);
//		}
//		return client;
//	}
//	protected String currentClientCd(HttpServletRequest request) {
//		String result = null;
//		ClientTrace client = currentClient(request);
//		if (null != client) {
//			result = client.getCd();
//		}
//		return result;
//	}
	/********************************************************************************************************************************
	 * 
	 * 提示信息
	 * 
	 ********************************************************************************************************************************/
	/**
	 * 设置提示信息
	 * 
	 * @param key
	 * @param message
	 * @param type
	 *            消息类别
	 */
	protected void setRequestMessage(HttpServletRequest request, String key, Object value, String type) {
		DataSet messages = null;
		messages = (DataSet) request.getAttribute(Constant.REQUEST_ATTR_MESSAGE);
		if (null == messages) {
			messages = new DataSet();
			request.setAttribute(Constant.REQUEST_ATTR_MESSAGE, messages);
		}
		DataRow row = new DataRow();
		row.put(Constant.MESSAGE_KEY, key);
		row.put(Constant.MESSAGE_VALUE, value);
		row.put(Constant.MESSAGE_TYPE, type);
		messages.addRow(row);
	}
	
	protected void setRequestMessage(HttpServletRequest request, String key, Object value) {
		setRequestMessage(request,key, value, null);
	}

	protected void setRequestMessage(HttpServletRequest request, Object value) {
		setRequestMessage(request,BasicUtil.getRandomLowerString(10), value, null);
	}
	protected void setMessage(HttpServletRequest request, String key, Object value, String type) {
		setRequestMessage(request, key, value, type);
	}

	protected void setMessage(HttpServletRequest request, String key, Object value) {
		setMessage(request, key, value, null);
	}
	
	protected void setMessage(HttpServletRequest request, Object value) {
		setMessage(request, BasicUtil.getRandomLowerString(10), value);
	}


	protected void setSessionMessage(HttpSession session, String key, Object value, String type) {
		DataSet messages = null;
		messages = (DataSet) session.getAttribute(Constant.SESSION_ATTR_MESSAGE);
		if (null == messages) {
			messages = new DataSet();
			session.setAttribute(Constant.SESSION_ATTR_MESSAGE, messages);
		}
		DataRow row = new DataRow();
		row.put(Constant.MESSAGE_KEY, key);
		row.put(Constant.MESSAGE_VALUE, value);
		row.put(Constant.MESSAGE_TYPE, type);
		messages.addRow(row);
	}


	protected void setSessionMessage(HttpSession session, String key, Object value) {
		setSessionMessage(session, key, value, null);
	}

	protected void setSessionMessage(HttpSession session, Object value) {
		setSessionMessage(session, BasicUtil.getRandomLowerString(10), value, null);
	}

	/**
	 * reffer
	 * 
	 * @return
	 */
	protected boolean hasReffer(HttpServletRequest request) {
		String reffer = request.getHeader("Referer");
		if (null == reffer || "".equals(reffer.trim())) {
			return false;
		}
		return true;
	}

	/**
	 * 是否是蜘蛛
	 * 
	 * @return
	 */
	protected boolean isSpider(HttpServletRequest request) {
		return !hasReffer(request);
	}


	/**
	 * 是否是移动��端
	 * 
	 * @return
	 */
	protected boolean isWap(HttpServletRequest request) {
		return WebUtil.isWap(request);
	}

	/******************************************************************************************************************
	 * 
	 * 实现接口
	 * 
	 *******************************************************************************************************************/
	

	public String getDir() {
		String dir = null;
		try{
			Field field = this.getClass().getField("dir");//子类dir必须public
			dir = (String)field.get(this);
		}catch(Exception e){
			e.printStackTrace();
		}
		return dir;
	}
	public String getUploadTable(HttpServletRequest request, String cf){
		if(null == cf){			
			cf = getParam(request,"cf");
		}
		if(null == cf){
			cf = (String)request.getSession().getAttribute("upload_config");
		}
		String table = ConfigTable.getString("UPLOAD_TABLE_"+cf);
		if(null == table){
			table = ConfigTable.getString("UPLOAD_TABLE");
		}
		if(null == table){
			table = "UPLOAD_FILE";
		}
		return table;
	}
	public String getUploadDir(HttpServletRequest request,String cf){
		if(null == cf){			
			cf = getParam(request,"cf");
		}
		if(null == cf){
			cf = (String)request.getSession().getAttribute("upload_config");
		}
		if(null == cf){
			cf = "common";
		}
		return cf;
	}
	/**
	 * 
	 * @param request
	 * @param response
	 * @param data
	 * @param navi	分页
	 * @param page	数据模板以WEB-INF为相对目录根目录
	 * @param ext	扩展数据
	 * @return
	 */
	public Map<String,Object> navi(HttpServletRequest request, HttpServletResponse response, Object data, PageNavi navi, String page, Object ext){
		if(null == data){
			data = (DataSet)request.getAttribute("_anyline_navi_data");
		}else{
			request.setAttribute("_anyline_navi_data", data);
		}
		String html = "";
		try{
			html = WebUtil.parseJsp(request, response, page);
		}catch(Exception e){
			e.printStackTrace();
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("BODY", BasicUtil.escape(html));
		if(null != navi){
			if(ConfigTable.isDebug()){
				log.warn("[LOAD JSP NAVI][ROWS:"+navi.getTotalRow()+"][PAGE:"+navi.getTotalPage()+"]");
			}
			int type = 0;
			String _type = request.getParameter("_anyline_navi_type");
			if("1".equals(_type)){
				type = 1;
			}
			navi.setType(type);
			map.put("NAVI", BasicUtil.escape(navi.ajaxPage()));
			map.put("TOTAL_ROW", navi.getTotalRow()+"");
			map.put("TOTAL_PAGE", navi.getTotalPage()+"");
			map.put("CUR_PAGE", navi.getCurPage()+"");
			map.put("FIRST_ROW", navi.getFirstRow()+"");
			map.put("LAST_ROW", navi.getLastRow()+"");
			map.put("PAGE_ROWS", navi.getPageRows()+"");
		}
		map.put("EXT", ext);
		return map;
	}

	public Map<String,Object> navi(HttpServletRequest request, HttpServletResponse response, Object data, PageNavi navi, String page){
		return navi(request, response, data, navi, page, null);
	}
}