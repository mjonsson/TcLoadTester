package com.siemens.tcloadtester.core;

import javax.xml.bind.annotation.XmlAttribute;

import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.soa.client.Connection;

/**
 * Super class for all modules. All fields and methods common for modules are
 * placed within this class.
 * 
 */
public class Module extends ApplicationObject {
	protected Timer timer = new Timer();
	protected String strPropertyPolicy;
	protected String miscInfo = "";
	protected String timeStr = "";
	protected long time = 0;
	protected long size = 0;
	private int retryCount = 0;
	private int retries = 0;
	private long retryInterval = 10;
	private long sleepInterval = 30;

	/**
	 * Property representing the module type
	 */
	@XmlAttribute(name = "type")
	protected String type;

	public Module() {
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param copy
	 *            Source object.
	 */
//	public Module(Module copy) {
//		super(copy);
//		this.type = copy.type;
//	}

//	/**
//	 * Constructor for creating a Module type object from an id and a
//	 * settingslist.
//	 * 
//	 * @param id
//	 *            Global id of object.
//	 * @param settingsList
//	 *            Local object settings.
//	 */
//	public Module(String id, String type, Setting[] settingsList) throws Exception {
//		super(id, settingsList);
//		this.type = type;
//		this.retryCount = getSettingAsInt("retry_count");
//		this.retryInterval = getSettingAsLong("retry_interval");
//		this.sleepInterval = getParsedSettingAsLong("sleep");
//	}

	protected final void init(String id, String type, Setting[] settingsList) throws Exception {
		this.id = id;
		this.type = type;
		if (settingsList != null)
			this.settingsList = settingsList.clone();
		this.retryCount = getSettingAsInt("retry_count");
		this.retryInterval = getSettingAsLong("retry_interval");
		this.sleepInterval = getParsedSettingAsLong("sleep");
	}
	
	public void setTime(long time) {
		this.time = time;

		long min = (long) Math.floor(time / 60000000000L);
		time = time % 60000000000L;
		long sec = (long) Math.floor(time / 1000000000L);
		time = time % 1000000000L;
		long msec = (long) Math.floor(time / 1000000L);
		timeStr = String.format("%02d:%02d.%03d", min, sec, msec);
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public long getTime() {
		// Convert to milliseconds
		return time / 1000000;
	}
	
	public long getSize() {
		return size;
	}
	
	public String getTimeStr() {
		return timeStr;
	}
	
	public String getMiscInfo() {
		return (miscInfo == null) ? "" : miscInfo;
	}
	
	public boolean continueOnError() {
		if (retryCount == 0) return true;
		else if (retries >= retryCount) return false;
		retries++;
		
		return true;
	}

	public void resetRetryCount() {
		retries = 0;
	}
	
	public void retrySleep() throws Exception {
		Thread.sleep(retryInterval * 1000);
	}

	/**
	 * Common method for running a module. This specific module is used by the
	 * login method.
	 * 
	 * @param progressEvent
	 *            An event object is passed into method for retrieving event
	 *            data after the module has been run.
	 * @return The Teamcenter connection object.
	 * @throws Exception
	 */
	public Connection run() throws Exception {
		return null;
	}

	/**
	 * A common method invoked by all modules when they go idle for a period.
	 * 
	 * @throws Exception
	 */
	public final void sleep() throws InterruptedException {
		Thread.sleep(sleepInterval * 1000);
	}

	protected void validateModule() throws Exception {
		validate(new String[] { "sleep", "retry_count", "retry_interval", "property_policy" } );
	}
	
	protected void validate() { }
	
	protected void initialize(Connection connection) throws Exception { }

	protected void execute(Connection connection) throws Exception { }

	/**
	 * Common method executed in the start of every module.
	 * 
	 * @throws Exception
	 */
	protected final void start(Connection connection) throws Exception {
		// Make sure no objects are cached in modelmanager store to save memory
		// when running lots of sessions
//		if (connection != null)
//			connection.getModelManager().removeAllObjectsFromStore();
		if (connection != null) {
			strPropertyPolicy = getParsedSetting("property_policy");
			SessionService ss = SessionService.getService(connection);
			ss.setObjectPropertyPolicy(strPropertyPolicy);
		}
	}
	
	/**
	 * Common method executed in the end of every module.
	 * 
	 * @throws Exception
	 */
	protected final void end(Connection connection) throws Exception {
		// Reset property policy
		if (connection != null) {
			SessionService ss = SessionService.getService(connection);
			ss.setObjectPropertyPolicy("Empty");
		}
	}
}
