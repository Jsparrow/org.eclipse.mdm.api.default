/********************************************************************************
 * Copyright (c) 2015-2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 ********************************************************************************/
package org.eclipse.mdm.api.dflt.model;

import org.eclipse.mdm.api.base.adapter.Core;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Test;

/**
 * Implementation of the pool entity type. The pool entity is used to establish
 * a further navigation structure through the stored measurement data. Its name
 * may be freely chosen but has to to be unique. A project contains {@link Test}
 * entities as its children.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see Project
 * @see Test
 */
public class Pool extends BaseEntity implements Deletable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * The {@link Project} parent type.
	 */
	public static final Class<Project> PARENT_TYPE_PROJECT = Project.class;

	/**
	 * The {@link Test} child type.
	 */
	public static final Class<Test> CHILD_TYPE_TEST = Test.class;

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	Pool(Core core) {
		super(core);
	}

}
