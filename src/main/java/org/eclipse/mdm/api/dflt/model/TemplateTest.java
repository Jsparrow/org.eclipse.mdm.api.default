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
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Test;
import org.eclipse.mdm.api.base.model.Value;

public final class TemplateTest extends BaseEntity implements Deletable, Describable, Versionable {

	// ======================================================================
	// Class variables
	// ======================================================================

	//	public static final String ATTR_NAMEHELPER_CLASSNAME = "NameHelperClassname";
	//	public static final String ATTR_DATASOURCE_PLUGIN_CLASSNAMES = "DataSourcePluginClassnames";
	//	public static final String ATTR_TESTORDER_ACTION_CLASSNAMES = "TestOrderActionClassnames";

	// ======================================================================
	// Constructors
	// ======================================================================

	TemplateTest(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	//	public String getNameHelperClassname() {
	//		return getValue(ATTR_NAMEHELPER_CLASSNAME).extract();
	//	}
	//
	//	public void setNameHelperClassname(String nameHelperClassname) {
	//		getValue(ATTR_NAMEHELPER_CLASSNAME).set(nameHelperClassname);
	//	}
	//
	//	public String[] getDataSourcePluginClassnames() {
	//		return getValue(ATTR_DATASOURCE_PLUGIN_CLASSNAMES).extract();
	//	}
	//
	//	public void setDataSourceClassnames(String[] dataSourcePluginClassnames) {
	//		getValue(ATTR_DATASOURCE_PLUGIN_CLASSNAMES).set(dataSourcePluginClassnames);
	//	}
	//
	//	public String[] getTestOrderActionClassnames() {
	//		return getValue(ATTR_TESTORDER_ACTION_CLASSNAMES).extract();
	//	}
	//
	//	public void setTestOrderActionClassnames(String[] testOrderActionClassnames) {
	//		getValue(ATTR_TESTORDER_ACTION_CLASSNAMES).set(testOrderActionClassnames);
	//	}

	public Optional<TemplateTestStepUsage> getTemplateTestStepUsage(String name) {
		return getTemplateTestStepUsages().stream().filter(ttsu -> ttsu.nameMatches(name)).findAny();
	}

	public List<TemplateTestStepUsage> getTemplateTestStepUsages() {
		return getCore().getChildrenStore().get(TemplateTestStepUsage.class);
	}

	public boolean removeTemplateTestStepUsage(String name) {
		Optional<TemplateTestStepUsage> templateTestStepUsage = getTemplateTestStepUsage(name);
		if(templateTestStepUsage.isPresent()) {
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
		if(!templateTestStepUsages.isEmpty()) {
			sb.append(", TemplateTestStepUsages = ").append(templateTestStepUsages);
		}

		return sb.append(')').toString();
	}

	public static Optional<TemplateTest> of(Test test) {
		return Optional.ofNullable(getCore(test).getMutableStore().get(TemplateTest.class));
	}

	boolean contains(TemplateTestStep templateTestStep) {
		return getTemplateTestStepUsages().stream().map(TemplateTestStepUsage::getTemplateTestStep)
				.filter(tts -> tts.getName().equals(templateTestStep.getName()))
				.filter(tts -> tts.getVersion().equals(templateTestStep.getVersion()))
				.findFirst().isPresent();
	}

}
