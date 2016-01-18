/* 
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anyline.util;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class MD5Util {
	private static Logger LOG = Logger.getLogger(MD5Util.class); 
	private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"}; 
	/**
	 * 字符MD5加密
	 * @param str
	 * @return
	 */
	public static String crypto(String str){
		if(null == str) return null;
		String result = null;
        if (!"".equals(str)){     
            try{
                MessageDigest md = MessageDigest.getInstance("MD5"); 
                //使用指定的字节数组对摘要进行最后更新，然后完成摘要计算     
                byte[] results = md.digest(str.getBytes());     
                //将得到的字节数组变成字符串返回     
                result = byteArrayToHexString(results);     
            } catch(Exception ex){     
                LOG.debug(ex);     
            }     
        }
        return result;     
    } 
	public static String crypto2(String str){
		return crypto(crypto(str));
	}
/////////////////////////////////////////////////////////////////////
    /**
    * 获取单个文件的MD5值！
    * @param file
    * @return
    */
    public static String getFileMD5(File file) {
	    if (null == file || !file.isFile() || !file.exists()){
	    	return null;
	    }
	    MessageDigest digest = null;
	    FileInputStream in=null;
	    byte buffer[] = new byte[1024];
	    int len;
	    try {
		    digest = MessageDigest.getInstance("MD5");
		    in = new FileInputStream(file);
		    while ((len = in.read(buffer, 0, 1024)) != -1) {
		    	digest.update(buffer, 0, len);
		    }
		    in.close();
	    } catch (Exception e) {
	    	LOG.error(e);
	    return null;
	    }
	    BigInteger bigInt = new BigInteger(1, digest.digest());
	    return bigInt.toString(16);
    }

    /**
    * 获取文件夹中文件的MD5值
    * @param file
    * @param recursion ;true递归子目录中的文件
    * @return
    */
    public static Map<String, String> getDirMD5(File file,boolean recursion) {
	    if(null == file || !file.isDirectory() || !file.exists()){
	    	return null;
	    }
	    Map<String, String> map=new HashMap<String, String>();
	    String md5;
	    File files[]=file.listFiles();
	    for(int i=0;i<files.length;i++){
	    	File f=files[i];
	    	if(f.isDirectory()&&recursion){
	    		map.putAll(getDirMD5(f, recursion));
	    	} else {
	    		md5=getFileMD5(f);
	    		if(md5!=null){
	    			map.put(f.getPath(), md5);
	    		}
	    	}
	    }
	    return map;
    }

    private static String byteArrayToHexString(byte[] b){     
        StringBuilder builder = new StringBuilder();     
        for (int i = 0; i < b.length; i++){     
            builder.append(byteToHexString(b[i]));     
        }     
        return builder.toString();     
    }     
    /**
     * 将一个字节转化成十六进制形式的字符串
     * @param b
     * @return
     */
    private static String byteToHexString(byte b){     
        int n = b;     
        if (n < 0)     
            n = 256 + n;     
        int d1 = n / 16;     
        int d2 = n % 16;     
        return hexDigits[d1] + hexDigits[d2];     
    }
}
