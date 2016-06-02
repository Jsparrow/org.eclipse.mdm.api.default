/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.mdm.api.base.model.BaseEntityFactory;
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Entity;
import org.eclipse.mdm.api.base.model.EntityCore;
import org.eclipse.mdm.api.base.model.MimeType;
import org.eclipse.mdm.api.base.model.ScalarType;
import org.eclipse.mdm.api.base.model.ValueType;
import org.eclipse.mdm.api.base.model.VersionState;

public abstract class DefaultEntityFactory extends BaseEntityFactory {

	public CatalogComponent createCatalogComponent(ContextType contextType, String name) {
		validateCatalogName(name, false);

		CatalogComponent catalogComponent = new CatalogComponent(createComponentCore(contextType, false));

		// properties
		catalogComponent.setName(name);
		catalogComponent.setMimeType(createCatalogMimeType(catalogComponent));
		catalogComponent.setDateCreated(LocalDateTime.now());

		// TODO we should check whether this property exists (seems to be a deprecated one..)
		catalogComponent.getValue("ValidFlag").set(VersionState.VALID);

		return catalogComponent;
	}

	// TODO some day...
	//	public CatalogSensor createCatalogSensor(String name, CatalogComponent catalogComponent) {
	//		validateCatalogName(name);
	//
	//		if(catalogComponent.getContextSensor(name).isPresent()) {
	//			throw new IllegalArgumentException("Sensor with name '" + name + "' already exists.");
	//		}
	//		// TODO: check context type of given catalog component!
	//
	//		EntityCore entityCore = null;
	//		CatalogSensor catalogSensor = new CatalogSensor(entityCore);
	//
	//		catalogSensor.setName(name);
	//		catalogSensor.setMimeType(createCatalogMimeType(entityCore));
	//		catalogSensor.setDateCreated(LocalDateTime.now());
	//
	//		catalogComponent.addCatalogSensor(catalogSensor);
	//
	//		return catalogSensor;
	//	}

	public CatalogAttribute createCatalogAttribute(String name, ValueType valueType,
			/* TODO create a common interface for catComp & catSensor */ CatalogComponent catalogComponent) {
		validateCatalogName(name, true);

		// TODO document allowed or not allowed value types...
		if(catalogComponent.getCatalogAttribute(name).isPresent()) {
			throw new IllegalArgumentException("Catalog attribute with name '" + name + "' already exists.");
		} else if(valueType.isEnumerationType() || valueType.isByteStreamType() ||
				valueType.isUnknown() || valueType.isBlob()) {
			throw new IllegalArgumentException("Given value type is not allowed.");
		}

		CatalogAttribute catalogAttribute = new CatalogAttribute(createAttributeCore(catalogComponent.getContextType(), false));

		// relations
		getPermanentStore(catalogAttribute).setParent(catalogComponent, false);
		getChildrenStore(catalogComponent).add(catalogAttribute);

		// properties
		catalogAttribute.setName(name);
		catalogAttribute.setMimeType(createCatalogMimeType(catalogAttribute));
		catalogAttribute.setScalarType(ScalarType.valueOf(valueType.toSingleType().name()));
		catalogAttribute.setSequence(valueType.isSequence());
		catalogAttribute.setSortIndex(Integer.valueOf(0)); // TODO

		return catalogAttribute;
	}

	public CatalogAttribute createCatalogAttribute(String name, Class<? extends Enum<?>> enumerationClass,
			/* TODO create a common interface for catComp & catSensor */ CatalogComponent catalogComponent) {
		validateCatalogName(name, true);
		if(catalogComponent.getCatalogAttribute(name).isPresent()) {
			throw new IllegalArgumentException("Catalog attribute with name '" + name + "' already exists.");
		}

		CatalogAttribute catalogAttribute = new CatalogAttribute(createAttributeCore(catalogComponent.getContextType(), false));

		// relations
		getPermanentStore(catalogAttribute).setParent(catalogComponent, false);
		getChildrenStore(catalogComponent).add(catalogAttribute);

		// properties
		catalogAttribute.setName(name);
		catalogAttribute.setMimeType(createCatalogMimeType(catalogAttribute));
		catalogAttribute.setEnumerationClass(enumerationClass);
		catalogAttribute.setSequence(false);
		catalogAttribute.setSortIndex(Integer.valueOf(0)); // TODO

		return catalogAttribute;
	}

	public TemplateRoot createTemplateRoot(ContextType contextType, String name) {
		TemplateRoot templateRoot = new TemplateRoot(createRootCore(contextType, true));

		// TODO: name and version have to be a unique combination!!!
		// => find a way to check this prior creating a new instance
		// ==> InsertStatement or Transaction.create

		// properties
		templateRoot.setName(name);
		templateRoot.setMimeType(createTemplateMimeType(templateRoot));
		templateRoot.setDateCreated(LocalDateTime.now());
		templateRoot.setVersionState(VersionState.EDITABLE);
		templateRoot.setVersion(Integer.valueOf(1));

		return templateRoot;
	}

	public TemplateComponent createTemplateComponent(String name, TemplateRoot templateRoot, CatalogComponent catalogComponent) {
		if(!templateRoot.getContextType().equals(catalogComponent.getContextType())) {
			throw new IllegalArgumentException("Context type of template root and catalog component do not match.");
		} else if(templateRoot.getTemplateComponent(name).isPresent()) {
			throw new IllegalArgumentException("Template component with name '" + name + "' already exists.");
		}

		TemplateComponent templateComponent = new TemplateComponent(createComponentCore(templateRoot.getContextType(), true));

		// relations
		getPermanentStore(templateComponent).setParent(templateRoot, false);
		getMutableStore(templateComponent).set(catalogComponent);
		getChildrenStore(templateRoot).add(templateComponent);

		// properties
		templateComponent.setName(name);
		templateComponent.setMimeType(createTemplateMimeType(templateComponent));
		templateComponent.setOptional(Boolean.TRUE);
		templateComponent.setDefaultActive(Boolean.TRUE);
		templateComponent.setSeriesConstant(Boolean.TRUE);
		templateComponent.setSortIndex(Integer.valueOf(0)); // TODO

		// create template attributes
		catalogComponent.getCatalogAttributes().forEach(ca -> createTemplateAttribute(ca.getName(), templateComponent));

		return templateComponent;
	}

	public TemplateComponent createTemplateComponent(String name, TemplateComponent partentComponentTemplate, CatalogComponent catalogComponent) {
		TemplateRoot templateRoot = partentComponentTemplate.getTemplateRoot();
		if(!templateRoot.getContextType().equals(catalogComponent.getContextType())) {
			throw new IllegalArgumentException("Context type of template root and catalog component do not match.");
		} else if(templateRoot.getTemplateComponent(name).isPresent()) {
			throw new IllegalArgumentException("Template component with name '" + name + "' already exists.");
		}

		TemplateComponent templateComponent = new TemplateComponent(createComponentCore(templateRoot.getContextType(), true));

		// relations
		getPermanentStore(templateComponent).setParent(partentComponentTemplate, false);
		getMutableStore(templateComponent).set(catalogComponent);
		getChildrenStore(partentComponentTemplate).add(templateComponent);

		// properties
		templateComponent.setName(name);
		templateComponent.setMimeType(createTemplateMimeType(templateComponent));
		templateComponent.setOptional(Boolean.TRUE);
		templateComponent.setDefaultActive(Boolean.TRUE);
		templateComponent.setSeriesConstant(Boolean.TRUE);
		templateComponent.setSortIndex(Integer.valueOf(0)); // TODO

		// create template attributes
		catalogComponent.getCatalogAttributes().forEach(ca -> createTemplateAttribute(ca.getName(), templateComponent));

		return templateComponent;
	}

	// TODO name must be one of the catalog componetn's attributes given template component is bound to
	public TemplateAttribute createTemplateAttribute(String name, TemplateComponent templateComponent) {
		if(templateComponent.getTemplateAttribute(name).isPresent()) {
			throw new IllegalArgumentException("Template attribute with name '" + name + "' already exists.");
		}

		Optional<CatalogAttribute> catalogAttribute = templateComponent.getCatalogComponent().getCatalogAttribute(name);
		if(catalogAttribute.isPresent()) {
			TemplateAttribute templateAttribute = new TemplateAttribute(createAttributeCore(templateComponent.getCatalogComponent().getContextType(), true));

			// relations
			getPermanentStore(templateAttribute).setParent(templateComponent, false);
			getMutableStore(templateAttribute).set(catalogAttribute.get());
			getChildrenStore(templateComponent).add(templateAttribute);

			// properties
			templateAttribute.setName(name);
			templateAttribute.setMimeType(createTemplateMimeType(templateAttribute));
			templateAttribute.setValueReadOnly(Boolean.FALSE);
			templateAttribute.setOptional(Boolean.TRUE);

			return templateAttribute;
		}

		throw new IllegalArgumentException("Catalog attribute with name '" + name + "' does not exists.");
	}

	public TemplateTestStep createTemplateTestStep(String name) {
		TemplateTestStep templateTestStep = new TemplateTestStep(createCore(TemplateTestStep.class));

		// TODO: name and version have to be a unique combination!!!
		// => find a way to check this prior creating a new instance
		// ==> InsertStatement or Transaction.create

		// properties
		templateTestStep.setName(name);
		templateTestStep.setMimeType(getDefaultMimeType(TemplateTestStep.class));
		templateTestStep.setDateCreated(LocalDateTime.now());
		templateTestStep.setVersionState(VersionState.EDITABLE);
		templateTestStep.setVersion(Integer.valueOf(1));

		return templateTestStep;
	}

	public TemplateTest createTemplateTest(String name) {
		TemplateTest templateTest = new TemplateTest(createCore(TemplateTest.class));

		// TODO: name and version have to be a unique combination!!!
		// => find a way to check this prior creating a new instance
		// ==> InsertStatement or Transaction.create

		// properties
		templateTest.setName(name);
		templateTest.setMimeType(getDefaultMimeType(TemplateTest.class));
		templateTest.setDateCreated(LocalDateTime.now());
		templateTest.setVersion(Integer.valueOf(1));
		templateTest.setVersionState(VersionState.EDITABLE);

		return templateTest;
	}

	public TemplateTestStepUsage createTemplateTestStepUsage(String name, TemplateTest templateTest, TemplateTestStep templateTestStep) {
		if(templateTest.getTemplateTestStepUsage(name).isPresent()) {
			throw new IllegalArgumentException("Template test step usage with name '" + name + "' already exists.");
		}

		TemplateTestStepUsage templateTestStepUsage = new TemplateTestStepUsage(createCore(TemplateTestStepUsage.class));

		// relations
		getPermanentStore(templateTestStepUsage).setParent(templateTest, false);
		getMutableStore(templateTestStepUsage).set(templateTestStep);
		getChildrenStore(templateTest).add(templateTestStepUsage);

		// properties
		templateTestStepUsage.setName(name);
		templateTestStepUsage.setMimeType(getDefaultMimeType(TemplateTestStepUsage.class));
		templateTestStepUsage.setOptional(Boolean.TRUE);
		templateTestStepUsage.setDefaultActive(Boolean.TRUE);
		templateTestStepUsage.setSortIndex(Integer.valueOf(1)); // TODO

		return templateTestStepUsage;
	}

	public ValueList createValueList(String name) {
		ValueList valueList = new ValueList(createCore(ValueList.class));

		// properties
		valueList.setName(name);
		valueList.setMimeType(getDefaultMimeType(ValueList.class));
		valueList.setDateCreated(LocalDateTime.now());

		return valueList;
	}

	public ValueListValue createValueListValue(String name, ValueList valueList) {
		if(valueList.getValueListValue(name).isPresent()) {
			throw new IllegalArgumentException("Value list value with name '" + name + "' already exists.");
		}

		ValueListValue valueListValue = new ValueListValue(createCore(ValueListValue.class));

		// relations
		getPermanentStore(valueListValue).setParent(valueList, false);
		getChildrenStore(valueList).add(valueListValue);

		// properties
		valueListValue.setName(name);
		valueListValue.setMimeType(getDefaultMimeType(ValueListValue.class));
		valueListValue.setSortIndex(Integer.valueOf(0)); // TODO

		// this property is hidden by the public API and is not allowed to be modified!
		valueListValue.getValue(ValueListValue.ATTR_SCALAR_TYPE).set(ScalarType.STRING);

		return valueListValue;
	}

	// TODO add ValueListValue

	protected abstract EntityCore createRootCore(ContextType contextType, boolean forTemplate);

	protected abstract EntityCore createComponentCore(ContextType contextType, boolean forTemplate);

	protected abstract EntityCore createSensorCore(boolean forTemplate);

	protected abstract EntityCore createAttributeCore(ContextType contextType, boolean forTemplate);

	/*
	 * TODO createCatalogMimeType() & createTemplateMimeType() do both exactly the same...
	 * is there a way to merge them?!
	 */

	@Deprecated
	protected abstract MimeType createCatalogMimeType(Entity entity);
	@Deprecated
	protected abstract MimeType createTemplateMimeType(Entity entity);

	private static void validateCatalogName(String name, boolean isAttributeName) {
		if(name == null || name.isEmpty() || name.length() > 30) {
			throw new IllegalArgumentException("A catalog name is not allowed to be empty and must not exceed 30 characters.");
		} else if(name.toLowerCase(Locale.ROOT).startsWith("ao")) {
			throw new IllegalArgumentException("A catalog name is not allowed to start with 'ao' (case ignored).");
		} else if(!name.matches("^[\\w]+$")) {
			throw new IllegalArgumentException("A calatog name may only constists of the following characters: a-z, A-Z, 0-9 or _.");
		} else if(isAttributeName && Arrays.asList("id", "name", "mimetype").contains(name.toLowerCase(Locale.ROOT))) {
			throw new IllegalArgumentException("A catalog attribute name is not allowed to be "
					+ "'id', 'name' or 'mimetype' (case ignored).");
		}
	}

}
