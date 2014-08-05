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

import com.plter.pws.core.SocketAcceptor;

public class UsingSockAcceptor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			SocketAcceptor acceptor = new SocketAcceptor(8888);
			acceptor.getFilterChain().push(new MyHandler());
			acceptor.listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
