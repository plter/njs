package com.plter.njs.web.system;

import java.net.URLClassLoader;



public class NJSystem {

	private static AppContext appContext=null;

	public static AppContext getAppContext() {
		return appContext;
	}

	public static void setAppContext(AppContext appContext) {
		NJSystem.appContext = appContext;
	}
	
	public static URLClassLoader getRootUrlClassLoader(){
		return getAppContext().getRootUrlClassLoader();
	}
}
