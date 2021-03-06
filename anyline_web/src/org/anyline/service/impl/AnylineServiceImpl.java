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


package org.anyline.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import net.sf.ehcache.Element;

import org.anyline.cache.CacheUtil;
import org.anyline.cache.PageLazyStore;
import org.anyline.config.db.Procedure;
import org.anyline.config.db.SQL;
import org.anyline.config.db.impl.PageNaviImpl;
import org.anyline.config.db.impl.ProcedureImpl;
import org.anyline.config.db.impl.SQLStoreImpl;
import org.anyline.config.db.sql.auto.impl.TableSQLImpl;
import org.anyline.config.db.sql.auto.impl.TextSQLImpl;
import org.anyline.config.http.ConfigStore;
import org.anyline.config.http.impl.ConfigStoreImpl;
import org.anyline.dao.AnylineDao;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.exception.SQLQueryException;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.regular.RegularUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("anylineService")
public class AnylineServiceImpl implements AnylineService {
	protected Logger log = Logger.getLogger(this.getClass());
	@Autowired(required = false)
	@Qualifier("anylineDao")
	protected AnylineDao dao;
	
	
	public AnylineDao getDao() {
		return dao;
	}
	private PageNavi setPageLazy(String src, ConfigStore configs, String ... conditions){
		PageNavi navi =  null;
		String lazyKey = null;
		if(null != configs){
			navi = configs.getPageNavi();
			if(null != navi && navi.isLazy()){
				lazyKey = CacheUtil.createCacheElementKey(false, false, src, configs, conditions);
				navi.setLazyKey(lazyKey);
				int total = PageLazyStore.getTotal(lazyKey, navi.getLazyPeriod());
				navi.setTotalRow(total);
			}
		}
		return navi;
	}
	private DataSet queryFromDao(DataSource ds, String src, ConfigStore configs, String... conditions){
		DataSet set = null;
		if(ConfigTable.isSQLDebug()){
			log.warn("[解析SQL] [src:" + src + "]");
		}
		//conditions = parseConditions(conditions);
		try {
			setPageLazy(src, configs, conditions);
			SQL sql = createSQL(src);
			set = dao.query(ds, sql, configs, conditions);
		} catch (Exception e) {
			set = new DataSet();
			set.setException(e);
			if(ConfigTable.isDebug()){
				e.printStackTrace();
			}
			log.error("QUERY ERROR:"+e);
		}
		return set;
	}
	/**
	 * 按条件查询
	 * @param ds 数据源
	 * @param src 表｜视图｜函数｜自定义SQL
	 * @param configs http参数封装
	 * @param conditions 固定查询条件
	 * @return
	 */
	@Override
	public DataSet query(DataSource ds, String src, ConfigStore configs, String... conditions) {
		DataSet set = queryFromDao(ds,src, configs, conditions);
        return set;
		
	}
	/**
	 * 解析SQL中指定的主键table(col1,col2)[pk1,pk2]
	 * @param src
	 * @param pks
	 * @return
	 */
	private String parsePrimaryKey(String src, List<String> pks){
		if(src.endsWith("]")){
			int fr = src.lastIndexOf("[");
			int to = src.lastIndexOf("]");
			if(fr != -1){
				String pkstr = src.substring(fr+1,to);
				src = src.substring(0, fr);
				String[] tmps = pkstr.split(",");
				for(String tmp:tmps){
					pks.add(tmp);
					if(ConfigTable.isSQLDebug()){
						log.warn("[解析SQL主键] [KEY:" + tmp + "]");
					}
				}
			}
		}
		return src;
	}
	private synchronized SQL createSQL(String src){
		SQL sql = null;
		src = src.trim();
		List<String> pks = new ArrayList<String>();
		//解析主键
		if (src.startsWith("{") && src.endsWith("}")) {
			if(ConfigTable.isSQLDebug()){
				log.warn("[解析SQL类型] [类型:{JAVA定义}] [src:" + src + "]");
			}
			src = src.substring(1,src.length()-1);
			src = parsePrimaryKey(src, pks);
			sql = new TextSQLImpl(src);
		} else {
			src = parsePrimaryKey(src, pks);
			if (src.toUpperCase().trim().startsWith("SELECT")
				|| src.toUpperCase().trim().startsWith("DELETE")
				|| src.toUpperCase().trim().startsWith("INSERT")
				|| src.toUpperCase().trim().startsWith("UPDATE")) {
				if(ConfigTable.isSQLDebug()){
					log.warn("[解析SQL类型] [类型:JAVA定义] [src:" + src + "]");
				}
				sql = new TextSQLImpl(src);
			}else if (RegularUtil.match(src, SQL.XML_SQL_ID_STYLE)) {
				/* XML定义 */
				if(ConfigTable.isSQLDebug()){
					log.warn("[解析SQL类型] [类型:XML定义] [src:" + src + "]");
				}
				sql = SQLStoreImpl.parseSQL(src);
			} else {
				/* 自动生成 */
				if(ConfigTable.isSQLDebug()){
					log.warn("[解析SQL类型] [类型:auto] [src:" + src + "]");
				}
				sql = new TableSQLImpl();
				sql.setDataSource(src);
			}
		}
		if(null != sql){
			sql.setPrimaryKey(pks);
		}
		return sql;
	}
	@Override
	public DataSet query(String src, ConfigStore configs, String... conditions) {
		return query(null, src, configs, conditions);
	}

	@Override
	public DataSet query(DataSource ds, String src, String... conditions) {
		return query(ds, src, null, conditions);
	}

	@Override
	public DataSet query(String src, String... conditions) {
		return query(null, src, null, conditions);
	}

	@Override
	public DataSet query(DataSource ds, String src, int fr, int to, String... conditions) {
		PageNaviImpl navi = new PageNaviImpl();
		navi.setFirstRow(fr);
		navi.setLastRow(to);
		navi.setCalType(1);
		navi.setTotalRow(to-fr+1);
		ConfigStore configs = new ConfigStoreImpl();
		configs.setPageNavi(navi);
		return query(ds, src, configs, conditions);
	}

	@Override
	public DataSet query(String src, int fr, int to, String... conditions) {
		return query(null, src, fr, to, conditions);
	}
	private DataSet queryFromCacheL2(DataSource ds, String cache2, DataSet l1, String src, ConfigStore configs, String ... conditions){
		if(ConfigTable.isDebug()){
			log.warn("[cache from L2][cache:"+cache2+"][src:"+src+"]");
		}
		DataSet set = new DataSet();
		SQL sql = createSQL(src);
		if(sql.hasPrimaryKeys()){
			//必须指定主键
			List<String> pks = sql.getPrimaryKeys();
			int size = l1.size();
			for(int i=0; i<size; i++){
				DataRow row = l1.getRow(i);
				DataRow cacheRow = null;
				row.setPrimaryKey(pks);
				String cache2Key = CacheUtil.crateCachePrimaryKey(sql.getTable(), row);
				Element cacheRowElement = CacheUtil.getElement(cache2, cache2Key);
				if(null != cacheRowElement){
					Object cacheRowValue = cacheRowElement.getObjectValue();
					cacheRow = (DataRow)cacheRowValue;
					cacheRow.setIsFromCache(true);
				}else{
					if(null == configs){
						configs = new ConfigStoreImpl();
					}
					for(String pk:pks){
						configs.addCondition(pk, row.get(pk), true);
					}
					cacheRow = queryRow(ds, src, configs, conditions);
		        	CacheUtil.put(cache2, cache2Key, cacheRow);   
				}
				set.add(cacheRow);
			}
		}else{
			throw new SQLQueryException("未指定缓存主键 src="+src);
		}
		return set;
	}
	private DataSet queryFromCacheL1(DataSource ds, boolean isUseCacheL2, String cache, String src, ConfigStore configs, String ... conditions){
		if(ConfigTable.isDebug()){
			log.warn("[cache from L1][cache:"+cache+"][src:"+src+"]");
		}
		DataSet set = null;
		String key = "SET:";
		String cache2 = "anyline_cache_l2";
		isUseCacheL2 = isUseCacheL2 && ConfigTable.getBoolean("IS_USE_CACHE_L2");
		if(cache.contains(">")){
			String tmp[] = cache.split(">");
			cache = tmp[0];
			cache2 = tmp[1];
		}
		if(cache.contains(":")){
			String[] tmp = cache.split(":");
			cache = tmp[0];
			key += tmp[1]+":";
		}
		key += CacheUtil.createCacheElementKey(true, true, src, configs, conditions);
		//一级缓存数据
		Element element = CacheUtil.getElement(cache, key);
		SQL sql = createSQL(src);
        if(null != element){
        	Object value = element.getObjectValue();
        	if(null != value && value instanceof DataSet){
            	set = (DataSet)value;
            	set.setIsFromCache(true);
        	}else{
        		log.error("[缓存设置错误,检查配置文件是否有重复cache.name 或Java代码调用中cache.name混淆][channel:"+cache+"]");
        	}
//        	//开启新线程提前更新缓存(90%时间)
        	long age =  (System.currentTimeMillis()- element.getCreationTime())/1000;
        	final int _max = element.getTimeToLive();
        	if(age > _max*0.9){
        		if(ConfigTable.isDebug()){
        			log.warn("[缓存即将到期提前刷新][src:"+src+"] [生存:"  + age + "/" + _max + "]");
        		}
        		final String _key = key;
        		final String _cache = cache;
        		final SQL _sql = sql;
        		final ConfigStore _configs = configs; 
        		final String[] _conditions = conditions;
        		final DataSource _ds = ds;
            	new Thread(new Runnable(){
            		public void run(){
                		CacheUtil.start(_key, _max/10);
            			DataSet newSet = dao.query(_ds, _sql, _configs, _conditions);
                    	CacheUtil.put(_cache, _key, newSet);          	
                		CacheUtil.stop(_key, _max/10);
            		}
            	}).start();	
        	}
        	
        }else{
        	//从数据库中提取数据填充一级缓存
			if(isUseCacheL2 && sql.hasPrimaryKeys() ){
				//如果二级缓存已开启, 一级缓存只查主键
				sql.setFetchKey(sql.getPrimaryKeys());
			}

			setPageLazy(src, configs, conditions);
			set = dao.query(ds, sql, configs, conditions);
			
        	CacheUtil.put(cache, key, set);        	
        }
        
        //二级缓存数据
    	if(isUseCacheL2  && sql.hasPrimaryKeys()){
			set = queryFromCacheL2(ds, cache2, set, src, configs, conditions);	
    	}
		return set;
	}
	public DataSet cache(DataSource ds, String cache, String src, ConfigStore configs, String ... conditions){
		DataSet set = null;
		if(null == cache){
			set = query(ds, src, configs, conditions);
		}else{
			if(ConfigTable.getBoolean("IS_USE_CACHE") || ConfigTable.getBoolean("IS_USE_CACHE_L1")){
				set = queryFromCacheL1(ds, true, cache, src, configs, conditions);
			}else{
				set = query(ds, src, configs, conditions);
			}
		}
		return set;
	}
	public DataSet cache(String cache, String src, ConfigStore configs, String ... conditions){
		return cache(null, cache, src, configs, conditions);
	}
	public DataSet cache(DataSource ds, String cache, String src, String ... conditions){
		return cache(ds, cache, src, null, conditions);
	}
	public DataSet cache(String cache, String src, String ... conditions){
		return cache(null, cache, src, null, conditions);
	}
	public DataSet cache(DataSource ds, String cache, String src, int fr, int to, String ... conditions){
		PageNaviImpl navi = new PageNaviImpl();
		navi.setFirstRow(fr);
		navi.setLastRow(to);
		navi.setCalType(1);
		navi.setTotalRow(to-fr+1);
		ConfigStore configs = new ConfigStoreImpl();
		configs.setPageNavi(navi);
		return cache(ds, cache, src, configs, conditions);
	}
	public DataSet cache(String cache, String src, int fr, int to, String ... conditions){
		return cache(null, cache, src, fr, to, conditions);
	}

	public DataSet cacheL1(DataSource ds, String cache, String src, ConfigStore configs, String ... conditions){
		DataSet set = null;
		if(null == cache){
			set = query(ds, src, configs, conditions);
		}else{
			if(ConfigTable.getBoolean("IS_USE_CACHE") || ConfigTable.getBoolean("IS_USE_CACHE_L1")){
				set = queryFromCacheL1(ds, false,  cache, src, configs, conditions);
			}else{
				set = query(ds, src, configs, conditions);
			}
		}
		return set;
	}
	public DataSet cacheL1(String cache, String src, ConfigStore configs, String ... conditions){
		return cacheL1(null, cache, src, configs, conditions);
	}
	public DataSet cacheL1(DataSource ds, String cache, String src, String ... conditions){
		return cacheL1(ds, cache, src, null, conditions);
	}
	public DataSet cacheL1(String cache, String src, String ... conditions){
		return cacheL1(null, cache, src, null, conditions);
	}
	public DataSet cacheL1(DataSource ds, String cache, String src, int fr, int to, String ... conditions){
		PageNaviImpl navi = new PageNaviImpl();
		navi.setFirstRow(fr);
		navi.setLastRow(to);
		navi.setCalType(1);
		navi.setTotalRow(to-fr+1);
		ConfigStore configs = new ConfigStoreImpl();
		configs.setPageNavi(navi);
		return cacheL1(ds, cache, src, configs, conditions);
	}
	public DataSet cacheL1(String cache, String src, int fr, int to, String ... conditions){
		return cacheL1(null, cache, src, fr, to, conditions);
	}
	

	@Override
	public DataRow queryRow(DataSource ds, String src, ConfigStore store, String... conditions) {
		PageNaviImpl navi = new PageNaviImpl();
		navi.setFirstRow(0);
		navi.setLastRow(0);
		navi.setCalType(1);
		if (null == store) {
			store = new ConfigStoreImpl();
		}
		store.setPageNavi(navi);
		DataSet set = query(ds, src, store, conditions);
		if (null != set && set.size() > 0) {
			DataRow row = set.getRow(0);
			return row;
		}
		return null;
	}

	@Override
	public DataRow queryRow(String src, ConfigStore configs, String... conditions) {
		return queryRow(null, src, configs, conditions);
	}

	@Override
	public DataRow queryRow(DataSource ds, String src, String... conditions) {
		return queryRow(ds, src, null, conditions);
	}

	@Override
	public DataRow queryRow(String src, String... conditions) {
		return queryRow(null, src, null, conditions);
	}

	public DataRow cacheRow(DataSource ds, String cache, String src, ConfigStore configs, String ... conditions){
		//是否启动缓存
		if(!ConfigTable.getBoolean("IS_USE_CACHE") || null == cache){
			return queryRow(ds, src, configs, conditions);
		}
		PageNaviImpl navi = new PageNaviImpl();
		navi.setFirstRow(0);
		navi.setLastRow(0);
		navi.setCalType(1);
		if (null == configs) {
			configs = new ConfigStoreImpl();
		} 
		configs.setPageNavi(navi);
		
		DataRow row = null;
		String key = "ROW:";

		if(cache.contains(":")){
			String[] tmp = cache.split(":");
			cache = tmp[0];
			key += tmp[1]+":";
		}
		key +=  CacheUtil.createCacheElementKey(true, true, src, configs, conditions);
		Element element = CacheUtil.getElement(cache, key);
        if(null != element){
            Object value = element.getObjectValue();
        	if(value instanceof DataRow){
            	row = (DataRow)value;
            	row.setIsFromCache(true);
            	return row;	
        	}else{
        		log.error("[缓存设置错误,检查配置文件是否有重复cache.name 或Java代码调用中cache.name混淆][channel:"+cache+"]");
        	}
        }
        // 调用实际 的方法
        row = queryRow(ds, src, configs, conditions);
        if(null != row){
	    	CacheUtil.put(cache, key, row);
        }
		return row;
	}
	public DataRow cacheRow(String cache, String src, ConfigStore configs, String ... conditions){
		return cacheRow(null, cache, src, configs, conditions);
	}
	public DataRow cacheRow(DataSource ds, String cache, String src, String ... conditions){
		return cacheRow(ds, cache, src, null, conditions);
	}
	public DataRow cacheRow(String cache, String src, String ... conditions){
		return cacheRow(null, cache, src, null, conditions);
	}
	
	
	/**
	 * 删除缓存 参数保持与查询参数完全一致
	 * @param cache
	 * @param src
	 * @param configs
	 * @param conditions
	 * @return
	 */
	public boolean removeCache(String channel, String src, ConfigStore configs, String ... conditions){
		conditions = BasicUtil.compressionSpace(conditions);
		String key = CacheUtil.createCacheElementKey(true, true, src, configs, conditions);
		CacheUtil.remove(channel, "SET:" + key);
		CacheUtil.remove(channel, "ROW:" + key);
		return true;
	}
	public boolean removeCache(String channel, String src, String ... conditions){
		return removeCache(channel, src, null, conditions);
	}
	public boolean removeCache(String channel, String src, int fr, int to, String ... conditions){
		PageNaviImpl navi = new PageNaviImpl();
		navi.setFirstRow(fr);
		navi.setLastRow(to);
		navi.setCalType(1);
		navi.setTotalRow(to-fr+1);
		ConfigStore configs = new ConfigStoreImpl();
		configs.setPageNavi(navi);
		return removeCache(channel, src, configs, conditions);
	}
	/**
	 * 清空缓存
	 * @param cache
	 * @return
	 */
	public boolean clearCache(String channel){
		return CacheUtil.clear(channel);
	}
	

	 /**
	 * 检查唯一性
	 * @param src
	 * @param configs
	 * @param conditions
	 * @return
	 */

	public boolean exists(DataSource ds, String src, ConfigStore configs, String ... conditions){
		boolean result = false;
		//conditions = parseConditions(conditions);
		SQL sql = createSQL(src);
		result = dao.exists(ds, sql, configs, conditions);
		return result;
	}
	public boolean exists(String src, ConfigStore configs, String ... conditions){
		return exists(null, src, configs, conditions);
	}
	public boolean exists(DataSource ds, String src, String ... conditions){
		return exists(ds, src, null, conditions);
	}
	public boolean exists(String src, String ... conditions){
		return exists(null, src, null, conditions);
	}
	/**
	 * 只根据主键判断
	 */
	public boolean exists(String src, DataRow row){
		if(null != row){
			List<String> keys = row.getPrimaryKeys();
			if(null != keys){
				String[] conditions = new String[keys.size()];
				int idx = 0;
				for(String key: keys){
					conditions[idx++] = key + ":" + row.getString(key);
				}
				return exists(null, src, null, conditions);
			}
			return false;
		}else{
			return false;
		}
	}
	public boolean exists(DataRow row){
		return exists(null, row);
	}
	
	public int count(DataSource ds, String src, ConfigStore configs, String ... conditions){
		int count = -1;
		try {
			//conditions = parseConditions(conditions);
			SQL sql = createSQL(src);
			count = dao.count(ds, sql, configs, conditions);
		} catch (Exception e) {
			if(ConfigTable.isDebug()){
				e.printStackTrace();
			}
			log.error("COUNT ERROR:"+e);
		}
		return count;
	}
	public int count(String src, ConfigStore configs, String ... conditions){
		return count(null, src, configs, conditions);
	}
	public int count(DataSource ds, String src, String ... conditions){
		return count(ds, src, null, conditions);
	}
	public int count(String src, String ... conditions){
		return count(null, src, null, conditions);
	}
	
	
	/**
	 * 更新记录
	 * 
	 * @param row
	 *            需要更新的数据
	 * @param columns
	 *            需要更新的列
	 * @return
	 */
	@Override
	public int update(boolean sync, DataSource ds, String dest, Object data, String... columns) {
		final String cols[] = BasicUtil.compressionSpace(columns);
		final DataSource _ds = ds;
		final String _dest = dest;
		final Object _data = data;
		if(sync){
			new Thread(new Runnable(){
				@Override
				public void run() {
					dao.update(_ds, _dest, _data, cols);
				}
			}).start();
			return 0;
		}else{
			return dao.update(ds, dest, data, cols);
		}
	}
	@Override
	public int update(DataSource ds, String dest, Object data, String... columns) {
		columns = BasicUtil.compressionSpace(columns);
		return dao.update(ds, dest, data, columns);
	}

	@Override
	public int update(String dest, Object data, String... columns) {
		return update(null, dest, data, columns);
	}
	@Override
	public int update(boolean sync, String dest, Object data, String... columns) {
		return update(false,null, dest, data, columns);
	}

	@Override
	public int update(DataSource ds, Object data, String... columns) {
		return update(ds, null, data, columns);
	}
	@Override
	public int update(boolean sync, DataSource ds, Object data, String... columns) {
		return update(sync, ds, null, data, columns);
	}

	@Override
	public int update(Object data, String... columns) {
		return update(null, null, data, columns);
	}
	@Override
	public int update(boolean sync, Object data, String... columns) {
		return update(sync, null, null, data, columns);
	}
	@Override
	public int update(DataSource ds,String dest, ConfigStore configs, String... conditions) {
		
        return 0;
		
	}
	@Override
	public int save(boolean sync, DataSource ds, String dest, Object data, boolean checkPrimary, String... columns) {
		if(sync){
			final DataSource _ds = ds;
			final String _dest = dest;
			final Object _data = data;
			final boolean _chk = checkPrimary;
			final String[] cols = columns;
			new Thread(new Runnable(){
				@Override
				public void run() {
					save(_ds, _dest, _data, _chk, cols);
				}
				
			}).start();
			return 0;
		}else{
			return save(ds, dest, data, checkPrimary, columns);
		}
		
	}
	@Override
	public int save(DataSource ds, String dest, Object data, boolean checkPrimary, String... columns) {
		if (null == data) {
			return 0;
		}
		if (data instanceof Collection) {
			Collection datas = (Collection) data;
			int cnt = 0;
			for (Object obj : datas) {
				cnt += save(ds, dest, obj, checkPrimary, columns);
			}
			return cnt;
		}
		return saveObject(ds, dest, data, checkPrimary, columns);
	}

	@Override
	public int save(String dest, Object data, boolean checkPrimary, String... columns) {
		return save(null, dest, data, checkPrimary, columns);
	}
	@Override
	public int save(boolean sync, String dest, Object data, boolean checkPrimary, String... columns) {
		return save(sync,null, dest, data, checkPrimary, columns);
	}

	@Override
	public int save(DataSource ds, Object data, boolean checkPrimary, String... columns) {
		return save(ds, null, data, checkPrimary, columns);
	}
	@Override
	public int save(boolean sync, DataSource ds, Object data, boolean checkPrimary, String... columns) {
		return save(sync, ds, null, data, checkPrimary, columns);
	}

	@Override
	public int save(Object data, boolean checkPrimary, String... columns) {
		return save(null, null, data, checkPrimary, columns);
	}
	@Override
	public int save(boolean sync, Object data, boolean checkPrimary, String... columns) {
		return save(sync, null, null, data, checkPrimary, columns);
	}

	@Override
	public int save(DataSource ds, Object data, String... columns) {
		return save(ds, null, data, false, columns);
	}

	@Override
	public int save(boolean sync, DataSource ds, Object data, String... columns) {
		return save(sync, ds, null, data, false, columns);
	}

	@Override
	public int save(String dest, Object data, String... columns) {
		return save(null, dest, data, false, columns);
	}
	@Override
	public int save(boolean sync, String dest, Object data, String... columns) {
		return save(sync, null, dest, data, false, columns);
	}

	@Override
	public int save(Object data, String... columns) {
		return save(null, null, data, false, columns);
	}
	@Override
	public int save(boolean sync, Object data, String... columns) {
		return save(sync, null, null, data, false, columns);
	}

	@Override
	public int save(DataSource ds, String dest, Object data, String... columns) {
		return save(ds, dest, data, false, columns);
	}

	@Override
	public int save(boolean sync, DataSource ds, String dest, Object data, String... columns) {
		return save(sync, ds, dest, data, false, columns);
	}

	private int saveObject(DataSource ds, String dest, Object data, boolean checkPrimary, String... columns) {
		return dao.save(ds, dest, data, checkPrimary, columns);
	}

	@Override
	public int insert(DataSource ds, String dest, Object data, boolean checkPrimary, String... columns) {
		return dao.insert(ds, dest, data, checkPrimary, columns);
	}

	@Override
	public int insert(String dest, Object data, boolean checkPrimary, String... columns) {
		return insert(null, dest, data, checkPrimary, columns);
	}

	@Override
	public int insert(DataSource ds, Object data, boolean checkPrimary, String... columns) {
		return insert(ds, null, data, checkPrimary, columns);
	}

	@Override
	public int insert(Object data, boolean checkPrimary, String... columns) {
		return insert(null, null, data, checkPrimary, columns);
	}

	@Override
	public int insert(DataSource ds, Object data, String... columns) {
		return insert(ds, null, data, false, columns);
	}

	@Override
	public int insert(String dest, Object data, String... columns) {
		return insert(null, dest, data, false, columns);
	}

	@Override
	public int insert(Object data, String... columns) {
		return insert(null, null, data, false, columns);
	}

	@Override
	public int insert(DataSource ds, String dest, Object data, String... columns) {
		return insert(ds, dest, data, false, columns);
	}

	@Override
	public int batchInsert(DataSource ds, String dest, Object data, boolean checkPrimary, String... columns) {
		return dao.batchInsert(ds, dest, data, checkPrimary, columns);
	}

	@Override
	public int batchInsert(String dest, Object data, boolean checkPrimary, String... columns) {
		return batchInsert(null, dest, data, checkPrimary, columns);
	}

	@Override
	public int batchInsert(DataSource ds, Object data, boolean checkPrimary, String... columns) {
		return batchInsert(ds, null, data, checkPrimary, columns);
	}

	@Override
	public int batchInsert(Object data, boolean checkPrimary, String... columns) {
		return batchInsert(null, null, data, checkPrimary, columns);
	}

	@Override
	public int batchInsert(DataSource ds, Object data, String... columns) {
		return batchInsert(ds, null, data, false, columns);
	}

	@Override
	public int batchInsert(String dest, Object data, String... columns) {
		return batchInsert(null, dest, data, false, columns);
	}

	@Override
	public int batchInsert(Object data, String... columns) {
		return batchInsert(null, null, data, false, columns);
	}

	@Override
	public int batchInsert(DataSource ds, String dest, Object data, String... columns) {
		return batchInsert(ds, dest, data, false, columns);
	}
	@Override
	public boolean executeProcedure(DataSource ds, String procedure, String... inputs) {
		Procedure proc = new ProcedureImpl();
		proc.setName(procedure);
		for (String input : inputs) {
			proc.addInput(input);
		}
		return executeProcedure(ds, proc);
	}

	@Override
	public boolean executeProcedure(String procedure, String... inputs) {
		return executeProcedure(null, procedure, inputs);
	}

	@Override
	public boolean executeProcedure(DataSource ds, Procedure procedure) {
		return dao.executeProcedure(ds, procedure);
	}

	@Override
	public boolean executeProcedure(Procedure procedure) {
		return executeProcedure(null, procedure);
	}

	/**
	 * 根据存储过程查询
	 * 
	 * @param procedure
	 * @param inputs
	 * @return
	 */
	@Override
	public DataSet queryProcedure(DataSource ds, Procedure procedure) {
		DataSet set = null;
		try {
			set = dao.queryProcedure(ds, procedure);
		} catch (Exception e) {
			set = new DataSet();
			set.setException(e);
			log.error("QUERY ERROR:"+e);
			if(ConfigTable.isDebug()){
				e.printStackTrace();
			}
		}
		return set;
	}

	@Override
	public DataSet queryProcedure(Procedure procedure) {
		return queryProcedure(null, procedure);
	}

	@Override
	public DataSet queryProcedure(DataSource ds, String procedure, String... inputs) {
		Procedure proc = new ProcedureImpl();
		proc.setName(procedure);
		for (String input : inputs) {
			proc.addInput(input);
		}
		return queryProcedure(ds, proc);
	}

	@Override
	public DataSet queryProcedure(String procedure, String... inputs) {
		return queryProcedure(null, procedure, inputs);
	}

	@Override
	public int execute(DataSource ds, String src, ConfigStore store, String... conditions) {
		int result = -1;
		conditions = BasicUtil.compressionSpace(conditions);
		SQL sql = createSQL(src);
		if (null == sql) {
			return result;
		}
		result = dao.execute(ds, sql, store, conditions);
		return result;
	}

	@Override
	public int execute(String src, ConfigStore configs, String... conditions) {
		return execute(null, src, configs, conditions);
	}

	@Override
	public int execute(DataSource ds, String src, String... conditions) {
		return execute(ds, src, null, conditions);
	}

	@Override
	public int execute(String src, String... conditions) {
		return execute(null, src, null, conditions);
	}
	@Override
	public int delete(DataSource ds, String dest, Object data) {
		if (null == data) {
			return 0;
		}
		if (data instanceof DataRow) {
			return deleteRow(ds, dest, (DataRow) data);
		}
		if (data instanceof DataSet) {
			DataSet set = (DataSet) data;
			int cnt = 0;
			int size = set.size();
			for (int i = 0; i < size; i++) {
				cnt += deleteRow(ds, dest, set.getRow(i));
			}
			return cnt;
		}
		if (data instanceof Collection) {
			Collection datas = (Collection) data;
			int cnt = 0;
			for (Object obj : datas) {
				cnt += delete(ds, dest, obj);
			}
			return cnt;
		}
		return deleteObject(ds, dest, data);
	}

	@Override
	public int delete(DataSource ds, Object data) {
		return delete(ds, null, data);
	}

	@Override
	public int delete(String dest, Object data) {
		return delete(null, dest, data);
	}

	@Override
	public int delete(Object data) {
		return delete(null, null, data);
	}

	private int deleteObject(DataSource ds, String dest, Object data, String... columns) {
		return 0;
	}

	private int deleteRow(DataSource ds, String dest, DataRow row, String... columns) {
		return dao.delete(ds, dest, row, columns);
	}
	
	public int delete(DataSource ds, String table, String key, Collection<Object> values){
		return dao.delete(ds, table, key, values);
	}
	public int delete(String table, String key, Collection<Object> values){
		return delete(null, table, key, values);
	}
	
	public int delete(DataSource ds, String table, String key, String ... values){
		return dao.delete(ds, table, key, values);
	}
	public int delete(String table, String key, String ... values){
		return delete(null, table, key, values);
	}

	/**
	 * @param conditions	固定查询条件  
	 * 			原生SQL(AND GROUP ORDER)
	 * 			{原生}
	 * 			+CD:1			拼接
	 * 			+CD:			拼接 IS NULL
	 * 			+CD:null		拼接 IS NULL
	 * 			+CD:NULL		拼接 IS NULL
	 * 			CD:1		拼接
	 * 			CD:			删除
	 * 			CD:null		删除
	 * 			CD:NULL		拼接 IS NULL
	 */
//	private String[] parseConditionsss(String[] conditions){
//		conditions = BasicUtil.compressionSpace(conditions);
//		if(null != conditions){
//			int length = conditions.length;
//			for(int i=0; i<length; i++){
//				String condition = conditions[i];
//				if(null == condition){
//					condition = "";
//					continue;
//				}
//				if(condition.startsWith("{") && condition.endsWith("}")){
//					//原生 SQL
//					continue;
//				}
//				if(null == condition || "".equals(condition)){
//					continue;
//				}
//				//CD:1 
//				//CD:'1' 
//				//CD=1 
//				//CD='1' 
//				//CD:2012-06-06 01:01:01
//				//CD:'2012-06-06 01:01:01'
//				//CD:01:01
//				//CD:'01:01'
//				//CD ='2012-06-06 01:01:01'  <= < != <> 
//				//CD IN (1,2)
//				//CD IN('2012-06-06 01:01:01')
//				//注意 时间中的: 与分隔符号 冲突(:号之前有运算符号的视为时间) [a-zA-Z]+[a-zA-Z0-9]*:([\\d]{4}-[\\d]{2}-[\\d]{2}\\s+)?\\d{2}:\\d{2}
///********************************************************* 待处理   时间中的: 与分隔符号 冲突***************************************************************************************/				
//				if(null != condition && condition.contains(":")){
//					if(RegularUtil.match(condition, "[a-zA-Z]+\\d{2}:\\d{2}")){
//						
//					}
//					String k = "";
//					String v = "";
//					String kv[] = condition.split(":");
//					if(kv.length > 0){
//						k = kv[0];
//					}
//					if(kv.length > 1){
//						v = kv[1];
//					}
//
//					if("".equals(k) || "+".equals(k)){
//						conditions[i] = "";
//						continue;
//					}
//					
//					if(k.startsWith("+")){
//						if("null".equalsIgnoreCase(v)){
//							conditions[i] = k +":NULL";
//						}
//					}else{
//						if("".equals(v) || "null".equals(v)){
//							conditions[i] = "";
//						}
//					}
//				}
//			}
//		}
//		return conditions;
//	}
}