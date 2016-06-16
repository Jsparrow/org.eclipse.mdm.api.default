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

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.StatusAttachable;

public final class Status extends BaseEntity implements Describable {

	// ======================================================================
	// Constructors
	// ======================================================================

	Status(Core core) {
		super(core);
	}

	public <T extends StatusAttachable> void assign(List<T> statusAttachables) {
		statusAttachables.forEach(this::assign);
	}

	public <T extends StatusAttachable> void assign(T statusAttachable) {
		getCore(statusAttachable).getMutableStore().set(this);
	}

	public static Optional<Status> of(StatusAttachable statusAttachable) {
		return Optional.ofNullable(getCore(statusAttachable).getMutableStore().get(Status.class));
	}

}
