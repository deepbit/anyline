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


package org.anyline.config.db.sql.xml;

import java.util.List;

import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLVariable;
/**
 * order 需要区分XML定义还是动态添加
 * @author Administrator
 *
 */
public interface XMLSQL extends SQL{
	public SQL init() ;

	/**
	 * 设置SQL 主体文本
	 * @param text
	 */
	public SQL setText(String text) ;
	public String getText();

	/**
	 * 添加静态文本查询条件
	 */
	public SQL addCondition(String condition) ;

	/* ***********************************************************************************************************************************
	 * 
	 * 														赋值
	 * 
	 * ***********************************************************************************************************************************/
	/**
	 * 添加查询条件
	 * @param	condition
	 * 			列名|查询条件ID
	 * @param	variable
	 * 			变量key
	 * @param	value
	 * 			值
	 */
	public SQL setConditionValue(String condition, String variable, Object value);
	/* ***********************************************************************************************************************************
	 * 
	 * 														生成SQL
	 * 
	 * ***********************************************************************************************************************************/
	/**
	 * 添加分组
	 * @param builder
	 */
	public List<SQLVariable> getSQLVariables();
}
