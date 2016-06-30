/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.mdm.api.base.model.AxisType;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Describable;
import org.eclipse.mdm.api.base.model.Interpolation;
import org.eclipse.mdm.api.base.model.ScalarType;
import org.eclipse.mdm.api.base.model.SequenceRepresentation;
import org.eclipse.mdm.api.base.model.Sortable;
import org.eclipse.mdm.api.base.model.TypeSpecification;
import org.eclipse.mdm.api.base.model.Unit;
import org.eclipse.mdm.api.base.model.Value;
import org.eclipse.mdm.api.base.model.VersionState;

public final class CatalogAttribute extends BaseEntity implements Deletable, Describable, Sortable {

	// ======================================================================
	// Class variables
	// ======================================================================

	public static final String ATTR_VALUE_LIST_REFERENCE = "ValueListRef";
	public static final String ATTR_VALUE_COPYABLE = "ValueCopyable";
	public static final String ATTR_ACTION_REQUEST_CLASSNAME = "ActionRequestClassname";

	public static final String VATTR_ENUMERATION_CLASS = "@EnumerationClass";
	public static final String VATTR_SCALAR_TYPE = "@ScalarType";
	public static final String VATTR_SEQUENCE = "@Sequence";

	// ======================================================================
	// Instance variables
	// ======================================================================

	private final Value enumerationClassValue;
	private final Value scalarTypeValue;
	private final Value sequenceValue;

	private Unit unit;

	// ======================================================================
	// Constructors
	// ======================================================================

	CatalogAttribute(Core core) {
		super(core);

		Map<String, Value> values = core.getValues();
		enumerationClassValue = values.remove(VATTR_ENUMERATION_CLASS);
		scalarTypeValue = values.remove(VATTR_SCALAR_TYPE);
		sequenceValue = values.remove(VATTR_SEQUENCE);

		unit = core.getMutableStore().get(Unit.class);
		core.getMutableStore().remove(Unit.class);
	}


	// ======================================================================
	// Public methods
	// ======================================================================

	// TODO JDoc if set only values from value list are allowed (verify this is correct!)
	public Boolean isValueListReference() {
		return getValue(ATTR_VALUE_LIST_REFERENCE).extract();
	}

	public void setValueListReference(Boolean valueListReference) {
		getValue(ATTR_VALUE_LIST_REFERENCE).set(valueListReference);
	}

	public Boolean isValueCopyable() {
		return getValue(ATTR_VALUE_COPYABLE).extract();
	}

	public void setValueCopyable(Boolean valueCopyable) {
		getValue(ATTR_VALUE_COPYABLE).set(valueCopyable);
	}

	public String getActionRequestClassname() {
		return getValue(ATTR_ACTION_REQUEST_CLASSNAME).extract();
	}

	public void setActionRequestClassname(String actionRequestClassname) {
		getValue(ATTR_ACTION_REQUEST_CLASSNAME).set(actionRequestClassname);
	}

	public Optional<ValueList> getValueList() {
		return Optional.ofNullable(getCore().getMutableStore().get(ValueList.class));
	}

	public void setValueList(ValueList valueList) {
		getCore().getMutableStore().set(valueList);
	}

	public Optional<CatalogComponent> getCatalogComponent() {
		return Optional.ofNullable(getCore().getPermanentStore().get(CatalogComponent.class));
	}

	public Optional<CatalogSensor> getCatalogSensor() {
		return Optional.ofNullable(getCore().getPermanentStore().get(CatalogSensor.class));
	}

	/*
	 * TODO properties below are virtual properties read from the associated entity type's meta data!
	 */

	public ScalarType getScalarType() {
		return scalarTypeValue.extract();
	}

	public Boolean isSequence() {
		return sequenceValue.extract();
	}

	public Class<? extends Enum<?>> getEnumerationClass() {
		if(!getScalarType().isEnumeration()) {
			throw new IllegalStateException("Catalog attribute is not of type enumeration.");
		}

		String enumName = enumerationClassValue.extract();
		Class<? extends Enum<?>> enumClass;

		if(ScalarType.class.getSimpleName().equals(enumName)) {
			enumClass = ScalarType.class;
		} else if(VersionState.class.getSimpleName().equals(enumName)) {
			enumClass = VersionState.class;
		} else if(AxisType.class.getSimpleName().equals(enumName)) {
			enumClass = AxisType.class;
		} else if(SequenceRepresentation.class.getSimpleName().equals(enumName)) {
			enumClass = SequenceRepresentation.class;
		} else if(TypeSpecification.class.getSimpleName().equals(enumName)) {
			enumClass = TypeSpecification.class;
		} else if(Interpolation.class.getSimpleName().equals(enumName)) {
			enumClass = Interpolation.class;
		} else {
			throw new IllegalStateException("Unable to determine enumeration class.");
		}

		return enumClass;
	}

	public Optional<Unit> getUnit() {
		return Optional.ofNullable(unit);
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('(');

		sb.append("ScalarType = ").append(getScalarType());
		if(getScalarType().isEnumeration()) {
			sb.append(", EnumerationClass = ").append(getEnumerationClass());
		}

		sb.append(", Sequence = ").append(isSequence());

		Optional<Unit> unit = getUnit();
		if(unit.isPresent()) {
			sb.append(", Unit = ").append(unit.get());
		}

		sb.append(", ").append(getValues().values().stream().map(Value::toString).collect(Collectors.joining(", ")));

		return sb.append(')').toString();
	}

	// ======================================================================
	// Package methods
	// ======================================================================

	// TODO once entity is written this is a read only property! (URI.getID > 0)
	void setScalarType(ScalarType scalarType) {
		scalarTypeValue.set(scalarType);
	}

	// TODO once entity is written this is a read only property! (URI.getID > 0)
	void setSequence(Boolean sequence) {
		sequenceValue.set(sequence);
	}

	// TODO once entity is written this is a read only property! (URI.getID > 0)
	void setEnumerationClass(Class<? extends Enum<?>> enumerationClass) {
		List<Class<? extends Enum<?>>> allowedEnumerationClasses = Arrays.asList(ScalarType.class,
				VersionState.class, Interpolation.class, AxisType.class, SequenceRepresentation.class,
				TypeSpecification.class);

		if(allowedEnumerationClasses.contains(enumerationClass)) {
			setScalarType(ScalarType.ENUMERATION);
			enumerationClassValue.set(enumerationClass.getSimpleName());
		} else {
			throw new IllegalArgumentException("Enumeration class '" + enumerationClass.getName() + "' is not supported.");
		}
	}

}
