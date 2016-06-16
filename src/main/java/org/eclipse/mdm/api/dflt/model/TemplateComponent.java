/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.ContextComponent;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Sortable;
import org.eclipse.mdm.api.base.model.Value;

public final class TemplateComponent extends BaseEntity implements Deletable, Sortable {

	// ======================================================================
	// Class variables
	// ======================================================================

	public static final String ATTR_SERIES_CONSTANT = "TestStepSeriesVariable";
	public static final String ATTR_DEFAULT_ACTIVE = "DefaultActive";
	public static final String ATTR_OPTIONAL = "Optional";

	// ======================================================================
	// Constructors
	// ======================================================================

	TemplateComponent(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	public Boolean isOptional() {
		return getValue(ATTR_OPTIONAL).extract();
	}

	public void setOptional(Boolean optional) {
		getValue(ATTR_OPTIONAL).set(optional);
	}

	public Boolean isDefaultActive() {
		return getValue(ATTR_DEFAULT_ACTIVE).extract();
	}

	public void setDefaultActive(Boolean defaultActive) {
		getValue(ATTR_DEFAULT_ACTIVE).set(defaultActive);
	}

	public Boolean isSeriesConstant() {
		return getValue(ATTR_SERIES_CONSTANT).extract();
	}

	public void setSeriesConstant(Boolean seriesConstant) {
		getValue(ATTR_SERIES_CONSTANT).set(seriesConstant);
	}

	public CatalogComponent getCatalogComponent() {
		return getCore().getMutableStore().get(CatalogComponent.class);
	}

	public TemplateRoot getTemplateRoot() {
		TemplateRoot templateRoot = getCore().getPermanentStore().get(TemplateRoot.class);
		if(templateRoot == null) {
			return getParentTemplateComponent().orElseThrow(() -> new IllegalStateException("Parent entity is unknown.")).getTemplateRoot();
		}

		return templateRoot;
	}

	public Optional<TemplateComponent> getParentTemplateComponent() {
		return Optional.ofNullable(getCore().getPermanentStore().get(TemplateComponent.class));
	}

	public Optional<TemplateAttribute> getTemplateAttribute(String name) {
		return getTemplateAttributes().stream().filter(ta -> ta.nameMatches(name)).findAny();
	}

	public List<TemplateAttribute> getTemplateAttributes() {
		return getCore().getChildrenStore().get(TemplateAttribute.class);
	}

	public boolean removeTemplateAttribute(String name) {
		Optional<TemplateAttribute> templateAttribute = getTemplateAttribute(name);
		if(templateAttribute.isPresent()) {
			getCore().getChildrenStore().remove(templateAttribute.get());
			return true;
		}

		return false;
	}

	// TODO java doc lookup is recursive in all template components children
	public Optional<TemplateComponent> getTemplateComponent(String name) {
		List<TemplateComponent> templateComponents = getTemplateComponents();
		Optional<TemplateComponent> templateComponent = templateComponents.stream().filter(tc -> tc.nameMatches(name)).findAny();
		if(templateComponent.isPresent()) {
			return templateComponent;
		}

		return templateComponents.stream().map(ct -> ct.getTemplateComponent(name)).filter(Optional::isPresent).map(Optional::get).findAny();
	}

	public List<TemplateComponent> getTemplateComponents() {
		return getCore().getChildrenStore().get(TemplateComponent.class);
	}

	public boolean removeTemplateComponent(String name) {
		Optional<TemplateComponent> templateComponent = getTemplateComponent(name);
		if(templateComponent.isPresent()) {
			getCore().getChildrenStore().remove(templateComponent.get());
			return true;
		}

		return false;
	}

	public Optional<TemplateSensor> getTemplateSensor(String name) {
		return getTemplateSensors().stream().filter(ts -> ts.nameMatches(name)).findAny();
	}

	public List<TemplateSensor> getTemplateSensors() {
		if(getCatalogComponent().getContextType().isTestEquipment()) {
			return getCore().getChildrenStore().get(TemplateSensor.class);
		}

		return Collections.emptyList();
	}

	public boolean removeTemplateSensor(String name) {
		Optional<TemplateSensor> templateSensor = getTemplateSensor(name);
		if(templateSensor.isPresent()) {
			getCore().getChildrenStore().remove(templateSensor.get());
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

		List<TemplateAttribute> templateAttributes = getTemplateAttributes();
		if(!templateAttributes.isEmpty()) {
			sb.append(", TemplateAttributes = ").append(templateAttributes);
		}

		List<TemplateSensor> templateSensors = getTemplateSensors();
		if(!templateSensors.isEmpty()) {
			sb.append(", TemplateSensors = ").append(templateSensors);
		}

		List<TemplateComponent> templateComponents = getTemplateComponents();
		if(!templateComponents.isEmpty()) {
			sb.append(", TemplateComponents = ").append(templateComponents);
		}

		return sb.append(')').toString();
	}

	public static TemplateComponent of(ContextComponent contextComponent) {
		return getCore(contextComponent).getMutableStore().get(TemplateComponent.class);
	}

}
