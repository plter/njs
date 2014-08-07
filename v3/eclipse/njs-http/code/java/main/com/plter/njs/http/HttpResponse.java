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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.plter.lib.java.utils.LogFactory;

public abstract class HttpResponse{

	private static final byte[] CRLF = new byte[] { 0x0D, 0x0A };

	/** HTTP response codes */
	public static final int HTTP_STATUS_SUCCESS = 200;
	public static final int HTTP_STATUS_ACCESS_DENIED = 403;
	public static final int HTTP_STATUS_NOT_FOUND = 404;
	public static final int HTTP_STATUS_SERVER_ERROR = 500;
	
	
	public HttpResponse() {
		this(false,HTTP_STATUS_SUCCESS,"text/html");
	}
	
	public HttpResponse(boolean keepAlive) {
		this(keepAlive,HTTP_STATUS_SUCCESS,"text/html");
	}
	
	
	public HttpResponse(boolean keepAlive,int responseCode,String contentType){
		initHeaders();
		
		setResponseCode(responseCode);
		setContentType(contentType);
		setKeepAlive(keepAlive);
	}

	private void initHeaders(){
		header("Server", "njs(New Java Server)");
		header("Cache-Control", "private");
		header("Connection", "keep-alive");
		header("Keep-Alive", "200");
	}

	public HttpResponse header(String name,String value){
		getHeaders().put(name, value);
		return this;
	}


	private final ByteBuffer headerBuf = ByteBuffer.allocateDirect(2048);

	private final void makeHeaderBuf(){

		try {
			headerBuf.clear();
			headerBuf.put("HTTP/1.1 ".getBytes(NJSHttpConfig.CHARSET));
			headerBuf.put(String.valueOf(getResponseCode()).getBytes(NJSHttpConfig.CHARSET));
			switch (getResponseCode()) {
			case HttpResponse.HTTP_STATUS_SUCCESS:
				headerBuf.put(" OK".getBytes(NJSHttpConfig.CHARSET));
				break;
			case HttpResponse.HTTP_STATUS_NOT_FOUND:
				headerBuf.put(" Not Found".getBytes(NJSHttpConfig.CHARSET));
				break;
			}
			headerBuf.put(CRLF);
			for (Entry<String, String> entry: getHeaders().entrySet()) {
				headerBuf.put(entry.getKey().getBytes(NJSHttpConfig.CHARSET));
				headerBuf.put(": ".getBytes(NJSHttpConfig.CHARSET));
				headerBuf.put(entry.getValue().getBytes(NJSHttpConfig.CHARSET));
				headerBuf.put(CRLF);
			}
			// now the content length is the body length
			if (getContentLength()>0) {
				headerBuf.put("Content-Length: ".getBytes(NJSHttpConfig.CHARSET));
				headerBuf.put(String.valueOf(getContentLength()).getBytes(NJSHttpConfig.CHARSET));
				headerBuf.put(CRLF);
			}
			headerBuf.put(CRLF);

			headerBuf.flip();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void handle(SelectionKey selectionKey,HttpRequest request){
		this.selectionKey = selectionKey;
		httpRequest = request;
		
		sendHeader();
		doHandle();
		
		if (!keepAlive) {
			close();
		}
	}
	
	public SelectionKey getSelectionKey() {
		return selectionKey;
	}
	
	public HttpRequest getHttpRequest() {
		return httpRequest;
	}
	
	public abstract void doHandle();
	
	/**
	 * Close the channel
	 */
	public void close(){
		if (selectionKey!=null) {
			((SelectionKeyAttachment)selectionKey.attachment()).getHttpRequestDecoderFilter().close(selectionKey);
		}
	}
	
	

	public void setContentType(String contentType){
		header("Content-Type", String.format("%s; charset=%s", contentType,NJSHttpConfig.CHARSET));
		this.contentType=contentType;
	}

	public String getContentType(){
		return contentType;
	}

	private String contentType=null;

	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}


	public Map<String, String> getHeaders() {
		return headers;
	}

	public long getContentLength(){
		return contentLength;
	}

	public void setContentLength(long value){
		contentLength=value;
	}

	public ByteBuffer getHeaderBuf() {
		makeHeaderBuf();
		return headerBuf;
	}
	
	void sendHeader(){
		if (selectionKey==null) {
			return;
		}
		
		try {
			((SocketChannel)selectionKey.channel()).write(getHeaderBuf());
		} catch (IOException e) {
			log.warning("IO error accur when send http response header");
			close();
		}
	}
	
	public void write(String content){
		try {
			write(ByteBuffer.wrap(content.getBytes(NJSHttpConfig.CHARSET)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void write(byte[] bytes){
		write(ByteBuffer.wrap(bytes));
	}
	
	public void write(ByteBuffer content){
		if (selectionKey==null) {
			return;
		}
		
		try {
			((SocketChannel)selectionKey.channel()).write(content);
		} catch (IOException e) {
			log.warning("IO error accur when write http response content");
			close();
		}
	}
	
	public HttpResponse setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
		return this;
	}
	
	public boolean isKeepAlive() {
		return keepAlive;
	}

	private long contentLength=0;
	private final Map<String, String> headers = new HashMap<String, String>();
	private int responseCode=200;
	private HttpRequest httpRequest=null;
	private SelectionKey selectionKey = null;
	private static final Logger log = LogFactory.getLogger();
	private boolean keepAlive = false;
}
