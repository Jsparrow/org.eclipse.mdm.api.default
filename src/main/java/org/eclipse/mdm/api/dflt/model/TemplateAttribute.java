/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.util.Optional;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.EntityCore;

public final class TemplateAttribute extends BaseEntity implements Deletable {

	// ======================================================================
	// Class variables
	// ======================================================================

	public static final String ATTR_DEFAULT_VALUE = "DefaultValue";
	public static final String ATTR_VALUE_READONLY = "ValueReadonly";
	public static final String ATTR_OPTIONAL = "Obligatory";

	// ======================================================================
	// Constructors
	// ======================================================================

	TemplateAttribute(EntityCore core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	public String getDefaultValue() {
		/*
		 * TODO: we need to ensure that given String value is compatible
		 * with the value type defined in the CatalogAttribute!
		 *
		 * TODO: see: Parameter.setValue?!
		 */
		return getValue(ATTR_DEFAULT_VALUE).extract();
	}

	public void setDefaultValue(String defaultValue) {
		/*
		 * TODO: we need to ensure that given String value is compatible
		 * with the value type defined in the CatalogAttribute!
		 *
		 * TODO: see: Parameter.setValue?!
		 */
		getValue(ATTR_DEFAULT_VALUE).set(defaultValue);
	}

	public Boolean isValueReadOnly() {
		return getValue(ATTR_VALUE_READONLY).extract();
	}

	public void setValueReadOnly(Boolean valueReadOnly) {
		getValue(ATTR_VALUE_READONLY).set(valueReadOnly);
	}

	public Boolean isOptional() {
		boolean mandatory = getValue(ATTR_OPTIONAL).extract();
		return mandatory ? Boolean.FALSE : Boolean.TRUE;
	}

	public void setOptional(Boolean optional) {
		getValue(ATTR_OPTIONAL).set(optional ? Boolean.FALSE : Boolean.TRUE);
	}

	public CatalogAttribute getCatalogAttribute() {
		return getCore().getMutableStore().get(CatalogAttribute.class);
	}

	public TemplateRoot getTemplateRoot() {
		Optional<TemplateComponent> templateComponent = getTemplateComponent();
		if(templateComponent.isPresent()) {
			return templateComponent.get().getTemplateRoot();
		}

		return getTemplateSensor().orElseThrow(() -> new IllegalStateException("Parent entity is unknown.")).getTemplateRoot();
	}

	public Optional<TemplateComponent> getTemplateComponent() {
		return Optional.ofNullable(getCore().getPermanentStore().get(TemplateComponent.class));
	}

	public Optional<TemplateSensor> getTemplateSensor() {
		return Optional.ofNullable(getCore().getPermanentStore().get(TemplateSensor.class));
	}

}
