package com.resonant.wrapper.lib.utility.text;

import java.text.DecimalFormat;

/**
 * Quick class to house utility methods related to strings
 * @author Darkguardsman
 */
public final class TextUtility {
	/**
	 * Formatter to clean up numbers
	 */
	public final static DecimalFormat NUMBER_FORMATTER_NO_DECIMALS = new DecimalFormat("#,###");
	public final static DecimalFormat NUMBER_FORMATTER = new DecimalFormat("#,###.00");

	/**
	 * Adds padding to the right of the string
	 * @param s - string
	 * @param n - number of spaces
	 * @return
	 */
	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

	/**
	 * Adds padding to the left of the string
	 * @param s - string
	 * @param n - number of spaces
	 * @return
	 */
	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + "s", s);
	}

	/**
	 * Formats a number to fit into so many spaces, and to contain ,
	 * @param num - number to format
	 * @param spaces - spaces to pad the front with
	 * @return formatted string
	 */
	public static String fitIntoSpaces(long num, int spaces) {
		return padLeft(NUMBER_FORMATTER_NO_DECIMALS.format(num), spaces);
	}

	/**
	 * Gets the difference in time formatted to be
	 * easy to read
	 * @param start - nano sec start time
	 * @return nano sec difference up to now formatted
	 */
	public static String formatTimeDifference(long start, long end) {
		return formatNanoTime(end - start);
	}

	/**
	 * Breaks nano second time long into seconds, miliseconds, microseconds, and nanosecond
	 * sections making it easier to read.
	 * @param nano - long value of time
	 * @return formatted string
	 */
	public static String formatNanoTime(long nano) {
		long s = nano / 1000000000;
		long ms = (nano % 1000000000) / 1000000;
		long us = ((nano % 1000000000) % 1000000) / 1000;
		long ns = ((nano % 1000000000) % 1000000) % 1000;
		String string = "";
		string += fitIntoSpaces(s, 3) + "s  ";
		string += fitIntoSpaces(ms, 3) + "ms  ";
		string += fitIntoSpaces(us, 3) + "us  ";
		string += fitIntoSpaces(ns, 3) + "ns  ";

		return string;
	}

}