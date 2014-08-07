package com.plter.pws;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class PluginsLoader {
	
	public PluginsLoader() {
	}
	
	public boolean load(){
		File pluginsDir = new File("plugins");
		File[] plugins = pluginsDir.listFiles();
		if(plugins==null||plugins.length<=0){
			return false;
		}
		
		File plugin;
		
		ArrayList<URL> pluginUrls = new ArrayList<URL>();
		String fileName=null;
		
		for (int i = 0; i < plugins.length; i++) {
			plugin = plugins[i];
			fileName=plugin.getName();
			if (fileName.endsWith(".jar")||fileName.endsWith(".JAR")) {
				try {
					pluginUrls.add(plugin.toURI().toURL());
					System.out.println("Add file "+fileName);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		URL[] urls = new URL[pluginUrls.size()];
		for (int i = 0; i < urls.length; i++) {
			urls[i]=pluginUrls.get(i);
		}
		
		urlClassLoader = URLClassLoader.newInstance(urls,ClassLoader.getSystemClassLoader());
		return true;
	}
	
	
	public boolean start(){
		Class<?> c;
		try {
			c = urlClassLoader.loadClass("com.plter.njs.web.NJS");
			c.getMethod("start",URLClassLoader.class).invoke(c.newInstance(),urlClassLoader);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private URLClassLoader urlClassLoader=null;
}
