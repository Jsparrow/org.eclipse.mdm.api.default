/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Datable;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.EntityCore;
import org.eclipse.mdm.api.base.model.Value;

public final class CatalogComponent extends BaseEntity implements Datable, Deletable, Describable {

	// ======================================================================
	// Instance variables
	// ======================================================================

	private final ContextType contextType;

	// ======================================================================
	// Constructors
	// ======================================================================

	CatalogComponent(EntityCore core) {
		super(core);

		String typeName = core.getURI().getTypeName().toUpperCase(Locale.ROOT);
		for(ContextType contextTypeCandidate : ContextType.values()) {
			if(typeName.contains(contextTypeCandidate.name())) {
				contextType = contextTypeCandidate;
				return;
			}
		}

		throw new IllegalStateException("Given core is incompatible.");
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Returns the {@link ContextType} of this catalog component.
	 *
	 * @return The {@code ContextType} is returned.
	 */
	public ContextType getContextType() {
		return contextType;
	}

	public Optional<CatalogAttribute> getCatalogAttribute(String name) {
		return getCatalogAttributes().stream().filter(ca -> ca.getName().equals(name)).findAny();
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

	public Optional<CatalogSensor> getCatalogSensor(String name) {
		return getCatalogSensors().stream().filter(cs -> cs.getName().equals(name)).findAny();
	}

	public List<CatalogSensor> getCatalogSensors() {
		if(contextType.isTestEquipment()) {
			return getCore().getChildrenStore().get(CatalogSensor.class);
		}

		return Collections.emptyList();
	}

	public boolean removeCatalogSensor(String name) {
		Optional<CatalogSensor> catalogSensor = getCatalogSensor(name);
		if(catalogSensor.isPresent()) {
			getCore().getChildrenStore().remove(catalogSensor.get());
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('(');
		sb.append("ContextType = ").append(getContextType()).append(", ");
		sb.append(getValues().values().stream().map(Value::toString).collect(Collectors.joining(", ")));

		List<CatalogAttribute> catalogAttributes = getCatalogAttributes();
		if(!catalogAttributes.isEmpty()) {
			sb.append(", CatalogAttributes = ").append(catalogAttributes);
		}

		List<CatalogSensor> catalogSensors = getCatalogSensors();
		if(!catalogSensors.isEmpty()) {
			sb.append(", CatalogSensors = ").append(catalogSensors);
		}

		return sb.append(')').toString();
	}

}
