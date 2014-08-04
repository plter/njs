package com.plter.pws.pages;

import com.plter.pws.conf.ServerInfo;

public class Page500 {

	
	public static String getContent(){
		return "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><title>PWS 500</title></head><body><h1 align=\"center\">500</h1><hr width=\"90%\"><h3 align=\"center\">"+ServerInfo.VERSION_STRING+" <a href=\"http://www.plter.com\" target=\"_blank\">PLTER.COM</a></h3></body></html>";
	}
	
}
