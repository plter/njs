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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class SocketFilter {

	private static final Logger log = Logger.getLogger(SocketFilter.class.toString());

	public void onSocketChannelOpen(SelectionKey selectionKey) {
		if(hasNext()){
			next().onSocketChannelOpen(selectionKey);
		}
	}

	public void onMessageReceived(SelectionKey selectionKey,Object message){
		if (hasNext()) {
			next().onMessageReceived(selectionKey, message);
		}
	}

	public void onSocketChannelClose(SelectionKey selectionKey){
		if (hasNext()) {
			next().onSocketChannelClose(selectionKey);
		}
	}

	public void writeMessage(SelectionKey selectionKey,Object message){
		if (hasPre()) {
			pre().writeMessage(selectionKey,message);
		}else{
			try {
				((SocketChannel)selectionKey.channel()).write((ByteBuffer) message);
			} catch (IOException e) {
				exceptionCaught(selectionKey,e);
			}
		}
	}

	public void close(SelectionKey selectionKey){
		try {
			if(selectionKey.channel().isOpen()){
				selectionKey.channel().close();
				onSocketChannelClose(selectionKey);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void exceptionCaught(SelectionKey selectionKey,Throwable err){
		close(selectionKey);

		log.severe(err.toString());
	}


	public final SocketFilter next(){
		return getSocketFilterChain().get(getIndex()+1);
	}

	public final SocketFilter pre(){
		return getSocketFilterChain().get(getIndex()-1);
	}

	public final boolean hasPre(){
		return getIndex()>0;
	}

	public final boolean hasNext(){
		return getIndex()<getSocketFilterChain().filterCount()-1;
	}

	public final String getName(){
		if (name==null) {
			name = getClass().getName();
		}
		return name;
	}
	private String name=null;


	private int index=0;final void setIndex(int index) {this.index = index;}public final int getIndex() {return index;}
	private SocketAcceptor socketAcceptor=null;void setSocketAcceptor(SocketAcceptor socketAcceptor) {this.socketAcceptor = socketAcceptor;}public SocketAcceptor getSocketAcceptor() {return socketAcceptor;}
	private SocketFilterChain socketFilterChain=null;public SocketFilterChain getSocketFilterChain() {return socketFilterChain;}void setSocketFilterChain(SocketFilterChain socketFilterChain) {this.socketFilterChain = socketFilterChain;}
}
