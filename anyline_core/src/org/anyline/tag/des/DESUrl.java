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
 */


package org.anyline.tag.des;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
/**
 * 整体加密url
 * @author Administrator
 *
 */
public class DESUrl extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(DESUrl.class);
	private String value;		//被加密数据

	public int doEndTag() throws JspException {
		try{
			value = BasicUtil.nvl(value,body,"").toString().trim();
			if(null != value && !"".equals(value)){
				String result = "";
				String url = value;
				String split = "";
				String param = "";
				if(value.contains("?")){
					url = value.substring(0, value.indexOf("?"));
					param = value.substring(value.indexOf("?")+1);
					split = "?";
				}
				result = url + split + WebUtil.encryptRequestParam(param);
				JspWriter out = pageContext.getOut();
				out.print(result);
			}
		}catch(Exception e){
			LOG.error(e);
		}finally{
			release();
		}
		return EVAL_PAGE;   
	}
	@Override
	public void release() {
		super.release();
		value = null;
	}
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
