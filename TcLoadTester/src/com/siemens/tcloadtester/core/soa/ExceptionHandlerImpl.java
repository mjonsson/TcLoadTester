//==================================================
//
//  Copyright 2008 Siemens Product Lifecycle Management Software Inc. All Rights Reserved.
//
//==================================================

package com.siemens.tcloadtester.core.soa;

import com.teamcenter.schemas.soa._2006_03.exceptions.ConnectionException;
import com.teamcenter.schemas.soa._2006_03.exceptions.InternalServerException;
import com.teamcenter.schemas.soa._2006_03.exceptions.ProtocolException;
import com.teamcenter.soa.client.ExceptionHandler;
import com.teamcenter.soa.exceptions.CanceledOperationException;

/**
 * Implementation of the ExceptionHandler. For ConnectionExceptions (server
 * temporarily down .etc) prompts the user to retry the last request. For other
 * exceptions convert to a RunTime exception.
 */
public class ExceptionHandlerImpl implements ExceptionHandler {

	/*
	 * @see
	 * com.teamcenter.soa.client.ExceptionHandler#handleException(com.teamcenter
	 * .schemas.soa._2006_03.exceptions.InternalServerException)
	 */
	public void handleException(InternalServerException ise) {
		if (ise instanceof ConnectionException) {
		} else if (ise instanceof ProtocolException) {
		} else {
		}

		throw new RuntimeException(ise);
	}

	/*
	 * @see
	 * com.teamcenter.soa.client.ExceptionHandler#handleException(com.teamcenter
	 * .soa.exceptions.CanceledOperationException)
	 */
	public void handleException(CanceledOperationException coe) {
		throw new RuntimeException(coe);
	}
}
