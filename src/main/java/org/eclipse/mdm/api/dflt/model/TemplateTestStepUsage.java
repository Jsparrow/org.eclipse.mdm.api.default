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
import org.eclipse.mdm.api.base.model.Test;
import org.eclipse.mdm.api.base.model.TestStep;

/**
 * Implementation of the template test step usage entity type. A template test
 * relates a {@link TemplateTestStep} with a parent {@link TemplateTest} (n:m).
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see TemplateTest
 * @see TemplateTestStep
 */
public class TemplateTestStepUsage extends BaseEntity implements Deletable, Sortable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * The optional flag {@code Predicate}.
	 */
	public static final Predicate<TemplateTestStepUsage> IS_OPTIONAL = TemplateTestStepUsage::isOptional;

	/**
	 * The mandatory flag {@code Predicate}. This is the inversion of
	 * {@link #IS_OPTIONAL} {@code Predicate}.
	 */
	public static final Predicate<TemplateTestStepUsage> IS_MANDATORY = IS_OPTIONAL.negate();

	/**
	 * The default active flag {@code Predicate}.
	 */
	public static final Predicate<TemplateTestStepUsage> IS_DEFAULT_ACTIVE = TemplateTestStepUsage::isDefaultActive;

	/**
	 * The implicit create flag {@code Predicate}. This is an OR combination of
	 * {@link #IS_DEFAULT_ACTIVE} and {@link #IS_MANDATORY} {@code Predicate}s.
	 */
	public static final Predicate<TemplateTestStepUsage> IS_IMPLICIT_CREATE = IS_DEFAULT_ACTIVE.or(IS_MANDATORY);

	/**
	 * The 'DefaultActive' attribute name.
	 */
	public static final String ATTR_DEFAULT_ACTIVE = "DefaultActive";

	/**
	 * The 'Optional' attribute name.
	 */
	public static final String ATTR_OPTIONAL = "Optional";

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	TemplateTestStepUsage(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Returns the default active flag of this template test step usage.
	 *
	 * @return Returns {@code true} if a {@link TestStep} has to be created
	 *         automatically each time a new {@link Test} is derived from the
	 *         {@link TemplateTest} this template test step usage belongs to.
	 */
	public Boolean isDefaultActive() {
		return getValue(ATTR_DEFAULT_ACTIVE).extract();
	}

	/**
	 * Sets a new default active flag for this template test step usage.
	 *
	 * @param defaultActive
	 *            The new default active flag.
	 */
	public void setDefaultActive(Boolean defaultActive) {
		getValue(ATTR_DEFAULT_ACTIVE).set(defaultActive);
	}

	/**
	 * Returns the optional flag of this template test step.
	 *
	 * @return Returns {@code true} if it is allowed to omit a {@link TestStep}
	 *         derived from this template test step usage.
	 */
	public Boolean isOptional() {
		return getValue(ATTR_OPTIONAL).extract();
	}

	/**
	 * Sets a new optional flag for this template test step usage.
	 *
	 * @param optional
	 *            The new optional flag.
	 */
	public void setOptional(Boolean optional) {
		getValue(ATTR_OPTIONAL).set(optional);
	}

	/**
	 * Returns the parent {@link TemplateTest}.
	 *
	 * @return The parent {@code TemplateTest} is returned.
	 */
	public TemplateTest getTemplateTest() {
		return getCore().getPermanentStore().get(TemplateTest.class);
	}

	/**
	 * Returns the {@link TemplateTestStep} related to this template test step
	 * usage.
	 *
	 * @return The {@code TemplateTestStep} is returned.
	 */
	public TemplateTestStep getTemplateTestStep() {
		return getCore().getMutableStore().get(TemplateTestStep.class);
	}

}
