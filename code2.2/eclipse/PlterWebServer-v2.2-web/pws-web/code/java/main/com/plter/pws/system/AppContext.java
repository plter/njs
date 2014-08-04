package com.plter.pws.system;

import java.net.URLClassLoader;

import com.plter.lib.java.event.EventListenerList;

public class AppContext extends EventListenerList<AppContextEvent>{

	public AppContext(URLClassLoader rootUrlClassLoader) {
		setRootUrlClassLoader(rootUrlClassLoader);
	}
	
	public URLClassLoader getRootUrlClassLoader() {
		return rootUrlClassLoader;
	}
	private void setRootUrlClassLoader(URLClassLoader rootUrlClassLoader) {
		this.rootUrlClassLoader = rootUrlClassLoader;
	}
	private URLClassLoader rootUrlClassLoader=null;
}
