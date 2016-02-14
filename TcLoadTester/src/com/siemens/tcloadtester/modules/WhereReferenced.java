package com.siemens.tcloadtester.modules;

import java.util.Arrays;

import com.siemens.tcloadtester.core.IModule;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedOutput;
import com.teamcenter.services.strong.core._2007_01.DataManagement.WhereReferencedResponse;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

/**
 * A module that executes a where referenced search.
 * 
 * @author Mattias Jonsson
 * @version 1.0.0 Initial
 * 
 */
public final class WhereReferenced extends QueryModule implements IModule {
	String[] targetObjects;

	public void validate() {
		validate(new String[] { "reference_levels" } );
	}

	public void inititalize(Connection connection) throws Exception {
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

		int levels = getParsedSettingAsInt("reference_levels");
		WorkspaceObject[] wsObjs = Arrays.copyOf(queryObjects,
				queryObjects.length, WorkspaceObject[].class);
		timer.start();
		WhereReferencedResponse response1 = dm.whereReferenced(wsObjs, levels);

		int referencers = 0;
		if (response1.output != null)
			for (WhereReferencedOutput output : response1.output)
				referencers += output.info.length;

		setTime(timer.stop());
		miscInfo = String.format(
				"Objects: %d, Referencers: %d, Levels: %d",
				queryObjects.length, referencers, levels);

	}
}
