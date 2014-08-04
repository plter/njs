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

import com.plter.pws.pages.Page500;


public class Error500TextResponse extends TextResponse {
	
	
	
	public Error500TextResponse() {
		setResponseCode(HttpResponse.HTTP_STATUS_SERVER_ERROR);
	}

	@Override
	protected String makeTextContent() {
		return Page500.getContent();
	}

}
