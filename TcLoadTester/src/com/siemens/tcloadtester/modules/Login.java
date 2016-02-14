package com.siemens.tcloadtester.modules;

import java.util.UUID;

import com.siemens.tcloadtester.core.Module;
import com.siemens.tcloadtester.core.soa.CredentialManagerImpl;
import com.siemens.tcloadtester.core.soa.ExceptionHandlerImpl;
import com.siemens.tcloadtester.core.soa.PartialErrorListenerImpl;
import com.teamcenter.services.strong.core._2007_01.Session;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.soa.SoaConstants;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.Property;

/**
 * A module that creates a Teamcenter session and logs into Teamcenter.
 * 
 */
public final class Login extends Module {
	/**
	 * The credential manager handles Teamcenter authentication.
	 */
	private CredentialManagerImpl credentialManager = null;
	/**
	 * Each connection to Teamcenter has a discrimator, which identifies the
	 * connection.
	 */
	private String discriminator = null;
	private String tcWebTier = null;
	private String tcServerid = null;
	private String tcSyslog = null;
	private String tcHostname = null;


	public void validate() {
		validate(new String[] { "endpoint", "user", "password", "group", "role", "discriminator", "bypass", "proxyhost" } );
	}

	/**
	 * Method that logs in to Teamcenter.
	 * 
	 * @throws Exception
	 */
	public Connection login(Connection connection) throws Exception {
		credentialManager = new CredentialManagerImpl();
		
		tcWebTier = getParsedSetting("endpoint");

		String protocol = tcWebTier.toLowerCase()
				.startsWith("http") ? SoaConstants.HTTP : SoaConstants.IIOP;

		connection = new Connection(tcWebTier,
				credentialManager, SoaConstants.REST, protocol);
		
		// Add an ExceptionHandler to the Connection, this will handle any
		// InternalServerException, communication errors, xml marshalling errors
		// .etc
		connection.setExceptionHandler(new ExceptionHandlerImpl());

		// While the above ExceptionHandler is required, all of the following
		// Listeners are optional. Client application can add as many or as few
		// Listeners
		// of each type that they want.

		// Add a Partial Error Listener, this will be notified when ever a
		// a service returns partial errors.
		connection.getModelManager().addPartialErrorListener(
				new PartialErrorListenerImpl());

		// Add a Change Listener, this will be notified when ever a
		// a service returns model objects that have been updated.
		// connection.getModelManager().addChangeListener(new
		// AppXUpdateObjectListener());

		// Add a Delete Listener, this will be notified when ever a
		// a service returns objects that have been deleted.
		// connection.getModelManager().addDeleteListener(new
		// AppXDeletedObjectListener());

		// Add a Request Listener, this will be notified before and after each
		// service request is sent to the server.
		// Connection.addRequestListener( new AppXRequestListener() );

		// If proxyhost has been set in configuration, add it
		if (!getSetting("proxyhost").equals(""))
			connection.setOption("HTTPProxyHost", getSetting("proxyhost"));

		// If bypass flag has been set in configuration, add it
		if (getSettingAsBool("bypass"))
			connection.setOption("bypassFlag", Property.toBooleanString(true));

		// Turn off caching of model objects to save memory usage
		connection.setOption("OPT_CACHE_MODEL_OBJECTS", "false");
		
		String tmpDiscriminator = getParsedSetting("discriminator").toLowerCase();
		if (tmpDiscriminator.equals("[randomnew]")) {
			discriminator = UUID.randomUUID().toString().replace("-", "");
		} else if (tmpDiscriminator.equals("[random]")
				&& discriminator == null) {
			discriminator = UUID.randomUUID().toString().replace("-", "");
		} else if (discriminator == null)
			discriminator = tmpDiscriminator;

		SessionService ss = SessionService.getService(connection);

		timer.start();
		ss.login(getParsedSetting("user"), getSetting("password"),
				getParsedSetting("group"), getParsedSetting("role"), null, discriminator);

		setTime(timer.stop());

		Session.GetTCSessionInfoResponse response = ss.getTCSessionInfo();

		tcServerid = (String) response.extraInfo.get("TcServerID");
		tcHostname = (String) response.extraInfo.get("hostName");
		tcSyslog = (String) response.extraInfo.get("syslogFile");

		miscInfo = String.format(
				"WebTier: %s, Server Id.: %s, Hostname: %s, Syslog: %s", tcWebTier, tcServerid,
				tcHostname, tcSyslog);
		
		return connection;
	}
}
