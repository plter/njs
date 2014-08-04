package com.plter.pws.conf;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Logger;

import com.plter.lib.java.utils.LogFactory;
import com.plter.lib.java.xml.XML;
import com.plter.lib.java.xml.XMLRoot;

public class PWSConfig {
	
	private static final Logger log = LogFactory.getLogger();
	
	public static boolean loadConfig(){
		
		log.info("Load PWS config file");
		
		try {
			File f = new File(SERVER_CONF);
			FileInputStream fis = new FileInputStream(f);
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			fis.close();
			
			XMLRoot root = XMLRoot.parse(new String(bytes,"utf-8"));
			port= new Integer(root.getChild("port").getAttr("value"));
			webroot=root.getChild("web").getAttr("root");
			
			XML srv = root.getChild("srv");
			srvroot=srv.getAttr("root");
			
			XML srvDefaultAppXML = srv.getChild("defaultApp");
			srvDefaultApp = srvDefaultAppXML.getAttr("file");
			srvDefaultResponse = srvDefaultAppXML.getAttr("defaultResponse");
			
			processorCount=Integer.parseInt(root.getChild("pws").getAttr("processorCount"));
			XML pages = root.getChild("pages");
			page403=pages.getAttr("page403");
			page404=pages.getAttr("page404");
			page500=pages.getAttr("page500");
			String autoStartClassesString = root.getChild("autoStart").getAttr("classes");
			if (autoStartClassesString!=null&&!autoStartClassesString.equals("")) {
				autoStartClasses=autoStartClassesString.split(",");
			}
			
			defaultDoc=root.getChild("default").getAttr("doc").split(",");
			if (defaultDoc.length<=0) {
				log.severe("No default doc defined.");
				return false;
			}
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	
	public static int port=8080,processorCount=200;
	public static boolean logOn=false;
	public static String webroot=null;
	public static String srvroot=null;
	public static String srvDefaultApp=null;
	public static String srvDefaultResponse=null;
	public static String[] defaultDoc=null;
	public static String[] autoStartClasses=null;
	public static String page404=null,page403=null,page500=null;
	public static final String SERVER_CONF="conf/server.xml";
	public static final String CHARSET="utf-8";
	public static final CharsetDecoder CHARSET_DECODER = Charset.forName(CHARSET).newDecoder();
}
