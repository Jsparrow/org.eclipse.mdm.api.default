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
import java.util.stream.Collectors;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Datable;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Value;

public final class CatalogSensor extends BaseEntity implements Datable, Deletable, Describable {

	// ======================================================================
	// Constructors
	// ======================================================================

	CatalogSensor(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	public CatalogComponent getCatalogComponent() {
		return getCore().getPermanentStore().get(CatalogComponent.class);
	}

	public Optional<CatalogAttribute> getCatalogAttribute(String name) {
		return getCatalogAttributes().stream().filter(ca -> ca.nameMatches(name)).findAny();
	}

	public List<CatalogAttribute> getCatalogAttributes() {
		return getCore().getChildrenStore().get(CatalogAttribute.class);
	}

	public boolean removeCatalogAttribute(String name) {
		Optional<CatalogAttribute> catalogAttribute = getCatalogAttribute(name);
		if(catalogAttribute.isPresent()) {
			getCore().getChildrenStore().remove(catalogAttribute.get());
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('(');
		sb.append(getValues().values().stream().map(Value::toString).collect(Collectors.joining(", ")));

		List<CatalogAttribute> catalogAttributes = getCatalogAttributes();
		if(!catalogAttributes.isEmpty()) {
			sb.append(", CatalogAttributes = ").append(catalogAttributes);
		}

		return sb.append(')').toString();
	}

}
