package com.siemens.tcloadtester.modules;

import java.math.BigInteger;

import com.siemens.tcloadtester.core.IModule;
import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedConfigParameters;
import com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedInputData;
import com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedOutputData;
import com.teamcenter.services.strong.core._2012_02.DataManagement.WhereUsedResponse;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.model.strong.WorkspaceObject;

/**
 * A module that executes a where used search.
 * 
 * @author Mattias Jonsson
 * @version 1.0.0 Initial
 * 
 */
public final class WhereUsed extends QueryModule implements IModule {
	String[] targetObjects;

	public void validate() {
		validate(new String[] { "revision_rule", "parent_levels", "precise_search" } );
	}

	/**
	 * Gets the saved query from the super class.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.siemens.tcloadtester.modules.QueryModule#initialize()
	 */
	public void initialize(Connection connection) throws Exception {
		super.initialize(connection);
	}

	@SuppressWarnings("unchecked")
	public void execute(Connection connection) throws Exception {
		super.execute(connection);

		DataManagementService dm = DataManagementService.getService(connection);

		String levels = getParsedSetting("parent_levels");
		boolean precise = getSettingAsBool("precise_search");

		WhereUsedInputData[] input = new WhereUsedInputData[queryObjects.length];
		
		for (int i = 0; i < queryObjects.length; i++) {
			input[i] = new WhereUsedInputData();
			input[i].inputObject = (WorkspaceObject)queryObjects[i];
			input[i].useLocalParams = false;
		}
		WhereUsedConfigParameters config = new WhereUsedConfigParameters();
		
		config.intMap.put("numLevels", new BigInteger(levels));
		config.tagMap.put("revision_rule", null);
		config.boolMap.put("whereUsedPreciseFlag", precise);

		timer.start();
		
		WhereUsedResponse response =  dm.whereUsed(input, config);

		setTime(timer.stop());

		int referencers = 0;
		if (response.output != null)
			for (WhereUsedOutputData output : response.output)
				referencers += output.info.length;

		miscInfo = String.format(
				"Objects: %d, Parents: %d, Levels: %s", queryObjects.length,
				referencers, levels);
	}
}
