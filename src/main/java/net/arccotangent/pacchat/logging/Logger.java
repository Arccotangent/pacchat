package net.arccotangent.pacchat.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger {
	
	private String tag = "";
	
	public Logger(String loggerTag) {
		tag = loggerTag;
	}
	
	public void setTag(String newTag) {
		tag = newTag;
	}
	
	private String getTime() {
		Calendar c = Calendar.getInstance();
		Date d = c.getTime();
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS aa z");
		return df.format(d);
	}
	
	public void i(String msg) {
		System.out.println(getTime() + " [INFO] [" + tag + "] " + msg);
	}
	
	public void w(String msg) {
		System.out.println(getTime() + " [WARNING] [" + tag + "] " + msg);
	}
	
	public void e(String msg) {
		System.out.println(getTime() + " [ERROR] [" + tag + "] " + msg);
	}
	
}
