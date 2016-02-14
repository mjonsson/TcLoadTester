package com.siemens.tcloadtester.core.exceptions;

import com.teamcenter.soa.client.model.ErrorStack;

/**
 * Exception class that wraps the Teamcenter SOA partial error structure.
 * 
 */
public class PartialErrorException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * The errorstack retrieved from Teamcenter.
	 */
	private ErrorStack[] errorStack;

	public PartialErrorException(ErrorStack[] errorStack) {
		this.errorStack = errorStack;
	}

	public PartialErrorException(String message, ErrorStack[] errorStack) {
		super(message);
		this.errorStack = errorStack;
	}

	public ErrorStack[] getErrorStack() {
		return errorStack;
	}

	public void setErrorStack(ErrorStack[] errorStack) {
		this.errorStack = errorStack;
	}
}
