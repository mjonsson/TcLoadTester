package com.siemens.tcloadtester;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.siemens.tcloadtester.core.Application;
import com.siemens.tcloadtester.core.Shutdown;
import com.siemens.tcloadtester.core.UserInterface;
import com.siemens.tcloadtester.core.events.Logger;
import com.siemens.tcloadtester.core.events.WorkerEventListener;
import com.siemens.util.PropertiesHelper;

/**
 * Main application startup class.
 * 
 */
public final class TcLoadTester {
	public static boolean debug = false;
	public static boolean gui = true;
	public static UserInterface userInterface;
	public static Scheduler scheduler = new Scheduler();
	public static Logger logger;
	private static Application app = null;
	public static File appPath = null;
	public static File configurationFile = null;
	public static File outputFile = null;
	public static String markerId = "default";

	public static String majorVersion = null;
	public static String minorVersion = null;
	public static String gitCommit = null;
	public static String appName = null;
	
	
	/**
	 * List of objects listening to events from this object
	 */
	private static List<WorkerEventListener> eventListeners = new ArrayList<WorkerEventListener>();;

	public static void addWorkerEventListener(WorkerEventListener wel) {
		eventListeners.add(wel);
	}

	public static List<WorkerEventListener> getWorkerEventListeners() {
		return eventListeners;
	}

	/**
	 * Method for instantiating and starting the application based from the
	 * information from the xml-configuration file.
	 */
	public static final void start() {
		try {
			JAXBContext context = JAXBContext.newInstance(Application.class);
			Unmarshaller um = context.createUnmarshaller();
			
			if (configurationFile != null && configurationFile.exists()) {
				SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema schema = sf.newSchema(TcLoadTester.class.getClassLoader().getResource("com/siemens/tcloadtester/TcLoadTester.xsd"));
				um.setSchema(schema);
				app = (Application) um.unmarshal(new FileInputStream(configurationFile));
			}
			else if (!gui) {
				throw new Exception("Configuration file does not exist.");
			}
			else
			{
				app = new Application();
			}
			
			app.init();

		} catch (Exception e) {
			if (gui) {
				UserInterface.DisplayError("Initialization error", e);
				UserInterface.loop();
			}
			else
			{
				e.printStackTrace();
			}
		}
	}
	
	public static final String parseArgs(String[] args, String arg)
	{
		
		for (String a : args) {
			String aLc = a.toLowerCase();
			String argLc = arg.toLowerCase();
			if (aLc.equals(argLc))
				return argLc;
			else if (aLc.startsWith(argLc + "="))
				return aLc.split("=")[1];
		}

		return null;
	}
	
	private static void usage() {
		System.out.println("\nUsage:\n");
		System.out.println("  TcLoadTesterCL.bat -config=<configuration xml-file> [-marker=<marker id>] [-output=<output csv-file>]\n");
	}

	private static void setGlobalProperties() throws Exception {
		Properties properties = PropertiesHelper.loadProperties("/com/siemens/tcloadtester/TcLoadTester.properties");
		
		majorVersion = properties.getProperty("application.version.MAJOR");
		minorVersion = properties.getProperty("application.version.MINOR");
		gitCommit = properties.getProperty("application.version.GIT");
		appName = properties.getProperty("application.NAME");
	}
	
	/**
	 * Application entry method that opens the xml-configuration file and parses
	 * it into class objects.
	 * 
	 * @param args
	 *            Args passed from command line.
	 */
	public static final void main(String[] args) {
		String config;
		String output;
		String marker;
		
		try {
			setGlobalProperties();
			
			appPath = new File(TcLoadTester.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath());

			if (parseArgs(args, "-debug") != null)
				debug = true;
			if (parseArgs(args, "-nogui") != null)
				gui = false;
			if ((config = parseArgs(args, "-config")) != null)
				configurationFile = new File(config); 
			if ((output = parseArgs(args, "-output")) != null)
				outputFile = new File(output);
			if ((marker = parseArgs(args, "-marker")) != null)
				markerId = marker;
			
			if (!gui && configurationFile == null) {
				usage();
				throw new Exception("No configuration defined.");
			}
			
			// Register shutdown hook to capture kill events
			Shutdown shutdownHook = new Shutdown();
			Runtime.getRuntime().addShutdownHook(shutdownHook.init());

			logger = new Logger();
			if (gui)
			{
				userInterface = new UserInterface();
				eventListeners.add(userInterface);
				UserInterface.init();
			}
			
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
