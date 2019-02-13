/********************************************************************************
 * Copyright (c) 2015-2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 ********************************************************************************/

package org.eclipse.mdm.api.dflt.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.mdm.api.base.adapter.Core;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.ContextComponent;
import org.eclipse.mdm.api.base.model.ContextRoot;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Sortable;
import org.eclipse.mdm.api.base.model.TestStep;
import org.eclipse.mdm.api.base.model.Value;

/**
 * Implementation of the template component entity type. A template attribute
 * adds meta data to a {@link CatalogComponent} it is associated with. It always
 * belongs to a template root or another template component. Its name has to be
 * unique within all template components belonging to the same
 * {@link TemplateRoot}. A template component may define {@link TemplateSensor}s
 * where each extends a {@link CatalogSensor} provided by the associated {@code
 * CatalogComponent}. It may define {@link TemplateAttribute}s where each
 * uniquely extends a {@link CatalogAttribute} provided by the associated
 * {@code CatalogComponent}.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see CatalogComponent
 * @see TemplateRoot
 * @see TemplateAttribute
 * @see TemplateSensor
 */
public class TemplateComponent extends BaseEntity implements Deletable, Sortable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * The optional flag {@code Predicate}.
	 */
	public static final Predicate<TemplateComponent> IS_OPTIONAL = TemplateComponent::isOptional;

	/**
	 * The mandatory flag {@code Predicate}. This is the inversion of
	 * {@link #IS_OPTIONAL} {@code Predicate}.
	 */
	public static final Predicate<TemplateComponent> IS_MANDATORY = IS_OPTIONAL.negate();

	/**
	 * The default active flag {@code Predicate}.
	 */
	public static final Predicate<TemplateComponent> IS_DEFAULT_ACTIVE = TemplateComponent::isDefaultActive;

	/**
	 * The series constant flag {@code Predicate}.
	 */
	public static final Predicate<TemplateComponent> IS_SERIES_CONSTANT = TemplateComponent::isSeriesConstant;

	/**
	 * The series variable flag {@code Predicate}. This is the inversion of the
	 * {@link #IS_SERIES_CONSTANT} {@code Predicate}.
	 */
	public static final Predicate<TemplateComponent> IS_SERIES_VARIABLE = IS_SERIES_CONSTANT.negate();

	/**
	 * The implicit create flag {@code Predicate}. This is an OR combination of
	 * {@link #IS_DEFAULT_ACTIVE} and {@link #IS_MANDATORY} {@code Predicate}s.
	 */
	public static final Predicate<TemplateComponent> IS_IMPLICIT_CREATE = IS_DEFAULT_ACTIVE.or(IS_MANDATORY);

	/**
	 * The 'TestStepSeriesVariable' attribute name.
	 */
	public static final String ATTR_SERIES_CONSTANT = "TestStepSeriesVariable";

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
	TemplateComponent(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Returns the optional flag of this template component.
	 *
	 * @return Returns {@code true} if it is allowed to omit a
	 *         {@link ContextComponent} derived from this template component.
	 */
	public Boolean isOptional() {
		return getValue(ATTR_OPTIONAL).extract();
	}

	/**
	 * Sets a new optional flag for this template component.
	 *
	 * @param optional
	 *            The new optional flag.
	 */
	public void setOptional(Boolean optional) {
		getValue(ATTR_OPTIONAL).set(optional);
	}

	/**
	 * Returns the default active flag of this template component.
	 *
	 * @return Returns {@code true} if a {@link ContextComponent} has to be
	 *         created automatically each time a new {@link ContextRoot} is
	 *         derived from the {@link TemplateRoot} this template component
	 *         belongs to.
	 */
	public Boolean isDefaultActive() {
		return getValue(ATTR_DEFAULT_ACTIVE).extract();
	}

	/**
	 * Sets a new default active flag for this template component.
	 *
	 * @param defaultActive
	 *            The new default active flag.
	 */
	public void setDefaultActive(Boolean defaultActive) {
		getValue(ATTR_DEFAULT_ACTIVE).set(defaultActive);
	}

	/**
	 * Returns the series constant flag of this template component.
	 *
	 * @return Returns {@code true} if the {@link ContextComponent}'s
	 *         {@link Value}s across {@link TestStep} siblings contain the same
	 *         values.
	 */
	public Boolean isSeriesConstant() {
		return getValue(ATTR_SERIES_CONSTANT).extract();
	}

	/**
	 * Sets a new series constant flag for this template component.
	 *
	 * @param seriesConstant
	 *            The new series constant flag.
	 */
	public void setSeriesConstant(Boolean seriesConstant) {
		getValue(ATTR_SERIES_CONSTANT).set(seriesConstant);
	}

	/**
	 * Returns the {@link CatalogComponent} this template component is
	 * associated with.
	 *
	 * @return The associated {@code CatalogComponent} is returned.
	 */
	public CatalogComponent getCatalogComponent() {
		return getCore().getMutableStore().get(CatalogComponent.class);
	}

	/**
	 * Returns the {@link TemplateRoot} this template component belongs to.
	 *
	 * @return The {@code TemplateRoot} is returned.
	 */
	public TemplateRoot getTemplateRoot() {
		TemplateRoot templateRoot = getCore().getPermanentStore().get(TemplateRoot.class);
		if (templateRoot == null) {
			return getParentTemplateComponent()
					.orElseThrow(() -> new IllegalStateException("Parent entity is unknown.")).getTemplateRoot();
		}

		return templateRoot;
	}

	/**
	 * Returns the parent {@link TemplateComponent} of this template component.
	 *
	 * @return {@code Optional} is empty if this template component is an
	 *         immediate child of the {@link TemplateRoot}.
	 * @see #getTemplateRoot()
	 */
	public Optional<TemplateComponent> getParentTemplateComponent() {
		return Optional.ofNullable(getCore().getPermanentStore().get(TemplateComponent.class));
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
		return getTemplateAttributes().stream().filter(ta -> ta.nameEquals(name)).findAny();
	}

	/**
	 * Returns all available {@link TemplateAttribute}s related to this template
	 * component.
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
		if (!templateAttribute.isPresent()) {
			return false;
		}
		getCore().getChildrenStore().remove(templateAttribute.get());
		return true;
	}

	/**
	 * Returns the {@link TemplateComponent} identified by given name.
	 *
	 * <p>
	 * <b>NOTE:</b> The names of <u>all</u> template components belonging to the
	 * same {@link TemplateRoot} must have unique names (no matter they are
	 * immediate children or not). Therefore, if this template component does
	 * not have an immediate template component with the given name, this lookup
	 * request is recursively delegated to all of its child template components.
	 *
	 * @param name
	 *            The name of the {@code TemplateComponent}.
	 * @return The {@code Optional} is empty if a {@code TemplateComponent} with
	 *         given name does not exist (neither this template component nor
	 *         one of its children has a template component with given name).
	 */
	public Optional<TemplateComponent> getTemplateComponent(String name) {
		List<TemplateComponent> templateComponents = getTemplateComponents();
		Optional<TemplateComponent> templateComponent = templateComponents.stream().filter(tc -> tc.nameEquals(name))
				.findAny();
		if (templateComponent.isPresent()) {
			return templateComponent;
		}

		return templateComponents.stream().map(ct -> ct.getTemplateComponent(name)).filter(Optional::isPresent)
				.map(Optional::get).findAny();
	}

	/**
	 * Returns all immediate {@link TemplateComponent}s related to this template
	 * component.
	 *
	 * @return The returned {@code List} is unmodifiable.
	 */
	public List<TemplateComponent> getTemplateComponents() {
		return getCore().getChildrenStore().get(TemplateComponent.class);
	}

	/**
	 * Removes the {@link TemplateComponent} identified by given name.
	 *
	 * <p>
	 * <b>NOTE:</b> The names of <u>all</u> template components belonging to the
	 * same {@link TemplateRoot} must have unique names (no matter they are
	 * immediate children or not). Therefore, if this template component does
	 * not have an immediate template component with the given name, this remove
	 * request is recursively delegated to all of its child template components.
	 *
	 * @param name
	 *            Name of the {@code TemplateComponent} that has to be removed.
	 * @return Returns {@code true} if the {@code TemplateComponent} with given
	 *         name has been removed.
	 */
	public boolean removeTemplateComponent(String name) {
		Optional<TemplateComponent> templateComponent = getTemplateComponent(name);
		if (!templateComponent.isPresent()) {
			return false;
		}
		Optional<TemplateComponent> parentTemplateComponent = templateComponent.get().getParentTemplateComponent();
		if (parentTemplateComponent.isPresent()) {
			parentTemplateComponent.get().removeTemplateComponent(name);
		} else {
			getCore().getChildrenStore().remove(templateComponent.get());
		}
		return true;
	}

	/**
	 * Returns the {@link TemplateSensor} identified by given name.
	 *
	 * @param name
	 *            The name of the {@code TemplateSensor}.
	 * @return The {@code Optional} is empty if a {@code TemplateSensor} with
	 *         given name does not exist.
	 */
	public Optional<TemplateSensor> getTemplateSensor(String name) {
		return getTemplateSensors().stream().filter(ts -> ts.nameEquals(name)).findAny();
	}

	/**
	 * Returns all available {@link TemplateSensor}s related to this template
	 * component.
	 *
	 * @return The returned {@code List} is unmodifiable.
	 */
	public List<TemplateSensor> getTemplateSensors() {
		if (getCatalogComponent().getContextType().isTestEquipment()) {
			return getCore().getChildrenStore().get(TemplateSensor.class);
		}

		return Collections.emptyList();
	}

	/**
	 * Removes the {@link TemplateSensor} identified by given name.
	 *
	 * @param name
	 *            Name of the {@code TemplateSensor} that has to be removed.
	 * @return Returns {@code true} if the {@code TemplateSensor} with given
	 *         name has been removed.
	 */
	public boolean removeTemplateSensor(String name) {
		Optional<TemplateSensor> templateSensor = getTemplateSensor(name);
		if (!templateSensor.isPresent()) {
			return false;
		}
		getCore().getChildrenStore().remove(templateSensor.get());
		return true;
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

		List<TemplateSensor> templateSensors = getTemplateSensors();
		if (!templateSensors.isEmpty()) {
			sb.append(", TemplateSensors = ").append(templateSensors);
		}

		List<TemplateComponent> templateComponents = getTemplateComponents();
		if (!templateComponents.isEmpty()) {
			sb.append(", TemplateComponents = ").append(templateComponents);
		}

		return sb.append(')').toString();
	}

	/**
	 * Returns the {@link TemplateComponent} the given {@link ContextComponent}
	 * is derived from.
	 *
	 * @param contextComponent
	 *            The {@code ContextComponent} whose {@code
	 * 		TemplateComponent} is requested.
	 * @return {@code Optional} is empty if the given {@code ContextComponent}
	 *         is not derived from a template, which is data source specific.
	 */
	public static Optional<TemplateComponent> of(ContextComponent contextComponent) {
		return Optional.ofNullable(getCore(contextComponent).getMutableStore().get(TemplateComponent.class));
	}

}
