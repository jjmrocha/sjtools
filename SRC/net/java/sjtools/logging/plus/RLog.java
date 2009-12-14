/*
 * SJTools - SysVision Java Tools
 * 
 * Copyright (C) 2006 SysVision - Consultadoria e Desenvolvimento em Sistemas de Inform�tica, Lda.  
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package net.java.sjtools.logging.plus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import net.java.sjtools.logging.Log;
import net.java.sjtools.logging.LogFactory;
import net.java.sjtools.logging.util.LogConfigReader;

public class RLog {
	private static final String DEFAULT_LOG_PROPERTY = "sjtools.logging.plus.default.logger";
	private static final String DEFAULT_LOG_VALUE = "RLOG_DEFAULT_LOGGER";

	private static ThreadLocal localLog = new ThreadLocal();
	private static ThreadLocal localID = new ThreadLocal();

	private static String baseRequestID = null;

	private static long lastID = 0;

	public static boolean isDebugEnabled() {
		return getCurrentLog().isDebugEnabled();
	}

	public static boolean isErrorEnabled() {
		return getCurrentLog().isErrorEnabled();
	}

	public static boolean isInfoEnabled() {
		return getCurrentLog().isInfoEnabled();
	}

	public static boolean isFatalEnabled() {
		return getCurrentLog().isFatalEnabled();
	}

	public static boolean isWarnEnabled() {
		return getCurrentLog().isWarnEnabled();
	}

	public static void debug(Object obj) {
		getCurrentLog().debug(getMessage(obj));
	}

	public static void debug(Object obj, Throwable throwable) {
		getCurrentLog().debug(getMessage(obj), throwable);
	}

	public static void info(Object obj) {
		getCurrentLog().info(getMessage(obj));
	}

	public static void info(Object obj, Throwable throwable) {
		getCurrentLog().info(getMessage(obj), throwable);
	}

	public static void warn(Object obj) {
		getCurrentLog().warn(getMessage(obj));
	}

	public static void warn(Object obj, Throwable throwable) {
		getCurrentLog().warn(getMessage(obj), throwable);
	}

	public static void error(Object obj) {
		getCurrentLog().error(getMessage(obj));
	}

	public static void error(Object obj, Throwable throwable) {
		getCurrentLog().error(getMessage(obj), throwable);
	}

	public static void fatal(Object obj) {
		getCurrentLog().fatal(getMessage(obj));
	}

	public static void fatal(Object obj, Throwable throwable) {
		getCurrentLog().fatal(getMessage(obj), throwable);
	}

	public static Log getCurrentLog() {
		Log log = (Log) localLog.get();

		if (log == null) {
			String loggerName = LogConfigReader.getParameter(DEFAULT_LOG_PROPERTY);

			if (loggerName == null) {
				loggerName = DEFAULT_LOG_VALUE;
			}

			log = LogFactory.getLog(loggerName);
		}

		return log;
	}

	public static String getRequestID() {
		String id = (String) localID.get();

		if (id == null) {
			return "00";
		}

		return id;
	}

	private static String getMessage(Object msg) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getCurrentMethod());
		buffer.append("[");
		buffer.append(getRequestID());
		buffer.append("] - ");
		buffer.append(msg);

		return buffer.toString();
	}

	private static String getCurrentMethod() {
		StackTraceElement ste = new Throwable().getStackTrace()[3];

		StringBuffer buffer = new StringBuffer();
		buffer.append(ste.getClassName());
		buffer.append(".");
		buffer.append(ste.getMethodName());

		return buffer.toString();
	}

	private static String getBaseRequestID() {
		if (baseRequestID == null) {
			generateBaseRequestID();
		}

		return baseRequestID;
	}

	private synchronized static void generateBaseRequestID() {
		if (baseRequestID != null) {
			return;
		}

		String ret = null;

		try {
			ret = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			ret = String.valueOf(new Random(System.currentTimeMillis()).nextInt());
		}

		baseRequestID = Long.toHexString(ret.hashCode()).concat("-");
	}

	private synchronized static long getNextID() {
		if (lastID == Long.MAX_VALUE) {
			return 1;
		} else {
			return ++lastID;
		}
	}

	public static void init(Log log) {
		localLog.set(log);
		localID.set(getBaseRequestID().concat(Long.toHexString(getNextID())));
	}

	public static void changeLog(Log log) {
		localLog.set(log);
	}
}