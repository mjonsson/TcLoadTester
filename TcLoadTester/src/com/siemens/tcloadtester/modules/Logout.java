package com.siemens.tcloadtester.modules;

import com.siemens.tcloadtester.core.Module;
import com.teamcenter.services.strong.core.SessionService;
import com.teamcenter.soa.client.Connection;

/**
 * A module that logs out of Teamcenter.
 * 
 */
public final class Logout extends Module {

	public void validate() {
	}

	public Connection logout(Connection connection) throws Exception {
		SessionService ss = SessionService.getService(connection);

		timer.start();
		
		ss.logout();
		
		setTime(timer.stop());
		
		return null;
	}
}
