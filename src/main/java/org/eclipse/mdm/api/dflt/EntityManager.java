/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt;

import java.util.List;
import java.util.Optional;

import org.eclipse.mdm.api.base.BaseEntityManager;
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Entity;
import org.eclipse.mdm.api.base.query.DataAccessException;
import org.eclipse.mdm.api.dflt.model.Versionable;

public interface EntityManager extends BaseEntityManager {

	// ======================================================================
	// Public methods
	// ======================================================================

	// this one is required to load template roots ... or catalog components
	default <T extends Entity> List<T> loadAll(Class<T> type, ContextType contextType) throws DataAccessException {
		return loadAll(type, contextType, "*");
	}

	<T extends Entity> List<T> loadAll(Class<T> type, ContextType contextType, String pattern) throws DataAccessException;

	default <T extends Versionable> Optional<T> loadLatestValid(Class<T> type, ContextType contextType, String name) throws DataAccessException {
		int currentVersion = 0;
		T result = null;

		/*
		 * TODO: this works but load unnecessarily all similar versionables including all of their children!
		 *
		 * alternative approach:
		 * 1. query versions of all VALID versionables -> find highest version
		 * (this must not necessarily be the one with the highest instance ID!)
		 * 2. load the versionable with a predefined filter!
		 * (described solution should outperform this default implementation)
		 */

		for(T versionable : loadAll(type, contextType, name)) {
			if(!versionable.getName().equals(name) || !versionable.getVersionState().isValid()) {
				continue;
			}

			if(currentVersion < versionable.getVersion()) {
				result = versionable;
				currentVersion = versionable.getVersion();
			}
		}

		return Optional.ofNullable(result);
	}

	// TODO name is NOT a pattern -> use EQUALS instead of LIKE operation!
	default <T extends Versionable> Optional<T> loadLatestValid(Class<T> type, String name) throws DataAccessException {
		int currentVersion = 0;
		T result = null;

		/*
		 * TODO: this works but load unnecessarily all similar versionables including all of their children!
		 *
		 * alternative approach:
		 * 1. query versions of all VALID versionables -> find highest version
		 * (this must not necessarily be the one with the highest instance ID!)
		 * 2. load the versionable with a predefined filter!
		 * (described solution should outperform this default implementation)
		 */

		for(T versionable : loadAll(type, name)) {
			if(!versionable.getName().equals(name) || !versionable.getVersionState().isValid()) {
				continue;
			}

			if(currentVersion < versionable.getVersion()) {
				result = versionable;
				currentVersion = versionable.getVersion();
			}
		}

		return Optional.ofNullable(result);
	}

	//	List<Status> loadStatus(Class<? extends StatusAttachable> type) throws DataAccessException;
	//
	//	Optional<Status> loadStatus(Class<? extends StatusAttachable> type, String name) throws DataAccessException;
	//
	//	default <T extends StatusAttachable> List<T> loadAll(Class<T> type, Status status) throws DataAccessException {
	//		return loadAll(type, status, "*");
	//	}
	//
	//	<T extends StatusAttachable> List<T> loadAll(Class<T> type, Status status, String pattern) throws DataAccessException;
	//
	//	default <T extends StatusAttachable> List<T> loadChildren(Entity parent, Class<T> type, Status status) throws DataAccessException {
	//		return loadChildren(parent, type, status, "*");
	//	}
	//
	//	<T extends StatusAttachable> List<T> loadChildren(Entity parent, Class<T> type, Status status, String pattern) throws DataAccessException;

}
