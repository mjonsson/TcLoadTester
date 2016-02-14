//==================================================
//
//  Copyright 2008 Siemens Product Lifecycle Management Software Inc. All Rights Reserved.
//
//==================================================

package com.siemens.tcloadtester.core.soa;

import com.teamcenter.soa.client.model.ChangeListener;
import com.teamcenter.soa.client.model.ModelObject;

/**
 * Implementation of the ChangeListener. Print out all objects that have been
 * updated.
 */
public class AppXUpdateObjectListener implements ChangeListener {

	public void modelObjectChange(ModelObject[] objects) {
		throw new UnsupportedOperationException();
	}

}
