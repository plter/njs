package com.plter.pws.system;

import com.plter.lib.java.event.Event;

public class AppContextEvent extends Event{

	/**
	 * 构建一个事件对象
	 * @param type	事件的类型
	 * @param data	事件所带的数据，在涉及到跨域操作时，此对象是简单对象或者经过序列化后的对象
	 */
	public AppContextEvent(String type, Object data) {
		super(type, data);
	}

}
