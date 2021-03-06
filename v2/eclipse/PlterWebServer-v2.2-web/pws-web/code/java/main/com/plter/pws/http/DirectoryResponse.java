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

import com.plter.pws.conf.PWSConfig;

public class DirectoryResponse extends FileResponse {

	public DirectoryResponse() {
	}
	
	
	@Override
	protected void handle(final IWriteCompleteListener completer, final IWriteErrorListener writeError) {
		File indexFile=null;
		
		for (int i = 0; i < PWSConfig.defaultDoc.length; i++) {
			indexFile = new File(getDirectory(), PWSConfig.defaultDoc[i]);
			if (indexFile.exists()) {
				
				setFile(indexFile);
				super.handle(completer,writeError);
				break;
			}else{
				indexFile=null;
			}
		}
	}
	

	public File getDirectory() {
		return directory;
	}

	public DirectoryResponse setDirectory(File directory) {
		this.directory = directory;
		return this;
	}

	private File directory=null;
}
