package com.plter.pws.system;

import java.net.URLClassLoader;



public class PWSystem {

	private static AppContext appContext=null;

	public static AppContext getAppContext() {
		return appContext;
	}

	public static void setAppContext(AppContext appContext) {
		PWSystem.appContext = appContext;
	}
	
	public static URLClassLoader getRootUrlClassLoader(){
		return getAppContext().getRootUrlClassLoader();
	}
}
