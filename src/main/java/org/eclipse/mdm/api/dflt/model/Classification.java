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

import java.util.List;

import org.eclipse.mdm.api.base.adapter.Core;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.StatusAttachable;

public class Classification
        extends BaseEntity  {

    // ======================================================================
    // Constructors
    // ======================================================================

    /**
     * Constructor.
     *
     * @param core
     *            The {@link Core}.
     */
    Classification(Core core) {
        super(core);
    }

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
}
