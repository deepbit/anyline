package org.anyline.easemob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.HttpClientUtil;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;

public class EasemobUtil {
	private static Logger log = Logger.getLogger(EasemobUtil.class);
//	private static final String orgName = EasemobConfig.ORG_NAME;
//	private static final String appName = EasemobConfig.APP_NAME;
//	private static final String clientId= EasemobConfig.CLIENT_ID;
//	private static final String clientSecret = EasemobConfig.CLIENT_SECRET;
//	private static final String host = EasemobConfig.HOST;
	/*
	<property key="HOST">https://a1.easemob.com</property>
	<property key="APP_KEY">1118161112115170#aisousuo</property>
	<property key="ORG_NAME">1118161112115170</property>
	<property key="APP_NAME">aisousuo</property>
	<property key="CLIENT_ID">YXA6x6A9oKpyEea9rcNB35LujQ</property>
	<property key="CLISENT_SECRET">YXA6vW-waLkDUv3nSCilUFKxP2jl-wE</property>*/
	private static final String orgName = "1118161112115170";
	private static final String appName = "aisousuo";
	private static final String clientId= "YXA6x6A9oKpyEea9rcNB35LujQ";
	private static final String clientSecret = "YXA6vW-waLkDUv3nSCilUFKxP2jl-wE";
	private static final String host = "https://a1.easemob.com";
	private static final String baseUrl = host + "/"+orgName+"/"+appName;
	private static String access_token = null;
	private static long access_token_expires = 0;
	

	
	/**
	 * 注册用户
	 * @param user
	 * @param password
	 * @param nick 昵称
	 * @return
	 */
	public static DataRow reg(String user, String password, String nickname){
		DataRow result = null;
		String url = baseUrl + "/users";
		Map<String,String> map = new HashMap<String,String>();
		map.put("username", user);
		map.put("password", password);
		map.put("nickname", nickname);
		Map<String,String> headers = defaultHeader();
		headers.put("Content-Type", "application/json");
		try {
			HttpEntity entity = new StringEntity(BeanUtil.map2json(map));
			String txt = HttpClientUtil.post(defaultHeader(), url, "UTF-8", entity).getText();
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.containsKey("entities")){
				DataSet set = row.getSet("entities");
				if(set.size() > 0){
					result = set.getRow(0);
				}
			}
			if(ConfigTable.isDebug()){
				log.warn("[REG USER][RESULT:"+txt+"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public static DataRow reg(String user, String password){
		return reg(user,password, user);
	}
	/**
	 * 批量注册
	 * @param list
	 * @return
	 */
	public static DataSet regs(List<Map<String,String>> list){
		DataSet result = new DataSet();
		StringBuilder json = new StringBuilder();
		json.append("[");
		if(null != list){
			int size = list.size();
			for(int i=0; i<size; i++){
				Map<String,String> map = list.get(i);
				if(i > 0){
					json.append(",");
				}
				String nickname = map.get("nickname");
				json.append("\"username\":\"").append(map.get("username"))
				.append("\",\"password\":\"").append(map.get("password")).append("\"");
				if(BasicUtil.isNotEmpty(nickname)){
					json.append("\"nickname\":\"").append(nickname).append("\"");
				}
				json.append("}");
			}
		}
		json.append("]");
		String url = baseUrl + "/users";
		Map<String,String> headers = defaultHeader();
		headers.put("Content-Type", "application/json");
		try{
			String txt = HttpClientUtil.post(headers, url,"UTF-8", new StringEntity(json.toString())).getText();
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.containsKey("entities")){
				result = row.getSet("entities");
			}
			if(ConfigTable.isDebug()){
				log.warn("[REG USERS][RESULT:"+txt+"]");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 修改密码
	 * @param user
	 * @param password
	 * @return
	 */
	public static boolean resetPassword(String user, String password){
		boolean result = false;
		String url = baseUrl + "/users/"+user+"/password";
		Map<String,String> map = new HashMap<String,String>();
		map.put("newpassword", password);
		try {
			String txt = HttpClientUtil.put(defaultHeader(), url,"UTF-8", new StringEntity(BeanUtil.map2json(map))).getText();
			if(ConfigTable.isDebug()){
				log.warn("[RESET PASSWOROD][RESULT:"+txt+"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		result = true;
		return result;
	}
	/**
	 * 修改昵称
	 * @param user
	 * @param nickname
	 * @return
	 */
	public static DataRow resetNickname(String user, String nickname){
		DataRow result = null;
		String url = baseUrl + "/users/"+user;
		Map<String,String> map = new HashMap<String,String>();
		map.put("nickname", nickname);
		try {
			String txt = HttpClientUtil.put(defaultHeader(), url,"UTF-8", new StringEntity(BeanUtil.map2json(map))).getText();
			result = parseUser(txt);
			if(ConfigTable.isDebug()){
				log.warn("[RESET NICKNAME][RESULT:"+txt+"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 删除用户
	 * @param user
	 * @return
	 */
	public static boolean delete(String user){
		boolean result = false;
		String url = baseUrl + "/users/" + user;
		try {
			String txt = HttpClientUtil.delete(defaultHeader(),url, "UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[DELETE USER][RESULT:"+ txt +"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		result = true;
		return result;
	}
	/**
	 * 用户信息
	 * @param user
	 * @return
	 */
	public static DataRow getUser(String user){
		DataRow result = new DataRow();
		String url = baseUrl +  "/users/" + user;
		try{
			String txt = HttpClientUtil.get(defaultHeader(),url, "UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[GET USER][RESULT:"+ txt +"]");
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.containsKey("entities")){
				DataSet set = row.getSet("entities");
				if(set.size() > 0){
					result = set.getRow(0);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 批量用户信息
	 * @param limit
	 * @param cursor 分页游标
	 * @return
	 */
	public static DataSet getUsers(int limit, String cursor){
		DataSet set = new DataSet();
		String url = baseUrl +  "/users/";
		Map<String,String> params = new HashMap<String,String>();
		params.put("limit", limit+"");
		if(BasicUtil.isNotEmpty(cursor)){
			params.put("cursor", cursor);
		}
		try{
			String txt = HttpClientUtil.get(defaultHeader(),url, "UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[GET USER LIST][RESULT:"+txt+"]");
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.containsKey("entities")){
				set = row.getSet("entities");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return set;
	}
	public static DataSet getUsers(int limit){
		return getUsers(limit, null);
	}
	/**
	 * 添加好友
	 * @param user
	 * @param friend
	 * @return
	 */
	public static DataRow addFriend(String user, String friend){
		DataRow result = null;
		String url = baseUrl + "/users/" + user + "/contacts/users/" + friend;
		try {
			String txt = HttpClientUtil.post(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[ADD FRIEND][RESULT:\n"+txt+"]");
			}
			result = parseUser(txt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 好友列表(只查username)
	 * @param user
	 * @return
	 */
	public static DataSet getFriends(String user){
		DataSet result = new DataSet();
		String url = baseUrl + "/users/" + user + "/contacts/users";
		try {
			String txt = HttpClientUtil.get(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[GET FRIEND LIST][RESULT:"+txt+"]");
			}
			DataRow json = DataRow.parseJson(txt);
			if(null != json && json.has("data")){
				List<?> datas = json.getList("data");
				for(Object data:datas){
					DataRow row = new DataRow();
					row.put("username", data);
					if(ConfigTable.isDebug()){
						log.warn("[GET FRIEND][FRIEND USERNAME:"+data+"]");
					}
					result.add(row);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 删除好友
	 * @param user
	 * @param friend
	 * @return 返回被删除的好友数据
	 */
	public static DataRow deleteFriend(String user, String friend){
		DataRow result = null;
		String url = baseUrl + "/users/" + user + "/contacts/users/" + friend;
		try {
			String txt = HttpClientUtil.delete(defaultHeader(), url,"UTF-8").getText();
			result = parseUser(txt);
			if(ConfigTable.isDebug()){
				log.warn("[DELETE FRIEND][RESULT:"+txt+"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 添加黑名单
	 * @param user
	 * @param block
	 * @return
	 */
	public static DataRow addBlock(String user, String block){
		DataRow result = null;
		//删除好友
		deleteFriend(user, block);
		String url = baseUrl + "/users/" + user + "/blocks/users";
		try {
			String params = "{\"usernames\":[\""+block+"\"]} ";
			String txt = HttpClientUtil.post(defaultHeader(), url,"UTF-8", new StringEntity(params)).getText();
			if(ConfigTable.isDebug()){
				log.warn("[ADD BLOCKS][RESULT:\n"+txt+"]");
			}
			//封装添加成功的用户username
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 好友列表(只查username)
	 * @param user
	 * @return
	 */
	public static DataSet getBlocks(String block){
		DataSet result = new DataSet();
		String url = baseUrl + "/users/" + block + "/blocks/users";
		try {
			String txt = HttpClientUtil.get(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[GET BLOCK LIST][RESULT:"+txt+"]");
			}
			DataRow json = DataRow.parseJson(txt);
			if(null != json && json.has("data")){
				List<?> datas = json.getList("data");
				for(Object data:datas){
					DataRow row = new DataRow();
					row.put("username", data);
					if(ConfigTable.isDebug()){
						log.warn("[GET BLOCK][BLOCK USERNAME:"+data+"]");
					}
					result.add(row);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 删除黑名单 
	 * @param user
	 * @param friend
	 * @return 返回被删除的黑名单数据
	 */
	public static DataRow deleteBlock(String user, String block){
		DataRow result = null;
		String url = baseUrl + "/users/" + user + "/blocks/users/" + block;
		try {
			String txt = HttpClientUtil.delete(defaultHeader(), url,"UTF-8").getText();
			result = parseUser(txt);
			if(ConfigTable.isDebug()){
				log.warn("[DELETE BLOCK][RESULT:"+txt+"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 在线状态  1:在线 0:离线
	 * @param user
	 * @return
	 */
	public static String status(String user){
		String result = "0";
		String url = baseUrl + "/users/" + user + "/status";
		try {
			String txt = HttpClientUtil.get(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[GET USER STATUS][RESULT:"+txt+"]");
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.has("data")){
				row = row.getRow("data");
				String status = row.getString(user);
				log.warn("[GET USER STATUS][STATUS:"+status+"]");
				if("online".equals(status)){
					result = "1";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 离线消息数量
	 * @param user
	 * @return
	 */
	public static int offlineMsgCount(String user){
		int result = 0;
		String url = baseUrl + "/users/" + user + "/offline_msg_count";
		try {
			String txt = HttpClientUtil.get(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[GET USER STATUS][RESULT:"+txt+"]");
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.has("data")){
				row = row.getRow("data");
				result = row.getInt(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 离线消息状态
	 * @param user
	 * @return
	 * deliverd:表示此用户的该条离线消息已经收到过了 undelivered:表示此用户的该条离线消息还未收到
	 */
	public static String offlineMsgStatus(String user, String msg){
		String result = "";
		String url = baseUrl + "/users/" + user + "/offline_msg_status/" + msg;
		try {
			String txt = HttpClientUtil.get(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[GET USER STATUS][RESULT:"+txt+"]");
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.has("data")){
				row = row.getRow("data");
				result = row.getString(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 禁用帐号
	 * @param user
	 * @return
	 */
	public static DataRow deactivate(String user){
		DataRow result = null;
		String url = baseUrl + "/users/" + user + "/deactivate";
		try {
			String txt = HttpClientUtil.post(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[DEACTIVATE USER][RESULT:"+txt+"]");
			}
			result = parseUser(txt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	

	/**
	 * 激活已禁用帐号
	 * @param user
	 * @return
	 */
	public static void activate(String user){
		String url = baseUrl + "/users/" + user + "/activate";
		try {
			String txt = HttpClientUtil.post(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[ACTIVATE USER][RESULT:"+txt+"]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 强制用户下线 
	 * @param user
	 * @return
	 */
	public static boolean disconnect(String user){
		boolean result = false;
		String url = baseUrl + "/users/" + user + "/disconnect";
		try {
			String txt = HttpClientUtil.post(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug()){
				log.warn("[DISCONNECT USER][RESULT:"+txt+"]");
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.has("data")){
				row = row.getRow("data");
				if(null != row && row.has("result") && row.getBoolean("result", false)){
					result = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String args[]){
		
		ConfigTable.setRoot("D:\\develop\\git\\anyline\\anyline_easemob");
		DataSet set;
		//EasemobUtil.reg("3","123","张三三");
//		DataRow row = EasemobUtil.addFriend("1", "3");
//		EasemobUtil.deleteFriend("1","2");
//		set = EasemobUtil.getFriends("1");
////		
//		System.out.println(set);
//		EasemobUtil.addBlock("1", "3");
//		EasemobUtil.deleteBlock("1", "3");
//		set = EasemobUtil.getBlocks("1");
		System.out.println(EasemobUtil.offlineMsgCount("1"));
	}
	
	/**
	 * 根据返回值解析用户数据 
	 * @param txt
	 * @return
	 */
	private static DataRow parseUser(String txt){
		DataRow user = null;
		DataRow row = DataRow.parseJson(txt);
		if(null != row && row.containsKey("entities")){
			Object entitys = row.get("entities");
			if(null != entitys && entitys instanceof List){
				List<DataRow> list = (List<DataRow>)entitys;
				if(list.size()>0){
					user = list.get(0);
				}
			}
		}
		return user;
	}
	
	
	
	
	
	private static Map<String,String> defaultHeader(){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Authorization", "Bearer " + getAccessToken());
		headers.put("Content-Type", "application/json");
		return headers;
	}
	private static String getAccessToken(){
		String token = null;
		if(System.currentTimeMillis()/1000 > access_token_expires){
			token = createNewAccessToken();
		}else{
			token = access_token;
		}
		return token;
	}
	
	/**
	 * 创建新token 
	 * @return
	 */
	private static String createNewAccessToken(){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/json");
		Map<String,String> map = new HashMap<String,String>();
		map.put("grant_type", "client_credentials");
		map.put("client_id", clientId);
		map.put("client_secret", clientSecret);
		try {
			String url = host + "/"+orgName+"/"+appName+"/token";
			String txt = HttpClientUtil.post(headers, url, "UTF-8", new StringEntity(BeanUtil.map2json(map))).getText();
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("access_token")){
				access_token = json.getString("access_token");
			}
			if(json.has("expires_in")){
				access_token_expires = System.currentTimeMillis()/1000 + json.getLong("expires_in");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return access_token;
	}
}
