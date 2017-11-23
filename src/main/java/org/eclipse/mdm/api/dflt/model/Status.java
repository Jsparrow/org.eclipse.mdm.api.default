/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.List;
import java.util.Optional;

import org.eclipse.mdm.api.base.adapter.Core;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.StatusAttachable;
import org.eclipse.mdm.api.base.model.Test;
import org.eclipse.mdm.api.base.model.TestStep;

/**
 * Implementation of the status entity type. A status may be attached to
 * {@link StatusAttachable} entities like {@link Test} or {@link TestStep} to
 * indicate they have a state in a complete workflow.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 */
public class Status extends BaseEntity implements Describable {

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	Status(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Assigns this status to given {@link StatusAttachable}s.
	 *
	 * @param <T>
	 *            The status attachable type.
	 * @param statusAttachables
	 *            This status will be assigned to all of them.
	 */
	public <T extends StatusAttachable> void assign(List<T> statusAttachables) {
		statusAttachables.forEach(this::assign);
	}

	/**
	 * Assigns this status to given {@link StatusAttachable}.
	 *
	 * @param <T>
	 *            The status attachable type.
	 * @param statusAttachable
	 *            This status will be assigned to it.
	 */
	public <T extends StatusAttachable> void assign(T statusAttachable) {
		getCore(statusAttachable).getMutableStore().set(this);
	}

	/**
	 * Returns the {@link Status} attached to given {@link StatusAttachable}.
	 *
	 * @param statusAttachable
	 *            The {@code StatusAttachable}.
	 * @return Optional is empty if no {@code Status} is attached.
	 */
	public static Optional<Status> of(StatusAttachable statusAttachable) {
		return Optional.ofNullable(getCore(statusAttachable).getMutableStore().get(Status.class));
	}

}
