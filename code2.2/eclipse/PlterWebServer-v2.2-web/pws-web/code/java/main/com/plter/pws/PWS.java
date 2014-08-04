/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package com.plter.pws;

import java.net.URLClassLoader;
import java.util.logging.Logger;

import com.plter.lib.java.utils.LogFactory;
import com.plter.pws.conf.ContentTypeMap;
import com.plter.pws.conf.PWSConfig;
import com.plter.pws.core.SocketAcceptor;
import com.plter.pws.http.DynamicWebFilter;
import com.plter.pws.http.HttpRequestDecoderFilter;
import com.plter.pws.http.StaticFileFilter;
import com.plter.pws.system.AppContext;
import com.plter.pws.system.PWSystem;

public class PWS {
	
	
	private static final Logger log = LogFactory.getLogger();
	
	public void start(URLClassLoader rootUrlClassLoader) {
		
		PWSystem.setAppContext(new AppContext(rootUrlClassLoader));
		
		if (!PWSConfig.loadConfig()) {
			log.severe("Failed to load config file");
			return;
		}
		
		
		if (!ContentTypeMap.loadContentTypesMap()) {
			log.severe("Failed to load content type config file");
			return;
		}
		if (!startServer()) {
			log.severe("Failed to start http server");
			System.exit(0);
			return;
		}
		
		if (PWSConfig.autoStartClasses!=null) {
			for (int i = 0; i < PWSConfig.autoStartClasses.length; i++) {
				try {
					Class.forName(PWSConfig.autoStartClasses[i]).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private static boolean startServer(){
		try {
			// Create an acceptor
			SocketAcceptor acceptor = new SocketAcceptor(PWSConfig.port);
			acceptor.getFilterChain().push(new HttpRequestDecoderFilter());
			acceptor.getFilterChain().push(new DynamicWebFilter());
			acceptor.getFilterChain().push(new StaticFileFilter());
			acceptor.listen();
			
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			
			return false;
		}
	}
}