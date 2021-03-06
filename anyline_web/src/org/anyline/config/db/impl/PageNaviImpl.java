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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.anyline.config.db.Order;
import org.anyline.config.db.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.tag.Navi;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.NumberUtil;
import org.apache.log4j.Logger;


public class PageNaviImpl implements PageNavi, Serializable{
	private static final long serialVersionUID = 3593100423479113410L;
	private static Logger log = Logger.getLogger(PageNaviImpl.class);
//
//	public static final String PAGE_VOL				= "pageRows"							;
//	public static final String PAGE_NO				= "pageNo"								;

	private static final String BR = "\n";
	private static final String TAB = "\t";
	private static final String BR_TAB = "\n\t";
	public static final String PAGE_ROWS			= "_anyline_page_rows"					;
	public static final String PAGE_NO				= "_anyline_page"						;
	
	private int totalRow					= 0			;	//记录总数
	private int totalPage					= 0 		;	//最大页数
	private int curPage						= 1 		;	//当前页数
	
	private int pageRange					= 10		;	//显示多少个分页下标
	private int pageRows					= 10		;	//每页多少条
	private int displayPageFirst 			= 0			;	//显示的第一页标签
	private int displayPageLast 			= 0			;	//显示的最后页标签
	private String baseLink					= null		;	//基础URL
	private OrderStore orders				= null 		;	//排序依据(根据 orderCol 排序分页)
	private int calType 					= 0			;	//分页计算方式(0-按页数 1-按开始结束数)
	private int firstRow 					= 0			;	//第一行
	private int lastRow 					= -1		;	//最后一行
	private boolean lazy 					= false		;
	private String flag  					= ""		;	//一个jsp中有多个分页时用来区分
	private long lazyPeriod 				= 0			;	//总条数懒加载时间间隔(秒)
	private String lazyKey 					= null		;	//懒加载
	private int range 						= 10		;	//下标数量
	private boolean showStat 				= false		;	//显示统计
	private boolean showJump 				= false		;	//显示跳转
	private int type 						= 0			;	//分页方式(0:下标 1:加载更多)
	private String loadMoreFormat 			= "加载更多"	;
	private Map<String,List<Object>> params	= null		;	//查询参数

	private String statFormat = "<div class='navi-summary'>共<span class='navi-total-row'>{totalRow}</span>条 第<span class='navi-cur-page'>{curPage}</span>/<span class='navi-total-page'>{totalPage}</span>页</div>";
	private String tagFirst = "第一页";
	private String tagPrev = "上一页";
	private String tagNext = "下一页";
	private String tagLast = "最后页";
	private String tagGo = "确定";
	
	
	public PageNaviImpl(int totalRow, int curPage, int pageRows, String baseLink) {
		this.totalRow = totalRow;
		this.curPage = curPage;
		setPageRows(pageRows);
		this.baseLink = baseLink;
	}
	public PageNaviImpl(int curPage,int pageRows, String baseLink){
		this.curPage = curPage;
		setPageRows(pageRows);
		this.baseLink = baseLink;
	}
	public PageNaviImpl(String baseLink){
		this.curPage = 1;
		this.baseLink = baseLink;
	}
	public PageNaviImpl(){}

	public String ajaxPage(){
		return html("ajax");
	}
	public String jspPage(){
		return html("html");
	}
	/**
	 * 
	 * @return
	 */
	public String html(String creater){
		calculate();
		StringBuilder builder = new StringBuilder();
		String configFlag = "";
		if("ajax".equals(creater)){
			configFlag = Navi.CONFIG_FLAG_KEY + flag;
		}
		if("html".equals(creater)){
			builder.append("<link rel=\"stylesheet\" href=\""+ConfigTable.getString("NAVI_STYLE_FILE_PATH")+"\" type=\"text/css\"/>\n");
			builder.append("<script type=\"text/javascript\" src=\""+ConfigTable.getString("NAVI_SCRIPT_FILE_PATH")+"\"></script>\n");
		}
		builder.append("<form action=\"" + baseLink + "\" method=\"post\">\n");
		builder.append("<input type='hidden' name='"+PageNavi.PAGE_NO+"' class='_anyline_navi_cur_page' value='"+curPage+"'/>\n");
		if("ajax".equals(creater)){
			builder.append("<input type='hidden' class='"+Navi.CONFIG_FLAG_KEY+"' value='" + configFlag + "'/>");
		}
		builder.append(createHidParams());
		builder.append("<div class=\"anyline_navi\">\n");
		//数据统计
		String stat = ConfigTable.getString("NAVI_STAT_FORMAT",statFormat); 
		stat = stat.replace("{totalRow}", totalRow+"").replace("{curPage}", curPage+"").replace("{totalPage}", totalPage+"");
		if(ConfigTable.getBoolean("NAVI_SHOW_STAT", showStat)){
			builder.append(stat).append("\n");
		}
		int range = ConfigTable.getInt("NAVI_PAGE_RANGE",10);
		int fr = NumberUtil.getMax(1,curPage - range/2);
		int to = fr + range - 1;
		boolean match = false;
		if(totalPage > range && curPage>range/2){
			match = ConfigTable.getBoolean("NAVI_PAGE_MATCH", true);
		}
		if(match){
			to = curPage + range/2;
		}
		if(totalPage - curPage < range/2){
			fr = totalPage - range;
		}
		fr = NumberUtil.getMax(fr, 1);
		to = NumberUtil.getMin(to, totalPage);
		
		if(type ==0){
			//下标导航

			//上一页 下一页
			if(ConfigTable.getBoolean("NAVI_SHOW_BUTTON", true)){
				createPageTag(builder, "navi-button navi-first-button", ConfigTable.getString("NAVI_TAG_FIRST", tagFirst), 1, configFlag);
				createPageTag(builder, "navi-button navi-prev-button", ConfigTable.getString("NAVI_TAG_PREV", tagPrev), NumberUtil.getMax(curPage-1,1), configFlag);
			}
			
			if(ConfigTable.getBoolean("NAVI_SHOW_INDEX", true)){
				builder.append("<div class='navi-num-border'>\n");
				for(int i=fr; i<=to; i++){
					createPageTag(builder, "navi-num-item", i + "", i, configFlag);
				}
				builder.append("</div>\n");
			}
			
			if(ConfigTable.getBoolean("NAVI_SHOW_BUTTON", true)){
				createPageTag(builder, "navi-button navi-next-button", ConfigTable.getString("NAVI_TAG_NEXT", tagNext), (int)NumberUtil.getMin(curPage+1, totalPage), configFlag);
				createPageTag(builder, "navi-button navi-last-button", ConfigTable.getString("NAVI_TAG_LAST", tagLast), totalPage, configFlag);
			}
			if(ConfigTable.getBoolean("NAVI_SHOW_JUMP",showJump)){
				builder.append("转到<input type='text' value='");
				builder.append(curPage);
				builder.append("' class='navi-go-txt _anyline_jump_txt'/>页<span class='navi-go-button' onclick='_navi_jump("+configFlag+")'>")
				.append(ConfigTable.getString("NAVI_TAG_GO",tagGo)).append("</span>\n");
			}
		}else if(type == 1){
			//加载更多
			String loadMoreFormat = this.loadMoreFormat;
			if(BasicUtil.isNotEmpty(loadMoreFormat)){
				loadMoreFormat = ConfigTable.getString("NAVI_LOAD_MORE_FORMAT", "加载更多"); 
			}
			createPageTag(builder, "navi-more-button", loadMoreFormat, (int)NumberUtil.getMin(curPage+1, totalPage+1), configFlag);
		}
		builder.append("</div>");
		builder.append("</form>\n");
		return builder.toString();
	}
	private void createPageTag(StringBuilder builder, String clazz, String tag, int page, String configFlag){
		builder.append("<span class ='").append(clazz);
		if(page == curPage && 0 == type){
			builder.append(" navi-disabled");
			if(clazz.contains("navi-num-item")){
				builder.append(" navi-num-item-cur");
			}
			builder.append("'");
		}else{
			builder.append("' onclick='_navi_go(").append(page);
			if(BasicUtil.isNotEmpty(configFlag)){
				builder.append(",").append(configFlag);
			}
			builder.append(")'");
		}
		builder.append(">");
		builder.append(tag).append("</span>\n");
	}
	/**
	 * 分页计算方式
	 * @param type	0-按页数 1-按开始结束记录数
	 */
	public void setCalType(int type){
		this.calType = type;
	}
	public int getCalType(){
		return calType;
	}
	/**
	 * 计算分页变量
	 */
	public void calculate() {
		setTotalPage((totalRow - 1) / pageRows + 1);					//总页数
		setDisplayPageFirst(curPage - pageRange/2);				//显示的第一页
		if(displayPageFirst > totalPage - pageRange){
			setDisplayPageFirst(totalPage - pageRange + 1);
		}
		if(displayPageFirst < 1){ 
			setDisplayPageFirst(1);
		}
		
		setDisplayPageLast(displayPageFirst + pageRange - 1);		//显示的最后页
		if (displayPageLast > totalPage)
			setDisplayPageLast(totalPage);
	}
	//创建隐藏参数
	private String createHidParams(){
		String html = "";
		try{
			if(null != params){
				for(Iterator<String> itrKey=params.keySet().iterator(); itrKey.hasNext();){
					String key = itrKey.next();
					Object values = params.get(key);
					html += createHidParam(key,values);
				}
			}
			html += createHidParam(PageNavi.SHOW_STAT,showStat);
			html += createHidParam(PageNavi.SHOW_JUMP,showJump);
		}catch(Exception e){
			e.printStackTrace();
		}
		return html;
	}
	
	/**
	 * 第一行
	 * @return
	 */
	public int getFirstRow(){
		if(calType == 0){
			if(curPage <= 0) {
				return 0;
			}
			return (curPage-1) * pageRows;
		}else{
			return firstRow;
		}
	}
	/**
	 * 最后一行
	 * @return
	 */
	public int getLastRow(){
		if(calType == 0){
			if(curPage == 0) {
				return pageRows -1;
			}
			return curPage * pageRows - 1;
		}else{
			return lastRow;
		}
	}
	/**
	 * 页面显示的第一页
	 * @return
	 */
	public int getDisplayPageFirst() {
		return displayPageFirst;
	}
	/**
	 * 设置页面显示的第一页
	 * @param displayPageFirst
	 */
	public void setDisplayPageFirst(int displayPageFirst) {
		this.displayPageFirst = displayPageFirst;
	}
	/**
	 * 页面显示的最后一页
	 * @return
	 */
	public int getDisplayPageLast() {
		return displayPageLast;
	}
	/**
	 * 设置页面显示的最后一页
	 * @param displayPageLast
	 */
	public void setDisplayPageLast(int displayPageLast) {
		this.displayPageLast = displayPageLast;
	}

	@SuppressWarnings("unchecked")
	public void addParam(String key, Object value){
		if(null == key || null == value){
			return;
		}
		if(null == this.params){
			this.params = new HashMap<String,List<Object>>();
		}
		List<Object> values = params.get(key);
		if(null == values){
			values = new ArrayList<Object>();
		}
		if(value instanceof Collection){
			values.addAll((Collection)value);
		}else{
			values.add(value);
		}
		params.put(key, values);
	}
	public Object getParams(String key){
		Object result = null;
		if(null != params){
			result = params.get(key);
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public Object getParam(String key){
		Object result = null;
		if(null != params){
			Object values = getParams(key);
			if(null != values && values instanceof List){
				result = ((List)values).get(0);
			}else{
				result = values;
			}
		}
		return result;
	}
	public String getOrderText(boolean require){
		//return getOrderText(require, null);
		return null;
	}
	public String getOrderText(boolean require, OrderStore store, String disKey){
		String result = "";
		if(null == orders){
			orders = store;
		}else{
			if(null != store){
				for(Order order:store.getOrders()){
					orders.order(order);
				}
			}
		}
		if(null != orders){
			result = orders.getRunText(disKey);
		}
		if(require && result.length() == 0){
			result = "ORDER BY " + ConfigTable.getString("DEFAULT_PRIMARY_KEY","CD");
		}
		return result;
	}
	/**
	 * 设置排序方式
	 * @param order
	 * @return
	 */
	public PageNavi order(Order order){
		if(null == orders){
			orders = new OrderStoreImpl();
		}
		orders.order(order);
		return this;
	}
	/**
	 * 设置排序方式
	 * @param order
	 * @param type
	 * @return
	 */
	@Override
	public PageNavi order(String order, String type){
		return order(new OrderImpl(order, type));
	}
	@Override
	public PageNavi order(String order){
		return order(new OrderImpl(order));
	}
	
	/**
	 * 设置总行数
	 * @param totalRow
	 */
	@Override
	public PageNavi setTotalRow(int totalRow) {
		this.totalRow = totalRow;
		return this;
	}
	/**
	 * 设置最后一页
	 * @param totalPage
	 */
	@Override
	public PageNavi setTotalPage(int totalPage) {
		this.totalPage = totalPage;
		return this;
	}
	/**
	 * 设置当前页
	 * @param curPage
	 */
	@Override
	public PageNavi setCurPage(int curPage) {
		this.curPage = curPage;
		return this;
	}
	/**
	 * 设置每页显示的行数
	 * @param pageRows
	 */
	@Override
	public PageNavi setPageRows(int pageRows) {
		if(pageRows > 0){
			this.pageRows = pageRows;
		}
		return this;
	}
	@Override
	public int getTotalRow() {
		return totalRow;
	}

	@Override
	public int getTotalPage() {
		return totalPage;
	}

	@Override
	public int getCurPage() {
		return curPage;
	}

	@Override
	public int getPageRows() {
		return pageRows;
	}

	@Override
	public String getBaseLink() {
		return baseLink;
	}
	@Override
	public PageNavi setBaseLink(String baseLink) {
		this.baseLink = baseLink;
		return this;
	}
	@Override
	public PageNavi setFirstRow(int firstRow) {
		this.firstRow = firstRow;
		return this;
	}
	@Override
	public PageNavi setLastRow(int lastRow) {
		this.lastRow = lastRow;
		return this;
	}
	
	@Override
	public boolean isLazy() {
		return this.lazy;
	}
	@Override
	public long getLazyPeriod() {
		return this.lazyPeriod;
	}
	@Override
	public PageNavi setLazy(long ms) {
		this.lazy = true;
		this.lazyPeriod = ms;
		return this;
	}
	@Override
	public PageNavi setLazyPeriod(long ms){
		this.lazy = true;
		this.lazyPeriod = ms;
		return this;
	}
	
	@Override
	public PageNavi setLazyKey(String key) {
		this.lazyKey = key;
		return this;
	}
	@Override
	public String getLazyKey() {
		return this.lazyKey;
	}
	@Override
	public String createHidParam(String name, Object values) {
		String html = "";
		if(null == values){
			html = "<input type='hidden' name='"+name+"' value=''>\n";
		}else{
			if(values instanceof Collection<?>){
				Collection<?> list = (Collection<?>)values;
				for(Object obj:list){
					html += "<input type='hidden' name='"+name+"' value='"+obj+"'>\n";
				}
			}else{
				html += "<input type='hidden' name='"+name+"' value='"+values+"'>\n";
			}
		}
		return html;
	}
	
	public String toString(){
		return html("html");
	}
	public String getFlag() {
		return flag;
	}
	public PageNavi setFlag(String flag) {
		this.flag = flag;
		return this;
	}
	public int getRange() {
		return range;
	}
	public void setRange(int range) {
		this.range = range;
	}
	public boolean isShowStat() {
		return showStat;
	}
	public PageNavi setShowStat(boolean showStat) {
		this.showStat = showStat;
		return this;
	}
	public boolean isShowJump() {
		return showJump;
	}
	public PageNavi setShowJump(boolean showJump) {
		this.showJump = showJump;
		return this;
	}
	public int getType() {
		return type;
	}
	public PageNavi setType(int type) {
		this.type = type;
		return this;
	}

}