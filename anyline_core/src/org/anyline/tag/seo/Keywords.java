/* 
 * Copyright 2006-2015 the original author or authors.
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

package org.anyline.tag.seo;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;

import org.apache.log4j.Logger;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SeoUtil;
import org.anyline.util.WebUtil;

/**
 * 随机插入关键词
 * 按星期(一年中的第几个星期)计算插入位置
 * @author Administrator
 *
 */
public class Keywords extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	private Logger log = Logger.getLogger(this.getClass());
	//<seo:keyword key="java" count="3"/>
	//<ic:param key ="" count="3"/>

	
	public int doAfterBody() throws JspException {
		return super.doAfterBody();
	}
	public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
	 public int doEndTag() throws JspException {
		JspWriter out = null;
		try{
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			boolean insert = false;	//是否插入关键词
			insert = ConfigTable.getBoolean("SEO_INSERT_KEYWORDS",insert);
			insert = ConfigTable.getBoolean("SEO_INSERT_KEYWORDS_"+request.getServerName(),insert);
			
			if(insert && WebUtil.isSpider(request)){
				List<String> keys = new ArrayList<String>();
				if(null != paramList){
					for(Object item:paramList){
						if(null != item){
							keys.add(item.toString().trim());
						}
					}
				}
				body = SeoUtil.insertKeyword(body, keys);
			}
			out = pageContext.getOut();
			out.print(body);
		}catch(Exception e){
			log.error(e);
		}finally{
			release();
		}
        return EVAL_PAGE;   
	}
	@Override
	public void release() {
		super.release();
		body = null;
	}

	public BodyContent getBodyContent() {
		return super.getBodyContent();
	}

	public void setBodyContent(BodyContent b) {
		super.setBodyContent(b);
	}

}