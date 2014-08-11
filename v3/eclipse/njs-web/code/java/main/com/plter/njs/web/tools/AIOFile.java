package com.plter.njs.web.tools;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.plter.lib.java.utils.LogFactory;

public class AIOFile {

	/**
	 * Read file data
	 * @param file
	 * @param progressListener
	 * @param completeListener
	 * @param errorListener
	 * @param fastMode If use fast mode
	 */
	public static void readFile(File file,
			OnAIOFileProgressListener progressListener,
			OnAIOFileCompleteListener completeListener,
			OnAIOFileErrorListener errorListener,boolean fastMode){

		new AIOFile(file, progressListener, completeListener, errorListener,fastMode);
	}

	private AIOFile(File file,
			OnAIOFileProgressListener progressListener,
			OnAIOFileCompleteListener completeListener,
			OnAIOFileErrorListener errorListener,boolean fastMode){

		this.progressListener = progressListener;
		this.completeListener = completeListener;
		this.errorListener = errorListener;
		this.fastMode = fastMode;

		fileSize = file.length();

		try {
			fileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);

			if (this.fastMode) {
				readData();
			}else{
				timerTask = new TimerTask() {

					@Override
					public void run() {
						readData();
					}
				};
				timer.schedule(timerTask, 10, 20);
			}
		} catch (IOException e) {
			if (this.errorListener!=null) {
				this.errorListener.onError(AIOFile.this,IO_ERROR);
			}
		}
	}

	private void readData(){
		dataForTransfor.clear();

		fileChannel.read(dataForTransfor, currentPosition, null, new CompletionHandler<Integer, Object>() {

			@Override
			public void completed(Integer result, Object attachment) {
				if (result==-1) {
					stopRead();
					
					if (errorListener!=null) {
						errorListener.onError(AIOFile.this,READ_DATA_ERROR);
					}
					return;
				}
				
				if (result==0) {
					if (fastMode) {
						readData();
					}
					return;
				}
				
				currentPosition+=result;
				dataForTransfor.flip();
				if (progressListener!=null) {
					progressListener.onProgress(AIOFile.this,dataForTransfor, ((double)currentPosition)/fileSize);
				}

				if (currentPosition<fileSize) {
					if (fastMode) {
						readData();
					}
				}else{
					//complete
					stopRead();

					if (completeListener!=null) {
						completeListener.onComplete(AIOFile.this);
					}
				}
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				if (errorListener!=null) {
					errorListener.onError(AIOFile.this,READ_DATA_ERROR);
				}

				stopRead();
			}
		});
	}

	public void stopRead(){
		try {
			fileChannel.close();
		} catch (IOException e) {
			log.finest("Error ocur when close file channel");
		}
		if(timerTask!=null){
			timerTask.cancel();
		}
	}

	private AsynchronousFileChannel fileChannel;
	private OnAIOFileProgressListener progressListener;
	private OnAIOFileCompleteListener completeListener;
	private OnAIOFileErrorListener errorListener;
	private ByteBuffer dataForTransfor = ByteBuffer.allocateDirect(1024*30);
	private long currentPosition = 0;
	private long fileSize = 0;
	private TimerTask timerTask=null;
	private boolean fastMode = false;

	private static final Timer timer = new Timer();
	private static final Logger log = LogFactory.getLogger();

	public static final int IO_ERROR = 2;
	public static final int READ_DATA_ERROR =3;
}
