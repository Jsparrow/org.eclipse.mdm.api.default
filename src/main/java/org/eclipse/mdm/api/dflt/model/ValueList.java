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

public final class ValueList extends BaseEntity implements Datable, Describable, Deletable {

	// ======================================================================
	// Constructors
	// ======================================================================

	ValueList(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	public Optional<ValueListValue> getValueListValue(String name) {
		return getValueListValues().stream().filter(vlv -> vlv.nameMatches(name)).findAny();
	}

	public List<ValueListValue> getValueListValues() {
		return getCore().getChildrenStore().get(ValueListValue.class);
	}

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
