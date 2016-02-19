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


package org.anyline.config.db.sql.auto;

import java.util.List;

import org.anyline.config.db.SQL;

public interface AutoSQL extends SQL{
	public SQL init();

	/**
	 * 设置数据源
	 */
	public SQL setDataSource(String table);
	
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
	public SQL addCondition(boolean requried, String column, Object value, int compare);

	/**
	 * 添加静态文本查询条件
	 */
	public SQL addCondition(String condition) ;
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
	
	public void createRunText(StringBuilder builder);
/* ******************************************* END SQL *********************************************************** */
	/**
	 * 添加列 
	 * CD
	 * CD,NM
	 * @param columns
	 */
	public void addColumn(String columns);
	public String getDataSource();
	public String getAuthor() ;
	public void setAuthor(String author) ;
	public String getTable() ;
	public void setTable(String table) ;
	public String getDistinct();
	public List<String> getColumns();
}
