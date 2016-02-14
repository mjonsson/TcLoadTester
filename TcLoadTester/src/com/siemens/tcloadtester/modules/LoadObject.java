package com.siemens.tcloadtester.modules;

import java.util.Arrays;

import com.siemens.tcloadtester.core.IModule;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

/**
 * A module that executes a load object for simulating property/summary pages.
 * 
 * @author Mattias Jonsson
 * @version 1.0.0 Initial
 * 
 */
public final class LoadObject extends QueryModule implements IModule {
	String[] targetObjects;


	public void validate() {
		super.validate();
		validate(new String[] { "load_properties" } );
	}

	public void initialize(Connection connection) throws Exception {
		super.initialize(connection);
	}

	/**
	 * Executes the saved query in the superclass and then runs the where
	 * referenced search.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.siemens.tcloadtester.modules.QueryModule#execute()
	 */
	public void execute(Connection connection) throws Exception {
		super.execute(connection);

		DataManagementService dm = DataManagementService.getService(connection);

		String loadProperties = getParsedSetting("load_properties");
		WorkspaceObject[] wsObjs = Arrays.copyOf(queryObjects,
				queryObjects.length, WorkspaceObject[].class);
		
		timer.start();
		
		dm.getProperties(wsObjs, loadProperties.split(","));

		setTime(timer.stop());
		
		miscInfo = String.format(
				"Objects: %d, Properties: %s",
				queryObjects.length, loadProperties);
	}
}
