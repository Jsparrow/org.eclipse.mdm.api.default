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
import org.eclipse.mdm.api.dflt.model.EntityFactory;
import org.eclipse.mdm.api.dflt.model.Versionable;

public interface EntityManager extends BaseEntityManager<EntityFactory> {

	// ======================================================================
	// Public methods
	// ======================================================================

	//	default Optional<Status> loadStatus(Class<? extends StatusAttachable> entityClass, String name) throws DataAccessException {
	//		return loadAllStatus(entityClass, name).stream()
	//				.filter(s -> s.nameMatches(name))
	//				.findAny();
	//	}

	<T extends Entity> T load(Class<T> entityClass, ContextType contextType, Long instanceID) throws DataAccessException;

	//	default List<Status> loadAllStatus(Class<? extends StatusAttachable> entityClass) throws DataAccessException {
	//		return loadAllStatus(entityClass, "*");
	//	}
	//
	//	List<Status> loadAllStatus(Class<? extends StatusAttachable> entityClass, String pattern) throws DataAccessException;

	default <T extends Entity> List<T> loadAll(Class<T> entityClass, ContextType contextType) throws DataAccessException {
		return loadAll(entityClass, contextType, "*");
	}

	//	default <T extends StatusAttachable> List<T> loadAll(Class<T> entityClass, Status status) throws DataAccessException {
	//		return loadAll(entityClass, status, "*");
	//	}

	//	<T extends StatusAttachable> List<T> loadAll(Class<T> entityClass, Status status, String pattern) throws DataAccessException;

	<T extends Entity> List<T> loadAll(Class<T> entityClass, ContextType contextType, String pattern) throws DataAccessException;

	default <T extends Versionable> Optional<T> loadLatestValid(Class<T> entityClass, String name) throws DataAccessException {
		return loadAll(entityClass, name).stream()
				.filter(v -> v.nameMatches(name))
				.filter(Versionable::isValid)
				.max(Versionable.COMPARATOR);
	}

	default <T extends Versionable> Optional<T> loadLatestValid(Class<T> entityClass, ContextType contextType, String name) throws DataAccessException {
		return loadAll(entityClass, contextType, name).stream()
				.filter(v -> v.nameMatches(name))
				.filter(Versionable::isValid)
				.max(Versionable.COMPARATOR);
	}

	//	default <T extends StatusAttachable> List<T> loadChildren(Entity parent, Class<T> entityClass, Status status) throws DataAccessException {
	//		return loadChildren(parent, entityClass, status, "*");
	//	}
	//
	//	<T extends StatusAttachable> List<T> loadChildren(Entity parent, Class<T> entityClass, Status status, String pattern) throws DataAccessException;

}
