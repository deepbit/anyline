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


package org.anyline.config.db;

import java.io.Serializable;
import java.util.List;

import org.anyline.config.db.run.RunSQL;
import org.anyline.config.http.ConfigStore;

public interface SQLCreater extends Serializable{

	public static final String TAB = "\t";
	public static final String BR = "\n";
	public static final String BR_TAB = "\n\t";
	
	/**
	 * 创建查询SQL
	 * @param sql
	 * @param configs
	 * @param conditions
	 * @return
	 */
	public RunSQL createQueryRunSQL(SQL sql, ConfigStore configs, String ... conditions);
	
	public RunSQL createDeleteRunSQL(String dest, Object obj, String ... columns);
	public RunSQL createDeleteRunSQL(String table, String key, Object values);
	public RunSQL createExecuteRunSQL(SQL sql, ConfigStore configs, String ... conditions);
	
	public String parseBaseQueryTxt(RunSQL run);
	/**
	 * 求总数SQL
	 * @param txt
	 * @return
	 */
	public String parseTotalQueryTxt(RunSQL run);
	
	public String parseExistsTxt(RunSQL run);
	/**
	 * 查询SQL
	 * @param baseTxt
	 * @return
	 */
	public String parseFinalQueryTxt(RunSQL run);
	public RunSQL createInsertTxt(String dest, Object obj, boolean checkPrimary, String ... columns);
	public RunSQL createUpdateTxt(String dest, Object obj, boolean checkPrimary, String ... columns);
	public String getDisKeyFr();
	public String getDisKeyTo();
	public String getDataSource(Object obj);
	public String getPrimaryKey(Object obj);
	public Object getPrimaryValue(Object obj);
	public List<String> confirmInsertColumns(String dst, Object data, String ... columns);
	/**
	 * 拼接字符串
	 * @param args
	 * @return
	 */
	public String concat(String ... args);
}
