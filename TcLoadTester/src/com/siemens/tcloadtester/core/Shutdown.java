package com.siemens.tcloadtester.core;

import java.util.ArrayList;

import com.siemens.tcloadtester.core.events.Logger;

/**
 * A thread instance of this class is created if user presses ctrl^c during
 * program execution in a console window or if the application is closed with
 * the close button from the GUI. The class is responsible for shutting down all
 * application threads in a graceful manner.
 * 
 */
public final class Shutdown implements Runnable {
	private static boolean shutdownInitiated = false;
	private static ArrayList<Thread> workerThreads = new ArrayList<Thread>();

	public final static synchronized void registerThread(Thread thread) {
		workerThreads.add(thread);
	}
	
	/**
	 * Initializes the thread before execution.
	 * 
	 * @return The initialized thread.
	 */
	public final Thread init() {
		Thread thread = new Thread(this, "Shutdown");
		thread.setDaemon(true);

		return thread;
	}

	public final static boolean isShutdownInitiated() {
		return shutdownInitiated;
	}

	/**
	 * Shutdown loop that stops all running worker threads gracefully.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		shutdownInitiated = true;
		try {
			System.err.println("\nWaiting for worker threads to shutdown gracefully...\n");

			for (Thread t : workerThreads) {
				if (t.isAlive()) {
					t.interrupt();
					Thread.sleep(Long.parseLong(Application
							.getGlobal("stop_delay")) * 1000);
					t.join();
				}
			}
		} catch (Exception e) {
		}
		finally {
			Logger.reset();
			System.err.println("\nAll worker threads has shutdown. Exiting...\n\n");
		}
	}
}