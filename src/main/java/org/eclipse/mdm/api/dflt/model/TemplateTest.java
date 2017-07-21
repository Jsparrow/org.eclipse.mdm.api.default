/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.ContextComponent;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Test;
import org.eclipse.mdm.api.base.model.Value;

/**
 * Implementation of the template test entity type. A template test consists of
 * {@link TemplateTestStepUsage}s with unique names. It describes a template for
 * a test run and provides meta data to resolve hooks and data sources for
 * naming, property validation, resolution and other purposes. A template test
 * implements the {@link Versionable} interface and hence has a version and a
 * state. As long as {@link #isEditable()} returns {@code true} any part of that
 * template test is allowed to be modified. Once a template test is set to be
 * valid ({@link #isValid()} == {@code true}) it may be used to define a test
 * run therefore is no longer allowed to be modified in any way. If a valid
 * template test needs to be modified, then a deep copy with a unique name and
 * version combination has to be created (deep copy means new instances).
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see TemplateTestStepUsage
 * @see Versionable
 */
public final class TemplateTest extends BaseEntity implements Deletable, Describable, Versionable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * The 'NameHelperClassname' attribute name.
	 */
	public static final String ATTR_NAMEHELPER_CLASSNAME = "NameHelperClassname";

	/**
	 * The 'DataSourcePluginClassnames' attribute name.
	 */
	public static final String ATTR_DATASOURCE_PLUGIN_CLASSNAMES = "DataSourcePluginClassnames";

	/**
	 * The 'TestOrderActionClassnames' attribute name.
	 */
	public static final String ATTR_TEST_ORDER_ACTION_CLASSNAMES = "TestOrderActionClassnames";

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	TemplateTest(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Returns the name helper class name of this template test.
	 *
	 * @return The name helper class name is returned.
	 */
	public String getNameHelperClassname() {
		return getValue(ATTR_NAMEHELPER_CLASSNAME).extract();
	}

	/**
	 * Sets a new name helper class name for this template test.
	 *
	 * @param nameHelperClassname
	 *            The new name helper class name.
	 */
	public void setNameHelperClassname(String nameHelperClassname) {
		getValue(ATTR_NAMEHELPER_CLASSNAME).set(nameHelperClassname);
	}

	/**
	 * Returns the data source plug-in class names of this template test.
	 *
	 * @return The data source plug-in class names are returned.
	 */
	public String[] getDataSourcePluginClassnames() {
		return getValue(ATTR_DATASOURCE_PLUGIN_CLASSNAMES).extract();
	}

	/**
	 * Sets new data source plug-in class names for this template test.
	 *
	 * @param dataSourcePluginClassnames
	 *            The new data source plug-in class names.
	 */
	public void setDataSourceClassnames(String[] dataSourcePluginClassnames) {
		getValue(ATTR_DATASOURCE_PLUGIN_CLASSNAMES).set(dataSourcePluginClassnames);
	}

	/**
	 * Returns the test order action class names of this template test.
	 *
	 * @return The test order action class names are returned.
	 */
	public String[] getTestOrderActionClassnames() {
		return getValue(ATTR_TEST_ORDER_ACTION_CLASSNAMES).extract();
	}

	/**
	 * Sets new test order action class names for this template test.
	 *
	 * @param testOrderActionClassnames
	 *            The new test order action class names.
	 */
	public void setTestOrderActionClassnames(String[] testOrderActionClassnames) {
		getValue(ATTR_TEST_ORDER_ACTION_CLASSNAMES).set(testOrderActionClassnames);
	}

	/**
	 * Returns the {@link TemplateTestStepUsage} identified by given name.
	 *
	 * @param name
	 *            The name of the {@code TemplateTestStepUsage}.
	 * @return The {@code Optional} is empty if a {@code TemplateTestStepUsage}
	 *         with given name does not exist.
	 */
	public Optional<TemplateTestStepUsage> getTemplateTestStepUsage(String name) {
		return getTemplateTestStepUsages().stream().filter(ttsu -> ttsu.nameMatches(name)).findAny();
	}

	/**
	 * Returns all available {@link TemplateTestStepUsage}s related to this
	 * template test.
	 *
	 * @return The returned {@code List} is unmodifiable.
	 */
	public List<TemplateTestStepUsage> getTemplateTestStepUsages() {
		return getCore().getChildrenStore().get(TemplateTestStepUsage.class);
	}

	/**
	 * Removes the {@link TemplateTestStepUsage} identified by given name.
	 *
	 * @param name
	 *            Name of the {@code TemplateTestStepUsage} that has to be
	 *            removed.
	 * @return Returns {@code true} if the {@code TemplateTestStepUsage} with
	 *         given name has been removed.
	 */
	public boolean removeTemplateTestStepUsage(String name) {
		Optional<TemplateTestStepUsage> templateTestStepUsage = getTemplateTestStepUsage(name);
		if (templateTestStepUsage.isPresent()) {
			getCore().getChildrenStore().remove(templateTestStepUsage.get());
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
		sb.append(getValues().values().stream().map(Value::toString).collect(Collectors.joining(", ")));

		List<TemplateTestStepUsage> templateTestStepUsages = getTemplateTestStepUsages();
		if (!templateTestStepUsages.isEmpty()) {
			sb.append(", TemplateTestStepUsages = ").append(templateTestStepUsages);
		}

		return sb.append(')').toString();
	}

	/**
	 * Returns the {@link TemplateComponent} the given {@link ContextComponent}
	 * is derived from.
	 *
	 * @param test
	 *            The {@code ContextComponent} whose {@code TemplateComponent}
	 *            is requested.
	 * @return {@code Optional} is empty if the given {@code ContextComponent}
	 *         is not derived from a template, which is data source specific.
	 */
	public static Optional<TemplateTest> of(Test test) {
		return Optional.ofNullable(getCore(test).getMutableStore().get(TemplateTest.class));
	}

	// ======================================================================
	// Package methods
	// ======================================================================

	/**
	 * Checks whether given {@link TemplateTestStepUsage} is contained in this
	 * template test.
	 *
	 * @param templateTestStep
	 *            The {@code TemplateTestStepUsage}.
	 * @return Returns {@code true} if given {@code TemplateTestStepUsage} is
	 *         contained in this template test.
	 */
	boolean contains(TemplateTestStep templateTestStep) {
		return getTemplateTestStepUsages().stream().map(TemplateTestStepUsage::getTemplateTestStep)
				.filter(tts -> tts.getName().equals(templateTestStep.getName()))
				.filter(tts -> tts.getVersion().equals(templateTestStep.getVersion())).findFirst().isPresent();
	}

}
