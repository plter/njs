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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.logging.Logger;

import com.plter.lib.java.utils.LogFactory;
import com.plter.njs.http.HttpRequest;
import com.plter.njs.http.HttpResponse;
import com.plter.njs.web.conf.ContentTypeMap;
import com.plter.njs.web.tools.AIOFile;


public class FileResponse extends HttpResponse {

	private static final Logger log = LogFactory.getLogger();

	public FileResponse() {
	}


	@Override
	public void handle(SelectionKey selectionKey, HttpRequest request) {
		setKeepAlive(true);

		if (getFile()!=null&&getFile().exists()&&getFile().isFile()&&getContentType()!=null) {
			setContentLength(getFile().length());
			
			super.handle(selectionKey, request);
		}else{
			ErrorResponses.handle404(getSelectionKey(), getHttpRequest());
		}
	}


	@Override
	public void doHandle() {
		
		AIOFile.readFile(getFile(), (AIOFile target,ByteBuffer data,double percent)->{
			if(!write(data)){
				target.stopRead();
			}
		}, (AIOFile target)->{
			//complete
			close();
		}, (AIOFile target,int errorCode)->{
			log.finer("Error ocur when read file data");
			close();
		},true /*getFile().length()<1024*1024*2*/);
	}

	public File getFile() {
		return file;
	}

	public FileResponse setFile(File file) {
		this.file = file;
		String name = file.getName();
		setContentType(ContentTypeMap.get(name.substring(name.lastIndexOf('.')+1)));
		return this;
	}

	private File file = null;
}
