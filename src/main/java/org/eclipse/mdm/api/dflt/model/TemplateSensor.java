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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.mdm.api.base.model.AxisType;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.ContextSensor;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Quantity;
import org.eclipse.mdm.api.base.model.Sortable;
import org.eclipse.mdm.api.base.model.Value;

public final class TemplateSensor extends BaseEntity implements Deletable, Describable, Sortable {

	// ======================================================================
	// Class variables
	// ======================================================================

	public static final String ATTR_OPTIONAL = "Optional";
	public static final String ATTR_DEFAULT_ACTIVE = "DefaultActive";

	public static final String ATTR_MEASRED_VALUES_EDITABLE = "MeaQuantityValuesEditable";

	public static final String ATTR_MEASRED_VALUES_GENERATOR_NAME = "MeaQuantityEditorPlugin";

	public static final String ATTR_MEASRED_VALUES_INDEPENDENT = "MeaQuantityIndependent";

	public static final String ATTR_MEASRED_VALUES_AXISTYPE = "MeaQuantityAxisType";

	public static final Predicate<TemplateSensor> IS_OPTIONAL = TemplateSensor::isOptional;
	public static final Predicate<TemplateSensor> IS_MANDATORY = IS_OPTIONAL.negate();
	public static final Predicate<TemplateSensor> IS_DEFAULT_ACTIVE = TemplateSensor::isDefaultActive;
	public static final Predicate<TemplateSensor> IS_IMPLICIT_CREATE = IS_DEFAULT_ACTIVE.or(IS_MANDATORY);

	// ======================================================================
	// Constructors
	// ======================================================================

	TemplateSensor(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	public Boolean areMeasuredValuesEditable() {
		return getValue(ATTR_MEASRED_VALUES_EDITABLE).extract();
	}

	public void setMeasuredValuesEditable(Boolean measuredValuesEditable) {
		getValue(ATTR_MEASRED_VALUES_EDITABLE).set(measuredValuesEditable);
	}

	public String getMeasuredValuesGeneratorName() {
		return getValue(ATTR_MEASRED_VALUES_GENERATOR_NAME).extract();
	}

	public void setMeasuredValuesGeneratorName(String measuredValuesGeneratorName) {
		getValue(ATTR_MEASRED_VALUES_GENERATOR_NAME).set(measuredValuesGeneratorName);
	}

	public Boolean areMeasuredValuesIndependent() {
		return getValue(ATTR_MEASRED_VALUES_INDEPENDENT).extract();
	}

	public void setMeasuredValuesIndependent(Boolean measuredValuesIndependent) {
		getValue(ATTR_MEASRED_VALUES_INDEPENDENT).set(measuredValuesIndependent);
	}

	public AxisType getMeasuredValuesAxisType() {
		return getValue(ATTR_MEASRED_VALUES_AXISTYPE).extract();
	}

	public void setMeasuredValuesAxisType(AxisType axisType) {
		getValue(ATTR_MEASRED_VALUES_AXISTYPE).set(axisType);
	}

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

	public CatalogSensor getCatalogSensor() {
		return getCore().getMutableStore().get(CatalogSensor.class);
	}

	public Quantity getQuantity() {
		return getCore().getMutableStore().get(Quantity.class);
	}

	public TemplateRoot getTemplateRoot() {
		return getTemplateComponent().getTemplateRoot();
	}

	public TemplateComponent getTemplateComponent() {
		return getCore().getPermanentStore().get(TemplateComponent.class);
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

		return sb.append(')').toString();
	}

	public static Optional<TemplateSensor> of(ContextSensor contextSensor) {
		return Optional.ofNullable(getCore(contextSensor).getMutableStore().get(TemplateSensor.class));
	}

}
