/*
This file is part of PacChat.

PacChat is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PacChat is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PacChat.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.arccotangent.pacchat.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger {
	
	private String tag = "";
	private final String ANSI_RESET = "\u001B[0m";
	private final String ANSI_BOLD = "\u001B[1m";
	private static boolean debug = false; //You can set this to true if you want debug mode enabled by default
	
	public Logger(String loggerTag) {
		tag = loggerTag;
	}
	
	public static void toggleDebug() {
		debug = !debug;
	}
	
	public static boolean debugEnabled() {
		return debug;
	}
	
	private String getTime() {
		Calendar c = Calendar.getInstance();
		Date d = c.getTime();
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS aa z");
		return df.format(d);
	}
	
	public void d(String msg) {
		if (debug) {
			String ANSI_MAGENTA = "\u001B[35m";
			System.out.println(ANSI_BOLD + ANSI_MAGENTA + getTime() + " [DEBUG] [" + tag + "] " + msg + ANSI_RESET);
		}
	}
	
	public void i(String msg) {
		String ANSI_GREEN = "\u001B[32m";
		System.out.println(ANSI_BOLD + ANSI_GREEN + getTime() + " [INFO] [" + tag + "] " + msg + ANSI_RESET);
	}
	
	public void w(String msg) {
		String ANSI_YELLOW = "\u001B[33m";
		System.out.println(ANSI_BOLD + ANSI_YELLOW + getTime() + " [WARNING] [" + tag + "] " + msg + ANSI_RESET);
	}
	
	public void e(String msg) {
		String ANSI_RED = "\u001B[31m";
		System.out.println(ANSI_BOLD + ANSI_RED + getTime() + " [ERROR] [" + tag + "] " + msg + ANSI_RESET);
	}
	
}
