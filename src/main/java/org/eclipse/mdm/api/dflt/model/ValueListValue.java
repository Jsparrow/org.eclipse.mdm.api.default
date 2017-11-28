/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import org.eclipse.mdm.api.base.adapter.Core;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Sortable;

/**
 * Implementation of the value list value entity type. A value list value is a
 * unique value option within the parent {@link ValueList}. Therefore the names
 * of the value list values within a {@code ValueList} have to be unique.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see ValueList
 */
public class ValueListValue extends BaseEntity implements Deletable, Describable, Sortable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * The 'ScalarType' attribute name.
	 */
	public static final String ATTR_SCALAR_TYPE = "DataType";

	/**
	 * The 'Value' attribute name.
	 */
	public static final String ATTR_VALUE = "Value";

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	ValueListValue(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setName(String name) {
		getValue(ATTR_VALUE).set(name);
		super.setName(name);
	}

	/**
	 * Returns the parent {@link ValueList}.
	 *
	 * @return The parent {@code ValueList} is returned.
	 */
	public ValueList getValueList() {
		return getCore().getPermanentStore().get(ValueList.class);
	}

}
