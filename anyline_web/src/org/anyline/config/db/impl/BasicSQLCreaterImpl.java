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
import java.util.Collection;
import java.util.List;

import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.run.RunSQL;
import org.anyline.config.db.run.impl.TableRunSQLImpl;
import org.anyline.config.db.run.impl.TextRunSQLImpl;
import org.anyline.config.db.run.impl.XMLRunSQLImpl;
import org.anyline.config.db.sql.auto.TableSQL;
import org.anyline.config.db.sql.auto.TextSQL;
import org.anyline.config.db.sql.xml.XMLSQL;
import org.anyline.config.http.ConfigStore;
import org.anyline.dao.PrimaryCreater;
import org.anyline.entity.AnylineEntity;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.exception.SQLException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * SQL生成 子类主要实现与分页相关的SQL 以及disKey
 * @author  
 * @since   1.0
 */

public abstract class BasicSQLCreaterImpl implements SQLCreater{
	private static final long serialVersionUID = -1280284751032142401L;
	protected static Logger log = Logger.getLogger(BasicSQLCreaterImpl.class);
	public String disKeyFr = "";
	public String disKeyTo = "";

	@Autowired(required=false)
	private PrimaryCreater primaryCreater;
	/**
	 * 创建查询SQL
	 */
	@Override
	public RunSQL createQueryRunSQL(SQL sql, ConfigStore configs, String ... conditions){
		RunSQL run = null;
		if(sql instanceof TableSQL){
			run = new TableRunSQLImpl();
		}else if(sql instanceof XMLSQL){
			run = new XMLRunSQLImpl();
		}else if(sql instanceof TextSQL){
			run = new TextRunSQLImpl();
		}
		if(null != run){
			run.setCreater(this);
			run.setSql(sql);
			run.setConfigStore(configs);
			run.addConditions(conditions);
			run.init();
		}
		return run;
	}
	@Override
	public RunSQL createExecuteRunSQL(SQL sql, ConfigStore configs, String ... conditions){
		RunSQL run = null;
		if(sql instanceof XMLSQL){
			run = new XMLRunSQLImpl();
		}else if(sql instanceof TextSQL){
			run = new TextRunSQLImpl();
		}
		if(null != run){
			run.setCreater(this);
			run.setSql(sql);
			run.setConfigStore(configs);
			run.addConditions(conditions);
			run.init();
		}
		return run;
	}
	@Override
	public RunSQL createDeleteRunSQL(String table, String key, Object values){
		return createDeleteRunSQLFromTable(table, key, values);
	}
	@Override
	public RunSQL createDeleteRunSQL(String dest, Object obj, String ... columns){
		if(null == obj){
			return null;
		}
		RunSQL run = null;
		if(null == dest){
			dest = getDataSource(obj);
		}
		if(obj instanceof DataRow){
			run = createDeleteRunSQLFromDataRow(dest, (DataRow)obj, columns);
		}else if(obj instanceof AnylineEntity){
			run = createDeleteRunSQLFromEntity(dest, (AnylineEntity)obj, columns);
		}
		return run;
	}
	private RunSQL createDeleteRunSQLFromTable(String table, String key, Object values){
		if(null == table || null == key || null == values){
			return null;
		}
		TableRunSQLImpl run = new TableRunSQLImpl();
		StringBuilder builder = run.getBuilder();
		builder.append("DELETE FROM ").append(table).append(" WHERE ");
		if(values instanceof Collection){
			Collection cons = (Collection)values;
			builder.append(getDisKeyFr()).append(key).append(getDisKeyTo());
			if(cons.size() > 1){
				builder.append(" IN(");
				int idx = 0;
				for(Object obj:cons){
					if(idx > 0){
						builder.append(",");
					}
					builder.append("'").append(obj).append("'");
					idx ++;
				}
				builder.append(")");
			}else if(cons.size() == 1){
				for(Object obj:cons){
					builder.append("=?");
					run.addValue(obj);
				}
			}else{
				throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
			}
		}else{
			builder.append(getDisKeyFr()).append(key).append(getDisKeyTo());
			builder.append("=?");
			run.addValue(values);
		}
		return run;
	}
	private RunSQL createDeleteRunSQLFromDataRow(String dest, DataRow obj, String ... columns){
		TableRunSQLImpl run = new TableRunSQLImpl();
		StringBuilder builder = run.getBuilder();
		builder.append("DELETE FROM ").append(parseTable(dest)).append(" WHERE ");
		List<String> keys = new ArrayList<String>();
		if(null != columns && columns.length>0){
			for(String col:columns){
				keys.add(col);
			}
		}else{
			keys = obj.getPrimaryKeys();
		}
		int size = keys.size();
		if(size >0){
			for(int i=0; i<size; i++){
				if(i > 0){
					builder.append(" AND ");
				}
				String key = keys.get(i);
				builder.append(getDisKeyFr()).append(key).append(getDisKeyTo()).append(" = ? ");
				run.addValue(obj.get(key));
			}
		}else{
			throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
		}
		return run;
	}

	private RunSQL createDeleteRunSQLFromEntity(String dest, AnylineEntity obj, String ... columns){
		TableRunSQLImpl run = new TableRunSQLImpl();
		run.getBuilder().append("DELETE FROM ").append(parseTable(dest))
		.append(" WHERE ").append(getDisKeyFr()).append(getPrimaryKey(obj)).append(getDisKeyTo())
		.append("=?");
		run.addValue(getPrimaryValue(obj));
		return run;
	}

	@Override
	public String getPrimaryKey(Object obj){
		if(null == obj){
			return null;
		}
		if(obj instanceof DataRow){
			return ((DataRow)obj).getPrimaryKey();
		}else{
			return null;
		}
	}
	@Override
	public Object getPrimaryValue(Object obj){
		if(null == obj){
			return null;
		}
		if(obj instanceof DataRow){
			return ((DataRow)obj).getPrimaryValue();
		}else{
			return null;
		}
	}
	/**
	 * 基础查询SQL
	 * RunSQL 反转调用
	 */
	@Override
	public String parseBaseQueryTxt(RunSQL run){
		return run.getBuilder().toString();
	}
	/**
	 * 求总数SQL
	 * RunSQL 反转调用
	 * @param txt
	 * @return
	 */
	@Override
	public String parseTotalQueryTxt(RunSQL run){
		String sql = "SELECT COUNT(0) AS CNT FROM (\n" + run.getBuilder().toString() +"\n) AS F";
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE");
		return sql;
	}

	@Override
	public String parseExistsTxt(RunSQL run){
		String sql = "SELECT EXISTS(\n" + run.getBuilder().toString() +"\n) AS IS_EXISTS";
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE");
		return sql;
	}
	
	


	@Override
	public String getDisKeyFr(){
		return disKeyFr;
	}
	@Override
	public String getDisKeyTo(){
		return disKeyTo;
	}

	@Override
	public RunSQL createInsertTxt(String dest, Object obj, boolean checkPrimary, String ... columns){
		if(null == obj){
			return null;
		}
		if(null == dest){
			dest = getDataSource(obj);
		}
		if(obj instanceof DataRow){
			DataRow row = (DataRow)obj;
			row.setDataSource(dest);
			return createInsertTxtFromDataRow(dest,row,checkPrimary, columns);
		}
		if(obj instanceof AnylineEntity){
			return createInsertTxtFromEntity(dest,(AnylineEntity)obj,checkPrimary, columns);	
		}
		if(obj instanceof DataSet){
			DataSet set = (DataSet)obj;
			set.setDataSource(dest);
			if(set.size() >0){
				return createInsertTxtFromDataSet(dest,set,checkPrimary, columns);
			}
		}
		return null;
	}
	@Override
	public String getDataSource(Object obj){
		if(null == obj){
			return null;
		}
		String result = "";
		if(obj instanceof DataRow){
			DataRow row = (DataRow)obj;
			result = row.getDataSource();
		}else if(obj instanceof DataSet){
			DataSet set = (DataSet)obj;
			if(set.size()>0){
				result = getDataSource(set.getRow(0));
			}
		}
//		else{
//			try{
//				Annotation annotation = obj.getClass().getAnnotation(Table.class);			//提取Table注解
//				Method method = annotation.annotationType().getMethod("name");				//引用name方法
//				result = (String)method.invoke(annotation);									//执行name方法返回结果
//				result = result.replace(getDisKeyFr(), "").replace(getDisKeyTo(),"");
//			}catch(NoClassDefFoundError e){
//				e.printStackTrace();
//			}catch(Exception e){
//				e.printStackTrace();
//				e.printStackTrace();
//			}
//		}
		return result;
	}
	private RunSQL createInsertTxtFromDataRow(String dest, DataRow row, boolean checkPrimary, String ... columns){
		RunSQL run = new TableRunSQLImpl();
		StringBuilder sql = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		if(BasicUtil.isEmpty(dest)){
			throw new SQLException("未指定表");
		}
		StringBuilder param = new StringBuilder();
		if(row.hasPrimaryKeys() && ConfigTable.getBoolean("AUTO_CREATE_PRIMARY_KEY") && BasicUtil.isEmpty(row.get(row.getPrimaryKey()))){
			String pk = row.getPrimaryKey();
			if(null == pk){
				pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY");
			}
			row.put(pk, primaryCreater.createPrimary(dest.replace(getDisKeyFr(), "").replace(getDisKeyTo(), ""), pk, null));
		}
		/*确定需要插入的列*/
		
		List<String> keys = confirmInsertColumns(dest, row, columns);
		if(null == keys || keys.size() == 0){
			throw new SQLException("未指定列");
		}
		sql.append("INSERT INTO ").append(parseTable(dest));
		sql.append("(");
		param.append(") VALUES (");
		
		int size = keys.size();
		for(int i=0; i<size; i++){
			String key = keys.get(i);
			Object value = row.get(key);
			sql.append(getDisKeyFr()).append(key).append(getDisKeyTo());
			if(null != value && value.toString().startsWith("{") && value.toString().endsWith("}")){
				String str = value.toString();
				value = str.substring(1, str.length()-1);
				param.append(value);
			}else{
				param.append("?");
				if("NULL".equals(value)){
					values.add(null);
				}else{
					values.add(value);
				}
			}
			if(i<size-1){
				sql.append(",");
				param.append(",");
			}
		}
		param.append(")");
		sql.append(param);
		run.setBuilder(sql);
		run.addValues(values);
		return run;
	}
	private RunSQL createInsertTxtFromDataSet(String dest, DataSet set, boolean checkPrimary, String ... columns){
		RunSQL run = new TableRunSQLImpl();
		StringBuilder sql = new StringBuilder();

		if(null == set || set.size() ==0){
			throw new SQLException("空数据");
		}
		if(BasicUtil.isEmpty(dest)){
			dest = getDataSource(set);
		}
		if(BasicUtil.isEmpty(dest)){
			dest = getDataSource(set.getRow(0));
		}
		if(BasicUtil.isEmpty(dest)){
			throw new SQLException("未指定表");
		}
		DataRow first = set.getRow(0);
		/*确定需要插入的列*/
		
		List<String> keys = confirmInsertColumns(dest, first, columns);
		if(null == keys || keys.size() == 0){
			throw new SQLException("未指定列");
		}
		sql.append("INSERT INTO ").append(parseTable(dest));
		sql.append("(");
		
		int keySize = keys.size();
		for(int i=0; i<keySize; i++){
			String key = keys.get(i);
			sql.append(getDisKeyFr()).append(key).append(getDisKeyTo());
			if(i<keySize-1){
				sql.append(",");
			}
		}
		sql.append(") VALUES ");
		int dataSize = set.size();
		for(int i=0; i<dataSize; i++){
			DataRow row = set.getRow(i);
			if(null == row){
				continue;
			}
			if(row.hasPrimaryKeys() && ConfigTable.getBoolean("AUTO_CREATE_PRIMARY_KEY") && BasicUtil.isEmpty(row.get(row.getPrimaryKey()))){
				String pk = row.getPrimaryKey();
				if(null == pk){
					pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY");
				}
				row.put(pk, primaryCreater.createPrimary(dest.replace(getDisKeyFr(), "").replace(getDisKeyTo(), ""), pk, null));
			}
			
			sql.append("(");
			for(int j=0; j<keySize; j++){
				Object value = row.get(keys.get(j));
				
				if(null == value || "NULL".equals(value)){
					sql.append("null");
				}else if(value instanceof String){
					String str = value.toString();
					if(str.startsWith("{") && str.endsWith("}")){
						str = str.substring(1, str.length()-1);
					}else{
						str = "'" + str.replace("'", "''") + "'";
					}
					sql.append(str);
				}else if(value instanceof Number || value instanceof Boolean){
					sql.append(value.toString());
				}else{
					sql.append(value.toString());
				}
				if(j<keySize-1){
					sql.append(",");
				}
			}
			sql.append(")");
			if(i<dataSize-1){
				sql.append(",");
			}
		}
		run.setBuilder(sql);
		return run;
	}
	
	private RunSQL createInsertTxtFromEntity(String dest, AnylineEntity entity, boolean checkPrimary, String ... columns){
		RunSQL run = new TableRunSQLImpl();
		StringBuilder sql = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		

		if(null == dest){
			dest = entity.getDataSource();
		}
		if(BasicUtil.isEmpty(dest)){
			throw new SQLException("未指定表");
		}
		
		
		/*确定需要更新的列*/
		List<String> keys = confirmInsertColumns(dest, entity, columns);
		if(null == keys || keys.size() == 0){
			throw new SQLException("未指定列");
		}
		sql.append("INSERT INTO ").append(parseTable(dest));
		sql.append("(");
		int size = keys.size();
		for(int i=0; i<size; i++){
			sql.append(getDisKeyFr()).append(keys.get(i)).append(getDisKeyTo());
			if(i<size-1){
				sql.append(",");
			}
		}
		sql.append(") VALUES (");
		for(int i=0; i<size; i++){
			sql.append("?");
			if(i<size-1){
				sql.append(",");
			}
			values.add(entity.getValueByColumn(keys.get(i)));
		}
		sql.append(")");
		run.setBuilder(sql);
		run.addValues(values);
		return run;
	}

	@Override
	public RunSQL createUpdateTxt(String dest, Object obj, boolean checkPrimary, String ... columns){
		if(null == obj){
			return null;
		}
		if(null == dest){
			dest = getDataSource(obj);
		}
		if(obj instanceof DataRow){
			return createUpdateTxtFromDataRow(dest,(DataRow)obj,checkPrimary, columns);
		}
		if(obj instanceof AnylineEntity){
			return createUpdateTxtFromEntity(dest,(AnylineEntity)obj,checkPrimary, columns);
		}
		return null;
	}

	private RunSQL createUpdateTxtFromEntity(String dest, AnylineEntity entity, boolean checkPrimary, String ... columns){
		RunSQL run = new TableRunSQLImpl();
//		if(null == entity){
		
//			throw new SQLException("更新空数据");
//		}
//		if(null == dest){
//			dest = entity.getDataSource();
//		}
//		if(BasicUtil.isEmpty(dest)){
//			throw new SQLException("未指定表");
//		}
//		List<String> primaryKeys = entity.getPrimaryKeys();
//		if(BasicUtil.isEmpty(true,primaryKeys)){
//			throw new SQLException("未指定主键");
//		}
//		
//		entity.processBeforeSave();	//保存之前预处理
//		
//		StringBuilder sql = new StringBuilder();
//		List<Object> values = new ArrayList<Object>();
//		/*确定需要更新的列*/
//		List<String> keys = confirmUpdateColumns(dest, entity, propertys);
//		/*不更新主键*/
//		for(String key:primaryKeys){
//			keys.remove(key);
//		}
//		
//		if(BasicUtil.isEmpty(true,keys)){
//			throw new SQLException("未指定更新列");
//		}
//
//		/*构造SQL*/
//		sql.append("UPDATE ").append(dest);
//		sql.append(" SET").append(SQLCreater.BR_TAB);
//		int size = keys.size();
//		for(int i=0; i<size; i++){
//			String key = keys.get(i);
//			sql.append(getDisKeyFr()).append(key).append(getDisKeyTo()).append(" = ?").append(SQLCreater.BR_TAB);
//			values.add(entity.getValueByColumn(key));
//			if(i<size-1){
//				sql.append(",");
//			}
//		}
//		//sql.append(SQL.BR);
//		sql.append("\nWHERE 1=1").append(SQLCreater.BR_TAB);
//		for(String primary:primaryKeys){
//			sql.append(" AND ").append(getDisKeyFr()).append(primary).append(getDisKeyTo()).append(" = ?");
//			values.add(entity.getValueByColumn(primary));
//		}
//		entity.processBeforeDisplay();	//显示之前预处理
		return run;
	}
	private RunSQL createUpdateTxtFromDataRow(String dest, DataRow row, boolean checkPrimary, String ... columns){
		RunSQL run = new TableRunSQLImpl();
		StringBuilder sql = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		/*确定需要更新的列*/
		List<String> keys = confirmUpdateColumns(dest, row, columns);
		List<String> primaryKeys = row.getPrimaryKeys();
		if(primaryKeys.size() == 0){
			throw new SQLUpdateException("更新异常:更新条件为空,upate方法不支持更新整表操作.");
		}
		/*不更新主键*/
		for(String pk:primaryKeys){
			keys.remove(pk);
		}
		if(BasicUtil.isEmpty(true,keys)){
			throw new SQLException("未指定更新列");
		}
		/*构造SQL*/
		sql.append("UPDATE ").append(parseTable(dest));
		sql.append(" SET").append(SQLCreater.BR_TAB);
		int size = keys.size();
		for(int i=0; i<size; i++){
			String key = keys.get(i);
			Object value = row.get(key);
			if(null != value && value.toString().startsWith("{") && value.toString().endsWith("}")){
				String str = value.toString();
				value = str.substring(1, str.length()-1);
				sql.append(getDisKeyFr()).append(key).append(getDisKeyTo()).append(" = ").append(value).append(SQLCreater.BR_TAB);
			}else{
				sql.append(getDisKeyFr()).append(key).append(getDisKeyTo()).append(" = ?").append(SQLCreater.BR_TAB);
				if("NULL".equals(value)){
					values.add(null);
				}else{
					values.add(row.get(key));
				}
			}
			if(i<size-1){
				sql.append(",");
			}
		}
		sql.append(SQLCreater.BR);
		sql.append("\nWHERE 1=1").append(SQLCreater.BR_TAB);
		for(String pk:primaryKeys){
			sql.append(" AND ").append(getDisKeyFr()).append(pk).append(getDisKeyTo()).append(" = ?");
			values.add(row.get(pk));
		}
		run.setBuilder(sql);
		run.addValues(values);
		return run;
	}
	/**
	 * 确认需要插入的列
	 * @param row
	 * @param columns
	 * @return
	 */
	private List<String> confirmInsertColumns(String dst, DataRow row, String ... columns){
		List<String> keys = null;/*确定需要插入的列*/
		if(null == row){
			return new ArrayList<String>();
		}
		boolean each = true;//是否需要从row中查找列
		List<String> mastKeys = new ArrayList<String>();		//必须插入列
		List<String> disKeys = new ArrayList<String>();			//必须不插入列
		List<String> factKeys = new ArrayList<String>();		//根据是否空值

		if(null != columns && columns.length>0){
			each = false;
			keys = new ArrayList<String>();
			for(String column:columns){
				if(BasicUtil.isEmpty(column)){
					continue;
				}
				if(column.startsWith("+")){
					column = column.substring(1, column.length());
					mastKeys.add(column);
					each = true;
				}else if(column.startsWith("-")){
					column = column.substring(1, column.length());
					disKeys.add(column);
					each = true;
				}else if(column.startsWith("?")){
					column = column.substring(1, column.length());
					factKeys.add(column);
					each = true;
				}
				keys.add(column);
			}
		}
		if(each){
//			if(!dst.equals(ConfigTable.getString("CLIENT_TRACE_TABLE")) && !dst.contains("CLIENT_TRACE")){
//				ClientTrace client = (ClientTrace)row.getClientTrace();
//				if(null != client){
//					row.put("REG_IP", client.getRemoteIP());
//					row.put("REG_CLIENT_CD", client.getCd());
//				}
//			}
			keys = row.keys();
			//是否插入null及""列
			boolean isInsertNullColumn = ConfigTable.getBoolean("IS_INSERT_NULL_COLUMN",false);
			boolean isInsertEmptyColumn = ConfigTable.getBoolean("IS_INSERT_EMPTY_COLUMN",false);
			int size = keys.size();
			for(int i=size-1;i>=0; i--){
				String key = keys.get(i);
				if(mastKeys.contains(key)){
					//必须插入
					continue;
				}
				if(disKeys.contains(key)){
					keys.remove(key);
					continue;
				}
				
				Object value = row.get(key);
				if(null == value){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}	
					if(!isInsertNullColumn){
						keys.remove(i);
						continue;
					}
				}else if("".equals(value.toString().trim())){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}	
					if(!isInsertEmptyColumn){
						keys.remove(i);
						continue;
					}
				}
				
			}
		}
		return keys;
	}
	/**
	 * 确认需要插入的列 
	 */
	public List<String> confirmInsertColumns(String dst, Object data, String ... columns){
		if(null == data){
			return null;
		}
		if(data instanceof DataRow){
			return confirmInsertColumns(dst, (DataRow)data, columns);
		}else if(data instanceof AnylineEntity){
			return confirmInsertColumns(dst, (AnylineEntity)data, columns);
		}
		return null;
	}
	/**
	 * 确认需要插入的列
	 * @param entity
	 * @param columns
	 * @return
	 */
	private List<String> confirmInsertColumns(String dst, AnylineEntity entity, String ... propertys){
		List<String> keys = null;/*确定需要插入的列*/
		if(null == entity){
			return new ArrayList<String>();
		}
		boolean each = true;//是否需要从row中查找列
		List<String> mastKeys = new ArrayList<String>();		//必须插入列
		List<String> disKeys = new ArrayList<String>();			//必须不插入列
		List<String> factKeys = new ArrayList<String>();		//根据是否空值

		if(null != propertys && propertys.length>0){
			each = false;
			keys = new ArrayList<String>();
			for(String property:propertys){
				if(BasicUtil.isEmpty(property)){
					continue;
				}
				if(property.startsWith("+")){
					property = property.substring(1, property.length());
					String column = entity.getColumnByProperty(property);
					mastKeys.add(column);
					each = true;
				}else if(property.startsWith("-")){
					property = property.substring(1, property.length());
					String column = entity.getColumnByProperty(property);
					disKeys.add(column);
					each = true;
				}else if(property.startsWith("?")){
					property = property.substring(1, property.length());
					String column = entity.getColumnByProperty(property);
					factKeys.add(column);
					each = true;
				}
				keys.add(entity.getColumnByProperty(property));
			}
		}
		if(each){
//			if(!dst.equals(ConfigTable.getString("CLIENT_TRACE_TABLE")) && !dst.contains("CLIENT_TRACE")){
//				ClientTrace client = (ClientTrace)entity.getClientTrace();
//				if(null != client){
//					entity.setRegIp(client.getRemoteIP());
//					entity.setRegClientCd(client.getCd());
//				}
//			}
			keys = entity.getColumns(true, false);
			//是否插入null及""列
			boolean isInsertNullColumn = ConfigTable.getBoolean("IS_INSERT_NULL_COLUMN",false);
			boolean isInsertEmptyColumn = ConfigTable.getBoolean("IS_INSERT_EMPTY_COLUMN",false);
			int size = keys.size();
			for(int i=size-1;i>=0; i--){
				String key = keys.get(i);
				if(mastKeys.contains(key)){
					//必须插入
					continue;
				}
				if(disKeys.contains(key)){
					keys.remove(key);
					continue;
				}
				
				Object value = BeanUtil.getValueByColumn(entity, key);
				if(null == value){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}	
					if(!isInsertNullColumn){
						keys.remove(i);
						continue;
					}
				}else if("".equals(value.toString().trim())){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}	
					if(!isInsertEmptyColumn){
						keys.remove(i);
						continue;
					}
				}
				
			}
		}
		return keys;
	}
	/**
	 * 确认需要更新的列
	 * @param row
	 * @param columns
	 * @return
	 */
	private List<String> confirmUpdateColumns(String dst, DataRow row, String ... columns){
		List<String> keys = null;/*确定需要更新的列*/
		if(null == row){
			return new ArrayList<String>();
		}
		boolean each = true;//是否需要从row中查找列
		List<String> mastKeys = new ArrayList<String>();		//必须更新列
		List<String> disKeys = new ArrayList<String>();			//必须不更新列
		List<String> factKeys = new ArrayList<String>();		//根据是否空值

		if(null != columns && columns.length>0){
			each = false;
			keys = new ArrayList<String>();
			for(String column:columns){
				if(BasicUtil.isEmpty(column)){
					continue;
				}
				if(column.startsWith("+")){
					column = column.substring(1, column.length());
					mastKeys.add(column);
					each = true;
				}else if(column.startsWith("-")){
					column = column.substring(1, column.length());
					disKeys.add(column);
					each = true;
				}else if(column.startsWith("?")){
					column = column.substring(1, column.length());
					factKeys.add(column);
					each = true;
				}
				keys.add(column);
			}
		}
		if(each){
//			if(!dst.equals(ConfigTable.getString("CLIENT_TRACE_TABLE")) && !dst.contains("CLIENT_TRACE")){
//				ClientTrace client = (ClientTrace)row.getClientTrace();
//				if(null != client){
//					row.put("UPT_IP", client.getRemoteIP());
//					row.put("UPT_CLIENT_CD", client.getCd());
//				}
//			}
			keys = row.getUpdateColumns();
			if(keys.size() ==0){
				keys = row.keys();
			}
			//是否更新null及""列
			boolean isUpdateNullColumn = ConfigTable.getBoolean("IS_UPDATE_NULL_COLUMN",false);
			boolean isUpdateEmptyColumn = ConfigTable.getBoolean("IS_UPDATE_EMPTY_COLUMN",false);
			int size = keys.size();
			for(int i=size-1;i>=0; i--){
				String key = keys.get(i);
				if(mastKeys.contains(key)){
					//必须更新
					continue;
				}
				if(disKeys.contains(key)){
					keys.remove(key);
					continue;
				}
				
				Object value = row.get(key);
				if(null == value){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}	
					if(!isUpdateNullColumn){
						keys.remove(i);
						continue;
					}
				}else if("".equals(value.toString().trim())){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}	
					if(!isUpdateEmptyColumn){
						keys.remove(i);
						continue;
					}
				}
				
			}
		}
		return keys;
	}
	/**
	 * 确认需要更新的列
	 * @param entity
	 * @param columns
	 * @return
	 */
	private List<String> confirmUpdateColumns(String dst, AnylineEntity entity, String ... propertys){
		List<String> keys = null;/*确定需要更新的列*/
		if(null == entity){
			return new ArrayList<String>();
		}
		boolean each = true;//是否需要从row中查找列
		List<String> mastKeys = new ArrayList<String>();		//必须更新列
		List<String> disKeys = new ArrayList<String>();			//必须不更新列
		List<String> factKeys = new ArrayList<String>();		//根据是否空值

		if(null != propertys && propertys.length>0){
			each = false;
			keys = new ArrayList<String>();
			for(String property:propertys){
				if(BasicUtil.isEmpty(property)){
					continue;
				}
				if(property.startsWith("+")){
					property = property.substring(1, property.length());
					String column = entity.getColumnByProperty(property);
					mastKeys.add(column);
					each = true;
				}else if(property.startsWith("-")){
					property = property.substring(1, property.length());
					String column = entity.getColumnByProperty(property);
					disKeys.add(column);
					each = true;
				}else if(property.startsWith("?")){
					property = property.substring(1, property.length());
					String column = entity.getColumnByProperty(property);
					factKeys.add(column);
					each = true;
				}
				keys.add(entity.getColumnByProperty(property));
			}
		}
		if(each){
//			if(!dst.equals(ConfigTable.getString("CLIENT_TRACE_TABLE")) && !dst.contains("CLIENT_TRACE")){
//				ClientTrace client = (ClientTrace)entity.getClientTrace();
//				if(null != client){
//					entity.setUptIp(client.getRemoteIP());
//					entity.setUptClientCd(client.getCd());
//				}
//			}
			keys = entity.getColumns(false, true);
			//是否更新null及""列
			boolean isUpdateNullColumn = ConfigTable.getBoolean("IS_UPDATE_NULL_COLUMN",false);
			boolean isUpdateEmptyColumn = ConfigTable.getBoolean("IS_UPDATE_EMPTY_COLUMN",false);
			int size = keys.size();
			for(int i=size-1;i>=0; i--){
				String key = keys.get(i);
				if(mastKeys.contains(key)){
					//必须更新
					continue;
				}
				if(disKeys.contains(key)){
					keys.remove(key);
					continue;
				}
				
				Object value = BeanUtil.getValueByColumn(entity, key);
				if(null == value){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}	
					if(!isUpdateNullColumn){
						keys.remove(i);
						continue;
					}
				}else if("".equals(value.toString().trim())){
					if(factKeys.contains(key)){
						keys.remove(key);
						continue;
					}	
					if(!isUpdateEmptyColumn){
						keys.remove(i);
						continue;
					}
				}
				
			}
		}
		return keys;
	}
	public String parseTable(String table){
		if(null == table){
			return table;
		}
		table = table.replace(getDisKeyFr(), "").replace(getDisKeyTo(), "");
		if(table.contains(".")){
			String tmps[] = table.split("\\.");
			table = getDisKeyFr() + tmps[0] + getDisKeyTo() + "." + getDisKeyFr() + tmps[1] + getDisKeyTo();
		}else{
			table = getDisKeyFr() + table + getDisKeyTo();
		}
		return table;
	}
}
