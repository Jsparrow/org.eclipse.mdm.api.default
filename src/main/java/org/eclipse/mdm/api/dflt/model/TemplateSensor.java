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
import org.eclipse.mdm.api.base.model.ContextComponent;
import org.eclipse.mdm.api.base.model.ContextSensor;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Quantity;
import org.eclipse.mdm.api.base.model.Sortable;
import org.eclipse.mdm.api.base.model.Value;

/**
 * Implementation of the template sensor entity type. A template sensor adds
 * meta data to a {@link CatalogSensor} it is associated with. It always belongs
 * to a template component or a template sensor. Its name has to be unique
 * within all template sensors belonging to the same {@link TemplateComponent}.
 * A template sensor define {@link TemplateAttribute}s where each uniquely
 * extends a {@link CatalogAttribute} provided by the associated {@code
 * CatalogSensor}.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see CatalogSensor
 * @see TemplateComponent
 * @see TemplateAttribute
 */
public final class TemplateSensor extends BaseEntity implements Deletable, Describable, Sortable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * The optional flag {@code Predicate}.
	 */
	public static final Predicate<TemplateSensor> IS_OPTIONAL = TemplateSensor::isOptional;

	/**
	 * The mandatory flag {@code Predicate}. This is the inversion of
	 * {@link #IS_OPTIONAL} {@code Predicate}.
	 */
	public static final Predicate<TemplateSensor> IS_MANDATORY = IS_OPTIONAL.negate();

	/**
	 * The default active flag {@code Predicate}.
	 */
	public static final Predicate<TemplateSensor> IS_DEFAULT_ACTIVE = TemplateSensor::isDefaultActive;

	/**
	 * The implicit create flag {@code Predicate}. This is an OR combination of
	 * {@link #IS_DEFAULT_ACTIVE} and {@link #IS_MANDATORY} {@code Predicate}s.
	 */
	public static final Predicate<TemplateSensor> IS_IMPLICIT_CREATE = IS_DEFAULT_ACTIVE.or(IS_MANDATORY);

	/**
	 * The 'Optional' attribute name.
	 */
	public static final String ATTR_OPTIONAL = "Optional";

	/**
	 * The 'DefaultActive' attribute name.
	 */
	public static final String ATTR_DEFAULT_ACTIVE = "DefaultActive";

	/**
	 * The 'MeasuredValuesEditable' attribute name.
	 */
	public static final String ATTR_MEASRED_VALUES_EDITABLE = "MeaQuantityValuesEditable";

	/**
	 * The 'MeasuredValuesGeneratorName' attribute name.
	 */
	public static final String ATTR_MEASRED_VALUES_GENERATOR_NAME = "MeaQuantityEditorPlugin";

	/**
	 * The 'MeasuredValuesIndependent' attribute name.
	 */
	public static final String ATTR_MEASRED_VALUES_INDEPENDENT = "MeaQuantityIndependent";

	/**
	 * The 'MeasuredValuesAxisType' attribute name.
	 */
	public static final String ATTR_MEASRED_VALUES_AXISTYPE = "MeaQuantityAxisType";

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	TemplateSensor(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Returns the measured values editable flag of this template sensor.
	 *
	 * @return The measured values editable flag is returned.
	 */
	public Boolean areMeasuredValuesEditable() {
		return getValue(ATTR_MEASRED_VALUES_EDITABLE).extract();
	}

	/**
	 * Sets a new measured values editable flag for this template sensor.
	 *
	 * @param measuredValuesEditable
	 *            The measured values editable flag.
	 */
	public void setMeasuredValuesEditable(Boolean measuredValuesEditable) {
		getValue(ATTR_MEASRED_VALUES_EDITABLE).set(measuredValuesEditable);
	}

	/**
	 * Returns the measured values generator name of this template sensor.
	 *
	 * @return The measured values generator name is returned.
	 */
	public String getMeasuredValuesGeneratorName() {
		return getValue(ATTR_MEASRED_VALUES_GENERATOR_NAME).extract();
	}

	/**
	 * Sets a new measured values generator name for this template sensor.
	 *
	 * @param measuredValuesGeneratorName
	 *            The measured values generator name.
	 */
	public void setMeasuredValuesGeneratorName(String measuredValuesGeneratorName) {
		getValue(ATTR_MEASRED_VALUES_GENERATOR_NAME).set(measuredValuesGeneratorName);
	}

	/**
	 * Returns the measured values independent flag of this template sensor.
	 *
	 * @return The measured values independent flag is returned.
	 */
	public Boolean areMeasuredValuesIndependent() {
		return getValue(ATTR_MEASRED_VALUES_INDEPENDENT).extract();
	}

	/**
	 * Sets a new measured values independent flag for this template sensor.
	 *
	 * @param measuredValuesIndependent
	 *            The measured values independent flag.
	 */
	public void setMeasuredValuesIndependent(Boolean measuredValuesIndependent) {
		getValue(ATTR_MEASRED_VALUES_INDEPENDENT).set(measuredValuesIndependent);
	}

	/**
	 * Returns the measured values {@link AxisType} of this template sensor.
	 *
	 * @return The measured values {@code AxisType} is returned.
	 */
	public AxisType getMeasuredValuesAxisType() {
		return getValue(ATTR_MEASRED_VALUES_AXISTYPE).extract();
	}

	/**
	 * Sets a new measured values {@link AxisType} for this template sensor.
	 *
	 * @param axisType
	 *            The measured values {@code AxisType}.
	 */
	public void setMeasuredValuesAxisType(AxisType axisType) {
		getValue(ATTR_MEASRED_VALUES_AXISTYPE).set(axisType);
	}

	/**
	 * Returns the optional flag of this template sensor.
	 *
	 * @return Returns {@code true} if it is allowed to omit a
	 *         {@link ContextSensor} derived from this template sensor.
	 */
	public Boolean isOptional() {
		return getValue(ATTR_OPTIONAL).extract();
	}

	/**
	 * Sets a new optional flag for this template sensor.
	 *
	 * @param optional
	 *            The new optional flag.
	 */
	public void setOptional(Boolean optional) {
		getValue(ATTR_OPTIONAL).set(optional);
	}

	/**
	 * Returns the default active flag of this template sensor.
	 *
	 * @return Returns {@code true} if a {@link ContextSensor} has to be created
	 *         automatically each time a new {@link ContextComponent} is derived
	 *         from the {@link TemplateComponent} this template sensor belongs
	 *         to.
	 */
	public Boolean isDefaultActive() {
		return getValue(ATTR_DEFAULT_ACTIVE).extract();
	}

	/**
	 * Sets a new default active flag for this template sensor.
	 *
	 * @param defaultActive
	 *            The new default active flag.
	 */
	public void setDefaultActive(Boolean defaultActive) {
		getValue(ATTR_DEFAULT_ACTIVE).set(defaultActive);
	}

	/**
	 * Returns the {@link CatalogSensor} this template sensor is associated
	 * with.
	 *
	 * @return The associated {@code CatalogSensor} is returned.
	 */
	public CatalogSensor getCatalogSensor() {
		return getCore().getMutableStore().get(CatalogSensor.class);
	}

	/**
	 * Returns the {@link Quantity} measured data are associated with.
	 *
	 * @return The {@code Quantity} is returned.
	 */
	public Quantity getQuantity() {
		return getCore().getMutableStore().get(Quantity.class);
	}

	/**
	 * Returns the {@link TemplateRoot} this template sensor belongs to.
	 *
	 * @return The {@code TemplateRoot} is returned.
	 */
	public TemplateRoot getTemplateRoot() {
		return getTemplateComponent().getTemplateRoot();
	}

	/**
	 * Returns the parent {@link TemplateComponent} of this template sensor.
	 *
	 * @return The parent {@code TemplateComponent} is returned.
	 */
	public TemplateComponent getTemplateComponent() {
		return getCore().getPermanentStore().get(TemplateComponent.class);
	}

	/**
	 * Returns the {@link TemplateAttribute} identified by given name.
	 *
	 * @param name
	 *            The name of the {@code TemplateAttribute}.
	 * @return The {@code Optional} is empty if a {@code TemplateAttribute} with
	 *         given name does not exist.
	 */
	public Optional<TemplateAttribute> getTemplateAttribute(String name) {
		return getTemplateAttributes().stream().filter(ta -> ta.nameMatches(name)).findAny();
	}

	/**
	 * Returns all available {@link TemplateAttribute}s related to this template
	 * sensor.
	 *
	 * @return The returned {@code List} is unmodifiable.
	 */
	public List<TemplateAttribute> getTemplateAttributes() {
		return getCore().getChildrenStore().get(TemplateAttribute.class);
	}

	/**
	 * Removes the {@link TemplateAttribute} identified by given name.
	 *
	 * @param name
	 *            Name of the {@code TemplateAttribute} that has to be removed.
	 * @return Returns {@code true} if the {@code TemplateAttribute} with given
	 *         name has been removed.
	 */
	public boolean removeTemplateAttribute(String name) {
		Optional<TemplateAttribute> templateAttribute = getTemplateAttribute(name);
		if (templateAttribute.isPresent()) {
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
		if (!templateAttributes.isEmpty()) {
			sb.append(", TemplateAttributes = ").append(templateAttributes);
		}

		return sb.append(')').toString();
	}

	/**
	 * Returns the {@link TemplateSensor} the given {@link ContextSensor} is
	 * derived from.
	 *
	 * @param contextSensor
	 *            The {@code ContextSensor} whose {@code
	 * 		TemplateSensor} is requested.
	 * @return {@code Optional} is empty if the given {@code ContextSensor} is
	 *         not derived from a template, which is data source specific.
	 */
	public static Optional<TemplateSensor> of(ContextSensor contextSensor) {
		return Optional.ofNullable(getCore(contextSensor).getMutableStore().get(TemplateSensor.class));
	}

}
