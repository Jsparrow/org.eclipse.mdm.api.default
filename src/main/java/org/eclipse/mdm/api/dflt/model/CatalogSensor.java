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
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Datable;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Value;

/**
 * Implementation of the catalog sensor entity type. A catalog sensor acts as a
 * container to store sensor context data ("as measured", "as ordered"). It
 * always belongs to a {@link CatalogComponent} of type
 * {@link ContextType#TESTEQUIPMENT}. Each catalog sensor has a corresponding
 * entity type whose name is equal to the name of the catalog sensor. Therefore
 * the name of a catalog sensor has to be unique and is not allowed to be
 * modified, once written. A catalog sensor consists of {@link CatalogAttribute}
 * which describe the attributes of this container.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see CatalogComponent
 * @see CatalogComponent
 */
public final class CatalogSensor extends BaseEntity implements Datable, Deletable, Describable {

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	CatalogSensor(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Returns the parent {@link CatalogComponent}.
	 *
	 * @return The parent {@code CatalogComponent} is returned.
	 */
	public CatalogComponent getCatalogComponent() {
		return getCore().getPermanentStore().get(CatalogComponent.class);
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
		return getCatalogAttributes().stream().filter(ca -> ca.nameMatches(name)).findAny();
	}

	/**
	 * Returns all available {@link CatalogAttribute}s related to this catalog
	 * sensor.
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
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('(');
		sb.append(getValues().values().stream().map(Value::toString).collect(Collectors.joining(", ")));

		List<CatalogAttribute> catalogAttributes = getCatalogAttributes();
		if (!catalogAttributes.isEmpty()) {
			sb.append(", CatalogAttributes = ").append(catalogAttributes);
		}

		return sb.append(')').toString();
	}

}
