/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.function.Predicate;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Sortable;

public final class TemplateTestStepUsage extends BaseEntity implements Deletable, Sortable {

	// ======================================================================
	// Class variables
	// ======================================================================

	public static final String ATTR_DEFAULT_ACTIVE = "DefaultActive";
	public static final String ATTR_OPTIONAL = "Optional";

	public static final Predicate<TemplateTestStepUsage> IS_OPTIONAL = TemplateTestStepUsage::isOptional;
	public static final Predicate<TemplateTestStepUsage> IS_MANDATORY = IS_OPTIONAL.negate();
	public static final Predicate<TemplateTestStepUsage> IS_DEFAULT_ACTIVE = TemplateTestStepUsage::isDefaultActive;
	public static final Predicate<TemplateTestStepUsage> IS_IMPLICIT_CREATE = IS_DEFAULT_ACTIVE.or(IS_MANDATORY);
	// ======================================================================
	// Constructors
	// ======================================================================

	TemplateTestStepUsage(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	public Boolean isDefaultActive() {
		return getValue(ATTR_DEFAULT_ACTIVE).extract();
	}

	public void setDefaultActive(Boolean defaultActive) {
		getValue(ATTR_DEFAULT_ACTIVE).set(defaultActive);
	}

	public Boolean isOptional() {
		return getValue(ATTR_OPTIONAL).extract();
	}

	public void setOptional(Boolean optional) {
		getValue(ATTR_OPTIONAL).set(optional);
	}

	public TemplateTest getTemplateTest() {
		return getCore().getPermanentStore().get(TemplateTest.class);
	}

	public TemplateTestStep getTemplateTestStep() {
		return getCore().getMutableStore().get(TemplateTestStep.class);
	}

}
