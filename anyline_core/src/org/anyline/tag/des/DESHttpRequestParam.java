

package org.anyline.tag.des;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
/**
 * http request 请求参数名=参数名加密
 * 逐个加密
 * @author Administrator
 *
 */
public class DESHttpRequestParam extends BaseBodyTag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private String value;		//被加密数据

	public int doEndTag() throws JspException {
		try{
			value = BasicUtil.nvl(value,body,"").toString().trim();
			if(null != value && !"".equals(value)){
				String result = "";
				String url = "";
				String split = "";
				if(value.contains("?")){
					url = value.substring(0, value.indexOf("?"));
					value = value.substring(value.indexOf("?")+1);
					split = "?";
				}
				if(value.startsWith("&")){
					split = "&";
				}
				String[] params = value.split("&");
				int size = params.length;
				for(int i=0; i<size; i++){
					String param = params[i];
					String[] keys = param.split("=");
					if(keys.length == 2){
						result += WebUtil.encryptHttpRequestParamKey(keys[0]) + "=" + WebUtil.encryptHttpRequestParamValue(keys[1]);
						if(i<size-1){
							result += "&";
						}
					}
				}
				result = url + split + result;
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
