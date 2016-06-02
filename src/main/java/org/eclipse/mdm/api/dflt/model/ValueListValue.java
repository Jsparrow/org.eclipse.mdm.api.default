/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.EntityCore;
import org.eclipse.mdm.api.base.model.Sortable;

public final class ValueListValue extends BaseEntity implements Deletable, Describable, Sortable<ValueListValue> {

	// ======================================================================
	// Class variables
	// ======================================================================

	public static final String ATTR_SCALAR_TYPE = "DataType";
	public static final String ATTR_VALUE = "Value";

	// ======================================================================
	// Class variables
	// ======================================================================

	ValueListValue(EntityCore core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	@Override
	public void setName(String name) {
		getValue(ATTR_VALUE).set(name);
		super.setName(name);
	}

	public ValueList getValueList() {
		return getCore().getPermanentStore().get(ValueList.class);
	}

}
