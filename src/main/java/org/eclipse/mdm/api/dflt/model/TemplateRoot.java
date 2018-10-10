/********************************************************************************
 * Copyright (c) 2015-2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 ********************************************************************************/

package org.eclipse.mdm.api.dflt.model;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.mdm.api.base.adapter.Core;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.ContextRoot;
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Value;

/**
 * Implementation of the template attribute entity type. A template root defines
 * a tree of {@code TemplateComponent}s. Such a tree forms a hierarchical
 * template structure which is used to describe the composition of a
 * {@link ContextRoot}. A template root implements the {@link Versionable}
 * interface and hence has a version and a state. As long as
 * {@link #isEditable()} returns {@code true} any part of that template root is
 * allowed to be modified. Once a template root is set to be valid
 * ({@link #isValid()} == {@code true}) it may be used to compose a
 * {@link TemplateTestStep} and then is no longer allowed to be modified in any
 * way. If a valid template root needs to be modified, then a deep copy with a
 * unique name and version combination has to be created (deep copy means new
 * instances).
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see TemplateComponent
 * @see Versionable
 */
public class TemplateRoot extends BaseEntity implements Deletable, Versionable {

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
	TemplateRoot(Core core) {
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
	 * Returns the {@link ContextType} of this template root.
	 *
	 * @return The {@code ContextType} is returned.
	 */
	public ContextType getContextType() {
		return contextType;
	}

	/**
	 * Returns the {@link TemplateComponent} identified by given name.
	 *
	 * <p>
	 * <b>NOTE:</b> The names of <u>all</u> {@code TemplateComponent}s belonging
	 * to the same template root must have unique names (no matter they are
	 * immediate children or not). Therefore, if this template root does not
	 * have an immediate {@code TemplateComponent} with the given name, this
	 * lookup request is recursively delegated to all of its child
	 * {@code TemplateComponent}s.
	 *
	 * @param name
	 *            The name of the {@code TemplateComponent}.
	 * @return The {@code Optional} is empty if a {@code TemplateComponent} with
	 *         given name does not exist at all within this template root.
	 */
	public Optional<TemplateComponent> getTemplateComponent(String name) {
		List<TemplateComponent> templateComponents = getTemplateComponents();
		Optional<TemplateComponent> templateComponent = templateComponents.stream().filter(tc -> tc.nameEquals(name))
				.findAny();
		if (templateComponent.isPresent()) {
			return templateComponent;
		}

		return templateComponents.stream().map(tc -> tc.getTemplateComponent(name)).filter(Optional::isPresent)
				.map(Optional::get).findAny();
	}

	/**
	 * Returns all immediate {@link TemplateComponent}s related to this template
	 * component.
	 *
	 * @return The returned {@code List} is unmodifiable.
	 */
	public List<TemplateComponent> getTemplateComponents() {
		return getCore().getChildrenStore().get(TemplateComponent.class);
	}

	/**
	 * Removes the {@link TemplateComponent} identified by given name.
	 *
	 * <p>
	 * <b>NOTE:</b> The names of <u>all</u> {@code TemplateComponent}s belonging
	 * to the same template roots must have unique names (no matter they are
	 * immediate children or not). Therefore, if this template root does not
	 * have an immediate {@code TemplateComponent} with the given name, this
	 * remove request is recursively delegated to all of its child
	 * {@code TemplateComponent}s.
	 *
	 * @param name
	 *            Name of the {@code TemplateComponent} that has to be removed.
	 * @return Returns {@code true} if the {@code TemplateComponent} with given
	 *         name has been removed.
	 */
	public boolean removeTemplateComponent(String name) {
		Optional<TemplateComponent> templateComponent = getTemplateComponent(name);
		if (templateComponent.isPresent()) {
			Optional<TemplateComponent> parentTemplateComponent = templateComponent.get().getParentTemplateComponent();
			if (parentTemplateComponent.isPresent()) {
				parentTemplateComponent.get().removeTemplateComponent(name);
			} else {
				getCore().getChildrenStore().remove(templateComponent.get());
			}
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

		List<TemplateComponent> templateComponents = getTemplateComponents();
		if (!templateComponents.isEmpty()) {
			sb.append(", TemplateComponents = ").append(templateComponents);
		}

		return sb.append(')').toString();
	}

	/**
	 * Returns the {@link TemplateRoot} the given {@link ContextRoot} is derived
	 * from.
	 *
	 * @param contextRoot
	 *            The {@code ContextRoot} whose {@code TemplateRoot} is
	 *            requested.
	 * @return {@code Optional} is empty if the given {@code ContextRoot} is not
	 *         derived from a template, which is data source specific.
	 */
	public static Optional<TemplateRoot> of(ContextRoot contextRoot) {
		return Optional.ofNullable(getCore(contextRoot).getMutableStore().get(TemplateRoot.class));
	}

}
