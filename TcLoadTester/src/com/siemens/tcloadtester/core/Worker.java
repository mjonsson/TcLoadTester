package com.siemens.tcloadtester.core;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import com.siemens.tcloadtester.TcLoadTester;
import com.siemens.tcloadtester.core.events.Console;
import com.siemens.tcloadtester.core.events.Event;
import com.siemens.tcloadtester.core.events.WorkerEventListener;
import com.siemens.tcloadtester.modules.Login;
import com.siemens.tcloadtester.modules.Logout;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.soa.client.Connection;

/**
 * Class that contains functionality for running all defined module occurrences
 * in a sequence.
 * 
 * @date Changed so that ProgressEvent is instantiated with the Worker object
 */
@XmlType
public final class Worker extends ApplicationObject implements Runnable {
	public Mode mode = Mode.STOPPED;
	public Status status = Status.NONE;
	public Status prevStatus = Status.NONE;
	public int tasks = 0;
	public int iterations = 0;
	public int totalIterations;
	public int totalTasks;
	private Module module;
	public ModuleOcc moduleOcc;
	private String scheduleId = null;

	@XmlTransient
	public Thread myThread = null;

	/**
	 * Sequence of module occurrences that are to be iterated.
	 */
	@XmlElementWrapper(name = "sequence")
	@XmlElement(name = "mod_occ")
	public ModuleOcc[] sequenceList;

	/**
	 * Each worker have its own tableitem object for displaying information in
	 * the GUI table.
	 */
	private TableItem tableItem;

	/**
	 * The Teamcenter connection object.
	 */
	private Connection connection;

	public TableItem getTableItem() {
		return tableItem;
	}

	public String getWorkerId() {
		return id;
	}

	public String getModuleId() {
		return (module == null) ? "" : module.id; 
	}

	public String getModuleType() {
		return (module == null) ? "" : module.type;
	}

	public String getModuleTimeDelta() {
		return (module == null) ? "" : module.getTimeStr();
	}

	public String getModuleMiscInfo() {
		return (module == null) ? "" : module.getMiscInfo();
	}

	public String getIterations() {
		if (totalIterations == 0)
			return String.format("%d", iterations);
		else
			return String.format("%d/%d", iterations, totalIterations);
	}

	public String getPercent() {
		if (totalIterations == 0)
			return "---";
		else
			return String.format("%d %%",
					Math.round((float) tasks / totalTasks * 100));
	}

	protected final void fireEvent() {
		Event event = new Event(this);
		for (WorkerEventListener l : TcLoadTester.getWorkerEventListeners())
			l.handleWorkerEvent(event);
	}

	/**
	 * Intializes the thread before execution.
	 * 
	 * @return The thread object.
	 * @throws Exception
	 */
	public boolean start() throws Exception {
		if (mode == Mode.STOPPED && !Shutdown.isShutdownInitiated()) {
			myThread = new Thread(this, id);
			Shutdown.registerThread(myThread);
			myThread.setDaemon(true);

			if (settingExist("cron")) {
				scheduleId = TcLoadTester.scheduler.schedule(getSetting("cron"), this);
				status = Status.SCHEDULED;
				fireEvent();
				return false;
			}
			else {
				myThread.start();
				return true;
			}
		}
		return false;
	}

	public void stop() {
		// If scheduled, remove schedule
		if (scheduleId != null) {
			TcLoadTester.scheduler.deschedule(scheduleId);
			scheduleId = null;
		}
		// If worker was scheduled, set it to FINISHED
		if (mode == Mode.STOPPED) {
			status = Status.FINISHED;
			fireEvent();
		}
		// If started, ask thread to interrupt gracefully
		else if (mode == Mode.STARTED) {
			myThread.interrupt();
		}
	}

	/**
	 * Convenience class to merge two arrays, the primary has higher priority
	 * than secondary.
	 * 
	 * @param primary
	 *            Master array
	 * @param secondary
	 *            Secondary array
	 * @return A merged array
	 */
	private Setting[] mergeSettings(Setting[] primary, Setting[] secondary) {
		List<Setting> temp = null;

		if (primary == null && secondary == null)
			return null;
		else if (primary != null && secondary == null)
			return primary;
		else if (primary == null && secondary != null)
			return secondary;
		else {
			temp = new ArrayList<Setting>(Arrays.asList(primary));

			for (Setting sec : secondary) {
				boolean exist = false;
				for (Setting pri : temp) {
					if (pri.name.equals(sec.name)) {
						exist = true;
						break;
					}
				}
				if (!exist)
					temp.add(sec);
			}
		}

		return temp.toArray(new Setting[temp.size()]);
	}

	/**
	 * Creates worker specific instances of all modules with reflection. This is
	 * needed to make every worker fully thread-safe.
	 * 
	 * @throws Exception
	 */
	public final void initialize() throws Exception {
		for (ModuleOcc mo : sequenceList) {
			Module mod = Application.getModule(mo.refid);

			if (mod == null) {
				throw new Exception("Module with ID \"" + mo.refid + "\" can not be found.");
			}

			Class<?> classObj = null;
			try {
				classObj = Class
						.forName(mod.type);
			}
			catch (ClassNotFoundException e) {
				throw new Exception("Module of type \"" + mod.type + "\" can not be identified.", e);
			}
			Constructor<?> construct = classObj.getConstructor();
			mo.moduleObj = (Module) construct.newInstance();
			mo.moduleObj.init(mod.id, mod.type,
					mergeSettings(mo.settingsList, mod.settingsList));
			mo.moduleObj.validateModule();
		}
		if (TcLoadTester.gui) {
			final TableItem tableItem = new TableItem(UserInterface.getTable(),
					SWT.NONE);
			tableItem.setData("worker", this);
			this.tableItem = tableItem;
			tableItem.setText(new String[] { null, null, "---", id, "---",
					"---", "---", "---" });
		}
	}

	public final void runModule(ModuleOcc mo) throws Exception {
		// Enter running phase
		status = Status.RUNNING;
		fireEvent();

		try {
			if (module instanceof Login) {
				connection = ((Login)module).login(connection);
			}
			else if (module instanceof Logout) {
				connection = ((Logout)module).logout(connection);
			}
			else if (mo.probability == 100 || rnd.nextInt(100) <= mo.probability) {
				module.start(connection);
				module.initialize(connection);
				module.execute(connection);
				module.end(connection);
			}
			else
				return;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			if (!module.continueOnError()) {
				throw e;
			}
			status = Status.RETRY;
			fireEvent();
			Console.err(e);
			module.retrySleep();
			runModule(mo);
		}
	}

	/**
	 * Run the module occurrence sequence for a defined number of iterations.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		Console.out(id, "Starting thread");

		try {
			mode = Mode.STARTED;
			status = Status.NONE;
			fireEvent();

			tasks = 0;
			totalTasks = 0;
			totalIterations = getSettingAsInt("iterations");
			for (ModuleOcc mo : sequenceList) {
				if (mo.runonce.equals("false"))
					totalTasks += totalIterations;
				else
					totalTasks++;
			}

			for (iterations = 1; totalIterations == 0 || iterations <= totalIterations; iterations++) {
				for (ModuleOcc mo : sequenceList) {
					moduleOcc = mo;
					// Get module for current task
					module = moduleOcc.moduleObj;

					if (mo.runonce.equals("false")
							|| (mo.runonce.equals("start") && iterations == 1)
							|| (mo.runonce.equals("end") && iterations == totalIterations)) {
						tasks++;

						runModule(mo);

						// Enter sleep phase
						status = Status.SLEEPING;
						fireEvent();
						module.resetRetryCount();
						module.sleep();
					}
				}
			}
			iterations = totalIterations;

			if (scheduleId == null)
				status = Status.FINISHED;
			else
				status = Status.SCHEDULED;
		} catch (InterruptedException e) {
			status = Status.FINISHED;
		} catch (Exception e) {
			status = Status.ERROR;
			Console.err(e);
		} finally {
			mode = Mode.STOPPED;
			fireEvent();
			// Make sure the session is logged out
			try {
				if (connection != null) {
					SessionService ss = SessionService.getService(connection);
					ss.logout();
					connection = null;
				}
			} catch (Exception e) {
			}

			Console.out(id, "Stopping thread");
		}
	}
}
