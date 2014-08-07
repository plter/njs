package com.plter.pws;


public class Boot {

	public static void main(String[] args) {

		PluginsLoader p = new PluginsLoader();

		System.out.println("Load lib");
		if(!p.load()){
			System.err.println("Can not load lib");
			return;
		}
		System.out.println("Start main method in plugin");
		if(!p.start()){
			System.err.println("Can not start method main");
			return;
		}
	}

}
