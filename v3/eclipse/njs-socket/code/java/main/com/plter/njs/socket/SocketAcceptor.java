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

package com.plter.njs.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Logger;

import com.plter.lib.java.utils.LogFactory;

public class SocketAcceptor{

	private static final Logger log = LogFactory.getLogger();

	public SocketAcceptor(int port){
		this.port=port;
		socketFilterChain=new FilterChain(this);
	}

	public boolean listen(){
		if (!getFilterChain().hasFilter()) {
			log.severe("There must be one filter at least");
			return false;
		}

		//setup
		Selector selector;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			log.severe("Can't open selector to listen the server");
			return false;
		}
		ServerSocketChannel serverSocketChannel;
		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(port));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			log.severe("Can't open ServerSocketChannel");
			return false;
		}

		new SocketAcceptThread(this, selector).start();
		
		log.info("Server started at port "+port);
		return true;
	}


	private int port=8888;
	public int getPort() {
		return port;
	}

	private FilterChain socketFilterChain=null;
	public FilterChain getFilterChain() {
		return socketFilterChain;
	}

}
