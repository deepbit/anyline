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


package org.anyline.config.db.impl;

import java.util.ArrayList;
import java.util.List;

import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.util.BasicUtil;

public class OrderStoreImpl implements OrderStore{
	private static final long serialVersionUID = -2129393152486629564L;
	private List<Order> orders = new ArrayList<Order>();

	public OrderStoreImpl() {
	}
	public void order(Order order) {
		if(null == order){
			return;
		}
		Order tmp = getOrder(order.getColumn());
		if(null != tmp){
			tmp.setType(order.getType());
		}else{
			orders.add(order);
		}
	}

	public void order(String col, String type) {
		order(new OrderImpl(col, type));
	}
	/**
	 * 排序多列以,分隔
	 * order("CD","DESC");
	 * order("CD DESC");
	 * order("CD DESC,NM ASC");
	 * @param str
	 */
	public void order(String str) {
		if (BasicUtil.isEmpty(str)) {
			return;
		}
		if (str.toUpperCase().contains("ORDER BY")) {
			str = str.toUpperCase().replace("ORDER BY", "").trim();
		}
		String[] tmps = str.split(","); // 多列排序
		for (String tmp : tmps) {
			order(new OrderImpl(tmp));
		}
	}

	public Order getOrder(String order){
		if(null == order){
			return null;
		}
		if(null != orders){
			for(Order o:orders){
				if(null != o && order.equalsIgnoreCase(o.getColumn())){
					return o;
				}
			}
		}
		return null;
	}
	public String getRunText(String disKey){
		StringBuilder builder = new StringBuilder();
		if(null != orders && orders.size() > 0){
			builder.append(" ORDER BY ");
			for(int i=0; i<orders.size(); i++){
				Order order = orders.get(i);
				if(null == order){
					continue;
				}
				builder.append(order.getColumn()).append(" ").append(order.getType());
				if(i<orders.size()-1){
					builder.append(",");
				}
			}
		}
		return builder.toString();
	}

	public void clear(){
		orders.clear();
	}
	public List<Order> getOrders(){
		return this.orders;
	}
}
