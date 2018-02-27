/*
 * Copyright (c) 2016-2018 Gigatronik Ingolstadt GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.mdm.api.dflt.model;

import org.eclipse.mdm.api.base.adapter.Core;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Deletable;

/**
 * Implementation of the project entity type. The project is the top level of
 * stored measurement data. Its name may be freely chosen but has to to be
 * unique. A project contains {@link Pool} entities as its children.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see Pool
 */
public class Project extends BaseEntity implements Deletable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * The {@link Pool} child type.
	 */
	public static final Class<Pool> CHILD_TYPE_POOL = Pool.class;

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	Project(Core core) {
		super(core);
	}

}
