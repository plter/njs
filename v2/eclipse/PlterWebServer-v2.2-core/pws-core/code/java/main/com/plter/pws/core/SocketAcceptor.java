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

package com.plter.pws.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Logger;

import com.plter.lib.java.utils.LogFactory;

public class SocketAcceptor{

	private static final Logger log = LogFactory.getLogger();

	public SocketAcceptor(int port){
		this.port=port;
		socketFilterChain=new SocketFilterChain(this);
	}

	public void listen() throws IOException{
		if (!getFilterChain().hasFilter()) {
			log.severe("There must be one filter at least");
			return;
		}

		//setup
		Selector selector = Selector.open();
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.socket().bind(new InetSocketAddress(port));

		log.info("Server started at port "+port);

		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		int selectedNum=0,bytesReaded=0;
		SelectionKey selectedKey;
		SocketChannel socketChannel;

		while(true){
			try {
				
				selectedNum = selector.select(300);

				if (selectedNum<=0) {
					continue;
				}

				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while(iterator.hasNext()){
					selectedKey = iterator.next();//>
					if(selectedKey.isValid()){
						if(selectedKey.isReadable()){
							socketChannel = (SocketChannel) selectedKey.channel();
							try{
								byteBufferForRead.clear();
								while((bytesReaded = socketChannel.read(byteBufferForRead))>0){
									getFilterChain().first().onMessageReceived(selectedKey, byteBufferForRead);
									byteBufferForRead.clear();
								}
								if(bytesReaded==-1){
									socketChannel.close();
									getFilterChain().first().onSocketChannelClose(selectedKey);
								}
							}catch(Exception err){
								if (socketChannel.isOpen()) {
									socketChannel.close();
									getFilterChain().first().onSocketChannelClose(selectedKey);
								}

								//TODO Unknown reason
//								log.config("This error can be ignore. >>>>>".concat(err.getClass().getName()).concat("<<<<<"));
							}
						}else if(selectedKey.isAcceptable()){
							socketChannel = ((ServerSocketChannel) selectedKey.channel()).accept();
							socketChannel.configureBlocking(false);
							getFilterChain().first().onSocketChannelOpen(socketChannel.register(selector, SelectionKey.OP_READ));
						}
					}
					iterator.remove();//<
				}


			} catch (IOException e) {
				e.printStackTrace();

				break;
			}
		}
	}


	private int port=8888;
	public int getPort() {
		return port;
	}

	private SocketFilterChain socketFilterChain=null;
	public SocketFilterChain getFilterChain() {
		return socketFilterChain;
	}

	private final ByteBuffer byteBufferForRead = ByteBuffer.allocateDirect(1024);

}
