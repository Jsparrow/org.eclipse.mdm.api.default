/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.TestStep;

/**
 * Implementation of the template test step entity type. A template test step
 * describes the composition of {@link TemplateRoot}s which in finally is a
 * template for a  persisted context description ("as measured", "as ordered").
 * A template test step implements the {@link Versionable} interface and hence
 * has a version and a state. As long as {@link #isEditable()} returns {@code
 * true} any part of that template test step is allowed to be modified. Once a
 * template test step is set to be valid ({@link #isValid()} == {@code true})
 * it may be used to compose a {@link TemplateTest} and then is no longer
 * allowed to be modified in any way. If a valid template test step needs to
 * be modified, then a deep copy with a unique name and version combination has
 * to be created (deep copy means new instances).
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see TemplateRoot
 */
public final class TemplateTestStep extends BaseEntity implements Deletable, Describable, Versionable {

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core The {@link Core}.
	 */
	TemplateTestStep(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Returns the related {@link TemplateRoot} with given {@link ContextType}
	 * of this template test step.
	 *
	 * @param contextType Used as identifier.
	 * @return {@code Optional} is empty if a {@code TemplateRoot} of given
	 * 		{@code ContextType} does not exist.
	 */
	public Optional<TemplateRoot> getTemplateRoot(ContextType contextType) {
		return Optional.ofNullable(getCore().getMutableStore().get(TemplateRoot.class, contextType));
	}

	/**
	 * Returns all available {@link TemplateRoot}s related to this template
	 * test step.
	 *
	 * @return Returns {@code List} contains up to 3 {@code TemplateRoot}s
	 * 		with distinct {@link ContextType}s.
	 */
	public List<TemplateRoot> getTemplateRoots() {
		List<TemplateRoot> templateRoots = new ArrayList<>();
		getTemplateRoot(ContextType.UNITUNDERTEST).ifPresent(templateRoots::add);
		getTemplateRoot(ContextType.TESTSEQUENCE).ifPresent(templateRoots::add);
		getTemplateRoot(ContextType.TESTEQUIPMENT).ifPresent(templateRoots::add);
		return templateRoots;
	}

	/**
	 * Replaces current {@link TemplateRoot} with the given one.
	 *
	 * @param templateRoot Is not allowed to be null.
	 * @see #removeTemplateRoot(ContextType)
	 */
	public void setTemplateRoot(TemplateRoot templateRoot) {
		getCore().getMutableStore().set(templateRoot, templateRoot.getContextType());
	}

	/**
	 * Removes current {@link TemplateRoot} with given {@link ContextType}.
	 *
	 * @param contextType Used as identifier.
	 * @return Returns {@code true} if the {@code TemplateRoot} has been removed.
	 */
	public boolean removeTemplateRoot(ContextType contextType) {
		boolean contained = getTemplateRoot(contextType).isPresent();
		getCore().getMutableStore().remove(TemplateRoot.class, contextType);
		return contained;
	}

	/**
	 * Returns the {@link TemplateTestStep} the given {@link TestStep}
	 * is derived from.
	 *
	 * @param testStep The {@code TestStep} whose {@code TemplateTestStep} is
	 * 		requested.
	 * @return {@code Optional} is empty if the given {@code TestStep}
	 * 		is not derived from a template.
	 */
	public static Optional<TemplateTestStep> of(TestStep testStep) {
		return Optional.ofNullable(getCore(testStep).getMutableStore().get(TemplateTestStep.class));
	}

}
