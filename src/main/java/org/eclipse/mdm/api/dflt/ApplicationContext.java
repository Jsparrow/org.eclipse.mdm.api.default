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

package org.eclipse.mdm.api.dflt;

import org.eclipse.mdm.api.base.BaseApplicationContext;
import org.eclipse.mdm.api.dflt.model.EntityFactory;

/**
 * Extends the {@link BaseApplicationContext} interface to return {@link EntityFactory} 
 * and {@link EntityManager}.
 *
 * @since 1.0.0
 */
public interface ApplicationContext extends BaseApplicationContext<EntityFactory, EntityManager> {

}
