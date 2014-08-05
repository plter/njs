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

package com.plter.njs.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
	
	public HttpRequest() {
	}
	
	public String POST(String name){
		if (postArgs.size()<=0) {
			byte[] bytes = new byte[(int) getContentLength()];
			try {
				getPostData().read(bytes);
				String str = new String(bytes,NJSHttpConfig.CHARSET);
				String[] kvs = str.split("&");
				String[] kv;
				for (int i = 0; i < kvs.length; i++) {
					kv = kvs[i].split("=");
					if (kv.length==2) {
						postArgs.put(kv[0], kv[1]);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return postArgs.get(name);
	}

	public String GET(String name){
		return getMap().get("@".concat(name));
	}

	public String getMethod() {
		return method;
	}

	public String getContext() {
		return context;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public String getUri() {
		return uri;
	}

	public InputStream getPostData() {
		if (postData==null) {
			if (postDataBuffer instanceof ByteArrayOutputStream) {
				postData = new ByteArrayInputStream(((ByteArrayOutputStream) postDataBuffer).toByteArray());
			}else if (postDataBuffer instanceof FileOutputStream) {
				try {
					postData = new FileInputStream(postDataBufferFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					postData=null;
				}
			}
		}
		
		return postData;
	}

	public String getFileType() {
		return fileType;
	}


	public final long getContentLength(){
		if (contentLength<0) {
			if (getMethod().equals(HttpMethod.POST)) {
				try{
					contentLength = Integer.parseInt(getMap().get("Content-Length"));
				}catch(Exception e){
					e.printStackTrace();
					contentLength = 0;
				}
			}else{
				contentLength = 0;
			}
		}
		return contentLength;
	}
	
	private long contentLength=-1;


	private HttpRequest setContext(String context) {
		this.context = context;
		return this;
	}

	HttpRequest setMap(Map<String, String> map) {
		this.map = map;

		setMethod(map.get("Method").toUpperCase());
		uri=map.get("URI");
		setContext(map.get("Context"));
		int start=getContext().lastIndexOf('.');
		if (start>-1) {
			this.fileType=getContext().substring(start+1).toLowerCase();
		}

		return this;
	}

	private HttpRequest setMethod(String method) {
		this.method = method;
		return this;
	}
	
	void writePostBuffedData(byte[] bytes) throws IOException{
		if (postDataBuffer==null) {
			if (getContentLength()<2048) {
				postDataBuffer = new ByteArrayOutputStream();
			}else{
				postDataBufferFile = File.createTempFile("postdata", ".tmp");
				postDataBuffer = new FileOutputStream(postDataBufferFile);
			}
		}
		
		postDataBuffer.write(bytes);
		currentPostDataSize += bytes.length;
	}
	
	public final long getCurrentPostDataSize(){
		return currentPostDataSize;
	}
	
	public final boolean isPostDataCompleted(){
		return getCurrentPostDataSize()>=getContentLength();
	}
	
	

	private String method="GET";
	private String context=null;
	private Map<String, String> map=null;
	private String uri=null;
	private InputStream postData=null;
	private String fileType=null;
	private OutputStream postDataBuffer = null;
	private File postDataBufferFile = null;
	private long currentPostDataSize = 0;
	private final Map<String, String> postArgs = new HashMap<>();
}
