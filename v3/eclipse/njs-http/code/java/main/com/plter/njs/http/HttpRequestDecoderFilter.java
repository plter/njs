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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.CharacterCodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.plter.njs.log.LogFactory;
import com.plter.njs.socket.BaseFilter;

public final class HttpRequestDecoderFilter extends BaseFilter {

	private static final Logger log = LogFactory.getLogger();

	@Override
	public void onSocketChannelOpen(SelectionKey selectionKey) {
		selectionKey.attach(new SelectionKeyAttachment());
		super.onSocketChannelOpen(selectionKey);
	}


	@Override
	public void onMessageReceived(SelectionKey selectionKey, Object message) {

		ByteBuffer msg = (ByteBuffer) message;
		msg.flip();
		
		SelectionKeyAttachment attachment = (SelectionKeyAttachment) selectionKey.attachment();
		String httpMethod = attachment.getHttpMethod();
		if(!attachment.isRequestCompleted()){
			attachment.getHttpRequestData().put(msg);
			attachment.getHttpRequestData().flip();

			if (requestComplete(selectionKey, attachment.getHttpRequestData())) {
				httpMethod = attachment.getHttpMethod();

				if("GET".equals(httpMethod)){
					decodeGetRequest(next(), selectionKey, attachment.getHttpRequestData());
				}else if("POST".equals(httpMethod)){
					decodePostRequest(next(), selectionKey, attachment.getHttpRequestData());
				}else{
					log.warning("Unsupport HTTP Method");
					close(selectionKey);
					return;
				}
			}
		}else{
			if ("POST".equals(httpMethod)) {
				byte[] bytes = new byte[msg.remaining()];
				msg.get(bytes);
				
				try {
					
					if (!attachment.getHttpRequest().isPostDataCompleted()) {
						attachment.getHttpRequest().writePostBuffedData(bytes);
						
						if (attachment.getHttpRequest().isPostDataCompleted()) {
							next().onMessageReceived(selectionKey, attachment.getHttpRequest());
						}
						
					}else{
						
						close(selectionKey);
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					close(selectionKey);
				}
			}else if("GET".equals(httpMethod)){
				log.warning("Received content data from http get method request.");
				close(selectionKey);
				return;
			}else{
				log.warning("Unsupport HTTP Method");
				close(selectionKey);
				return;
			}
		}
	}


	private final HttpRequest decodeRequestHeader(SelectionKey selectionKey,String headerData){
		Map<String, String> map = new HashMap<String, String>();
		
		BufferedReader br=null;
		br = new BufferedReader(new StringReader(headerData));
		
		try{
			String line = br.readLine();

			String[] url = line.split(" ");
			if(url==null||url.length<3){selectionKey.channel().close();return null;}
			map.put("URI", url[1]);map.put("Method", url[0]);map.put("Protocol", url[2]);

			while((line=br.readLine())!=null){
				String[] tokens = line.split(": ");
				if (tokens==null||tokens.length<2) {break;}
				map.put(tokens[0], tokens[1]);
			}

			//read get args
			map.put("Context",url[1]);
			String paramStr=null;
			int idx = url[1].indexOf('?');
			if (idx > -1) {
				map.put("Context",url[1].substring(0, idx));
				paramStr = url[1].substring(idx + 1);

				if (paramStr != null) {
					String[] match = paramStr.split("&");
					for (String element : match) {
						String[] tokens = element.split("=");
						if (tokens.length==2) {
							map.put("@".concat(tokens[0]), tokens[1]);
						}
					}
				}
			}
			
			return new HttpRequest().setMap(map);
			
		}catch(IOException ioerr){
			ioerr.printStackTrace();
			
			close(selectionKey);
		}finally{
			try {br.close();} catch (IOException e) {}
		}

		return null;
	}
	
	@Override
	public void close(SelectionKey selectionKey) {
		super.close(selectionKey);
	}


	private final void decodeGetRequest(BaseFilter nextFilter,SelectionKey selectionKey,ByteBuffer dataBuf){
		
		HttpRequest request;
		try {
			request = decodeRequestHeader(selectionKey, NJSHttpConfig.CHARSET_DECODER.decode(dataBuf).toString());
			if (request!=null) {
				((SelectionKeyAttachment)selectionKey.attachment()).setHttpRequest(request);
				nextFilter.onMessageReceived(selectionKey, request);
			}else{
				close(selectionKey);
			}
		} catch (CharacterCodingException e) {
			e.printStackTrace();
			close(selectionKey);
		}
		
	}

	private void decodePostRequest(BaseFilter nextFilter,SelectionKey selectionKey,ByteBuffer dataBuf){
		
		SelectionKeyAttachment attachment = (SelectionKeyAttachment) selectionKey.attachment();
		
		byte[] bytes = new byte[attachment.getPostHeaderEnd()];
		dataBuf.get(bytes);
		try {
			HttpRequest request = decodeRequestHeader(selectionKey,new String(bytes, NJSHttpConfig.CHARSET));
			
			if (request!=null) {
				attachment.setHttpRequest(request);
				
				if (dataBuf.hasRemaining()) {
					byte[] b = new byte[dataBuf.remaining()];
					dataBuf.get(b);
					try {
						request.writePostBuffedData(b);
						
						if (request.isPostDataCompleted()) {
							nextFilter.onMessageReceived(selectionKey, request);
						}
					} catch (IOException e) {
						e.printStackTrace();
						close(selectionKey);
					}
				}else if (request.getContentLength()==0) {
					nextFilter.onMessageReceived(selectionKey, request);
				}
				
			}else{
				close(selectionKey);
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			close(selectionKey);
		}
		
	}


	private boolean requestComplete(SelectionKey selectionKey,ByteBuffer dataBuf){
		int last = dataBuf.remaining() - 1;
		SelectionKeyAttachment attachment = (SelectionKeyAttachment) selectionKey.attachment();

		if (dataBuf.remaining() < 4) {
			return false;
		}

		if (dataBuf.get(0) == (byte) 'G' && dataBuf.get(1) == (byte) 'E'
				&& dataBuf.get(2) == (byte) 'T') {
			// Http GET request therefore the last 4 bytes should be 0x0D 0x0A 0x0D 0x0A
			attachment.setHttpMethod(HttpMethod.GET);
			attachment.setRequestCompleted(true);

			return dataBuf.get(last) == (byte) 0x0A
					&& dataBuf.get(last - 1) == (byte) 0x0D
					&& dataBuf.get(last - 2) == (byte) 0x0A && dataBuf.get(last - 3) == (byte) 0x0D;
		} else if (dataBuf.get(0) == (byte) 'P' && dataBuf.get(1) == (byte) 'O'
				&& dataBuf.get(2) == (byte) 'S' && dataBuf.get(3) == (byte) 'T') {
			// Http POST request
			// first the position of the 0x0D 0x0A 0x0D 0x0A bytes
			int postHeaderEnd = -1;
			for (int i = 4; i < dataBuf.limit()-3; i++) {
				if (dataBuf.get(i) == (byte) 0x0D && dataBuf.get(i + 1) == (byte) 0x0A
						&& dataBuf.get(i + 2) == (byte) 0x0D
						&& dataBuf.get(i + 3) == (byte) 0x0A) {
					postHeaderEnd = i + 4;
					break;
				}
			}
			if(postHeaderEnd>3){
				attachment.setHttpMethod(HttpMethod.POST);
				attachment.setPostHeaderEnd(postHeaderEnd);
				attachment.setRequestCompleted(true);
				return true;
			}
		}else{
			close(selectionKey);
		}

		// the message is not complete and we need more data
		return false;
	}
}
