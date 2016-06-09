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


package org.anyline.util;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class ConfigTable {
	private static Logger LOG = Logger.getLogger(ConfigTable.class);
	private static String webRoot;
	private static Hashtable<String,String> configs;
	private static long lastLoadTime = 0;	//最后一次加载时间
	private static int reload = 0;			//重新加载间隔
	private static boolean debug = false;
	private static boolean sqlDebug = false;
	private static final String version = "6.X";
	private static final String minVersion = "0709";
	static{
		init();
		debug();
	}
	private static void line(String src, String chr, boolean center){
		int len = 80;
		int fill = 0 ;
		String line = "";
		if(center){
			fill = (len - src.length() -2)/2;
			line = "*"+BasicUtil.fillChar("", chr, fill) + src +BasicUtil.fillChar("", chr, fill) +"*";
		}else{
			fill = len - src.length() - 2;
			line = "*" + src + BasicUtil.fillChar("", chr, fill)+"*";
		}
		System.out.println(line);
	}
	
	private static void debug(){
		if(!isDebug()){
			return;
		}
		String path =ConfigTable.class.getResource("").getPath();
		//path = path.substring(path.indexOf("/"),path.indexOf("!"));
		String time = new File(path).lastModified()+"";
		line("","*", true);
		line("Anyline Core " + version, " ", true);
		line(" www.anyline.org", " ", true);
		line(""," ", true);
		line("MinVersion " + minVersion + "[" + time+"]", " ", true);
		line(""," ", true);
		line("","*", true);
		line(" git:https://git.oschina.net/anyline/anyline.git", " ", false);
		line(" svn:svn://git.oschina.net/anyline/anyline", " ", false);
		line("","*", true);
		line(" Debug 环境下输出以上版本信息 QQ群技术支持86020680[提供MinVersion]                         ", "", false);
		line(" debug状态设置:anyline-config.xml:<property key=\"DEBUG\">false</property>         ", "", false);
		line("","*", true);
	}

	private ConfigTable() {}
	
	
	public static String getWebRoot() {
		return webRoot;
	}

	public static void init() {
		String path =  "";
		try{
			path = ConfigTable.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		}catch(Exception e){
			LOG.error(e);
		}
		Properties props=System.getProperties(); //获得系统属性集    
		String osName = props.getProperty("os.name"); //操作系统名称    
		if(null != osName && osName.toUpperCase().contains("WINDOWS") && path.startsWith("/")){
			path = path.substring(1);
		}
		webRoot = path.substring(0,path.indexOf("WEB-INF")-1);
		
		//加载配置文件
		loadConfig();
	}
	/**
	 * 加载配置文件
	 */
	@SuppressWarnings("unchecked")
	private static void loadConfig() {
		try {
			if(null == configs){
				configs = new Hashtable<String,String>();
			}
			configs.put("HOME_DIR", webRoot);
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(webRoot , "/WEB-INF/classes/anyline-config.xml"));
			Element root = document.getRootElement();
			for(Iterator<Element> itrProperty=root.elementIterator("property"); itrProperty.hasNext();){
				Element propertyElement = itrProperty.next();
				String key = propertyElement.attributeValue("key");
				String value = propertyElement.getTextTrim();
				configs.put(key, value);
			}
		} catch (Exception e) {
			LOG.error("配置文件解析异常:"+e);
		}
		lastLoadTime = System.currentTimeMillis();
		reload = getInt("RELOAD");
		debug = getBoolean("DEBUG");
		sqlDebug = getBoolean("SQL_DEBUG");
	}
	public static String get(String key){
		String val = null;
		if(reload > 0 && (System.currentTimeMillis() - lastLoadTime)/1000 > reload){
			//重新加载
			init();
		}
		val = configs.get(key);
		return val;
	}
	public static String getString(String key) {
		return get(key);
	}
	public static String getString(String key, String def){
		String val = getString(key);
		if(BasicUtil.isEmpty(val)){
			val = def;
		}
		return val;
	}
	public static boolean getBoolean(String key){
		return getBoolean(key,false);
	}
	public static boolean getBoolean(String key, boolean def){
		return BasicUtil.parseBoolean(get(key), def);
	}
	public static int getInt(String key) {
		return BasicUtil.parseInt(get(key),0);
	}
	public static int getInt(String key, int def){
		return BasicUtil.parseInt(get(key), def);
	}
	public static String getVersion(){
		return version;
	}
	public static String getMinVersion(){
		return minVersion;
	}

	public static int getReload() {
		return reload;
	}

	public static boolean isDebug() {
		return debug;
	}
	public static boolean isSQLDebug() {
		return sqlDebug;
	}
}
