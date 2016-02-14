package com.siemens.tcloadtester.core.events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.siemens.tcloadtester.core.exceptions.PartialErrorException;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ErrorValue;

/**
 * Class used for outputting console event messages.
 * 
 */
public class Console {
	/**
	 * Initates the dateformat object shared by all event types.
	 */
	private static final DateFormat dateFormat = new SimpleDateFormat(
			"yy/MM/dd HH:mm:ss");

	/**
	 * Prints the message to a standard output.
	 */
	public static final synchronized void out(String format, Object... args) {
		String date = dateFormat.format(new Date());
		String message = String.format(format, args);
		String method = "---";
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		if (st != null && st.length > 2) {
			String className = st[2].getClassName();
			method = String.format("%s.%s", className.substring(className.lastIndexOf('.')+1), st[2].getMethodName());
		}
		System.out.format("[%-20s][%s] - %s\n", method, date, message);
	}

	/**
	 * Prints the message to a standard output.
	 */
	public static final synchronized void out(String source, String format, Object... args) {
		String date = dateFormat.format(new Date());
		String message = String.format(format, args);
		System.out.format("[%-20s][%s] - %s\n", source, date, message);
	}

	/**
	 * Prints the message to a standard error.
	 */
	public static final synchronized void err(String format, Object... args) {
		String date = dateFormat.format(new Date());
		String message = String.format(format, args);
		String method = "---";
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		if (st != null && st.length > 2) {
			String className = st[2].getClassName();
			method = String.format("%s.%s", className.substring(className.lastIndexOf('.')+1), st[2].getMethodName());
		}
		System.err.format("[%-20s][%s] - %s\n", method, date, message);
	}

	/**
	 * Prints the message to a standard error.
	 */
	public static final synchronized void err(String source, String format, Object... args) {
		String date = dateFormat.format(new Date());
		String message = String.format(format, args);
		System.err.format("[%-20s][%s] - %s\n", source, date, message);
	}

	/**
	 * Prints the exception message to System.error in a nice format.
	 */
	public static final synchronized void err(Exception exception) {
		if (exception instanceof PartialErrorException) {
			PartialErrorException peex = (PartialErrorException) exception;

			System.err
					.println("A partial error was caught:\n\nInformation:\n\n");

			for (ErrorStack errStack : peex.getErrorStack()) {
				for (ErrorValue errVal : errStack.getErrorValues()) {
					System.err.format("Code: %s, Level: %s, Message: %s\n\n",
							errVal.getCode(), errVal.getLevel(),
							errVal.getMessage());
				}
			}
		} else {
			System.err.println(
					"An exception was caught:\n\nStack trace:\n\n");
			exception.printStackTrace();
		}
	}
}
