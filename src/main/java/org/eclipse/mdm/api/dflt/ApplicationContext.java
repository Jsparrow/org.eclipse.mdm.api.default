/*
 * Copyright (c) 2017 Peak Solution GmbH and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
