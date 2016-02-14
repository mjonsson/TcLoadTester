package com.siemens.tcloadtester.core;

/**
 * A basic timer class.
 * 
 */
public final class Timer {
	/**
	 * The start time in millseconds.
	 */
	private long _start;

	/**
	 * Start the timer.
	 */
	public final void start() {
		_start = System.nanoTime();
	}

	/**
	 * Stop the timer.
	 * 
	 * @return Delta time value
	 */
	public final long stop() {
		return System.nanoTime() - _start;
	}
}