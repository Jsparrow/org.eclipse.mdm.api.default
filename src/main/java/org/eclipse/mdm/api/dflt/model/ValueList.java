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
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Datable;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Value;

/**
 * Implementation of the value list entity type. A value list provides a set of
 * default values which may be used as options while defining default values in
 * templates or modifying context data.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see ValueListValue
 * @see CatalogAttribute
 */
public final class ValueList extends BaseEntity implements Datable, Describable, Deletable {

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core The {@link Core}.
	 */
	ValueList(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Returns the {@link ValueListValue} identified by given name.
	 *
	 * @param name The name of the {@code ValueListValue}.
	 * @return The {@code Optional} is empty if a {@code ValueListValue} with
	 * 		given name does not exist.
	 */
	public Optional<ValueListValue> getValueListValue(String name) {
		return getValueListValues().stream().filter(vlv -> vlv.nameMatches(name)).findAny();
	}

	/**
	 * Returns all available {@link ValueListValue}s related to this value list.
	 *
	 * @return The returned {@code List} is unmodifiable.
	 */
	public List<ValueListValue> getValueListValues() {
		return getCore().getChildrenStore().get(ValueListValue.class);
	}

	/**
	 * Removes the {@link ValueListValue} identified by given name.
	 *
	 * @param name Name of the {@code ValueListValue} that has to be removed.
	 * @return Returns {@code true} if the {@code ValueListValues} with given
	 * 		name has been removed.
	 */
	public boolean removeValueListValue(String name) {
		Optional<ValueListValue> valueListValue = getValueListValue(name);
		if(valueListValue.isPresent()) {
			getCore().getChildrenStore().remove(valueListValue.get());
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

		List<ValueListValue> valueListValues = getValueListValues();
		if(!valueListValues.isEmpty()) {
			sb.append(", ValueListValues = ").append(valueListValues);
		}

		return sb.append(')').toString();
	}

}
