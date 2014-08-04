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

package com.plter.pws.core.tests;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class EchoServer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Selector selector = Selector.open();

		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		InetSocketAddress address = new InetSocketAddress(9000);
		serverSocketChannel.bind(address);


		SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


		while (true){
			int selectedNum = selector.select();
			System.out.println("Selected Number is :"+selectedNum);
			Iterator iter = selector.selectedKeys().iterator();

			while(iter.hasNext()){
				SelectionKey selectedKey = (SelectionKey)iter.next();

				if ((selectedKey.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT){
					ServerSocketChannel serverChannel = (ServerSocketChannel)selectedKey.channel();
					SocketChannel socketChannel = serverChannel.accept();
					socketChannel.configureBlocking(false);

					SelectionKey readKey = socketChannel.register(selector, SelectionKey.OP_READ);                    
					iter.remove();

					//System.out.println("buffer: "+new String(buffer.array()));                
				}
				else if ( (selectedKey.readyOps()&SelectionKey.OP_READ) == SelectionKey.OP_READ ){
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					SocketChannel socketChannel = (SocketChannel)selectedKey.channel();
					while (true){
						buffer.clear();
						int i=socketChannel.read(buffer);

						if (i == -1) break;

						buffer.flip();
						socketChannel.write(buffer);
					}
				}
				
			}

		}
	}

}
