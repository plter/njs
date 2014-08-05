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
package com.plter.pws.http;

import java.io.File;
import java.nio.channels.SelectionKey;

import com.plter.lib.java.lang.FilePool;
import com.plter.lib.java.lang.ObjectPool;
import com.plter.pws.conf.PWSConfig;
import com.plter.pws.core.SocketFilter;



public class StaticFileFilter extends SocketFilter {
	
	@Override
	public void onMessageReceived(SelectionKey selectionKey, Object message) {
		HttpRequest request = (HttpRequest) message;
		if (request==null) {
			close(selectionKey);
			return;
		}

		String context = request.getContext();

		File f=FilePool.getFile(PWSConfig.webroot+context);

		if (f.exists()) {
			if(f.isFile()){
				ObjectPool.get(FileResponse.class).setFile(f).handle(selectionKey, request);
			}else if (f.isDirectory()) {
				ObjectPool.get(DirectoryResponse.class).setDirectory(f).handle(selectionKey, request);
			}else{
				close(selectionKey);
			}
		}else{
			ErrorResponses.handle404(selectionKey,request);
		}
	}

}
