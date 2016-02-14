package com.siemens.tcloadtester.modules;

import com.siemens.tcloadtester.core.IModule;
import com.teamcenter.soa.client.Connection;

/**
 * A module that executes a saved query.
 * 
 */
public final class SavedQuery extends QueryModule implements IModule {
	
	public void validate() {
		super.validate();
	}
	
	public void initialize(Connection connection) throws Exception {
		super.initialize(connection);
	}
	
	public void execute(Connection connection) throws Exception {
		super.execute(connection);
	}
}
