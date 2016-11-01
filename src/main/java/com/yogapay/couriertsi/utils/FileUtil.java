package com.yogapay.couriertsi.utils;

import java.io.File;

public class FileUtil {
	
	/**
	 *   删除文件
	 * @param file 文件名
	 * @return 
	 */
	
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName) ;
		if (file.isFile()) {
			return file.delete() ;
		}else {
			return false ;
		}
	}

}
