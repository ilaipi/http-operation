package com.richeninfo.http.utils;
/**
 * 字符集
 * @author mz.yyam
 * @date 2013-5-6 21:28:48
 */
public enum Charset {
	GBK("GBK"),
	UTF_8("UTF-8"), 
	BIG5("BIG5"), 
	ISO_8859_1("ISO-8859-1"),
	GB2312("GB2312");

	private final String charset;

	private Charset(String charset) {
		this.charset = charset;
	}

	public String getCharset() {
		return charset;
	}
}
