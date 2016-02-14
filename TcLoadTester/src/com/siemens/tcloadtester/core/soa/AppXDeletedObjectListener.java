//==================================================
//
//  Copyright 2008 Siemens Product Lifecycle Management Software Inc. All Rights Reserved.
//
//==================================================

package com.siemens.tcloadtester.core.soa;

import com.teamcenter.soa.client.model.DeleteListener;

/**
 * Implementation of the DeleteListener, simply prints out list of all objects
 * that are deleted.
 */
public class AppXDeletedObjectListener implements DeleteListener {

	public void modelObjectDelete(String[] uids) {
		throw new UnsupportedOperationException();
	}

}
