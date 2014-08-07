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
package com.plter.njs.web;

import java.net.URLClassLoader;
import java.util.logging.Logger;

import com.plter.lib.java.utils.LogFactory;
import com.plter.njs.http.HttpRequestDecoderFilter;
import com.plter.njs.socket.SocketAcceptor;
import com.plter.njs.web.conf.ContentTypeMap;
import com.plter.njs.web.conf.PWSConfig;
import com.plter.njs.web.dynamic.DynamicWebFilter;
import com.plter.njs.web.dynamic.StaticFileFilter;
import com.plter.njs.web.system.AppContext;
import com.plter.njs.web.system.NJSystem;

public class NJS {


	private static final Logger log = LogFactory.getLogger();

	public void start(URLClassLoader rootUrlClassLoader) {

		NJSystem.setAppContext(new AppContext(rootUrlClassLoader));

		if (!PWSConfig.loadConfig()) {
			log.severe("Failed to load config file");
			return;
		}


		if (!ContentTypeMap.loadContentTypesMap()) {
			log.severe("Failed to load content type config file");
			return;
		}
		
		startServer();

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


	private void startServer(){
		// Create an acceptor
		SocketAcceptor acceptor = new SocketAcceptor(PWSConfig.port);
		acceptor.getFilterChain().push(new HttpRequestDecoderFilter());
		acceptor.getFilterChain().push(new DynamicWebFilter());
		acceptor.getFilterChain().push(new StaticFileFilter());
		acceptor.listen();
	}
}