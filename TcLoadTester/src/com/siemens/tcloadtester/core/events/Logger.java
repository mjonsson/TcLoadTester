package com.siemens.tcloadtester.core.events;

import java.io.FileWriter;
import com.siemens.tcloadtester.TcLoadTester;
import com.siemens.tcloadtester.core.Application;
import com.siemens.tcloadtester.core.Status;
import com.siemens.tcloadtester.core.Worker;

public class Logger implements WorkerEventListener {
	private static FileWriter fw = null;
	
	private static final int flushFrequency = 60000;
	private static long lastFlush = System.currentTimeMillis();

	public Logger() {
		TcLoadTester.addWorkerEventListener(this);
	}

	/**
	 * The operating system specific separator object.
	 */
	private static final String separator = System
			.getProperty("line.separator");

	public static final void reset() {
		try {
			if (fw != null) {
				fw.flush();
				fw.close();
				fw = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final void flush() {
		try {
			if (fw != null) {
				fw.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleWorkerEvent(Event e) {
		try {
			if (e.getSource() instanceof Worker) {
				if (TcLoadTester.outputFile != null) {
					Worker w = (Worker) e.getSource();
					if (w.status == Status.SLEEPING) {
						if (fw == null) {
							boolean appendToFile = Boolean.valueOf(Application
									.getGlobal("output_append"));
							fw = new FileWriter(
									TcLoadTester.outputFile.getAbsoluteFile(),
									appendToFile);

							if (!appendToFile
									|| (appendToFile && !TcLoadTester.outputFile
											.exists())) {
								fw.write(String
										.format("\"DATE\";\"MARKER\";\"WORKER\";\"MODULE\";\"ITERATION\";\"TIME\";\"MISC.\"%s",
												separator));
							}
						}
						fw.write(String
								.format("\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\"%s",
										e.getDate(), TcLoadTester.markerId,
										w.getWorkerId(), w.getModuleId(),
										w.getIterations(),
										w.getModuleTimeDelta(),
										w.getModuleMiscInfo(), separator));

						long currentTime = System.currentTimeMillis();
						if (currentTime - lastFlush > flushFrequency) {
							flush();
							lastFlush = currentTime;
						}
					}
				}
			}
		} catch (Exception ex) {
			Console.err(ex);
		}
	}
}
