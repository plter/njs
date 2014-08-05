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

	public int getPostHeaderEnd() {
		return postHeaderEnd;
	}

	public void setPostHeaderEnd(int postHeaderEnd) {
		this.postHeaderEnd = postHeaderEnd;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	private final ByteBuffer httpRequestData = ByteBuffer.allocateDirect(2048);
	private boolean requestCompleted=false;
	private String httpMethod = HttpMethod.GET;
	private int postHeaderEnd=0;
	private HttpRequest httpRequest=null;
	
}
