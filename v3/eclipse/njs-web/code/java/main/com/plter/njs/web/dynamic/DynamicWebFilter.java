package com.plter.njs.web.dynamic;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.SelectionKey;
import java.util.WeakHashMap;

import com.plter.njs.http.HttpRequest;
import com.plter.njs.http.HttpResponse;
import com.plter.njs.socket.BaseFilter;
import com.plter.njs.web.conf.PWSConfig;
import com.plter.njs.web.system.NJSystem;

public final class DynamicWebFilter extends BaseFilter{


	@Override
	public void onMessageReceived(SelectionKey selectionKey, Object message) {

		HttpRequest request = (HttpRequest) message;

		String[] m_c = request.getContext().split("/");

		switch (m_c.length) {
		case 0:
			handleDynamicWebRequest(selectionKey, message, request, PWSConfig.srvDefaultApp, PWSConfig.srvDefaultResponse);
			break;
		case 2:
			handleDynamicWebRequest(selectionKey, message, request, PWSConfig.srvDefaultApp, m_c[1]);
			break;
		case 3:
			handleDynamicWebRequest(selectionKey, message, request, m_c[1], m_c[2]);
			break;
		default:
			super.onMessageReceived(selectionKey, message);
			break;
		}
	}

	private final void handleDynamicWebRequest(final SelectionKey selectionKey,final Object message,final HttpRequest request,final String moduleName,final String className){
		try{
			File mFile = new File(String.format("%s/%s.jar",PWSConfig.srvroot,moduleName));
			String mFileUrl = mFile.getAbsolutePath();

			URLClassLoader mLoader=null;
			Lib lib=libFiles.get(mFileUrl);

			if (lib!=null) {
				if (lib.lastModified!=mFile.lastModified()) {
					if (mFile.exists()) {
						mLoader = URLClassLoader.newInstance(new URL[]{mFile.toURI().toURL()},NJSystem.getRootUrlClassLoader());
						libFiles.put(mFileUrl, new Lib(mLoader, mFile));
					}else{
						mLoader=lib.urlClassLoader;
					}
				}else{
					mLoader=lib.urlClassLoader;
				}
			}else{
				if (mFile.exists()) {
					mLoader = URLClassLoader.newInstance(new URL[]{mFile.toURI().toURL()},NJSystem.getRootUrlClassLoader());
					libFiles.put(mFileUrl, new Lib(mLoader, mFile));
				}else{
					super.onMessageReceived(selectionKey, message);
				}
			}

			if(mLoader!=null){
				Class<?> c = mLoader.loadClass(className);
				((HttpResponse)c.newInstance()).handle(selectionKey, request);
			}

		}catch(ClassNotFoundException classNotFoundException){
			super.onMessageReceived(selectionKey, message);
		}catch(Exception e){
			e.printStackTrace();

			ErrorResponses.handle500(selectionKey, request);
		}
	}


	private static final WeakHashMap<String, Lib> libFiles = new WeakHashMap<String, Lib>();
	static class Lib{
		public Lib(URLClassLoader urlClassLoader,File file) {
			this.urlClassLoader=urlClassLoader;
			this.file=file;
			lastModified=file.lastModified();
		}
		public URLClassLoader urlClassLoader=null;
		public File file=null;
		public long lastModified=0;
	}
}
