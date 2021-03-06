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
package org.ddq.common.security;

import org.ddq.common.context.AppProperties;
import org.ddq.common.security.md5.Md5;

public class MD5Util {
	
	
	/**
	 * 获取系统密码的加密串
	 * @param password
	 * @return
	 */
	public static String  getEncryptPassword(String password){
		String pwdIsEncrypt = AppProperties.getProperties(AppProperties.PASSWORD_IS_ENCRYPT);
		String addP = "";
		//为1是说明已经加密
		if("1".equals(pwdIsEncrypt)){
			addP = AppProperties.getProperties(AppProperties.PASSWORD_MD5_KEY);
		}
		String newPas = getEncryptByKey(password, addP);
		return newPas;
	}
	
	/**
	 * 给字符串附加key加密
	 * @param str 需要加密的字符串
	 * @param key 附加的key
	 * @return
	 * @throws Exception 
	 */
	public static String  getEncryptByKey(String str,String key){
		String newPas = Md5.getInstance().encrypt(str + key);
		return newPas;
	}
	
	public static void main(String[] args) {
		//b9d11b3be25f5a1a7dc8ca04cd310b28
		System.out.println(getEncryptByKey("123456","Liuenc"));
	}


}