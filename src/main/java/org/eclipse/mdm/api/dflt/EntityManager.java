/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.mdm.api.base.BaseEntityManager;
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Entity;
import org.eclipse.mdm.api.base.query.DataAccessException;
import org.eclipse.mdm.api.dflt.model.EntityFactory;
import org.eclipse.mdm.api.dflt.model.Versionable;

/**
 * Extends the {@link BaseEntityManager} interface with additional load methods
 * dedicated to the default application models.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 */
public interface EntityManager extends BaseEntityManager<EntityFactory> {

	// ======================================================================
	// Public methods
	// ======================================================================

	// default Optional<Status> loadStatus(Class<? extends StatusAttachable>
	// entityClass, String name)
	// throws DataAccessException {
	// return loadAllStatus(entityClass, name).stream()
	// .filter(s -> s.nameMatches(name))
	// .findAny();
	// }

	/**
	 * Loads the entity identified by given entity class, {@link ContextType}
	 * and its instance ID.
	 *
	 * @param <T>
	 *            The desired type.
	 * @param entityClass
	 *            Type of the returned entity.
	 * @param contextType
	 *            The {@link ContextType}.
	 * @param instanceID
	 *            The instance ID.
	 * @return The entity with given instance ID is returned.
	 * @throws DataAccessException
	 *             Thrown if unable to retrieve the entity.
	 */
	default <T extends Entity> T load(Class<T> entityClass, ContextType contextType, String instanceID)
			throws DataAccessException {
		List<T> entities = load(entityClass, contextType, Collections.singletonList(instanceID));
		if (entities.size() != 1) {
			throw new DataAccessException("Failed to load entity by instance ID.");
		}
		return entities.get(0);
		
	}

	<T extends Entity> List<T> load(Class<T> entityClass, ContextType contextType, Collection<String> instanceIDs)
			throws DataAccessException;
	
	// default List<Status> loadAllStatus(Class<? extends StatusAttachable>
	// entityClass) throws DataAccessException {
	// return loadAllStatus(entityClass, "*");
	// }

	// List<Status> loadAllStatus(Class<? extends StatusAttachable> entityClass,
	// String pattern)
	// throws DataAccessException;

	/**
	 * Loads all available entities of given type.
	 *
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	List<CatalogComponent> catalogComponents = entityManager.loadAll(CatalogComponent.class, UNITUNDERTEST);
	 * }
	 * </pre>
	 *
	 * @param <T>
	 *            The desired type.
	 * @param entityClass
	 *            Type of the returned entities.
	 * @param contextType
	 *            The {@link ContextType}.
	 * @return Entities are returned in a {@code List}.
	 * @throws DataAccessException
	 *             Thrown if unable to retrieve the entities.
	 * @see #loadAll(Class, ContextType, String)
	 */
	default <T extends Entity> List<T> loadAll(Class<T> entityClass, ContextType contextType)
			throws DataAccessException {
		return loadAll(entityClass, contextType, "*");
	}

	// default <T extends StatusAttachable> List<T> loadAll(Class<T>
	// entityClass, Status status)
	// throws DataAccessException {
	// return loadAll(entityClass, status, "*");
	// }

	// <T extends StatusAttachable> List<T> loadAll(Class<T> entityClass, Status
	// status, String pattern)
	// throws DataAccessException;

	/**
	 * Loads all available entities of given type whose name fulfills the given
	 * pattern.
	 *
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	// retrieve all template roots whose name starts with 'Example'
	 * 	List<TemplateRoot> templateRoots = entityManager.loadAll(TemplateRoot.class, UNITUNDERTEST, "Example*");
	 * }
	 * </pre>
	 *
	 * @param <T>
	 *            The desired type.
	 * @param entityClass
	 *            Type of the returned entities.
	 * @param contextType
	 *            The {@link ContextType}.
	 * @param pattern
	 *            Is always case sensitive and may contain wildcard characters
	 *            as follows: "?" for one matching character and "*" for a
	 *            sequence of matching characters.
	 * @return Matched entities are returned in a {@code List}.
	 * @throws DataAccessException
	 *             Thrown if unable to retrieve the entities.
	 * @see #loadAll(Class)
	 */
	<T extends Entity> List<T> loadAll(Class<T> entityClass, ContextType contextType, String pattern)
			throws DataAccessException;

	/**
	 * Loads the latest valid {@link Versionable} entity of given type and name.
	 *
	 * @param <T>
	 *            The desired type.
	 * @param entityClass
	 *            Type of the returned entity.
	 * @param name
	 *            The exact name of the requested entity.
	 * @return Optional is empty if no such entity was found
	 * @throws DataAccessException
	 *             Thrown if unable to retrieve the entity.
	 */
	default <T extends Versionable> Optional<T> loadLatestValid(Class<T> entityClass, String name)
			throws DataAccessException {
		return loadAll(entityClass, name).stream().filter(v -> v.nameEquals(name)).filter(Versionable::isValid)
				.max(Versionable.COMPARATOR);
	}

	/**
	 * Loads the latest valid {@link Versionable} entity of given type,
	 * {@link ContextType} and name.
	 *
	 * @param <T>
	 *            The desired type.
	 * @param entityClass
	 *            Type of the returned entity.
	 * @param contextType
	 *            The {@code ContextType}.
	 * @param name
	 *            The exact name of the requested entity.
	 * @return Optional is empty if no such entity was found
	 * @throws DataAccessException
	 *             Thrown if unable to retrieve the entity.
	 */
	default <T extends Versionable> Optional<T> loadLatestValid(Class<T> entityClass, ContextType contextType,
			String name) throws DataAccessException {
		return loadAll(entityClass, contextType, name).stream().filter(v -> v.nameEquals(name))
				.filter(Versionable::isValid).max(Versionable.COMPARATOR);
	}

	// default <T extends StatusAttachable> List<T> loadChildren(Entity parent,
	// Class<T> entityClass,
	// Status status) throws DataAccessException {
	// return loadChildren(parent, entityClass, status, "*");
	// }

	// <T extends StatusAttachable> List<T> loadChildren(Entity parent, Class<T>
	// entityClass,
	// Status status, String pattern) throws DataAccessException;

}
