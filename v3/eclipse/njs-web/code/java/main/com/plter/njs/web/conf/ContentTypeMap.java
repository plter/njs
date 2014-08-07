package com.plter.njs.web.conf;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.plter.lib.java.utils.LogFactory;
import com.plter.lib.java.xml.XML;
import com.plter.lib.java.xml.XMLList;
import com.plter.lib.java.xml.XMLRoot;


/**
 * 
 * @author xtiqin http://plter.com
 *
 */
public class ContentTypeMap {

	private static final Logger log = LogFactory.getLogger();
	
	private static final Map<String, String> contentTypeMap = new HashMap<String, String>();
	public static final String CONTENT_TYPE_FILE="conf/content-type.xml";
	
	public static boolean loadContentTypesMap(){
		
		log.info("Load content types map file");
		
		try {
			File f = new File(CONTENT_TYPE_FILE);
			FileInputStream fis = new FileInputStream(f);
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			fis.close();
			XMLRoot root = XMLRoot.parse(new String(bytes,"utf-8"));
			XML children = root.getChild("file");
			XML child;
			
			if (children instanceof XMLList) {
				XMLList list = (XMLList) children;
				
				for (int i = 0; i < list.length(); i++) {
					child=list.get(i);
					contentTypeMap.put(child.getAttr("type"), child.getAttr("contentType"));
				}
			}else{
				contentTypeMap.put(children.getAttr("type"), children.getAttr("contentType"));
			}
			
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public static String get(String fileType){
		return fileType!=null?contentTypeMap.get(fileType):null;
	}

}
