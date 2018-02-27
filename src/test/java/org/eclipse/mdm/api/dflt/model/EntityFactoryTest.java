/*
 * Copyright (c) 2017-2018 Peak Solution GmbH and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.mdm.api.dflt.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.mdm.api.base.model.Channel;
import org.junit.Test;

public class EntityFactoryTest {

	@Test
	public void createBaseEntities() {
		EntityFactory entityFactory = mock(EntityFactory.class);
		when(entityFactory.createBaseEntity(any(), any())).thenCallRealMethod();

		// The Entities below were chosen as representatives of their respective
		// packages since their constructors don't do anything with the Core instances
		// passed to them, so a null pointer is sufficient for this test.

		// Channel class is in package org.eclipse.mdm.api.base.model
		// (BaseEntityFactory's package):
		assertThat(entityFactory.createBaseEntity(Channel.class, null)).isInstanceOf(Channel.class);
		// CatalogSensor class is in package org.eclipse.mdm.api.dflt.model
		// (EntityFactory's package):
		assertThat(entityFactory.createBaseEntity(CatalogSensor.class, null)).isInstanceOf(CatalogSensor.class);
	}
}
