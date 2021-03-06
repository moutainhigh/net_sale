/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package org.llw.common.core.dao.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.llw.com.core.SpringAppContext;
import org.llw.com.util.StringUtil;
import org.llw.common.core.dao.Dao;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Lazy
@Service("daoMybatis")
@Slf4j
public class DaoMybatisImpl implements Dao{
	private static final String SQL_SESSION_TEMPLATE = "sqlSessionTemplate";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private SqlSessionTemplate getDao(String db){
		String dbSqlSessionTemplateName = StringUtil.getString(db) + SQL_SESSION_TEMPLATE;
		log.info("*************** dbSqlSessionTemplate Name :" + dbSqlSessionTemplateName);
		return (SqlSessionTemplate)SpringAppContext.getBean(dbSqlSessionTemplateName);
	}
	

	public List<Map<String, Object>> query(String namespace,String statement,String db) {
		long bengin = System.currentTimeMillis();
		List<Map<String, Object>> dataList = getDao(db).<Map<String,Object>>selectList(changeStatement(namespace,statement));
		
		log.info("["+namespace+"."+statement+"] execute, cost:"+(System.currentTimeMillis() - bengin) + "ms");
		return dataList;
	}

	public List<Map<String, Object>> query(String namespace,String statement,
			Map<String, Object> paramData,String db) {
		long bengin = System.currentTimeMillis();
		List<Map<String, Object>> dataList = getDao(db).<Map<String,Object>>selectList(changeStatement(namespace,statement),paramData);
		log.info("["+namespace+"."+statement+"] execute, cost:"+(System.currentTimeMillis() - bengin) + "ms");
		return dataList;
	}

	public List<Map<String, Object>> query(String namespace,String statement,
			Map<String, Object> paramData, int limit, int offset, String db) {
		long bengin = System.currentTimeMillis();
		RowBounds rowBounds = new RowBounds((offset-1)*limit, limit);
		List<Map<String, Object>> dataList = getDao(db).<Map<String,Object>>selectList(changeStatement(namespace,statement),paramData,rowBounds);
		log.info("["+namespace+"."+statement+"] execute, cost:"+(System.currentTimeMillis() - bengin) + "ms");
		return dataList;
	}

	public int count(String namespace,String statement, String db) {
		return getDao(db).<Integer>selectOne(changeStatement(namespace,statement));
	}

	
	public int count(String namespace,String statement, Map<String, Object> paramData, String db) {
		return getDao(db).<Integer>selectOne(changeStatement(namespace,statement),paramData);
	}


	public Map<String, Object> get(String namespace,String statement,
			Map<String, Object> paramData, String db) {
		Map<String, Object> dataMap =  getDao(db).<Map<String, Object>>selectOne(changeStatement(namespace,statement),paramData);
		return dataMap;
	}


	public Map<String, Object> load(String namespace, String key,
			String value, String db) {
		Map<String,String> param = new HashMap<String,String>();
		param.put(key, value);
		Map<String, Object> dataMap = getDao(db).<Map<String, Object>>selectOne(changeStatement(namespace,"load"),param);
		return dataMap;
	}

	
	public int insert(String namespace,String statement, Map<String, Object> paramData, String db) {
		return getDao(db).insert(changeStatement(namespace,statement), paramData);
	}

	
	public int update(String namespace,String statement, Map<String, Object> paramData, String db) {
		return getDao(db).update(changeStatement(namespace,statement), paramData);
	}

	
	public int delete(String namespace,String statement, Map<String, Object> paramData, String db) {
		return getDao(db).delete(changeStatement(namespace,statement), paramData);
	}
	
	private String changeStatement(String namespace,String statement){
		return namespace + "." + statement;
	}

	@SuppressWarnings("unused")
	private void changeDateType(List<Map<String,Object>> dataList){
		if(dataList == null || dataList.size() == 0){
			return;
		}
		
		for(Map<String,Object> map : dataList){
			for(Map.Entry<String, Object> entry : map.entrySet()){
				if(entry.getValue().getClass() == Timestamp.class){
					map.put(entry.getKey(), sdf.format((Timestamp)entry.getValue()));
				}
			}
		}
		
	}
	
	@SuppressWarnings("unused")
	private void changeDateType(Map<String,Object> dataMap){
		if(dataMap == null || dataMap.size() == 0){
			return;
		}
		
		for(Map.Entry<String, Object> entry : dataMap.entrySet()){
			if(entry.getValue().getClass() == Timestamp.class){
				dataMap.put(entry.getKey(),  sdf.format((Timestamp)entry.getValue()));
			}
		}
			
	}

	public List<Map<String, Object>> query(String namespace, String statement,
			int limit, int offset, String db) {
		long bengin = System.currentTimeMillis();
		RowBounds rowBounds = new RowBounds((offset-1)*limit, limit);
		List<Map<String, Object>> dataList = getDao(db).<Map<String,Object>>selectList(changeStatement(namespace,statement),null,rowBounds);
		log.info("["+namespace+"."+statement+"] execute, cost:"+(System.currentTimeMillis() - bengin) + "ms");
		return dataList;
	}


	public String getSql(String namespace, String statement, String ds,
			Map<String, Object> paramData, String db) {
		MappedStatement ms = getDao(db).getConfiguration().getMappedStatement(namespace+"."+statement);
		BoundSql boundSql = ms.getBoundSql(paramData);
		return boundSql.getSql();
	}

	@Override
	public String getSql(String namespace, String statement,
			Map<String, Object> paramData, String db) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int batchInsert(String namespace, String statement, List<Map<String, Object>> paramData, String db) {
		long bengin = System.currentTimeMillis();
		int size = getDao(db).insert(changeStatement(namespace,statement), paramData);
		log.info("["+namespace+"."+statement+"] execute, cost:"+(System.currentTimeMillis() - bengin) + "ms");
		return size;
	}


	@Override
	public int batchDelete(String namespace, String statement,
			List<Map<String, Object>> paramData, String db) {
		long bengin = System.currentTimeMillis();
		int size = getDao(db).delete(changeStatement(namespace,statement), paramData);
		log.info("["+namespace+"."+statement+"] execute, cost:"+(System.currentTimeMillis() - bengin) + "ms");
		return size;
	}
	
	
		
}