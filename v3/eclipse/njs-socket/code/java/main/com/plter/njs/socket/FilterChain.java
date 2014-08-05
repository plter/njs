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

import java.util.ArrayList;
import java.util.List;

public final class FilterChain {
	
	
	public FilterChain(SocketAcceptor socketAcceptor) {
		this.socketAcceptor=socketAcceptor;
	}
	
	private final List<BaseFilter> socketFilters = new ArrayList<>();
	
	public BaseFilter push(BaseFilter filter){
		socketFilters.add(filter);
		filter.setIndex(socketFilters.size()-1);
		setupSocketFilter(filter);
		return filter;
	}
	
	
	public BaseFilter addAfter(String name,BaseFilter filter){
		for (int i = 0; i < socketFilters.size(); i++) {
			if(socketFilters.get(i).getName().equals(name)){
				insert(i+1,filter);
				break;
			}
		}
		return filter;
	}
	
	
	public BaseFilter addBefore(String name,BaseFilter filter){
		for (int i = 0; i < socketFilters.size(); i++) {
			if(socketFilters.get(i).getName().equals(name)){
				insert(i,filter);
				break;
			}
		}
		return filter;
	}
	
	
	public BaseFilter insert(int index,BaseFilter filter){
		socketFilters.add(index, filter);
		setupSocketFilter(filter);
		updateFiltersIndex();
		return filter;
	}
	
	public BaseFilter pop(){
		return socketFilters.remove(socketFilters.size()-1);
	}
	
	public BaseFilter shift(){
		BaseFilter sf = socketFilters.remove(0);
		updateFiltersIndex();
		return sf;
	}
	
	public BaseFilter remove(int index){
		BaseFilter sf = socketFilters.remove(index);
		updateFiltersIndex();
		return sf;
	}
	
	public BaseFilter first(){
		return get(0);
	}
	
	public BaseFilter last(){
		return get(socketFilters.size()-1);
	}
	
	public BaseFilter get(int index){
		return socketFilters.get(index);
	}
	
	public int filterCount(){
		return socketFilters.size();
	}
	
	public boolean hasFilter(){
		return filterCount()>0;
	}
	
	private void setupSocketFilter(BaseFilter filter){
		filter.setSocketAcceptor(socketAcceptor);
		filter.setSocketFilterChain(this);
	}
	
	private void updateFiltersIndex(){
		for (int i = 0; i < socketFilters.size(); i++) {
			socketFilters.get(i).setIndex(i);
		}
	}
	
	private SocketAcceptor socketAcceptor=null;
	public SocketAcceptor getSocketAcceptor(){
		return socketAcceptor;
	}
	
}
