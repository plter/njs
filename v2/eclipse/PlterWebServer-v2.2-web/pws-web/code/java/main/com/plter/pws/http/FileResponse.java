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

package com.plter.pws.http;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

import com.plter.lib.java.utils.LogFactory;
import com.plter.pws.conf.ContentTypeMap;


public class FileResponse extends HttpResponse {

	private static final Logger log = LogFactory.getLogger();

	public FileResponse() {

	}

	protected void handle(final IWriteCompleteListener completer,final IWriteErrorListener writeError) {
		if (getFile().isFile()) {
			if (getContentType()!=null) {
				setupHeaders();
				setResponseCode(HTTP_STATUS_SUCCESS);
				setContentLength(getFile().length());

				sendHeaders(new HttpResponse.IWriteCompleteListener() {

					@Override
					public void completed() {
						fileDataReadedLength=0;
						writeFileOutput(getFile(),completer,writeError,0);
					}
				},writeError);

			}else{
				ErrorResponses.handle404(getSelectionKey(), getRequest());
			}
		}
	};


	protected void writeFileOutput(File f,final HttpResponse.IWriteCompleteListener completeCallback,final IWriteErrorListener writeError,long position){
		try {

			AsynchronousFileChannel afc = AsynchronousFileChannel.open(getFile().toPath(), StandardOpenOption.READ);

			writeFileOutputAttachment.setAsynchronousFileChannel(afc);
			writeFileOutputAttachment.setWriteOutputFileCompleteCallback(completeCallback);
			byteBuffer.clear();
			writeFileOutputAttachment.setByteBuffer(byteBuffer);
			afc.read(byteBuffer, position, writeFileOutputAttachment, completionHandler);
		} catch (IOException e) {
			close();
			e.printStackTrace();
		}
	}
	private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024*30);

	private final CompletionHandler<Integer, WriteFileOutputAttachment> completionHandler = new CompletionHandler<Integer, WriteFileOutputAttachment>() {

		@Override
		public void completed(final Integer result,final WriteFileOutputAttachment attachment) {

			if(result>0){
				attachment.getByteBuffer().flip();

				write(byteBuffer, new IWriteCompleteListener() {

					@Override
					public void completed() {
						fileDataReadedLength+=result;
						if(fileDataReadedLength<fileLength){
							attachment.getByteBuffer().clear();
							attachment.getAsynchronousFileChannel().read(attachment.getByteBuffer(), fileDataReadedLength, attachment, completionHandler);
						}else{
							try {
								attachment.getAsynchronousFileChannel().close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							if(attachment.getWriteOutputFileCompleteCallback()!=null) attachment.getWriteOutputFileCompleteCallback().completed();
						}
					}
				},new IWriteErrorListener() {
					@Override
					public void error(int code) {
						close();
						try {
							attachment.getAsynchronousFileChannel().close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				},writeDataDelay,writeDataPeriod);
			}

		}

		@Override
		public void failed(Throwable exc, WriteFileOutputAttachment attachment) {
			close();
			attachment.getByteBuffer().clear();
			try {
				attachment.getAsynchronousFileChannel().close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			log.severe("Error occurred when reading file,".concat(getFile().getAbsolutePath()));
		}
	};

	public File getFile() {
		return file;
	}

	public FileResponse setFile(File file) {
		this.file = file;
		fileLength=file.length();
		String name = file.getName();
		setContentType(ContentTypeMap.get(name.substring(name.lastIndexOf('.')+1)));


		//If the file size is bigger than 2M,we limit it's download speed up to 30KB,the speed is also affected by this.byteBuffer size.
		if (fileLength<=2048000) {
			writeDataDelay=0;
			writeDataPeriod=100;
		}else{
			writeDataDelay=1000;
			writeDataPeriod=1000;
		}
		return this;
	}

	private File file = null;
	private long fileLength=0;
	protected long fileDataReadedLength=0;
	private int writeDataDelay=100;
	private int writeDataPeriod=100;

	private final WriteFileOutputAttachment writeFileOutputAttachment = new WriteFileOutputAttachment(null, null,null);

	private static final class WriteFileOutputAttachment{

		public WriteFileOutputAttachment(AsynchronousFileChannel afc,ByteBuffer byteBuffer,IWriteCompleteListener writeOutputFileCompleteCallback) {
			asynchronousFileChannel=afc;
			this.writeOutputFileCompleteCallback=writeOutputFileCompleteCallback;
			setByteBuffer(byteBuffer);
		}

		public IWriteCompleteListener getWriteOutputFileCompleteCallback() {
			return writeOutputFileCompleteCallback;
		}
		public void setWriteOutputFileCompleteCallback(
				HttpResponse.IWriteCompleteListener writeOutputFileCompleteCallback) {
			this.writeOutputFileCompleteCallback = writeOutputFileCompleteCallback;
		}

		public AsynchronousFileChannel getAsynchronousFileChannel() {
			return asynchronousFileChannel;
		}

		public void setAsynchronousFileChannel(AsynchronousFileChannel asynchronousFileChannel) {
			this.asynchronousFileChannel = asynchronousFileChannel;
		}

		public ByteBuffer getByteBuffer() {
			return byteBuffer;
		}

		public void setByteBuffer(ByteBuffer byteBuffer) {
			this.byteBuffer = byteBuffer;
		}

		private AsynchronousFileChannel asynchronousFileChannel=null;
		private IWriteCompleteListener writeOutputFileCompleteCallback=null;
		private ByteBuffer byteBuffer=null;
	}

}
