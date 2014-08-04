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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.plter.pws.conf.PWSConfig;

public abstract class TextResponse extends HttpResponse {

	
	@Override
	protected void handle(final IWriteCompleteListener completer, final IWriteErrorListener writeError) {
		try {

			//init data
			setContentType("text/html");	
			final byte[] bytes = makeTextContent().getBytes(PWSConfig.CHARSET);
			setContentLength(bytes.length);

			//send data
			sendHeaders(new HttpResponse.IWriteCompleteListener() {

				@Override
				public void completed() {
					write(ByteBuffer.wrap(bytes), completer,writeError);
				}
			},writeError);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	

	protected abstract String makeTextContent();

}
