package com.plter.njs.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogFactory {

public static final String LOG_NAME="com.plter.njs.log";
	
	private static Logger log = null;
	public static final Logger getLogger(){
		if (log==null) {
			log = Logger.getLogger(LOG_NAME);
			log.setUseParentHandlers(false);
			ConsoleHandler ch = new ConsoleHandler();
			ch.setLevel(Level.ALL);
			log.setLevel(Level.ALL);
			log.addHandler(ch);
		}
		return log;
	}
}
