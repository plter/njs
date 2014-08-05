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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.plter.pws.conf.PWSConfig;


public abstract class DynamicWebResponse extends HttpResponse {



	@Override
	void handle(SelectionKey selectionKey, HttpRequest request) {
		setSelectionKey(selectionKey);
		setRequest(request);

		setKeepAlive(false);
		setupHeaders();
		setContentType("text/html");
		setContentLength(0);
		setResponseCode(HTTP_STATUS_SUCCESS);
		bos=new ByteArrayOutputStream();

		handle(new HttpResponse.IWriteCompleteListener() {

			@Override
			public void completed() {

				sendHeaders(new HttpResponse.IWriteCompleteListener() {

					@Override
					public void completed() {

						writeBody(new HttpResponse.IWriteCompleteListener() {

							@Override
							public void completed() {
								try {bos.close();} catch (IOException e) {}

								if (!keepAlive) {
									close();
								}
							}
						},new IWriteErrorListener() {
							
							@Override
							public void error(int code) {
								close();
							}
						});
					}
				},new IWriteErrorListener() {
					
					@Override
					public void error(int code) {
						close();
					}
				});

			}
		},new IWriteErrorListener() {
			
			@Override
			public void error(int code) {
				close();
			}
		});
	}


	@Override
	public void write(ByteBuffer data,final HttpResponse.IWriteCompleteListener completer,final IWriteErrorListener writeError) {
		if (isHeadersSent()) {
			super.write(data,completer,writeError);
		}else{
			if (bos==null) {
				return;
			}

			byte[] bytes = new byte[data.remaining()];
			data.get(bytes, 0, bytes.length);
			try {
				bos.write(bytes);
				if(completer!=null) completer.completed();
			} catch (IOException e) {
				e.printStackTrace();
				if(writeError!=null) writeError.error(IWriteErrorListener.IO_ERROR);
			}
		}
	}


	public void write(String str,final HttpResponse.IWriteCompleteListener completer,final IWriteErrorListener writeError){
		write(str,PWSConfig.CHARSET,completer,writeError);
	}
	
	public void write(String str,IWriteCompleteListener completeListener){
		write(str, completeListener, null);
	}
	
	public void write(String str){
		write(str, null);
	}

	public void write(String str,String charset,final HttpResponse.IWriteCompleteListener completer,final IWriteErrorListener writeError){
		try {
			write(ByteBuffer.wrap(str.getBytes(charset)),completer,writeError);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			if(writeError!=null) writeError.error(IWriteErrorListener.UNSUPPORTED_ENCODING);
		}
	}


	/**
	 * Send header and body data
	 * @throws IOException 
	 */
	private void writeBody(final HttpResponse.IWriteCompleteListener completer,final IWriteErrorListener writeError){
		byte[] bytes = bos.toByteArray();
		super.write(ByteBuffer.wrap(bytes),completer,writeError);
	}


	public final boolean isKeepAlive() {
		return keepAlive;
	}


	public final void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}


	private ByteArrayOutputStream bos = null;
	private boolean keepAlive=false;

}
