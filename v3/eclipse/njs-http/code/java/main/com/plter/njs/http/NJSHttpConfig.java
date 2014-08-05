package com.plter.njs.http;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class NJSHttpConfig {

	public static final String CHARSET = "utf-8";
	public static final CharsetDecoder CHARSET_DECODER = Charset.forName("utf-8").newDecoder();
}
