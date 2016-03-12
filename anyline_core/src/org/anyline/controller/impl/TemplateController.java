package org.anyline.controller.impl;
import org.anyline.plugin.springmvc.TemplateModelAndView;
import org.anyline.plugin.springmvc.TemplateView;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DESUtil;
import org.anyline.util.WebUtil;
import org.springframework.web.servlet.ModelAndView;
 
 

public class TemplateController extends AnylineController {
	private String dir = "default";
	/**
	 * 创建显示视图(page目录下)
	 * @param name
	 * @param template
	 * @return
	 */
	protected ModelAndView template(String name, String template){
		return createView(name, template);
	}
	protected TemplateModelAndView createView(String name, String template){
		TemplateModelAndView tv = new TemplateModelAndView();
		
		String base = this.getClass().getPackage().getName().replace(ConfigTable.getString("BASE_PACKAGE")+".", "").replace("controller", "").replace(".", "/");
		String dir = (String)BeanUtil.getFieldValue(this, "dir");
		if(!name.startsWith("/")){
			if(BasicUtil.isNotEmpty(dir)){
				if(dir.endsWith("/")){
					name = dir+name;
				}else{
					name = dir+"/"+name;
				}
			}
		}
		if(!name.startsWith("/")){
			if(!name.startsWith("page"))
			name = base + "page/" + name;
		}
		String content_template = base + "template/layout/" + template + ".jsp";
		tv.setViewName(name);
		tv.addObject(TemplateView.TEMPLATE_NAME, content_template);
		tv.addObject(TemplateModelAndView.CONTENT_URL,getRequest().getRequestURI());
		String data_template =name.substring(0,name.lastIndexOf("/")+1).replace("/page/", "/template/data/");
		try{
			tv.addObject(TemplateView.DATA_TEMPLATE_DES, DESUtil.getInstance().encrypt(data_template));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String clazz = this.getClass().getName();
		tv.setFromClass(clazz);
		return tv;
	}

	protected TemplateModelAndView createView(String name){
		return createView(name,TemplateView.TEMPLATE_NAME_DEFAULT);
	}
	protected TemplateModelAndView template(String name){
		return createView(name);
	}
	
	/**
	 * 加载数据 数据模板中的数据
	 * @param objects
	 * @return
	 */
	protected String createTemplateData(Object obj, String ... keys){
		BasicUtil.toUpperCaseKey(obj, keys);
		WebUtil.encryptKey(obj, keys);
		return success(obj);
	}
	protected String loadData(Object obj, String ...keys){
		return createTemplateData(obj, keys);
	}
	
	protected ModelAndView error(String ... msgs ){
		return errorView(msgs);
	}
	protected ModelAndView errorView(String ... msgs){
		String message ="";
		String bak_url = getRequest().getHeader("Referer");
		if(null != msgs){
			for(String msg:msgs){
				message += "<br/>"+ msg;
			}
		}
		
		ModelAndView view = new ModelAndView(ConfigTable.getString("ERROR_PAGE_PATH"));
		view.addObject("msg", message);
		view.addObject("bak_url",bak_url);
		return view;
	}
	protected ModelAndView emptyView(){
		ModelAndView view = new ModelAndView(ConfigTable.getString("EMPTY_PAGE_PATH"));
		return view;
	}
}