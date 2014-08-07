package com.plter.njs.http.tests;

import java.nio.channels.SelectionKey;

import com.plter.njs.http.HttpRequest;
import com.plter.njs.http.HttpRequestDecoderFilter;
import com.plter.njs.http.HttpResponse;
import com.plter.njs.socket.BaseFilter;
import com.plter.njs.socket.SocketAcceptor;

public class UsingHttp {

	public static void main(String[] args) {
		SocketAcceptor acceptor = new SocketAcceptor(9999);
		acceptor.getFilterChain().push(new HttpRequestDecoderFilter());
		acceptor.getFilterChain().push(new BaseFilter(){
			
			@Override
			public void onMessageReceived(SelectionKey selectionKey,
					Object message) {
				
				HttpRequest request = (HttpRequest) message;
				
				new HttpResponse(){
					public void doHandle() {
						write("Hello Client");
					};
					
				}.handle(selectionKey, request);
				
				super.onMessageReceived(selectionKey, message);
			}
			
		});
		acceptor.listen();
	}
}
