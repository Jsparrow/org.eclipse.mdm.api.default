/*
 * Copyright (c) 2016 Gigatronik Ingolstadt GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.mdm.api.dflt.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.eclipse.mdm.api.base.adapter.Core;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.BaseEntityFactory;
import org.eclipse.mdm.api.base.model.ContextComponent;
import org.eclipse.mdm.api.base.model.ContextRoot;
import org.eclipse.mdm.api.base.model.ContextSensor;
import org.eclipse.mdm.api.base.model.ContextType;
import org.eclipse.mdm.api.base.model.Entity;
import org.eclipse.mdm.api.base.model.Enumeration;
import org.eclipse.mdm.api.base.model.Measurement;
import org.eclipse.mdm.api.base.model.Quantity;
import org.eclipse.mdm.api.base.model.ScalarType;
import org.eclipse.mdm.api.base.model.Test;
import org.eclipse.mdm.api.base.model.TestStep;
import org.eclipse.mdm.api.base.model.Value;
import org.eclipse.mdm.api.base.model.ValueType;
import org.eclipse.mdm.api.base.model.VersionState;

/**
 * Creates new entities of the default application model.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 */
public abstract class EntityFactory extends BaseEntityFactory {

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Always throws an UnsupportedOperationException since in a default model
	 * each {@link Test} has a parent {@link Pool}.
	 *
	 * @throws UnsupportedOperationException
	 *             Is always thrown.
	 */
	@Override
	public Test createTest(String name) {
		throw new UnsupportedOperationException("Test requires a parent Pool.");
	}

	/**
	 * Creates a new {@link Project}.
	 *
	 * @param name
	 *            Name of the created {@code Project}.
	 * @return The created {@code Project} is returned.
	 */
	public Project createProject(String name) {
		Project project = new Project(createCore(Project.class));

		// properties
		project.setName(name);

		return project;
	}

	/**
	 * Creates a new {@link Pool} for given {@link Project}.
	 *
	 * @param name
	 *            Name of the created {@code Pool}.
	 * @param project
	 *            The parent {@code Project}.
	 * @return The created {@code Pool} is returned.
	 */
	public Pool createPool(String name, Project project) {
		Pool pool = new Pool(createCore(Pool.class));

		// relations
		getCore(pool).getPermanentStore().set(project);
		getCore(project).getChildrenStore().add(pool);

		// properties
		pool.setName(name);

		return pool;
	}

	/**
	 * Creates a new {@link Test} for given {@link Pool} using given
	 * {@link TemplateTest}.
	 *
	 * @param name
	 *            Name of the created {@code Test}.
	 * @param pool
	 *            The parent {@code Pool}.
	 * @param templateTest
	 *            The template the returned {@code Test} will be derived from.
	 * @return The created {@code Test} is returned.
	 */
	// TODO make a decision: status in or out!
	public Test createTest(String name, Pool pool, TemplateTest templateTest) {
		return createTest(name, pool, null, null, templateTest);
	}

	/**
	 * Creates a new {@link ContextRoot} for given {@link TestStep} using given
	 * {@link TemplateRoot}.
	 *
	 * @param testStep
	 *            The parent {@code TestStep}.
	 * @param templateRoot
	 *            The template the returned {@code ContextRoot} will be derived
	 *            from.
	 * @return The created {@code ContextRoot} is returned.
	 */
	public ContextRoot createContextRoot(TestStep testStep, TemplateRoot templateRoot) {
		ContextRoot contextRoot = createContextRoot(templateRoot);

		// relations
		getCore(testStep).getMutableStore().set(contextRoot, templateRoot.getContextType());

		return contextRoot;
	}

	/**
	 * Creates a new {@link ContextRoot} for given {@link Measurement} using
	 * given {@link TemplateRoot}.
	 *
	 * @param measurement
	 *            The parent {@code Measurement}.
	 * @param templateRoot
	 *            The template the returned {@code ContextRoot} will be derived
	 *            from.
	 * @return The created {@code ContextRoot} is returned.
	 */
	public ContextRoot createContextRoot(Measurement measurement, TemplateRoot templateRoot) {
		ContextRoot contextRoot = createContextRoot(templateRoot);

		// relations
		getCore(measurement).getMutableStore().set(contextRoot, templateRoot.getContextType());

		return contextRoot;
	}

	/**
	 * Creates a new {@link ContextRoot} using given {@link TemplateRoot}.
	 *
	 * @param templateRoot
	 *            The template the returned {@code ContextRoot} will be derived
	 *            from.
	 * @return The created {@code ContextRoot} is returned.
	 */
	public ContextRoot createContextRoot(TemplateRoot templateRoot) {
		ContextRoot contextRoot = createContextRoot(templateRoot.getName(), templateRoot.getContextType());

		// relations
		getCore(contextRoot).getMutableStore().set(templateRoot);

		// create default active and mandatory context components
		templateRoot.getTemplateComponents().stream()
				.filter(TemplateComponent.IS_DEFAULT_ACTIVE.or(TemplateComponent.IS_MANDATORY))
				.forEach(templateComponent -> {
					createContextComponent(templateComponent.getName(), contextRoot);
				});

		return contextRoot;
	}

	/**
	 * @throws IllegalArgumentException
	 *             Thrown if given name is already in use or
	 *             {@link TemplateComponent} with given name does not exist.
	 */
	@Override
	public ContextComponent createContextComponent(String name, ContextRoot contextRoot) {
		if (contextRoot.getContextComponent(name).isPresent()) {
			throw new IllegalArgumentException("Context component with name '" + name + "' already exists.");
		}

		TemplateRoot templateRoot = TemplateRoot.of(contextRoot)
				.orElseThrow(() -> new IllegalArgumentException("Template root is not available."));

		Optional<TemplateComponent> templateComponent = templateRoot.getTemplateComponent(name);
		if (templateComponent.isPresent()) {
			// recursively create missing parent context components
			templateComponent.get().getParentTemplateComponent()
					.filter(tc -> !contextRoot.getContextComponent(tc.getName()).isPresent())
					.ifPresent(tc -> createContextComponent(tc.getName(), contextRoot));

			// create context component if not already done
			if (!contextRoot.getContextComponent(name).isPresent()) {
				ContextComponent contextComponent = super.createContextComponent(
						templateComponent.get().getCatalogComponent().getName(), contextRoot);

				// relations
				getCore(contextComponent).getMutableStore().set(templateComponent.get());

				// properties
				contextComponent.setName(name);
				contextComponent
						.setMimeType(contextComponent.getMimeType().addSubType(templateComponent.get().getName()));
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContextSensor createContextSensor(String name, ContextComponent contextComponent) {
		if (contextComponent.getContextSensor(name).isPresent()) {
			throw new IllegalArgumentException("Context sensor with name '" + name + "' already exists.");
		}

		TemplateComponent templateComponent = TemplateComponent.of(contextComponent)
				.orElseThrow(() -> new IllegalArgumentException("Template component is not available."));

		Optional<TemplateSensor> templateSensor = templateComponent.getTemplateSensor(name);
		if (templateSensor.isPresent()) {
			ContextSensor contextSensor = super.createContextSensor(templateSensor.get().getCatalogSensor().getName(),
					contextComponent);

			// relations
			getCore(contextSensor).getMutableStore().set(templateSensor.get());

			// properties
			contextSensor.setName(name);
			hideValues(getCore(contextSensor), templateSensor.get().getTemplateAttributes());
			templateSensor.get().getTemplateAttributes().forEach(ta -> {
				contextSensor.getValue(ta.getName()).set(ta.getDefaultValue().extract());
			});
			
			return contextSensor;
		}

		throw new IllegalArgumentException("Template sensor with name '" + name + "' does not exist.");
	}

	/**
	 * Creates a new {@link CatalogComponent} with given {@link ContextType} and
	 * name.
	 *
	 * @param contextType
	 *            The {@code ContextType}.
	 * @param name
	 *            Name of the created {@code CatalogComponent}.
	 * @return The created {@code CatalogComponent} is returned.
	 * @throws IllegalArgumentException
	 *             Thrown if name is not allowed.
	 */
	public CatalogComponent createCatalogComponent(ContextType contextType, String name) {
		validateCatalogName(name, false);

		CatalogComponent catalogComponent = new CatalogComponent(createCore(CatalogComponent.class, contextType));

		// properties
		catalogComponent.setName(name);
		catalogComponent.setDateCreated(LocalDateTime.now());
		catalogComponent.getValue("ValidFlag").set(VersionState.VALID);

		return catalogComponent;
	}

	/**
	 * Creates a new {@link CatalogAttribute} for given
	 * {@link CatalogComponent}. The {@link ValueType} may be one of the
	 * following:
	 *
	 * <ul>
	 * <li>{@link ValueType#STRING}</li>
	 * <li>{@link ValueType#STRING_SEQUENCE}</li>
	 * <li>{@link ValueType#DATE}</li>
	 * <li>{@link ValueType#DATE_SEQUENCE}</li>
	 * <li>{@link ValueType#BOOLEAN}</li>
	 * <li>{@link ValueType#BOOLEAN_SEQUENCE}</li>
	 * <li>{@link ValueType#BYTE}</li>
	 * <li>{@link ValueType#BYTE_SEQUENCE}</li>
	 * <li>{@link ValueType#SHORT}</li>
	 * <li>{@link ValueType#SHORT_SEQUENCE}</li>
	 * <li>{@link ValueType#INTEGER}</li>
	 * <li>{@link ValueType#INTEGER_SEQUENCE}</li>
	 * <li>{@link ValueType#LONG}</li>
	 * <li>{@link ValueType#LONG_SEQUENCE}</li>
	 * <li>{@link ValueType#FLOAT}</li>
	 * <li>{@link ValueType#FLOAT_SEQUENCE}</li>
	 * <li>{@link ValueType#DOUBLE}</li>
	 * <li>{@link ValueType#DOUBLE_SEQUENCE}</li>
	 * <li>{@link ValueType#BYTE_STREAM}</li>
	 * <li>{@link ValueType#BYTE_STREAM_SEQUENCE}</li>
	 * <li>{@link ValueType#FLOAT_COMPLEX}</li>
	 * <li>{@link ValueType#FLOAT_COMPLEX_SEQUENCE}</li>
	 * <li>{@link ValueType#DOUBLE_COMPLEX}</li>
	 * <li>{@link ValueType#DOUBLE_COMPLEX_SEQUENCE}</li>
	 * <li>{@link ValueType#FILE_LINK}</li>
	 * <li>{@link ValueType#FILE_LINK_SEQUENCE}</li>
	 * </ul>
	 *
	 *
	 * @param name
	 *            Name of the created {@code CatalogAttribute}.
	 * @param valueType
	 *            The {@code ValueType}.
	 * @param catalogComponent
	 *            The parent {@code CatalogComponent}.
	 * @return The created {@code CatalogAttribute} is returned.
	 * @throws IllegalArgumentException
	 *             Thrown if given name is already in use or not allowed or
	 *             given {@code ValueType} is not supported.
	 */
	public CatalogAttribute createCatalogAttribute(String name, ValueType<?> valueType,
			CatalogComponent catalogComponent) {
		validateCatalogName(name, true);

		if (catalogComponent.getCatalogAttribute(name).isPresent()) {
			throw new IllegalArgumentException("Catalog attribute with name '" + name + "' already exists.");
		} else if (valueType.isEnumerationType() || valueType.isByteStreamType() || valueType.isUnknown()
				|| valueType.isBlob()) {
			throw new IllegalArgumentException("Value type '" + valueType + "' is not allowed.");
		}

		CatalogAttribute catalogAttribute = new CatalogAttribute(
				createCore(CatalogAttribute.class, catalogComponent.getContextType()));

		// relations
		getCore(catalogAttribute).getPermanentStore().set(catalogComponent);
		getCore(catalogComponent).getChildrenStore().add(catalogAttribute);

		// properties
		catalogAttribute.setName(name);
		catalogAttribute.setValueType(valueType);
		catalogAttribute.setSortIndex(nextIndex(catalogComponent.getCatalogAttributes()));

		return catalogAttribute;
	}

	/**
	 * Creates a new {@link CatalogAttribute} for given
	 * {@link CatalogComponent}.
	 *
	 * @param name
	 *            Name of the created {@code CatalogAttribute}.
	 * @param enumerationObject
	 *            The enumeration.
	 * @param catalogComponent
	 *            The parent {@code CatalogComponent}.
	 * @return The created {@code CatalogAttribute} is returned.
	 * @throws IllegalArgumentException
	 *             Thrown if given name is already in use or not allowed or
	 *             given enumeration class is not supported.
	 */
	public CatalogAttribute createCatalogAttribute(String name, Enumeration<?> enumerationObj,
			CatalogComponent catalogComponent) {
		validateCatalogName(name, true);
		validateEnum(enumerationObj);
		if (catalogComponent.getCatalogAttribute(name).isPresent()) {
			throw new IllegalArgumentException("Catalog attribute with name '" + name + "' already exists.");
		}

		CatalogAttribute catalogAttribute = new CatalogAttribute(
				createCore(CatalogAttribute.class, catalogComponent.getContextType()));

		// relations
		getCore(catalogAttribute).getPermanentStore().set(catalogComponent);
		getCore(catalogComponent).getChildrenStore().add(catalogAttribute);

		// properties
		catalogAttribute.setName(name);
		catalogAttribute.setEnumerationObj(enumerationObj);
		catalogAttribute.setSortIndex(nextIndex(catalogComponent.getCatalogAttributes()));

		return catalogAttribute;
	}
	
	/**
	 * Creates a new {@link CatalogAttribute} for given {@link CatalogSensor}. The
	 * {@link ValueType} may be one of the following like in
	 * {@link #createCatalogAttribute(String, ValueType, CatalogComponent)}:
	 *
	 * @param name
	 *            Name of the created {@code CatalogAttribute}.
	 * @param valueType
	 *            The {@code ValueType}.
	 * @param catalogSensor
	 *            The parent {@code CatalogSensor}.
	 * @return The created {@code CatalogAttribute} is returned.
	 * @throws IllegalArgumentException
	 *             Thrown if given name is already in use or not allowed or given
	 *             {@code ValueType} is not supported.
	 */
	public CatalogAttribute createCatalogSensorAttribute(String name, ValueType<?> valueType,
			CatalogSensor catalogSensor) {
		validateCatalogName(name, true);

		if (catalogSensor.getCatalogAttribute(name).isPresent()) {
			throw new IllegalArgumentException("CatalogSensor attribute with name '" + name + "' already exists.");
		} else if (valueType.isEnumerationType() || valueType.isByteStreamType() || valueType.isUnknown()
				|| valueType.isBlob()) {
			throw new IllegalArgumentException("Value type '" + valueType + "' is not allowed.");
		}

		CatalogAttribute catalogAttribute = new CatalogAttribute(
				createCore(CatalogAttribute.class));

		// relations
		getCore(catalogAttribute).getPermanentStore().set(catalogSensor);
		getCore(catalogSensor).getChildrenStore().add(catalogAttribute);

		// properties
		catalogAttribute.setName(name);
		catalogAttribute.setValueType(valueType);
		catalogAttribute.setSortIndex(nextIndex(catalogSensor.getCatalogAttributes()));

		return catalogAttribute;
	}

	/**
	 * Creates a new {@link CatalogSensor} for given {@link CatalogComponent}.
	 *
	 * @param name
	 *            Name of the created {@code CatalogSensor}.
	 * @param enumerationObject
	 *            The enumeration.
	 * @param catalogComponent
	 *            The parent {@code CatalogComponent}.
	 * @return The created {@code CatalogSensor} is returned.
	 * @throws IllegalArgumentException
	 *             Thrown if given name is already in use or not allowed
	 */
	public CatalogSensor createCatalogSensor(String name, CatalogComponent catalogComponent) {
		validateCatalogName(name, false);

		if (!catalogComponent.getContextType().isTestEquipment()) {
			throw new IllegalArgumentException("Catalog component is not of type 'TESTEQUIPMENT'");
		} else if (catalogComponent.getCatalogSensor(name).isPresent()) {
			throw new IllegalArgumentException("Catalog sensor with name '" + name + "' already exists.");
		}

		CatalogSensor catalogSensor = new CatalogSensor(createCore(CatalogSensor.class));

		// relations
		getPermanentStore(catalogSensor).set(catalogComponent);
		getChildrenStore(catalogComponent).add(catalogSensor);

		// properties
		catalogSensor.setName(name);
		catalogSensor.setDateCreated(LocalDateTime.now());

		return catalogSensor;
	}

	/**
	 * Creates a new {@link TemplateRoot} with given {@link ContextType} and
	 * name.
	 *
	 * @param contextType
	 *            The {@code ContextType}.
	 * @param name
	 *            Name of the created {@code TemplateRoot}.
	 * @return The created {@code TemplateRoot} is returned.
	 */
	public TemplateRoot createTemplateRoot(ContextType contextType, String name) {
		TemplateRoot templateRoot = new TemplateRoot(createCore(TemplateRoot.class, contextType));

		// properties
		templateRoot.setName(name);
		templateRoot.setDateCreated(LocalDateTime.now());
		templateRoot.setVersionState(VersionState.EDITABLE);
		templateRoot.setVersion(Integer.valueOf(1));

		return templateRoot;
	}

	/**
	 * Creates a new {@link TemplateComponent} for given {@link TemplateRoot}
	 * using given {@link CatalogComponent}.
	 *
	 * @param name
	 *            Name of the created {@code TemplateComponent}.
	 * @param templateRoot
	 *            The parent {@code TemplateRoot}.
	 * @param catalogComponent
	 *            The associated {@link CatalogComponent}.
	 * @return The created {@code TemplateComponent} is returned.
	 * @throws IllegalArgumentException
	 *             Thrown if {@code ContextType} of {@code
	 * 		TemplateRoot} and {@code CatalogComponent} do not match or given
	 *             name is already in use.
	 */
	public TemplateComponent createTemplateComponent(String name, TemplateRoot templateRoot,
			CatalogComponent catalogComponent) {
		if (!templateRoot.getContextType().equals(catalogComponent.getContextType())) {
			throw new IllegalArgumentException("Context type of template root and catalog component do not match.");
		} else if (templateRoot.getTemplateComponent(name).isPresent()) {
			throw new IllegalArgumentException("Template component with name '" + name + "' already exists.");
		}

		TemplateComponent templateComponent = new TemplateComponent(
				createCore(TemplateComponent.class, templateRoot.getContextType()));

		// relations
		getCore(templateComponent).getPermanentStore().set(templateRoot);
		getCore(templateComponent).getMutableStore().set(catalogComponent);
		getCore(templateRoot).getChildrenStore().add(templateComponent);

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

	/**
	 * Creates a new {@link TemplateComponent} for given
	 * {@link TemplateComponent} using given {@link CatalogComponent}.
	 *
	 * @param name
	 *            Name of the created {@code TemplateComponent}.
	 * @param partentComponentTemplate
	 *            The parent {@code TemplateComponent}.
	 * @param catalogComponent
	 *            The associated {@link CatalogComponent}.
	 * @return The created {@code TemplateComponent} is returned.
	 * @throws IllegalArgumentException
	 *             Thrown if {@code ContextType} of {@code
	 * 		TemplateComponent} and {@code CatalogComponent} do not match or
	 *             given name is already in use.
	 */
	public TemplateComponent createTemplateComponent(String name, TemplateComponent partentComponentTemplate,
			CatalogComponent catalogComponent) {
		TemplateRoot templateRoot = partentComponentTemplate.getTemplateRoot();
		if (!templateRoot.getContextType().equals(catalogComponent.getContextType())) {
			throw new IllegalArgumentException("Context type of template root and catalog component do not match.");
		} else if (templateRoot.getTemplateComponent(name).isPresent()) {
			throw new IllegalArgumentException("Template component with name '" + name + "' already exists.");
		}

		TemplateComponent templateComponent = new TemplateComponent(
				createCore(TemplateComponent.class, templateRoot.getContextType()));

		// relations
		getCore(templateComponent).getPermanentStore().set(partentComponentTemplate);
		getCore(templateComponent).getMutableStore().set(catalogComponent);
		getCore(partentComponentTemplate).getChildrenStore().add(templateComponent);

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

	/**
	 * Creates a new {@link TemplateAttribute} for given
	 * {@link TemplateComponent}.
	 *
	 * @param name
	 *            Name of the created {@code TemplateAttribute}.
	 * @param templateComponent
	 *            The parent {@code TemplateComponent}.
	 * @return The created {@code TemplateAttribute} is returned.
	 * @throws IllegalArgumentException
	 *             Thrown if given name is already in use.
	 */
	public TemplateAttribute createTemplateAttribute(String name, TemplateComponent templateComponent) {
		if (templateComponent.getTemplateAttribute(name).isPresent()) {
			throw new IllegalArgumentException("Template attribute with name '" + name + "' already exists.");
		}

		CatalogComponent catalogComponent = templateComponent.getCatalogComponent();
		Optional<CatalogAttribute> catalogAttribute = catalogComponent.getCatalogAttribute(name);
		if (catalogAttribute.isPresent()) {
			TemplateAttribute templateAttribute = new TemplateAttribute(
					createCore(TemplateAttribute.class, catalogComponent.getContextType()));

			// relations
			getCore(templateAttribute).getPermanentStore().set(templateComponent);
			getCore(templateAttribute).getMutableStore().set(catalogAttribute.get());
			getCore(templateComponent).getChildrenStore().add(templateAttribute);

			// properties
			templateAttribute.setName(name);
			templateAttribute.setValueReadOnly(Boolean.FALSE);
			templateAttribute.setOptional(Boolean.TRUE);

			return templateAttribute;
		}

		throw new IllegalArgumentException("Catalog attribute with name '" + name + "' does not exists.");
	}

	/**
	 * Creates a new {@link TemplateSensor} for given {@link TemplateComponent}
	 * based on the given {@link CatalogSensor}.
	 *
	 * @param name
	 *            Name of the created {@code TemplateAttribute}.
	 * @param templateComponent
	 *            The parent {@code TemplateComponent}.
	 * @param catalogSensor
	 *            reference CatalogSensor
	 * @param quantity
	 *            Quantity to create {@link TemplateSensor} for
	 * @return The created {@code TemplateSensor} is returned.
	 * @throws IllegalArgumentException
	 *             Thrown if given name is already in use.
	 */
	public TemplateSensor createTemplateSensor(String name, TemplateComponent templateComponent,
			CatalogSensor catalogSensor, Quantity quantity) {
		if (templateComponent.getTemplateSensor(name)
				.isPresent()) {
			throw new IllegalArgumentException("Template sensor with name '" + name + "' already exists.");
		}

		if (catalogSensor != null) {
			TemplateSensor templateSensor = new TemplateSensor(createCore(TemplateSensor.class));
			// create all implicit TemplateAttributes
			for (CatalogAttribute catAttr : catalogSensor.getCatalogAttributes()) {
				TemplateAttribute tplAttr = new TemplateAttribute(createCore(TemplateAttribute.class));
				tplAttr.setName(catAttr.getName());
				tplAttr.setValueReadOnly(Boolean.FALSE);
				tplAttr.setOptional(Boolean.TRUE);
				getPermanentStore(tplAttr).set(templateSensor);
				getMutableStore(tplAttr).set(catAttr);
				getCore(templateSensor).getChildrenStore()
						.add(tplAttr);
			}

			// relations
			getPermanentStore(templateSensor).set(templateComponent);
			getMutableStore(templateSensor).set(catalogSensor);
			getMutableStore(templateSensor).set(quantity);
			getChildrenStore(templateComponent).add(templateSensor);

			// properties
			templateSensor.setName(name);
			templateSensor.setDefaultActive(true);
			templateSensor.setOptional(Boolean.TRUE);

			return templateSensor;
		}

		throw new IllegalArgumentException("Catalog attribute with name '" + name + "' does not exists.");
	}

	/**
	 * Creates a new {@link TemplateTestStep}.
	 *
	 * @param name
	 *            Name of the created {@code TemplateTestStep}.
	 * @return The created {@code TemplateTestStep} is returned.
	 */
	public TemplateTestStep createTemplateTestStep(String name) {
		TemplateTestStep templateTestStep = new TemplateTestStep(createCore(TemplateTestStep.class));

		// properties
		templateTestStep.setName(name);
		templateTestStep.setDateCreated(LocalDateTime.now());
		templateTestStep.setVersionState(VersionState.EDITABLE);
		templateTestStep.setVersion(Integer.valueOf(1));

		return templateTestStep;
	}

	/**
	 * Creates a new {@link TemplateTest}.
	 *
	 * @param name
	 *            Name of the created {@code TemplateTest}.
	 * @return The created {@code TemplateTest} is returned.
	 */
	public TemplateTest createTemplateTest(String name) {
		TemplateTest templateTest = new TemplateTest(createCore(TemplateTest.class));

		// properties
		templateTest.setName(name);
		templateTest.setDateCreated(LocalDateTime.now());
		templateTest.setVersion(Integer.valueOf(1));
		templateTest.setVersionState(VersionState.EDITABLE);

		return templateTest;
	}

	/**
	 * Creates a new {@link TemplateTestStepUsage} for given
	 * {@link TemplateTest} using given {@link TemplateTestStep}.
	 *
	 * @param name
	 *            Name of the created {@code TemplateTestStepUsage}.
	 * @param templateTest
	 *            The parent {@link TemplateTest}.
	 * @param templateTestStep
	 *            The related {@link TemplateTestStep}.
	 * @return The created {@code TemplateTestStepUsage} is returned.
	 */
	public TemplateTestStepUsage createTemplateTestStepUsage(String name, TemplateTest templateTest,
			TemplateTestStep templateTestStep) {
		if (templateTest.getTemplateTestStepUsage(name).isPresent()) {
			throw new IllegalArgumentException("Template test step usage with name '" + name + "' already exists.");
		}

		TemplateTestStepUsage templateTestStepUsage = new TemplateTestStepUsage(
				createCore(TemplateTestStepUsage.class));

		// relations
		getCore(templateTestStepUsage).getPermanentStore().set(templateTest);
		getCore(templateTestStepUsage).getMutableStore().set(templateTestStep);
		getCore(templateTest).getChildrenStore().add(templateTestStepUsage);

		// properties
		templateTestStepUsage.setName(name);
		templateTestStepUsage.setOptional(Boolean.TRUE);
		templateTestStepUsage.setDefaultActive(Boolean.TRUE);
		templateTestStepUsage.setSortIndex(nextIndex(templateTest.getTemplateTestStepUsages()));

		return templateTestStepUsage;
	}

	/**
	 * Creates a new {@link ValueList}.
	 *
	 * @param name
	 *            Name of the created {@code ValueList}.
	 * @return The created {@code ValueList} is returned.
	 */
	public ValueList createValueList(String name) {
		ValueList valueList = new ValueList(createCore(ValueList.class));

		// properties
		valueList.setName(name);
		valueList.setDateCreated(LocalDateTime.now());

		return valueList;
	}

	/**
	 * Creates a new {@link ValueListValue} for given {@link ValueList}.
	 *
	 * @param name
	 *            Name of the created {@code ValueListValue}.
	 * @param valueList
	 *            The parent {@code ValueList}.
	 * @return The created {@code ValueListValue} is returned.
	 */
	public ValueListValue createValueListValue(String name, ValueList valueList) {
		if (valueList.getValueListValue(name).isPresent()) {
			throw new IllegalArgumentException("Value list value with name '" + name + "' already exists.");
		}

		ValueListValue valueListValue = new ValueListValue(createCore(ValueListValue.class));

		// relations
		getCore(valueListValue).getPermanentStore().set(valueList);
		getCore(valueList).getChildrenStore().add(valueListValue);

		// properties
		valueListValue.setName(name);
		valueListValue.setSortIndex(nextIndex(valueList.getValueListValues()));

		// this property is hidden by the public API and is not allowed to be
		// modified!
		valueListValue.getValue(ValueListValue.ATTR_SCALAR_TYPE).set(ScalarType.STRING);

		return valueListValue;
	}

	// ======================================================================
	// Protected methods
	// ======================================================================

	/**
	 * Creates a new {@link Test} for given {@link Pool} using given
	 * {@link TemplateTest}.
	 *
	 * @param name
	 *            Name of the created {@code Test}.
	 * @param pool
	 *            The parent {@code Pool}.
	 * @param statusTest
	 *            The related {@link Status} of the created {@code
	 * 		Test}.
	 * @param statusTestStep
	 *            The related {@link Status} of the created {@code
	 * 		TestStep}.
	 * @param templateTest
	 *            The template the returned {@code Test} will be derived from.
	 * @return The created {@code Test} is returned.
	 */
	// TODO make a decision: status in or out!
	protected Test createTest(String name, Pool pool, Status statusTest, Status statusTestStep,
			TemplateTest templateTest) {
		Test test = createTest(name, pool, statusTest);

		// relations
		getCore(test).getMutableStore().set(templateTest);

		// create default active and mandatory test steps according to the
		// template
		templateTest.getTemplateTestStepUsages().stream().filter(TemplateTestStepUsage.IS_IMPLICIT_CREATE)
				.map(TemplateTestStepUsage::getTemplateTestStep).forEach(templateTestStep -> {
					createTestStep(test, statusTestStep, templateTestStep);
				});

		return test;
	}

	/**
	 * Creates a new {@link Test} for given {@link Pool}.
	 *
	 * @param name
	 *            Name of the created {@code Test}.
	 * @param pool
	 *            The parent {@code Pool}.
	 * @param status
	 *            The related {@link Status}.
	 * @return The created {@code Test} is returned.
	 */
	// TODO make a decision: status in or out!
	protected Test createTest(String name, Pool pool, Status status) {
		Test test = super.createTest(name);

		// relations
		getCore(test).getPermanentStore().set(pool);
		getCore(pool).getChildrenStore().add(test);

		if (status != null) {
			status.assign(test);
		}

		return test;
	}

	/**
	 * Creates a new {@link TestStep} for given {@link Test} using given
	 * {@link TemplateTestStep}.
	 *
	 * @param test
	 *            The parent {@code Test}.
	 * @param templateTestStep
	 *            The template the returned {@code TestStep} will be derived
	 *            from.
	 * @return The created {@code TestStep} is returned.
	 */
	protected TestStep createTestStep(Test test, TemplateTestStep templateTestStep) {
		return createTestStep(test, null, templateTestStep);
	}

	/**
	 * Creates a new {@link TestStep} for given {@link Test} using given
	 * {@link TemplateTestStep}.
	 *
	 * @param test
	 *            The parent {@code Test}.
	 * @param status
	 *            The related {@link Status}.
	 * @param templateTestStep
	 *            The template the returned {@code TestStep} will be derived
	 *            from.
	 * @return The created {@code TestStep} is returned.
	 */
	// TODO make a decision: status in or out!
	protected TestStep createTestStep(Test test, Status status, TemplateTestStep templateTestStep) {
		TemplateTest templateTest = TemplateTest.of(test)
				.orElseThrow(() -> new IllegalArgumentException("Template test is not available."));
		if (!templateTest.contains(templateTestStep)) {
			throw new IllegalArgumentException("Template test step is part of the test template.");
		}

		TestStep testStep = createTestStep(templateTestStep.getName(), test, status);

		// relations
		getCore(testStep).getMutableStore().set(templateTestStep);

		// create initial context roots
		templateTestStep.getTemplateRoots().forEach(templateRoot -> createContextRoot(testStep, templateRoot));

		return testStep;
	}

	/**
	 * Creates a new {@link TestStep} for given {@link Test}.
	 *
	 * @param name
	 *            Name of the created {@code Test}.
	 * @param test
	 *            The parent {@code Test}.
	 * @param status
	 *            The related {@link Status}.
	 * @return The created {@code TestStep} is returned.
	 */
	// TODO make a decision: status in or out!
	protected TestStep createTestStep(String name, Test test, Status status) {
		TestStep testStep = super.createTestStep(name, test);

		if (status != null) {
			status.assign(testStep);
		}

		return testStep;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected <T extends BaseEntity> T createBaseEntity(Class<T> clazz, Core core) {
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor(Core.class);
			try {
				return constructor.newInstance(core);
			} catch (IllegalAccessException exc) {
				return super.createBaseEntity(clazz, core);
			}
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException exc) {
			throw new IllegalStateException(exc.getMessage(), exc);
		}
	}

	/**
	 * Checks whether given enumeration is defined in the application
	 * model or not.
	 *
	 * @param enumerationObj
	 *            The checked enumeration class.
	 * @throws IllegalArgumentException
	 *             Thrown if given enumeration class is not supported.
	 */
	protected abstract void validateEnum(Enumeration<?> enumerationObj);

	// ======================================================================
	// Private methods
	// ======================================================================

	/**
	 * Checks whether given catalog name is allowed or not.
	 *
	 * @param name
	 *            The checked name.
	 * @param isAttributeName
	 *            Flag indicates whether given name is for a catalog attribute.
	 * @throws IllegalArgumentException
	 *             Thrown if given name is not allowed.
	 */
	private static void validateCatalogName(String name, boolean isAttributeName) {
		if (!isValidCatalogName(name)) {
			throw new IllegalArgumentException(
					"A catalog name is not allowed to be empty and " + "must not exceed 30 characters.");
		} else if (name.toLowerCase(Locale.ROOT).startsWith("ao")) {
			throw new IllegalArgumentException("A catalog name is not allowed to " + "start with 'ao' (case ignored).");
		} else if (!name.matches("^[\\w]+$")) {
			throw new IllegalArgumentException(
					"A calatog name may only constists of the " + "following characters: a-z, A-Z, 0-9 or _.");
		} else if (isAttributeName && Arrays.asList("id", "name", "mimetype").contains(name.toLowerCase(Locale.ROOT))) {
			throw new IllegalArgumentException(
					"A catalog attribute name is not allowed to be " + "'id', 'name' or 'mimetype' (case ignored).");
		}
	}
	
	/**
	 * Checks whether given catalog name is valid
	 *
	 * @param name
	 *            The checked name.
	 * @return Returns {@code true} if name is a valid catalog name
	 */
	private static boolean isValidCatalogName(String name) {
		return name != null && !name.isEmpty() && name.length() <= 30;
	}

	/**
	 * Hides {@link Value} containers missing in the templates.
	 *
	 * @param contextCore
	 *            The {@link ContextComponent} {@link Core}.
	 * @param templateAttributes
	 *            The {@link TemplateAttribute}s of the template.
	 */
	private static void hideValues(Core contextCore, List<TemplateAttribute> templateAttributes) {
		Set<String> names = new HashSet<>(contextCore.getValues().keySet());
		names.remove(Entity.ATTR_NAME);
		names.remove(Entity.ATTR_MIMETYPE);
		templateAttributes.stream().map(Entity::getName).forEach(names::remove);
		contextCore.hideValues(names);
	}

}
