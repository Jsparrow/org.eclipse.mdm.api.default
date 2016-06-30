/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.ContextRoot;
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Value;

public final class TemplateRoot extends BaseEntity implements Deletable, Versionable {

	// ======================================================================
	// Instance variables
	// ======================================================================

	private final ContextType contextType;

	// ======================================================================
	// Constructors
	// ======================================================================

	TemplateRoot(Core core) {
		super(core);

		String typeName = core.getTypeName().toUpperCase(Locale.ROOT);
		for(ContextType contextTypeCandidate : ContextType.values()) {
			if(typeName.contains(contextTypeCandidate.name())) {
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

	// TODO java doc lookup is recursive in all template components children
	public Optional<TemplateComponent> getTemplateComponent(String name) {
		List<TemplateComponent> templateComponents = getTemplateComponents();
		Optional<TemplateComponent> templateComponent = templateComponents.stream().filter(tc -> tc.nameMatches(name)).findAny();
		if(templateComponent.isPresent()) {
			return templateComponent;
		}

		return templateComponents.stream().map(tc -> tc.getTemplateComponent(name)).filter(Optional::isPresent).map(Optional::get).findAny();
	}

	public List<TemplateComponent> getTemplateComponents() {
		return getCore().getChildrenStore().get(TemplateComponent.class);
	}

	// TODO search the complete tree!
	public boolean removeTemplateComponent(String name) {
		Optional<TemplateComponent> templateComponent = getTemplateComponent(name);
		if(templateComponent.isPresent()) {
			Optional<TemplateComponent> parentTemplateComponent = templateComponent.get().getParentTemplateComponent();
			if(parentTemplateComponent.isPresent()) {
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
		if(!templateComponents.isEmpty()) {
			sb.append(", TemplateComponents = ").append(templateComponents);
		}

		return sb.append(')').toString();
	}

	public static Optional<TemplateRoot> of(ContextRoot contextRoot) {
		return Optional.ofNullable(getCore(contextRoot).getMutableStore().get(TemplateRoot.class));
	}

}
