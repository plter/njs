package com.plter.njs.web.tools;

import java.nio.ByteBuffer;

public interface OnAIOFileProgressListener {

	/**
	 * 
	 * @param data		The data read
	 * @param percent	The total progress
	 */
	void onProgress(AIOFile target,ByteBuffer data,double percent);
}
