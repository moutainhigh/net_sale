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
package org.ddq.common.util;

import org.apache.ibatis.mapping.MappedStatement;
import org.ddq.common.core.SpringAppContext;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DaoUtil {
	private volatile static DaoUtil dataUtil = null;
	private static JdbcTemplate jdbcTmeplate = null;
	private static SqlSessionTemplate sqlSessionTemplate = null;
	
	private DaoUtil(){}
	
	public static DaoUtil getInstance(){
		if(dataUtil  == null ){
			synchronized (DaoUtil.class) {
				if(dataUtil == null){
					dataUtil = new DaoUtil();
				}
			}
		}
		return dataUtil;
	}
	
	
	public  JdbcTemplate getJdbcTemplate(){
		if(jdbcTmeplate == null){
			jdbcTmeplate = (JdbcTemplate)SpringAppContext.getBean("jdbcTemplate");
		}
		return jdbcTmeplate;
	}
	
	
	public  SqlSessionTemplate getSqlSessionTemplate(){
		if(sqlSessionTemplate == null){
			sqlSessionTemplate = (SqlSessionTemplate)SpringAppContext.getBean("sqlSessionTemplate");
		}
		return sqlSessionTemplate;
	}
	
	public static String getSql(String mothodMame,Object paramObject){
		MappedStatement mappedStatement = sqlSessionTemplate.getConfiguration().getMappedStatement(mothodMame);
		return mappedStatement.getBoundSql(paramObject).getSql();
	}
	
	public static String getDataBasePre(Object paramObject){
		String packageUrl = paramObject.getClass().getPackage().getName();
		
		log.info("service packageUrl:" + packageUrl);
		if(packageUrl.startsWith("org.xxjr.busiIn")) {
			return "busiIn_";
		}
		
		if(packageUrl.startsWith("org.xxjr.cust")) {
			return "cust_";
		}
		
		if(packageUrl.startsWith("org.xxjr.summary")) {
			return "summer_";
		}
		if(packageUrl.startsWith("org.xxjr.sys")) {
			return "sys_";
		}
		return "";
	}
	
	public static String getDefaultDbPre(String dateBase){
		return StringUtils.hasText(dateBase) ? dateBase : "busiIn_";
	}
}