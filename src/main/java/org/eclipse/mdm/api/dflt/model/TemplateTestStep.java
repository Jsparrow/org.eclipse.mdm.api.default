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
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.TestStep;

public final class TemplateTestStep extends BaseEntity implements Deletable, Describable, Versionable {

	// ======================================================================
	// Constructors
	// ======================================================================

	TemplateTestStep(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	public Optional<TemplateRoot> getTemplateRoot(ContextType contextType) {
		return Optional.ofNullable(getCore().getMutableStore().get(TemplateRoot.class, contextType));
	}

	public List<TemplateRoot> getTemplateRoots() {
		List<TemplateRoot> templateRoots = new ArrayList<>();
		getTemplateRoot(ContextType.UNITUNDERTEST).ifPresent(templateRoots::add);
		getTemplateRoot(ContextType.TESTSEQUENCE).ifPresent(templateRoots::add);
		getTemplateRoot(ContextType.TESTEQUIPMENT).ifPresent(templateRoots::add);
		return templateRoots;
	}

	public void setTemplateRoot(TemplateRoot templateRoot) {
		getCore().getMutableStore().set(templateRoot, templateRoot.getContextType());
	}

	public boolean removeTemplateRoot(ContextType contextType) {
		boolean contained = getTemplateRoot(contextType).isPresent();
		getCore().getMutableStore().remove(TemplateRoot.class, contextType);
		return contained;
	}

	public static Optional<TemplateTestStep> of(TestStep testStep) {
		return Optional.ofNullable(getCore(testStep).getMutableStore().get(TemplateTestStep.class));
	}

}
