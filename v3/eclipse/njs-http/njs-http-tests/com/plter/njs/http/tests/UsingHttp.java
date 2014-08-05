package com.plter.njs.http.tests;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.plter.njs.http.HttpRequestDecoderFilter;
import com.plter.njs.http.HttpResponseHeader;
import com.plter.njs.socket.SocketAcceptor;
import com.plter.njs.socket.BaseFilter;

public class UsingHttp {

	public static void main(String[] args) {
		SocketAcceptor acceptor = new SocketAcceptor(9999);
		acceptor.getFilterChain().push(new HttpRequestDecoderFilter());
		acceptor.getFilterChain().push(new BaseFilter(){
			
			@Override
			public void onMessageReceived(SelectionKey selectionKey,
					Object message) {
				
				try {
					writeMessage(selectionKey, new HttpResponseHeader().getHeaderBuf());
					writeMessage(selectionKey, ByteBuffer.wrap("Hello Client".getBytes("utf-8")));
					close(selectionKey);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				super.onMessageReceived(selectionKey, message);
			}
			
		});
		acceptor.listen();
	}
}
