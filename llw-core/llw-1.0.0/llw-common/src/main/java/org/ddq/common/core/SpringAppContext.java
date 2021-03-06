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
package org.ddq.common.core;
import org.ddq.common.util.LogerUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringAppContext implements ApplicationContextAware {

	private static ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		SpringAppContext.context = applicationContext;
		System.out.println("---------------------------------------------------------------------");
		System.out.println("========SpringAppContext init success ========");
        System.out.println("---------------------------------------------------------------------");
	}
	
	public static ApplicationContext getApplicationContext()
			throws BeansException {
		return context;
	}
	
	/***
	 * 判断 beanId 是否存在，存在返回实例，不存在返回null
	 * @param beanId
	 * @return
	 */
	public static Object getBean(String beanId) {
		if (beanId == null || beanId.length() == 0) {
			return null;
		}
		try{
			Object object = context.getBean(beanId);
			return object;
		}catch(Exception e){
		    LogerUtil.debug("not found Bean:" + beanId);
		}
		return null;
	}
	
	/***
	 * 判断 class 是否存在，存在返回实例，不存在返回null
	 * @param beanId
	 * @return
	 */
	public static  <T> T getBean(Class<T> clazz) {
		if (clazz == null ) {
			return null;
		}
		try{
			return context.<T>getBean(clazz);
		}catch(Exception e){
			LogerUtil.debug("not found Bean:" + clazz);
		}
		return null;
	}
	
	
	
	
	
}