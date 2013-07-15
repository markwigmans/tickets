package com.chessix.tickets.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author Mark Wigmans
 *
 */
public final class EventUtils {

	private EventUtils() {
		throw new Error("Utility class");
	}

	public static String getEventName(final String event) {
		return StringUtils.lowerCase(StringUtils.trimToEmpty(event));
	}

	public static void busy() {
	    try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
	}
}

