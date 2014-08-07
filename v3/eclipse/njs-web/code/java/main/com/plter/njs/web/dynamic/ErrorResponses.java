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

package com.plter.njs.web.dynamic;

import java.io.File;
import java.nio.channels.SelectionKey;

import com.plter.njs.http.HttpRequest;
import com.plter.njs.web.conf.PWSConfig;

public class ErrorResponses {

	
	public static final void handle404(SelectionKey selectionKey,HttpRequest request){
		
		
		File f = new File(PWSConfig.page404);
		if (f.exists()) {
			new Error404FileResponse().handle(selectionKey, request);
		}else{
			new Error404TextResponse().handle(selectionKey, request);
		}
	}
	
	public static final void handle500(SelectionKey selectionKey,HttpRequest request){
		
		
		File f = new File(PWSConfig.page500);
		if (f.exists()) {
			new Error500FileResponse().handle(selectionKey, request);
		}else{
			new Error500TextResponse().handle(selectionKey, request);
		}
	}
	
}
