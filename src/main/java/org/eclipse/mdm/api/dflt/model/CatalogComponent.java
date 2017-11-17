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

import org.eclipse.mdm.api.base.core.Core;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Datable;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Value;

/**
 * Implementation of the catalog component entity type. A catalog component acts
 * as a container to store context data ("as measured", "as ordered"). Each
 * catalog component has a corresponding entity type whose name is equal to the
 * name of the catalog component. Therefore the name of a catalog component has
 * to be unique and is not allowed to be modified, once written. A catalog
 * component consists of {@link CatalogAttribute} which describe the attributes
 * of this container. In case of {@link ContextType#TESTEQUIPMENT} it may have
 * {@link CatalogSensor}s which describe the available measurement sensors.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see CatalogAttribute
 * @see CatalogSensor
 */
public class CatalogComponent extends BaseEntity implements Datable, Deletable, Describable {

	// ======================================================================
	// Instance variables
	// ======================================================================

	private final ContextType contextType;

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	CatalogComponent(Core core) {
		super(core);

		String typeName = core.getTypeName().toUpperCase(Locale.ROOT);
		for (ContextType contextTypeCandidate : ContextType.values()) {
			if (typeName.contains(contextTypeCandidate.name())) {
				contextType = contextTypeCandidate;
				return;
			}
		}

		throw new IllegalStateException("Core is incompatible.");
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

	/**
	 * Returns the {@link CatalogAttribute} identified by given name.
	 *
	 * @param name
	 *            The name of the {@code CatalogAttribute}.
	 * @return The {@code Optional} is empty if a {@code CatalogAttribute} with
	 *         given name does not exist.
	 */
	public Optional<CatalogAttribute> getCatalogAttribute(String name) {
		return getCatalogAttributes().stream().filter(ca -> ca.nameEquals(name)).findAny();
	}

	/**
	 * Returns all available {@link CatalogAttribute}s related to this catalog
	 * component.
	 *
	 * @return The returned {@code List} is unmodifiable.
	 */
	public List<CatalogAttribute> getCatalogAttributes() {
		return getCore().getChildrenStore().get(CatalogAttribute.class);
	}

	/**
	 * Removes the {@link CatalogAttribute} identified by given name.
	 *
	 * @param name
	 *            Name of the {@code CatalogAttribute} that has to be removed.
	 * @return Returns {@code true} if the {@code CatalogAttribute} with given
	 *         name has been removed.
	 */
	public boolean removeCatalogAttribute(String name) {
		Optional<CatalogAttribute> catalogAttribute = getCatalogAttribute(name);
		if (catalogAttribute.isPresent()) {
			getCore().getChildrenStore().remove(catalogAttribute.get());
			return true;
		}

		return false;
	}

	/**
	 * Returns the {@link CatalogSensor} identified by given name.
	 *
	 * @param name
	 *            The name of the {@code CatalogSensor}.
	 * @return The {@code Optional} is empty if a {@code CatalogSensor} with
	 *         given name does not exist.
	 */
	public Optional<CatalogSensor> getCatalogSensor(String name) {
		return getCatalogSensors().stream().filter(cs -> cs.nameEquals(name)).findAny();
	}

	/**
	 * Returns all available {@link CatalogSensor}s related to this catalog
	 * component.
	 *
	 * @return The returned {@code List} is unmodifiable.
	 */
	public List<CatalogSensor> getCatalogSensors() {
		if (contextType.isTestEquipment()) {
			return getCore().getChildrenStore().get(CatalogSensor.class);
		}

		return Collections.emptyList();
	}

	/**
	 * Removes the {@link CatalogSensor} identified by given name.
	 *
	 * @param name
	 *            Name of the {@code CatalogSensor} that has to be removed.
	 * @return Returns {@code true} if the {@code CatalogSensor} with given name
	 *         has been removed.
	 */
	public boolean removeCatalogSensor(String name) {
		Optional<CatalogSensor> catalogSensor = getCatalogSensor(name);
		if (catalogSensor.isPresent()) {
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
		if (!catalogAttributes.isEmpty()) {
			sb.append(", CatalogAttributes = ").append(catalogAttributes);
		}

		List<CatalogSensor> catalogSensors = getCatalogSensors();
		if (!catalogSensors.isEmpty()) {
			sb.append(", CatalogSensors = ").append(catalogSensors);
		}

		return sb.append(')').toString();
	}

}
