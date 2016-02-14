//==================================================
//
//  Copyright 2008 Siemens Product Lifecycle Management Software Inc. All Rights Reserved.
//
//==================================================

package com.siemens.tcloadtester.core.soa;

import com.siemens.tcloadtester.core.events.Console;
import com.siemens.tcloadtester.core.exceptions.PartialErrorException;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.PartialErrorListener;

/**
 * Implementation of the PartialErrorListener. Print out any partial errors
 * returned.
 */
public class PartialErrorListenerImpl implements PartialErrorListener {
	/*
	 * @see
	 * com.teamcenter.soa.client.model.PartialErrorListener#handlePartialError
	 * (com.teamcenter.soa.client.model.ErrorStack[])
	 */
	public void handlePartialError(ErrorStack[] stacks) {
		Console.err(new PartialErrorException(stacks));
	}
}
