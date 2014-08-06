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

import java.nio.ByteBuffer;

public class SelectionKeyAttachment {

	public SelectionKeyAttachment() {
	}
	
	ByteBuffer getHttpRequestData() {
		return httpRequestData;
	}

	public boolean isRequestCompleted() {
		return requestCompleted;
	}

	void setRequestCompleted(boolean requestCompleted) {
		this.requestCompleted = requestCompleted;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public int getHeaderEnd() {
		return headerEnd;
	}

	void setHeaderEnd(int headerEnd) {
		this.headerEnd = headerEnd;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}
	
	/**
	 * If the space is enough to append new data,the method will append the new data,and then return true,or it will return false
	 * @param data
	 * @return
	 */
	boolean appendHttpRequestData(ByteBuffer data){
		if (data.remaining()+httpRequestData.position()<httpRequestData.capacity()) {
			httpRequestData.put(data);
			return true;
		}else{
			return false;
		}
	}

	private final ByteBuffer httpRequestData = ByteBuffer.allocateDirect(2048);
	private boolean requestCompleted=false;
	private String httpMethod = HttpMethod.GET;
	private int headerEnd=0;
	private HttpRequest httpRequest=null;
	
}
