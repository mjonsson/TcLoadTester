package com.siemens.tcloadtester.core;

import com.teamcenter.soa.client.Connection;

public interface IModule {
	public void validate();
	public void initialize(Connection connection) throws Exception;
	public void execute(Connection connection) throws Exception;
}
