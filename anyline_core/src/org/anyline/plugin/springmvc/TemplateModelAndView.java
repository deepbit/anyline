package org.anyline.plugin.springmvc;

import org.anyline.util.DESUtil;
import org.springframework.web.servlet.ModelAndView;

public class TemplateModelAndView extends ModelAndView{
	public static final String DATA_URL 		= "anyline_template_data_url";			//加载数据URL 
	public static final String DATA_TEMPLATE 	= "anyline_template_data_template"; 	//加载数据模板文件URL
	public static final String DATA_PARSER 		= "anyline_template_data_parser";  		//模板文件解析配置
	public static final String CONTENT_URL 		= "anyline_template_content_url"; 		//内容页面加载URL
	public static final String PAGE_TITLE		= "anyline_template_content_title";		//页面标题
	
	private String fromClass = "";	//创建视图的类
	
	public TemplateModelAndView setTitle(String title){
		addObject(PAGE_TITLE, title);
		return this;
	}
	public TemplateModelAndView setDataUrl(String url){
		if(null != url && !url.startsWith("/")){
			String base = (String)getModel().get(CONTENT_URL);
			base = base.substring(0,base.lastIndexOf("/"));
			if(null != base){
				if(base.endsWith("/")){
					url = base + url;
				}else{
					url = base + "/" + url;
				}
			}
		}
		this.addObject(DATA_URL, url);
		return this;
	}
	/**
	 * 数据模板目录
	 * 文件名:方法名(String)
	 * 或文件名:类名.方法名(String)
	 * @param template
	 * @return
	 */
	public TemplateModelAndView setDataTemplate(String template){
		
		try{
			String data_template= createFullTemplatePath(template);
			addObject(TemplateView.DATA_TEMPLATE_DES, DESUtil.getInstance().encrypt(data_template));
		}catch(Exception e){
			e.printStackTrace();
		}
		return this;
	}
	/**
	 * 构造完整path
	 * @param path
	 * @return
	 */
	public String createFullTemplatePath(String path){
		String viewName = this.getViewName();
		String data_template = viewName.substring(0,viewName.lastIndexOf("/")+1).replace("/page/", "/template/data/")+path;
		int idx = data_template.indexOf(":");
		if(idx > 0){
			String method = data_template.substring(idx+1);
			if(method.indexOf(".") == -1){
				data_template = data_template.replace(method, fromClass+"."+ method);
			}
		}
		return data_template;
	}
	public TemplateModelAndView setDataParser(String ... parser){
		String str = "{";
		int size = parser.length;
		for(int i=0; i<size; i++){
			String p =parser[i];
			str += "'"+p.replace(":", "':'")+"'";
			if(i<size-1){
				str += ",";
			}
		}
		str += "}";
		this.addObject(DATA_PARSER, str);
		return this;
	}
	public String getFromClass() {
		return fromClass;
	}
	public void setFromClass(String fromClass) {
		this.fromClass = fromClass;
	}
	
	
}
