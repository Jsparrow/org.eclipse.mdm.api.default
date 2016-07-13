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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.eclipse.mdm.api.base.model.BaseEntityFactory;
import org.eclipse.mdm.api.base.model.ContextComponent;
import org.eclipse.mdm.api.base.model.ContextRoot;
import org.eclipse.mdm.api.base.model.ContextSensor;
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Entity;
import org.eclipse.mdm.api.base.model.Measurement;
import org.eclipse.mdm.api.base.model.ScalarType;
import org.eclipse.mdm.api.base.model.Test;
import org.eclipse.mdm.api.base.model.TestStep;
import org.eclipse.mdm.api.base.model.ValueType;
import org.eclipse.mdm.api.base.model.VersionState;

public abstract class EntityFactory extends BaseEntityFactory {

	//	@Override
	//	public Test createTest(String name) {
	//		throw new UnsupportedOperationException("Test requires a status."); // TODO ...
	//	}
	//
	//	@Override
	//	public TestStep createTestStep(String name, Test test) {
	//		throw new UnsupportedOperationException("Test step requires a status."); // TODO ...
	//	}

	public Project createProject(String name) {
		Project project = new Project(createCore(Project.class));
		project.setName(name);
		return project;
	}
	
	public Pool createPool(String name, Project project) {
		Pool pool = new Pool(createCore(Pool.class));
		
		getPermanentStore(pool).set(project);
		getChildrenStore(project).add(pool);
		
		pool.setName(name);
		return pool;
	}
	
	
	// TODO make a decision: status in or out!
	public Test createTest(String name, Pool pool, TemplateTest templateTest) {
		return createTest(name, pool, null, null, templateTest);
	}

	// TODO make a decision: status in or out!
	protected Test createTest(String name, Pool pool, Status statusTest, Status statusTestStep, TemplateTest templateTest) {
		Test test = createTest(name, pool, statusTest);

		// relations
		getMutableStore(test).set(templateTest);

		// create default active and mandatory test steps according to the template

		templateTest.getTemplateTestStepUsages().stream().filter(TemplateTestStepUsage.IS_IMPLICIT_CREATE)
		.map(TemplateTestStepUsage::getTemplateTestStep).forEach(templateTestStep -> {
			createTestStep(test, statusTestStep, templateTestStep);
		});

		return test;
	}

	// TODO make a decision: status in or out!
	protected Test createTest(String name, Pool pool, Status status) {
		Test test = super.createTest(name);
		
		getPermanentStore(test).set(pool);
		getChildrenStore(pool).add(test);
		
		if(status != null) {
			status.assign(test);
		}
		
		return test;
	}

	// TODO make a decision: status in or out!
	protected TestStep createTestStep(Test test, TemplateTestStep templateTestStep) {
		return createTestStep(test, null, templateTestStep);
	}

	// TODO make a decision: status in or out!
	// TODO renaming is possible using TestStep.setName();
	protected TestStep createTestStep(Test test, Status status, TemplateTestStep templateTestStep) {
		TemplateTest templateTest = TemplateTest.of(test)
				.orElseThrow(() -> new IllegalArgumentException("Template test is not available."));
		if(!templateTest.contains(templateTestStep)) {
			throw new IllegalArgumentException("Template test step is part of the test template.");
		}

		TestStep testStep = createTestStep(templateTestStep.getName(), test, status);

		// relations
		getMutableStore(testStep).set(templateTestStep);

		// create initial context roots
		templateTestStep.getTemplateRoots().forEach(templateRoot -> createContextRoot(testStep, templateRoot));

		return testStep;
	}

	// TODO make a decision: status in or out!
	protected TestStep createTestStep(String name, Test test, Status status) {
		TestStep testStep = super.createTestStep(name, test);
		if(status != null) {
			status.assign(testStep);
		}
		return testStep;
	}

	// ################################## CONTEXTS ##################################

	public ContextRoot createContextRoot(TestStep testStep, TemplateRoot templateRoot) {
		ContextRoot contextRoot = createContextRoot(templateRoot);

		// relations
		getMutableStore(testStep).set(contextRoot, templateRoot.getContextType());

		return contextRoot;
	}

	public ContextRoot createContextRoot(Measurement measurement, TemplateRoot templateRoot) {
		ContextRoot contextRoot = createContextRoot(templateRoot);

		// relations
		getMutableStore(measurement).set(contextRoot, templateRoot.getContextType());

		return contextRoot;
	}

	public ContextRoot createContextRoot(TemplateRoot templateRoot) {
		ContextRoot contextRoot = createContextRoot(templateRoot.getName(), 
			templateRoot.getVersion(), templateRoot.getContextType());

		// relations
		getMutableStore(contextRoot).set(templateRoot);

		// create default active and mandatory context components
		templateRoot.getTemplateComponents().stream()
		.filter(TemplateComponent.IS_DEFAULT_ACTIVE.or(TemplateComponent.IS_MANDATORY))
		.forEach(templateComponent -> {
			createContextComponent(templateComponent.getName(), contextRoot);
		});

		return contextRoot;
	}

	// TODO: name is name of the template component!
	@Override
	public ContextComponent createContextComponent(String name, ContextRoot contextRoot) {
		if(contextRoot.getContextComponent(name).isPresent()) {
			throw new IllegalArgumentException("Context component with name '" + name + "' already exists.");
		}

		TemplateRoot templateRoot = TemplateRoot.of(contextRoot)
				.orElseThrow(() -> new IllegalArgumentException("Template root is not available."));

		Optional<TemplateComponent> templateComponent = templateRoot.getTemplateComponent(name);
		if(templateComponent.isPresent()) {
			// recursively create missing parent context components
			templateComponent.get().getParentTemplateComponent()
			.filter(tc -> !contextRoot.getContextComponent(tc.getName()).isPresent())
			.ifPresent(tc -> createContextComponent(tc.getName(), contextRoot));

			// create context component if not already done
			if(!contextRoot.getContextComponent(name).isPresent()) {
				ContextComponent contextComponent = super.createContextComponent(templateComponent.get().getCatalogComponent().getName(), contextRoot);

				// relations
				getMutableStore(contextComponent).set(templateComponent.get());

				// properties
				contextComponent.setName(name);
				contextComponent.setMimeType(contextComponent.getMimeType().addSubType(templateComponent.get().getName()));
				hideValues(getCore(contextComponent), templateComponent.get().getTemplateAttributes());
				templateComponent.get().getTemplateAttributes().forEach(ta -> {
					contextComponent.getValue(ta.getName()).set(ta.getDefaultValue().extract());
				});

				// create default active and mandatory child context components
				templateComponent.get().getTemplateComponents().stream().filter(TemplateComponent.IS_IMPLICIT_CREATE)
				.forEach(childTemplateComponent -> {
					createContextComponent(childTemplateComponent.getName(), contextRoot);
				});

				// create default active and mandatory context sensors
				templateComponent.get().getTemplateSensors().stream().filter(TemplateSensor.IS_IMPLICIT_CREATE)
				.forEach(templateSensor -> {
					createContextSensor(templateSensor.getName(), contextComponent);
				});

				return contextComponent;
			}
		}

		throw new IllegalArgumentException("Template component with name '" + name + "' does not exist.");
	}

	@Override
	public ContextSensor createContextSensor(String name, ContextComponent contextComponent) {
		if(contextComponent.getContextSensor(name).isPresent()) {
			throw new IllegalArgumentException("Context sensor with name '" + name + "' already exists.");
		}

		TemplateComponent templateComponent = TemplateComponent.of(contextComponent)
				.orElseThrow(() -> new IllegalArgumentException("Template component is not available."));

		Optional<TemplateSensor> templateSensor = templateComponent.getTemplateSensor(name);
		if(templateSensor.isPresent()) {
			ContextSensor contextSensor = super.createContextSensor(templateSensor.get().getCatalogSensor().getName(), contextComponent);

			// relations
			getMutableStore(contextSensor).set(templateSensor.get());

			// properties
			contextSensor.setName(name);
			hideValues(getCore(contextSensor), templateSensor.get().getTemplateAttributes());
			templateSensor.get().getTemplateAttributes().forEach(ta -> {
				contextSensor.getValue(ta.getName()).set(ta.getDefaultValue().extract());
			});
		}

		throw new IllegalArgumentException("Template sensor with name '" + name + "' does not exist.");
	}

	// ################################## CONTEXTS ##################################

	// administrative stuff

	public CatalogComponent createCatalogComponent(ContextType contextType, String name) {
		validateCatalogName(name, false);

		CatalogComponent catalogComponent = new CatalogComponent(createCore(CatalogComponent.class, contextType));

		// properties
		catalogComponent.setName(name);
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

	public CatalogAttribute createCatalogAttribute(String name, ValueType valueType, CatalogComponent catalogComponent) {
		validateCatalogName(name, true);

		// TODO document allowed or not allowed value types...
		if(catalogComponent.getCatalogAttribute(name).isPresent()) {
			throw new IllegalArgumentException("Catalog attribute with name '" + name + "' already exists.");
		} else if(valueType.isEnumerationType() || valueType.isByteStreamType() ||
				valueType.isUnknown() || valueType.isBlob()) {
			throw new IllegalArgumentException("Value type '" + valueType + "' is not allowed.");
		}

		CatalogAttribute catalogAttribute = new CatalogAttribute(createCore(CatalogAttribute.class, catalogComponent.getContextType()));

		// relations
		getPermanentStore(catalogAttribute).set(catalogComponent);
		getChildrenStore(catalogComponent).add(catalogAttribute);

		// properties
		catalogAttribute.setName(name);
		catalogAttribute.setScalarType(ScalarType.valueOf(valueType.toSingleType().name()));
		catalogAttribute.setSequence(valueType.isSequence());
		catalogAttribute.setSortIndex(nextIndex(catalogComponent.getCatalogAttributes()));

		return catalogAttribute;
	}

	public CatalogAttribute createCatalogAttribute(String name, Class<? extends Enum<?>> enumerationClass,
			CatalogComponent catalogComponent) {
		validateCatalogName(name, true);
		if(catalogComponent.getCatalogAttribute(name).isPresent()) {
			throw new IllegalArgumentException("Catalog attribute with name '" + name + "' already exists.");
		}

		CatalogAttribute catalogAttribute = new CatalogAttribute(createCore(CatalogAttribute.class, catalogComponent.getContextType()));

		// relations
		getPermanentStore(catalogAttribute).set(catalogComponent);
		getChildrenStore(catalogComponent).add(catalogAttribute);

		// properties
		catalogAttribute.setName(name);
		catalogAttribute.setEnumerationClass(enumerationClass);
		catalogAttribute.setSequence(false);
		catalogAttribute.setSortIndex(nextIndex(catalogComponent.getCatalogAttributes()));

		return catalogAttribute;
	}

	public TemplateRoot createTemplateRoot(ContextType contextType, String name) {
		TemplateRoot templateRoot = new TemplateRoot(createCore(TemplateRoot.class, contextType));

		// TODO: name and version have to be a unique combination!!!
		// => find a way to check this prior creating a new instance
		// ==> InsertStatement or Transaction.create

		// properties
		templateRoot.setName(name);
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

		TemplateComponent templateComponent = new TemplateComponent(createCore(TemplateComponent.class, templateRoot.getContextType()));

		// relations
		getPermanentStore(templateComponent).set(templateRoot);
		getMutableStore(templateComponent).set(catalogComponent);
		getChildrenStore(templateRoot).add(templateComponent);

		// properties
		templateComponent.setName(name);
		templateComponent.setOptional(Boolean.TRUE);
		templateComponent.setDefaultActive(Boolean.TRUE);
		templateComponent.setSeriesConstant(Boolean.TRUE);
		templateComponent.setSortIndex(nextIndex(templateRoot.getTemplateComponents()));

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

		TemplateComponent templateComponent = new TemplateComponent(createCore(TemplateComponent.class, templateRoot.getContextType()));

		// relations
		getPermanentStore(templateComponent).set(partentComponentTemplate);
		getMutableStore(templateComponent).set(catalogComponent);
		getChildrenStore(partentComponentTemplate).add(templateComponent);

		// properties
		templateComponent.setName(name);
		templateComponent.setOptional(Boolean.TRUE);
		templateComponent.setDefaultActive(Boolean.TRUE);
		templateComponent.setSeriesConstant(Boolean.TRUE);
		templateComponent.setSortIndex(nextIndex(partentComponentTemplate.getTemplateComponents()));

		// create template attributes
		catalogComponent.getCatalogAttributes().forEach(ca -> createTemplateAttribute(ca.getName(), templateComponent));

		return templateComponent;
	}

	// TODO name must be one of the catalog componetn's attributes given template component is bound to
	public TemplateAttribute createTemplateAttribute(String name, TemplateComponent templateComponent) {
		if(templateComponent.getTemplateAttribute(name).isPresent()) {
			throw new IllegalArgumentException("Template attribute with name '" + name + "' already exists.");
		}

		CatalogComponent catalogComponent = templateComponent.getCatalogComponent();
		Optional<CatalogAttribute> catalogAttribute = catalogComponent.getCatalogAttribute(name);
		if(catalogAttribute.isPresent()) {
			TemplateAttribute templateAttribute = new TemplateAttribute(createCore(TemplateAttribute.class, catalogComponent.getContextType()));

			// relations
			getPermanentStore(templateAttribute).set(templateComponent);
			getMutableStore(templateAttribute).set(catalogAttribute.get());
			getChildrenStore(templateComponent).add(templateAttribute);

			// properties
			templateAttribute.setName(name);
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
		getPermanentStore(templateTestStepUsage).set(templateTest);
		getMutableStore(templateTestStepUsage).set(templateTestStep);
		getChildrenStore(templateTest).add(templateTestStepUsage);

		// properties
		templateTestStepUsage.setName(name);
		templateTestStepUsage.setOptional(Boolean.TRUE);
		templateTestStepUsage.setDefaultActive(Boolean.TRUE);
		templateTestStepUsage.setSortIndex(nextIndex(templateTest.getTemplateTestStepUsages()));

		return templateTestStepUsage;
	}

	public ValueList createValueList(String name) {
		ValueList valueList = new ValueList(createCore(ValueList.class));

		// properties
		valueList.setName(name);
		valueList.setDateCreated(LocalDateTime.now());

		return valueList;
	}

	public ValueListValue createValueListValue(String name, ValueList valueList) {
		if(valueList.getValueListValue(name).isPresent()) {
			throw new IllegalArgumentException("Value list value with name '" + name + "' already exists.");
		}

		ValueListValue valueListValue = new ValueListValue(createCore(ValueListValue.class));

		// relations
		getPermanentStore(valueListValue).set(valueList);
		getChildrenStore(valueList).add(valueListValue);

		// properties
		valueListValue.setName(name);
		valueListValue.setSortIndex(nextIndex(valueList.getValueListValues()));

		// this property is hidden by the public API and is not allowed to be modified!
		valueListValue.getValue(ValueListValue.ATTR_SCALAR_TYPE).set(ScalarType.STRING);

		return valueListValue;
	}

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

	private static void hideValues(Core contextCore, List<TemplateAttribute> templateAttributes) {
		Set<String> names = new HashSet<>(contextCore.getValues().keySet());
		names.remove(Entity.ATTR_NAME);
		names.remove(Entity.ATTR_MIMETYPE);
		templateAttributes.stream().map(Entity::getName).forEach(names::remove);
		contextCore.hideValues(names);
	}

}
