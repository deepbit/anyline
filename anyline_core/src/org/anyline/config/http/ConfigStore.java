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

package org.anyline.config.http;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.anyline.config.db.Group;
import org.anyline.config.db.GroupStore;
import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.config.db.PageNavi;


/**
 * 查询参数
 * @author Administrator
 *
 */
public interface ConfigStore {
	
	
	/**
	 * 解析查询配置参数
	 * @param configs	
	 * 			"COMPANY_CD:company","NM:nmEn% | NM:nmCn%","STATUS_VALUE:[status]"
	 * @return
	 */
	public Config parseConfig(String config);
	public ConfigStore setPageNavi(PageNavi navi);
	public ConfigStore copyPageNavi(PageNavi navi);
	public ConfigStore addParam(String key, String value);
	public ConfigStore setValue(HttpServletRequest request);
	public ConfigChain getConfigChain();
	public Config getConfig(String key);
	public List<Object> getConfigValues(String key);
	public Object getConfigValue(String key);
	public ConfigStore addConditions(String key, Object value);
	public ConfigStore addCondition(String key, Object value);
	/**
	 * 添加排序
	 * @param order
	 * @return
	 */
	public ConfigStore order(Order order);

	public ConfigStore order(String column, String type);
	public ConfigStore order(String order);
	public OrderStore getOrders() ;
	public ConfigStore setOrders(OrderStore orders) ;
	/**
	 * 添加分组
	 * @param group
	 * @return
	 */
	public ConfigStore group(Group group);

	public ConfigStore group(String group);
	public GroupStore getGroups() ;
	public ConfigStore setGroups(GroupStore groups) ;
	public PageNavi getPageNavi();
	/**
	 * 提取部分查询条件
	 * @param keys
	 * @return
	 */
	public ConfigStore fetch(String ... keys);
	
	public String toString();
}


