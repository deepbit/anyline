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


package org.anyline.service;

import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.anyline.config.db.Procedure;
import org.anyline.config.http.ConfigStore;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;

public interface AnylineService{
	/**
	 * 按条件查询
	 * @param src			数据源(表或自定义SQL或SELECT语句)
	 * @param configs		封装来自于http的查询条件
	 * @param conditions	固定查询条件  
	 * 			原生SQL(AND GROUP ORDER)
	 * 			{原生}
	 * 			[+]CD:1
	 * 			[+]CD:
	 * 			[+]CD:null
	 * 			[+]CD:NULL
	 * 			
	 * @return
	 */
	public DataSet query(DataSource ds, String src, ConfigStore configs, String ... conditions);
	public DataSet query(String src, ConfigStore configs, String ... conditions);
	public DataSet query(DataSource ds, String src, String ... conditions);
	public DataSet query(String src, String ... conditions);
	public DataSet query(DataSource ds, String src, int fr, int to, String ... conditions);
	public DataSet query(String src, int fr, int to, String ... conditions);
	/**
	 * 如果二级缓存开启 会从二级缓存中提取数据
	 * @param ds
	 * @param cache	对应ehcache缓存配置文件 中的cache.name
	 * @param src
	 * @param configs
	 * @param conditions
	 * @return
	 */
	public DataSet cache(DataSource ds, String cache, String src, ConfigStore configs, String ... conditions);
	public DataSet cache(String cache, String src, ConfigStore configs, String ... conditions);
	public DataSet cache(DataSource ds, String cache, String src, String ... conditions);
	public DataSet cache(String cache, String src, String ... conditions);
	public DataSet cache(DataSource ds, String cache, String src, int fr, int to, String ... conditions);
	public DataSet cache(String cache, String src, int fr, int to, String ... conditions);
	/**
	 * 只用一级缓存 忽略二级缓存
	 * @param ds
	 * @param cache
	 * @param src
	 * @param configs
	 * @param conditions
	 * @return
	 */
	public DataSet cacheL1(DataSource ds, String cache, String src, ConfigStore configs, String ... conditions);
	public DataSet cacheL1(String cache, String src, ConfigStore configs, String ... conditions);
	public DataSet cacheL1(DataSource ds, String cache, String src, String ... conditions);
	public DataSet cacheL1(String cache, String src, String ... conditions);
	public DataSet cacheL1(DataSource ds, String cache, String src, int fr, int to, String ... conditions);
	public DataSet cacheL1(String cache, String src, int fr, int to, String ... conditions);

	
	public DataRow queryRow(DataSource ds, String src, ConfigStore configs, String ... conditions);
	public DataRow queryRow(String src, ConfigStore configs, String ... conditions);
	public DataRow queryRow(DataSource ds, String src, String ... conditions);
	public DataRow queryRow(String src, String ... conditions);

	public DataRow cacheRow(DataSource ds, String cache, String src, ConfigStore configs, String ... conditions);
	public DataRow cacheRow(String cache, String src, ConfigStore configs, String ... conditions);
	public DataRow cacheRow(DataSource ds, String cache, String src, String ... conditions);
	public DataRow cacheRow(String cache, String src, String ... conditions);
	

	/**
	 * 删除缓存 参数保持与查询参数完全一致
	 * @param cache
	 * @param src
	 * @param configs
	 * @param conditions
	 * @return
	 */
	public boolean removeCache(String channel, String src, ConfigStore configs, String ... conditions);
	public boolean removeCache(String channel, String src, String ... conditions);
	public boolean removeCache(String channel, String src, int fr, int to, String ... conditions);
	/**
	 * 清空缓存
	 * @param channel
	 * @return
	 */
	public boolean clearCache(String channel);
	
	/**
	 * 是否存在
	 * @param src
	 * @param configs
	 * @param conditions
	 * @return
	 */
	public boolean exists(DataSource ds, String src, ConfigStore configs, String ... conditions);
	public boolean exists(String src, ConfigStore configs, String ... conditions);
	public boolean exists(DataSource ds, String src, String ... conditions);
	public boolean exists(String src, String ... conditions);
	public boolean exists(String src, DataRow row);
	public boolean exists(DataRow row);
	
	public int count(DataSource ds, String src, ConfigStore configs, String ... conditions);
	public int count(String src, ConfigStore configs, String ... conditions);
	public int count(DataSource ds, String src, String ... conditions);
	public int count(String src, String ... conditions);
	
	
	
	/**
	 * 更新记录
	 * @param row		
	 * 			需要更新的数据
	 * @param columns	
	 * 			需要更新的列
	 * @param dest	
	 * 			表
	 * @return
	 */
	public int update(DataSource ds, String dest, Object data, String ... columns);
	public int update(String dest, Object data, String ... columns);
	public int update(DataSource ds, Object data, String ... columns);
	public int update(Object data, String ... columns);
	public int update(DataSource ds, String dest, ConfigStore configs, String ... conditions);
	
	public int update(boolean sync, DataSource ds, String dest, Object data, String ... columns);
	public int update(boolean sync, String dest, Object data, String ... columns);
	public int update(boolean sync, DataSource ds, Object data, String ... columns);
	public int update(boolean sync, Object data, String ... columns);
	/**
	 * 保存(insert|update)
	 * @param data
	 * @param checkPriamry
	 * @param columns
	 * @param dest 表
	 * @return
	 */
	public int save(DataSource ds, String dest, Object data, boolean checkPriamry, String ... columns);
	public int save(String dest, Object data, boolean checkPriamry, String ... columns);
	public int save(DataSource ds, Object data, boolean checkPriamry, String ... columns);
	public int save(Object data, boolean checkPriamry, String ... columns);
	public int save(DataSource ds, Object data, String ... columns);
	public int save(String dest, Object data, String ... columns);
	public int save(Object data, String ... columns);
	public int save(DataSource ds, String dest, Object data, String ... columns);
//
	public int save(boolean sync, DataSource ds, String dest, Object data, boolean checkPriamry, String ... columns);
	public int save(boolean sync, String dest, Object data, boolean checkPriamry, String ... columns);
	public int save(boolean sync, DataSource ds, Object data, boolean checkPriamry, String ... columns);
	public int save(boolean sync, Object data, boolean checkPriamry, String ... columns);
	public int save(boolean sync, DataSource ds, Object data, String ... columns);
	public int save(boolean sync, String dest, Object data, String ... columns);
	public int save(boolean sync, Object data, String ... columns);
	public int save(boolean sync, DataSource ds, String dest, Object data, String ... columns);


	public int insert(DataSource ds, String dest, Object data, boolean checkPriamry, String ... columns);
	public int insert(String dest, Object data, boolean checkPriamry, String ... columns);
	public int insert(DataSource ds, Object data, boolean checkPriamry, String ... columns);
	public int insert(Object data, boolean checkPriamry, String ... columns);
	public int insert(DataSource ds, Object data, String ... columns);
	public int insert(String dest, Object data, String ... columns);
	public int insert(Object data, String ... columns);
	public int insert(DataSource ds, String dest, Object data, String ... columns);


	public int batchInsert(DataSource ds, String dest, Object data, boolean checkPriamry, String ... columns);
	public int batchInsert(String dest, Object data, boolean checkPriamry, String ... columns);
	public int batchInsert(DataSource ds, Object data, boolean checkPriamry, String ... columns);
	public int batchInsert(Object data, boolean checkPriamry, String ... columns);
	public int batchInsert(DataSource ds, Object data, String ... columns);
	public int batchInsert(String dest, Object data, String ... columns);
	public int batchInsert(Object data, String ... columns);
	public int batchInsert(DataSource ds, String dest, Object data, String ... columns);
	/**
	 * save insert区别
	 * 操作单个对象时没有区别
	 * 在操作集合时区别:
	 * save会循环操作数据库每次都会判断insert|update
	 * save 集合中的数据可以是不同的表不同的结构 
	 * insert 集合中的数据必须保存到相同的表,结构必须相同
	 * insert 将一次性插入多条数据整个过程有可能只操作一次数据库  并 不考虑update情况 对于大批量数据来说 性能是主要优势
	 * 
	 */
	
	/**
	 * 执行
	 * @param src
	 * @param configs
	 * @param conditions
	 * @return
	 */
	public int execute(DataSource ds, String src, ConfigStore configs, String ... conditions);
	public int execute(String src, ConfigStore configs, String ... conditions);
	public int execute(DataSource ds, String src, String ... conditions);
	public int execute(String src, String ... conditions);
	/**
	 * 执行存储过程
	 * @param procedure
	 * @param inputs
	 * @param outputs
	 * @return
	 */
	public boolean executeProcedure(DataSource ds, String procedure, String... inputs);
	public boolean executeProcedure(String procedure, String... inputs);
	public boolean executeProcedure(DataSource ds, Procedure procedure);
	public boolean executeProcedure(Procedure procedure);
	/**
	 * 根据存储过程查询
	 * @param procedure
	 * @param inputs
	 * @return
	 */
	public DataSet queryProcedure(DataSource ds, String procedure, String ... inputs);
	public DataSet queryProcedure(String procedure, String ... inputs);
	public DataSet queryProcedure(DataSource ds, Procedure procedure);
	public DataSet queryProcedure(Procedure procedure);
	
	/**
	 * 删除 根据主键删除
	 * @param ds
	 * @param dest
	 * @param data
	 * @return
	 */
	public int delete(DataSource ds, String dest, Object data);
	public int delete(DataSource ds, Object data);
	public int delete(String dest, Object data);
	public int delete(Object data);
	
	public int delete(DataSource ds, String table, String key, Collection<Object> values);
	public int delete(String table, String key, Collection<Object> values);
	public int delete(DataSource ds, String table, String key, String ... values);
	public int delete(String table, String key, String ... values);
}