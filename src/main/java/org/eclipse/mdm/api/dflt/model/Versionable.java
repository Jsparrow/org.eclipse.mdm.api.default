/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.Comparator;

import org.eclipse.mdm.api.base.model.Datable;
import org.eclipse.mdm.api.base.model.VersionState;

/**
 * This interface extends the {@link Datable} interface and provides getter
 * and setter methods for the 'Version' and 'VersionState' fields of an entity.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 */
public interface Versionable extends Datable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * This {@code Comparator} compares {@link Versionable}s by their name (in
	 * ascending order) and version (in descending order).
	 */
	static final Comparator<Versionable> COMPARATOR = Comparator.comparing(Versionable::getName)
			.thenComparing(Comparator.comparing(Versionable::getVersion).reversed());

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

	/**
	 * Checks whether parts of this versionable are allowed to be modified.
	 *
	 * @return Returns {@code true} if modifications are allowed.
	 */
	default boolean isEditable() {
		return getVersionState().isEditable();
	}

	/**
	 * Checks whether this versionable is valid and therefore is no longer
	 * allowed to be modified.
	 *
	 * @return Returns {@code true} if this versionable is valid.
	 */
	default boolean isValid() {
		return getVersionState().isValid();
	}

	/**
	 * Checks whether this versionable is archived.
	 *
	 * @return Returns {@code true} if this versionable is archived.
	 */
	default boolean isArchived() {
		return getVersionState().isArchived();
	}

}
