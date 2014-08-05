/**
   Copyright [2013-2018] [plter] http://plter.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.plter.pws.http;

import java.io.File;
import java.nio.channels.SelectionKey;

import com.plter.lib.java.lang.FilePool;
import com.plter.lib.java.lang.ObjectPool;
import com.plter.pws.conf.PWSConfig;

public class ErrorResponses {

	
	public static final void handle404(SelectionKey selectionKey,HttpRequest request){
		
		
		File f = FilePool.getFile(PWSConfig.page404);
		if (f.exists()) {
			ObjectPool.get(Error404FileResponse.class).handle(selectionKey, request);
		}else{
			ObjectPool.get(Error404TextResponse.class).handle(selectionKey, request);
		}
	}
	
	public static final void handle500(SelectionKey selectionKey,HttpRequest request){
		
		
		File f = FilePool.getFile(PWSConfig.page500);
		if (f.exists()) {
			ObjectPool.get(Error500FileResponse.class).handle(selectionKey, request);
		}else{
			ObjectPool.get(Error500TextResponse.class).handle(selectionKey, request);
		}
	}
	
}
