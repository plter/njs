package com.plter.njs.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Logger;

import com.plter.njs.log.LogFactory;

public class SocketAcceptThread extends Thread {


	public SocketAcceptThread(SocketAcceptor socketAcceptor,Selector selector) {
		this.selector = selector;
		this.socketAcceptor = socketAcceptor;
	}


	@Override
	public void run() {
		int bytesReaded=-1,selectedCount=0;
		SelectionKey selectedKey;
		SocketChannel socketChannel;

		while(true){

			try {
				selectedCount = selector.select();
			} catch (IOException e) {
				log.severe("IO error accur when run select()");
				break;
			}

			if (selectedCount<=0) {
				continue;
			}

			Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
			while(iterator.hasNext()){
				selectedKey = iterator.next();//>

				if(selectedKey.isValid()){
					if(selectedKey.isReadable()){
						socketChannel = (SocketChannel) selectedKey.channel();
						byteBufferForRead.clear();
						try {
							while((bytesReaded = socketChannel.read(byteBufferForRead))>0){
								socketAcceptor.getFilterChain().first().onMessageReceived(selectedKey, byteBufferForRead);
								byteBufferForRead.clear();
							}
						} catch (IOException e) {
							log.finest("IO error accur when read data from channel");
							closeClient(socketChannel, selectedKey);
						}
						
						if(bytesReaded==-1){
							closeClient(socketChannel, selectedKey);
						}
						
						bytesReaded = -1;
					}else if(selectedKey.isAcceptable()){
						try {
							socketChannel = ((ServerSocketChannel) selectedKey.channel()).accept();
							socketChannel.configureBlocking(false);
							socketAcceptor.getFilterChain().first().onSocketChannelOpen(socketChannel.register(selector, SelectionKey.OP_READ));
						} catch (IOException e) {
							log.severe("IO error accur when accept");
						}
					}
				}

				iterator.remove();//<
			}

			selectedCount = 0;
		}


		super.run();
	}

	private void closeClient(SocketChannel sc,SelectionKey selectedKey){
		try {
			sc.close();
			socketAcceptor.getFilterChain().first().onSocketChannelClose(selectedKey);
		} catch (IOException e) {
			log.severe("IO error accur when close SocketChannel");
		}
	}

	private Selector selector;
	private SocketAcceptor socketAcceptor;
	private final ByteBuffer byteBufferForRead = ByteBuffer.allocateDirect(1024);
	private static Logger log = LogFactory.getLogger();

}
