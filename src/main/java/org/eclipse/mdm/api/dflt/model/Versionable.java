/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import org.eclipse.mdm.api.base.model.Datable;
import org.eclipse.mdm.api.base.model.VersionState;

public interface Versionable extends Datable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * The 'Version' attribute name.
	 */
	static final String ATTR_VERSION = "Version";

	/**
	 * The 'VersionState' attribute name.
	 */
	static final String ATTR_VERSION_STATE = "ValidFlag";

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Returns the version of this entity.
	 *
	 * @return The version is returned.
	 */
	default Integer getVersion() {
		return Integer.valueOf(getValue(ATTR_VERSION).extract());
	}

	/**
	 * Sets new version for this entity.
	 *
	 * @param version The new version.
	 */
	default void setVersion(Integer version) {
		getValue(ATTR_VERSION).set(version.toString());
	}

	/**
	 * Returns the {@link VersionState} of this entity.
	 *
	 * @return The {@code VersionState} is returned.
	 */
	default VersionState getVersionState() {
		return getValue(ATTR_VERSION_STATE).extract();
	}

	/**
	 * Sets new {@link VersionState} for this entity.
	 *
	 * @param versionState The new {@code VersionState}.
	 */
	default void setVersionState(VersionState versionState) {
		getValue(ATTR_VERSION_STATE).set(versionState);
	}

	default boolean isEditable() {
		/*
		 * TODO
		 * editable if:
		 *  - oldVersionState == EDITBALE
		 *  - newVersionState == EDITBALE
		 *
		 *  ==> getValue(ATTR_VERSION_STATE).getInitialValue().isEditable();
		 *
		 *  ==> maybe it makes more sense to check here only the current
		 *  value and leave checking of the initial value to the insert
		 *  / update statements?!
		 */
		return getVersionState().isEditable();
	}

}
