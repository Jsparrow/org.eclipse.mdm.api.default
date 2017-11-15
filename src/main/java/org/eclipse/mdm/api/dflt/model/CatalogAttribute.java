/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.EnumRegistry;
import org.eclipse.mdm.api.base.model.EnumerationValue;
import org.eclipse.mdm.api.base.model.Enumeration;
import org.eclipse.mdm.api.base.model.ScalarType;
import org.eclipse.mdm.api.base.model.Sortable;
import org.eclipse.mdm.api.base.model.Unit;
import org.eclipse.mdm.api.base.model.Value;
import org.eclipse.mdm.api.base.model.ValueType;

/**
 * Implementation of the catalog attribute entity type. A catalog attribute is a
 * generic attribute description to store context data ("as measured", "as
 * ordered"). It always belongs to a {@link CatalogComponent} or a
 * {@link CatalogSensor}. Its name has to be unique within the parent and may
 * not be changed, once written.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see CatalogComponent
 * @see CatalogSensor
 */
public class CatalogAttribute extends BaseEntity implements Deletable, Describable, Sortable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * The 'ValueListReference' attribute name.
	 */
	public static final String ATTR_VALUE_LIST_REFERENCE = "ValueListRef";

	/**
	 * The 'ValueCopyable' attribute name.
	 */
	public static final String ATTR_VALUE_COPYABLE = "ValueCopyable";

	/**
	 * The 'ActionRequestClassname' attribute name.
	 */
	public static final String ATTR_ACTION_REQUEST_CLASSNAME = "ActionRequestClassname";

	/**
	 * The <u>virtual</u> '{@literal @}EnumerationName' attribute name.
	 */
	public static final String VATTR_ENUMERATION_NAME = "@EnumerationName";

	/**
	 * The <u>virtual</u> '{@literal @}ScalarType' attribute name.
	 */
	public static final String VATTR_SCALAR_TYPE = "@ScalarType";

	/**
	 * The <u>virtual</u> '{@literal @}Sequence' attribute name.
	 */
	public static final String VATTR_SEQUENCE = "@Sequence";

	// ======================================================================
	// Instance variables
	// ======================================================================

	private Enumeration<?> enumerationObj;
	private final Value scalarTypeValue;
	private final Value sequenceValue;

	private Unit unit;

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	CatalogAttribute(Core core) {
		super(core);

		Map<String, Value> values = core.getValues();
		EnumRegistry er = EnumRegistry.getInstance();
		Value enumValue = values.remove(VATTR_ENUMERATION_NAME);
		if (enumValue != null) {
			enumerationObj = er.get(enumValue.extract(ValueType.STRING));
		}
		scalarTypeValue = values.remove(VATTR_SCALAR_TYPE);
		sequenceValue = values.remove(VATTR_SEQUENCE);

		unit = core.getMutableStore().get(Unit.class);
		core.getMutableStore().remove(Unit.class);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Checks whether it is allowed to store other data than those provided via
	 * related {@link ValueList}.
	 *
	 * @return Returns {@code false} if it is allowed to store other values then
	 *         those provided.
	 */
	public Boolean isValueListReference() {
		boolean valueListReference = getValue(ATTR_VALUE_LIST_REFERENCE).extract();
		return valueListReference && getValueList().isPresent();
	}

	/**
	 * Sets a new value reference flag for this catalog attribute.
	 *
	 * @param valueListReference
	 *            The new value list reference flag.
	 */
	public void setValueListReference(Boolean valueListReference) {
		getValue(ATTR_VALUE_LIST_REFERENCE).set(valueListReference);
	}

	/**
	 * Checks whether it is allowed to copy the contained value in case of an
	 * associated descriptive component is copied.
	 *
	 * @return Returns {@code true} if it allowed to copy a contained value.
	 */
	public Boolean isValueCopyable() {
		return getValue(ATTR_VALUE_COPYABLE).extract();
	}

	/**
	 * Sets a new value copyable flag for this catalog attribute.
	 *
	 * @param valueCopyable
	 *            The new value copyable flag.
	 */
	public void setValueCopyable(Boolean valueCopyable) {
		getValue(ATTR_VALUE_COPYABLE).set(valueCopyable);
	}

	/**
	 * Returns the action request class name of this catalog attribute.
	 *
	 * @return The action request class name is returned.
	 */
	public String getActionRequestClassname() {
		return getValue(ATTR_ACTION_REQUEST_CLASSNAME).extract();
	}

	/**
	 * Sets a new action request class name for this attribute.
	 *
	 * @param actionRequestClassname
	 *            The new action request class name.
	 */
	public void setActionRequestClassname(String actionRequestClassname) {
		getValue(ATTR_ACTION_REQUEST_CLASSNAME).set(actionRequestClassname);
	}

	/**
	 * Returns the related {@link ValueList}.
	 *
	 * @return {@code Optional} is empty if no {@code ValueList} is related.
	 */
	public Optional<ValueList> getValueList() {
		return Optional.ofNullable(getCore().getMutableStore().get(ValueList.class));
	}

	/**
	 * Replaces current {@link ValueList} relation with the given one.
	 *
	 * @param valueList
	 *            The new {@code ValueList} may be null.
	 */
	public void setValueList(ValueList valueList) {
		if (valueList == null) {
			getCore().getMutableStore().remove(ValueList.class);
			setValueListReference(Boolean.FALSE);
		} else {
			getCore().getMutableStore().set(valueList);
		}
	}

	/**
	 * Returns the parent {@link CatalogComponent}.
	 *
	 * @return {@code Optional} is empty if a {@link CatalogSensor} is parent of
	 *         this catalog attribute.
	 * @see #getCatalogSensor()
	 */
	public Optional<CatalogComponent> getCatalogComponent() {
		return Optional.ofNullable(getCore().getPermanentStore().get(CatalogComponent.class));
	}

	/**
	 * Returns the parent {@link CatalogSensor}.
	 *
	 * @return {@code Optional} is empty if a {@link CatalogComponent} is parent
	 *         of this catalog attribute.
	 * @see #getCatalogComponent()
	 */
	public Optional<CatalogSensor> getCatalogSensor() {
		return Optional.ofNullable(getCore().getPermanentStore().get(CatalogSensor.class));
	}

	/**
	 * Returns the virtual and unmodifiable {@link ValueType} of this catalog
	 * attribute.
	 *
	 * @return The {@code ValueType} is returned.
	 */
	public ValueType<?> getValueType() {
		ScalarType scalarType = scalarTypeValue.extract();
		Boolean sequence = sequenceValue.extract();
		return sequence.booleanValue() ? scalarType.toValueType() : scalarType.toSingleValueType();
	}

	/**
	 * Returns the virtual and unmodifiable enumeration class of this catalog
	 * attribute.
	 *
	 * @return The enumeration class is returned.
	 * @throws IllegalStateException
	 *             Thrown if this catalog attribute's {@link ValueType} returns
	 *             {@code true} when {@link ValueType#isEnumerationType()} is
	 *             called.
	 */
	@SuppressWarnings("rawtypes")
	public Enumeration getEnumerationObject() {
		if (!getValueType().isEnumerationType()) {
			throw new IllegalStateException("Catalog attribute is not of type enumeration.");
		}
		return enumerationObj;
	}

	/**
	 * Returns the {@link Unit} of this catalog attribute.
	 *
	 * @return {@code Optional} is empty if no unit is defined.
	 */
	public Optional<Unit> getUnit() {
		return Optional.ofNullable(unit);
	}

	/**
	 * Sets a new {@link Unit} for this catalog attribute.
	 *
	 * @param unit
	 *            The new {@code Unit} may be null.
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('(');

		ScalarType scalarType = scalarTypeValue.extract();
		sb.append("ScalarType = ").append(scalarType);
		if (scalarType.isEnumeration()) {
			sb.append(", EnumerationObject = ").append(getEnumerationObject());
		}

		sb.append(", Sequence = ").append((boolean) sequenceValue.extract());

		Optional<Unit> catalogUnit = getUnit();
		if (catalogUnit.isPresent()) {
			sb.append(", Unit = ").append(catalogUnit.get());
		}

		sb.append(", ").append(getValues().values().stream().map(Value::toString).collect(Collectors.joining(", ")));

		return sb.append(')').toString();
	}

	// ======================================================================
	// Package methods
	// ======================================================================

	/**
	 * Sets {@link ValueType} of this catalog attribute.
	 *
	 * @param valueType
	 *            The {@link ValueType}.
	 */
	void setValueType(ValueType<?> valueType) {
		Enumeration<?> scalarTypeEnum = EnumRegistry.getInstance().get(EnumRegistry.SCALAR_TYPE);
		scalarTypeValue.set(scalarTypeEnum.valueOf(valueType.toSingleType().name()));
		sequenceValue.set(valueType.isSequence());
	}

	/**
	 * Sets enumeration Object of this catalog attribute.
	 *
	 * @param enumerationObj
	 *            The enumeration.
	 */
	void setEnumerationObj(Enumeration<?> enumerationObj) {
		setValueType(ValueType.ENUMERATION);
		this.enumerationObj=enumerationObj;
	}

}
