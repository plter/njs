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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.plter.lib.java.lang.ObjectPool;
import com.plter.lib.java.utils.LogFactory;
import com.plter.lib.java.utils.Looper;
import com.plter.pws.conf.PWSConfig;
import com.plter.pws.conf.ServerInfo;

public abstract class HttpResponse {

	private static final byte[] CRLF = new byte[] { 0x0D, 0x0A };

	/** HTTP response codes */
	public static final int HTTP_STATUS_SUCCESS = 200;
	public static final int HTTP_STATUS_ACCESS_DENIED = 403;
	public static final int HTTP_STATUS_NOT_FOUND = 404;
	public static final int HTTP_STATUS_SERVER_ERROR = 500;
	
	private static final Logger log = LogFactory.getLogger();
	
	public HttpResponse() {
	}
	
	public final static HttpResponse getHttpResponse(){
		return ObjectPool.get(HttpResponse.class);
	}
	
	public void recycle(){
		setRequest(null);
		setSelectionKey(null);
		setContentLength(0);
		setContentType("text/html");
		setResponseCode(HTTP_STATUS_SUCCESS);
		getHeaders().clear();
		setHeadersSent(false);
		ObjectPool.recycle(this);
	}


	private HttpRequest request=null;
	public HttpRequest getRequest() {
		return request;
	}

	private SelectionKey selectionKey=null;
	public SelectionKey getSelectionKey() {
		return selectionKey;
	}
	HttpResponse setRequest(HttpRequest request) {
		this.request = request;
		return this;
	}
	HttpResponse setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
		return this;
	}

	/**
	 * If return true,the channel connection is closed,else you should close the connection by your self.
	 * @return
	 */
	void handle(SelectionKey selectionKey,HttpRequest request){
		setRequest(request);
		setSelectionKey(selectionKey);

		handle(new IWriteCompleteListener() {
			
			@Override
			public void completed() {
				close();
			}
		},new IWriteErrorListener() {
			
			@Override
			public void error(int code) {
				close();
			}
		});
	}


	/**
	 * If return true,the channel connection would close immediately,else you should close the connection by your self.
	 * @return
	 */
	abstract protected void handle(final IWriteCompleteListener completer,final IWriteErrorListener writeError);

	protected void setupHeaders(){
		getHeaders().put("Server", "HttpServer (" + ServerInfo.VERSION_STRING + ')');
		getHeaders().put("Cache-Control", "private");
		getHeaders().put("Connection", "keep-alive");
		getHeaders().put("Keep-Alive", "200");
		getHeaders().put("Date", new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date()));
		getHeaders().put("Last-Modified", new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date()));
	}

	protected void header(String name,String value){
		getHeaders().put(name, value);
	}


	private final ByteBuffer headerBuf = ByteBuffer.allocateDirect(1024);
	
	protected final void sendHeaders(final IWriteCompleteListener completer,final IWriteErrorListener writeError){
		
		if (headersSent) {
			return;
		}
		
		try {
			headerBuf.clear();
			headerBuf.put("HTTP/1.1 ".getBytes(PWSConfig.CHARSET));
			headerBuf.put(String.valueOf(getResponseCode()).getBytes(PWSConfig.CHARSET));
			switch (getResponseCode()) {
			case HttpResponse.HTTP_STATUS_SUCCESS:
				headerBuf.put(" OK".getBytes(PWSConfig.CHARSET));
				break;
			case HttpResponse.HTTP_STATUS_NOT_FOUND:
				headerBuf.put(" Not Found".getBytes(PWSConfig.CHARSET));
				break;
			}
			headerBuf.put(CRLF);
			for (Entry<String, String> entry: getHeaders().entrySet()) {
				headerBuf.put(entry.getKey().getBytes(PWSConfig.CHARSET));
				headerBuf.put(": ".getBytes(PWSConfig.CHARSET));
				headerBuf.put(entry.getValue().getBytes(PWSConfig.CHARSET));
				headerBuf.put(CRLF);
			}
			// now the content length is the body length
			if (getContentLength()>0) {
				headerBuf.put("Content-Length: ".getBytes(PWSConfig.CHARSET));
				headerBuf.put(String.valueOf(getContentLength()).getBytes(PWSConfig.CHARSET));
			}
			headerBuf.put(CRLF);
			headerBuf.put(CRLF);

			headerBuf.flip();
			
			internal_write(headerBuf,new IWriteCompleteListener() {
				
				@Override
				public void completed() {
					setHeadersSent(true);
					completer.completed();
				}
			},writeError);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			close();
		}
	}
	
	
	private void internal_write(final ByteBuffer data,final IWriteCompleteListener completer,final IWriteErrorListener writeError,long delay,long period){
		
		Looper.loop(new Looper.Condition() {
			
			@Override
			public boolean condition() {
				return data.hasRemaining();
			}
		}, new Looper.Executer() {
			
			@Override
			public void execute() {
				try {
					native_write(data);
				} catch (IOException e) {
					log.config(e.toString());
					e.printStackTrace();
					
					if(writeError!=null) writeError.error(IWriteErrorListener.IO_ERROR);
					cancel();
				}
			}
		}, new Looper.Completer() {
			
			@Override
			public void onComplete() {
				if(completer!=null) completer.completed();
			}
		}, delay,period);
		
	}
	
	private void internal_write(final ByteBuffer data,final IWriteCompleteListener completer,final IWriteErrorListener writeError){
		internal_write(data, completer,writeError, 1, 100);
	}
	
	private int native_write(ByteBuffer data) throws IOException{
		return ((SocketChannel)getSelectionKey().channel()).write(data);
	}


	public void write(final ByteBuffer data,final IWriteCompleteListener completer,final IWriteErrorListener writeError,final long delay,final long period){
		internal_write(data,completer,writeError,delay,period);
	}
	
	public void write(final ByteBuffer data,final IWriteCompleteListener completer,final IWriteErrorListener writeError){
		write(data, completer,writeError, 0, 100);
	}


	public void setContentType(String contentType){
		header("Content-Type", String.format("%s; charset=%s", contentType,PWSConfig.CHARSET));
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

	public void close(){

		try {
			getSelectionKey().channel().close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		SelectionKeyAttachment attachment = (SelectionKeyAttachment) getSelectionKey().attachment();
		if (attachment!=null) {
			attachment.recycle();
		}
		
		getRequest().recycle();
		this.recycle();
	}

	public boolean isHeadersSent() {
		return headersSent;
	}

	private void setHeadersSent(boolean headerSent) {
		this.headersSent = headerSent;
	}


	private long contentLength=0;
	private final Map<String, String> headers = new HashMap<String, String>();
	private int responseCode=200;
	private boolean headersSent=false;
	
	
	public static interface IWriteCompleteListener{
		void completed();
	}
	
	public static interface IWriteErrorListener{
		
		/**
		 * May be IwriteError.IO_ERROR
		 * @param code
		 */
		void error(int code);
		
		int IO_ERROR=2;
		int UNSUPPORTED_ENCODING=3;
	}
}
