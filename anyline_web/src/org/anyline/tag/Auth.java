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


package org.anyline.tag;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspWriter;

import org.anyline.qq.mp.util.QQMPConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
import org.anyline.weixin.mp.util.WXMPConfig;
import org.apache.log4j.Logger;

public class Auth extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private String wx_host  = "https://open.weixin.qq.com/connect/oauth2/authorize";
	private String qq_host  = "https://graph.qq.com/oauth2.0/authorize";
	
	private String appid;
	private String type;	//wx:微信 qq:QQ
	private String redirect;
	private String state;
	private String scope;
	private String params;
	
	private static Logger log = Logger.getLogger(Auth.class);
	public int doEndTag() {
		JspWriter writer = null;
		String result = "";
		try {
			writer = pageContext.getOut();
			if("wx".equalsIgnoreCase(type) || "weixin".equalsIgnoreCase(type)){
				if(BasicUtil.isEmpty(appid)){
					appid = WXMPConfig.getInstance().APP_ID;
				}
				Map<String,String> map = new HashMap<String,String>();
				if(null != params){
					String[] items = params.split(",");
					for(String item:items){
						String[] kv = item.split(":");
						if(kv.length ==2){
							map.put(kv[0], kv[1]);
						}
					}
				}
				if(BasicUtil.isEmpty(scope)){
					scope = "snsapi_base";
				}
				if(BasicUtil.isEmpty(redirect)){
					redirect = WXMPConfig.getInstance().OAUTH_REDIRECT_URL;
				}
				redirect = URLEncoder.encode(redirect, "UTF-8");
				String url =  wx_host + "?appid="+appid+"&redirect_uri="+redirect+"&response_type=code&scope="+scope+"&state="+state+"#wechat_redirect";
				
				result = "<a href=\""+url+"\">" + body + "</a>";
			}else if("qq".equalsIgnoreCase(type)){
				if(BasicUtil.isEmpty(appid)){
					appid = QQMPConfig.getInstance().APP_ID;
				}
				Map<String,String> map = new HashMap<String,String>();
				if(null != params){
					String[] items = params.split(",");
					for(String item:items){
						String[] kv = item.split(":");
						if(kv.length ==2){
							map.put(kv[0], kv[1]);
						}
					}
				}
				String response_type = "code";
				if(BasicUtil.isEmpty(scope)){
					scope = "get_user_info";
				}
				if(BasicUtil.isEmpty(redirect)){
					redirect = QQMPConfig.getInstance().OAUTH_REDIRECT_URL;
				}
				redirect = URLEncoder.encode(redirect, "UTF-8");
				String url =  qq_host + "?client_id="+appid+"&response_type="+response_type+"&redirect_uri="+redirect+"&scope="+scope+"&state="+WebUtil.encrypt(state);
				
				result = "<a href=\""+url+"\">" + body + "</a>";
				
			}
			writer.print(result);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			release();
		}
		return EVAL_PAGE;// 标签执行完毕之后继续执行下面的内容
	}

	@Override
	public void release() {
		super.release();
		type = null;
		appid = null;
		redirect = null;
		params = null;
		body = null;
		scope = null;
		state = null;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
}
