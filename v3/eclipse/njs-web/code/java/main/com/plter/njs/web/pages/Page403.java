package com.plter.njs.web.pages;

import com.plter.njs.web.conf.ServerInfo;

public class Page403 {

	
	public static String getContent(){
		return "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><title>PWS 403</title></head><body><h1 align=\"center\">403</h1><hr width=\"90%\"><h3 align=\"center\">"+ServerInfo.VERSION_STRING+" <a href=\"http://www.plter.com\" target=\"_blank\">PLTER.COM</a></h3></body></html>";
	}
	
}
