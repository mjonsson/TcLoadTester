package com.siemens.tcloadtester.core.events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Super class for event types.
 * 
 */
public class Event {
	/**
	 * Initates the dateformat object shared by all event types.
	 */
	private final DateFormat dateFormat = new SimpleDateFormat(
			"yy/MM/dd HH:mm:ss");
	
	/**
	 * The source object of the event
	 */
	protected Object source;
	
	/**
	 * The date string.
	 */
	protected String date;

	public Object getSource() {
		return source;
	}

	public String getDate() {
		return date;
	}

	/**
	 * Super constructor for all events.
	 */
	public Event(Object source) {
		this.date = dateFormat.format(new Date());
		this.source = source;
	}
	
	/**
	 * Super constructor for all events.
	 * 
	 * @param date A pre-defined date.
	 */
	public Event(Object source, String date) {
		this.date = date;
		this.source = source;
	}
}
