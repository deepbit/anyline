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


package org.anyline.config.db.sql.auto.impl;

import java.util.ArrayList;
import java.util.List;

import org.anyline.config.ConfigParser;
import org.anyline.config.ParseResult;
import org.anyline.config.db.Condition;
import org.anyline.config.db.ConditionChain;
import org.anyline.config.db.Order;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLVariable;
import org.anyline.config.db.impl.BasicSQL;
import org.anyline.config.db.sql.auto.AutoSQL;
import org.anyline.util.BasicUtil;

public class AutoSQLImpl extends BasicSQL implements AutoSQL{
	private static final long serialVersionUID = 4804654368819564162L;
	protected String schema;
	protected String table;
	protected List<String> columns;
	protected String distinct = "";
	
	public AutoSQLImpl(){
		super();
		chain = new AutoConditionChainImpl();
	}
	public SQL init(){
		return this;
	}
	/**
	 * 设置数据源
	 * table(c1,c2)[pk1,pk2]
	 */
	public SQL setDataSource(String table){
		if(null == table){
			return this;
		}
		table = table.toUpperCase();
		if(table.contains("(")){
			//指定列名
			setTable(table.substring(0,table.indexOf("(")));
			int colIdx0 = table.indexOf("(");
			int colIdx1 = table.lastIndexOf(")");
			String columns = table.substring(colIdx0+1,colIdx1);
			if(columns.contains("DISTINCT")){
				//distinct
				columns = columns.replace("DISTINCT","");
				columns = columns.trim();
				distinct = "DISTINCT";
			}
			addColumn(columns);
		}else{
			setTable(table);
		}
		return this;
	}
	
	/*******************************************************************************************
	 * 
	 * 										添加条件
	 * 
	 ********************************************************************************************/
	/**
	 * 添加查询条件
	 * @param	required
	 * 			是否必须
	 * @param	column
	 * 			列名
	 * @param	value
	 * 			值
	 * @param	compare
	 * 			比较方式
	 */
	public SQL addCondition(boolean requried, String column, Object value, int compare){
		if(null == chain){
			chain = new AutoConditionChainImpl();
		}
		Condition condition = new AutoConditionImpl(requried,column, value, compare);
		chain.addCondition(condition);
		return this;
	}

	/**
	 * 添加静态文本查询条件
	 */
	public SQL addCondition(String condition) {
		if(BasicUtil.isEmpty(condition)){
			return this;
		}
		if(condition.contains(":")){
			ParseResult parser = ConfigParser.parse(condition, false);
			Object value = ConfigParser.getValues(parser);
			addCondition(parser.isRequired(),parser.getId(),value,parser.getCompare());
		}else{
			Condition con = new AutoConditionImpl(condition);
			chain.addCondition(con);
		}
		return this;
	}
	 /*******************************************************************************************
	 * 
	 * 										赋值
	 * 
	 ********************************************************************************************/
	
	
	/********************************************************************************************
	 * 
	 * 										生成 SQL
	 * 
	 ********************************************************************************************/
	/**
	 * 添加列 
	 * CD
	 * CD,NM
	 * @param columns
	 */
	public void addColumn(String columns){
		if(BasicUtil.isEmpty(columns)){
			return;
		}
		if(null == this.columns){
			this.columns = new ArrayList<String>();
		}
		if(columns.contains(",")){
			//多列
			parseMultColumns(columns);
		}else{
			//单列
			this.columns.add(columns);
		}
	}
	/**
	 * 解析多列 
	 * @param src
	 */
	private void parseMultColumns(String src){
		List<String> cols = new ArrayList<String>();
		//拆分转义字段({}) CD, {ISNULL(NM,'') AS NM}, {CASE WHEN AGE>0 THEN 0 AGE ELSE 0 END AS AGE}, TITLE  
		while(src.contains("{")){
			src = src.trim();
			int fr = src.indexOf("{");
			String tmp = "";
			if(0 == fr){
				tmp = src.substring(0, src.indexOf("}")+1);
				src = src.substring(src.indexOf("}")+1);
			}else{
				tmp = src.substring(0, fr);
				src = src.substring(fr);
			}
			cols.add(tmp);
		}
		cols.add(src);
		//二次拆分
		for(String c:cols){
			if(c.contains("{")){
				this.columns.add(c);
			}else{
				String[] cs = c.split(",");
				for(String item:cs){
					item = item.trim();
					if(item.length()>0)
						this.columns.add(item);
				}
			}
		}
	}
	public String getDataSource(){
		return table;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	@Override
	public String getTable() {
		return table;
	}
	@Override
	public void setTable(String table) {
		this.table = table;
	}
	@Override
	public SQL order(Order order) {
		return this;
	}
	@Override
	public ConditionChain getConditionChain() {
		return this.chain;
	}
	@Override
	public void createRunText(StringBuilder builder) {
	}
	@Override
	public String getDistinct() {
		return this.distinct;
	}
	@Override
	public List<String> getColumns() {
		return this.columns;
	}
	@Override
	public String getText() {
		return null;
	}
	@Override
	public List<SQLVariable> getSQLVariables() {
		return null;
	}
	
	
}
