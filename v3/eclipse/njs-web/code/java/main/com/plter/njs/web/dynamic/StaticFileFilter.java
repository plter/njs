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
package com.plter.njs.web.dynamic;

import java.io.File;
import java.nio.channels.SelectionKey;

import com.plter.njs.http.HttpRequest;
import com.plter.njs.socket.BaseFilter;
import com.plter.njs.web.conf.PWSConfig;



public class StaticFileFilter extends BaseFilter {
	
	@Override
	public void onMessageReceived(SelectionKey selectionKey, Object message) {
		HttpRequest request = (HttpRequest) message;
		if (request==null) {
			close(selectionKey);
			return;
		}

		String context = request.getContext();

		File f=new File(PWSConfig.webroot+context);

		if (f.exists()) {
			if(f.isFile()){
				new FileResponse().setFile(f).handle(selectionKey, request);
			}else if (f.isDirectory()) {
				new DirectoryResponse().setDirectory(f).handle(selectionKey, request);
			}else{
				close(selectionKey);
			}
		}else{
			ErrorResponses.handle404(selectionKey,request);
		}
	}

}
