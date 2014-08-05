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

package com.plter.njs.core.tests;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.logging.Logger;

import com.plter.njs.core.SocketFilter;

public class MyHandler extends SocketFilter {
	
	private static final Logger log = Logger.getLogger(MyHandler.class.toString());
	
	@Override
	public void onSocketChannelClose(SelectionKey selectionKey) {
		
		log.info("Socket close");
		
		super.onSocketChannelClose(selectionKey);
	}
	
	
	@Override
	public void onSocketChannelOpen(SelectionKey selectionKey) {
		
		log.info("Socket open");
		
		super.onSocketChannelOpen(selectionKey);
	}
	
	@Override
	public void onMessageReceived(SelectionKey selectionKey, Object message) {
		
		ByteBuffer b = (ByteBuffer) message;
		b.flip();
		
		writeMessage(selectionKey, b);
		
		super.onMessageReceived(selectionKey, message);
	}

}
