package com.plter.pws;


public class Boot {

	public static void main(String[] args) {

		PluginsLoader p = new PluginsLoader();

		System.out.println("Load plugin");
		if(!p.load()){
			System.err.println("Can not load plugin");
			return;
		}
		System.out.println("Start main method in plugin");
		if(!p.start()){
			System.err.println("Can not start method main");
			return;
		}

	}

}
