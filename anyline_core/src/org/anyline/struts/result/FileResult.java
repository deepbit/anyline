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

package org.anyline.struts.result;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.entity.DataRow;
import org.apache.log4j.Logger;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.ActionInvocation;

public class FileResult extends StrutsResultSupport {
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(FileResult.class);
	private Object data = null;

	protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
		HttpServletResponse response = (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE);
		HttpServletRequest request = (HttpServletRequest) invocation.getInvocationContext().get(HTTP_REQUEST);
		ServletContext sc = (ServletContext) invocation.getInvocationContext().get(SERVLET_CONTEXT);

		FileInputStream in = null;
		OutputStream out = null;
		try {

			File file = null;
			String title = null;
			data = invocation.getStack().findValue("data");
			if (data instanceof File) {
				file = (File) data;
				title = file.getName();
			} else if (data instanceof DataRow) {
				DataRow row = (DataRow) data;
				file = new File(row.getString("PATH"));
				title = row.getString("NM");
			}
			if (null != file && file.exists()) {
				response.setCharacterEncoding("UTF-8");
				response.setHeader("Location", title);
				response.setHeader("Content-Disposition", "attachment; filename=" + title);
				String mimeType = sc.getMimeType(file.getAbsolutePath());
				response.setContentType(mimeType);
				in = new FileInputStream(file);
				out = response.getOutputStream();
				byte[] buf = new byte[1024];
				int count = 0;
				LOG.info("在正传输文件:" + file.getAbsolutePath() + ",请求来自" + request.getRequestURL() + "?" + request.getQueryString());
				while ((count = in.read(buf)) >= 0) {
					out.write(buf, 0, count);
				}
				LOG.info("传输完成:" + file.getAbsolutePath() + ",请求来自" + request.getRequestURL() + "?" + request.getQueryString());
			}
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			if (null != in) {
				in.close();
			}
			if (null != out) {
				out.close();
			}
		}
	}

}
