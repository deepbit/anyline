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
package org.anyline.struts.action;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.config.http.ConfigStore;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DESUtil;
import org.anyline.util.WebUtil;
import org.anyline.util.regular.RegularUtil;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.support.WebApplicationContextUtils;
@ParentPackage("anyline-default")
@Namespace("/al/tmp")
@Scope("prototype")
public class TemplateAction extends AnylineAction {
	@Action(value = "load", results = { @Result(type="json")})
	public String load(){
		String path = getParam("path", false, true);
		String html = "";
		try{
			html = WebUtil.parseJsp(request, response, path);
		}catch(Exception e){
			
		}
		html = BasicUtil.escape(html);
		return success(html);
	}

	/**
	 * @return
	 */
	@Action(value = "load_template_style", results = { @Result(type="json") })
	public String loadDataTemplateStyle(){
		String template_path = request.getParameter("template");
		if(null == template_path || template_path.isEmpty()){
			template_path = "default.jsp";
		}else{
			try{
				template_path = DESUtil.getInstance().decrypt(template_path);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if(!template_path.startsWith("/")){
			template_path = "/"+template_path;
		}
		String content = parseTemplate(request, response,template_path);
		try{
			List<List<String>> vars= RegularUtil.fetch(content, "{([\\w.]+)}");//RegularUtil.REGEX_VARIABLE
			for(List<String> var:vars){
				String fullVar = var.get(0);
				String simVar = var.get(1).toUpperCase().trim();
				if("ROW_NUMBER".equals(simVar)){
					continue;
				}
				if(simVar.contains(".")){
					String pre = simVar.substring(0,simVar.lastIndexOf("."));
					String suf = simVar.substring(simVar.lastIndexOf(".")+1);
					simVar = pre + "." + WebUtil.encryptKey(suf).toUpperCase(); 
				}else{
					simVar = WebUtil.encryptKey(simVar).toUpperCase();
				}
				content = content.replace(fullVar, "{"+simVar+"}");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return success(content);
	}
	
	private String parseTemplate(HttpServletRequest request, HttpServletResponse response, String path){
		String result = "";
		 //构造填充数据的Map 
		if(null == path){
			return result;
		}
		Map map = new HashMap();
		int idx = path.indexOf(":");
		if(idx > 0){
			String cm = path.substring(idx+1);
			path = path.substring(0, idx);
			idx = cm.lastIndexOf(".");
			String clazz = cm.substring(0,idx);
			String method = cm.substring(idx+1);
			String arg = method.substring(method.indexOf("(")+1, method.indexOf(")"));
			method = method.substring(0, method.indexOf("("));
			try{
				Class c = Class.forName(clazz);
				ApplicationContext ac1 = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
				
				Object instance = ac1.getBean(c);
				Method m = c.getMethod(method, String.class);
				if(null != m){
					map = (Map)m.invoke(instance, arg);
					if(null != map){
						Set keys = map.keySet();
						for(Object key:keys){
							request.setAttribute(key.toString(), map.get(key));
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
        try{
        	File dir = new File(ConfigTable.getWebRoot());
        	File file = new File(dir, path);
        	if(!file.exists()){
        		//文件不存在
        		return result;
        	}
        	result = WebUtil.parseJsp(request, response, path);
        }catch(Exception e){
        	e.printStackTrace();
        }
		return result;
	}
}
