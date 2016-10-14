package net.arccotangent.pacchat.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger {
	
	private String tag = "";
	private final String ANSI_GREEN = "\u001B[32m";
	private final String ANSI_YELLOW = "\u001B[33m";
	private final String ANSI_RED = "\u001B[31m";
	private final String ANSI_RESET = "\u001B[0m";
	private final String ANSI_BOLD = "\u001B[1m";
	
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
		System.out.println(ANSI_BOLD + ANSI_GREEN + getTime() + " [INFO] [" + tag + "] " + msg + ANSI_RESET);
	}
	
	public void w(String msg) {
		System.out.println(ANSI_BOLD + ANSI_YELLOW + getTime() + " [WARNING] [" + tag + "] " + msg + ANSI_RESET);
	}
	
	public void e(String msg) {
		System.out.println(ANSI_BOLD + ANSI_RED + getTime() + " [ERROR] [" + tag + "] " + msg + ANSI_RESET);
	}
	
}
