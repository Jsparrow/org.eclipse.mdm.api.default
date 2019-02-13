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

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.mdm.api.base.adapter.Core;
import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.DoubleComplex;
import org.eclipse.mdm.api.base.model.Enumeration;
import org.eclipse.mdm.api.base.model.FileLink;
import org.eclipse.mdm.api.base.model.FloatComplex;
import org.eclipse.mdm.api.base.model.MimeType;
import org.eclipse.mdm.api.base.model.Value;
import org.eclipse.mdm.api.base.model.ValueType;

/**
 * Implementation of the template attribute entity type. A template attribute
 * adds meta data to a {@link CatalogAttribute} it is associated with. It always
 * belongs to a {@link TemplateComponent} or a {@link TemplateSensor}. Its name
 * is the same as the name of the associated {@code CatalogAttribute} and is not
 * allowed to be modified at all.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see TemplateComponent
 * @see TemplateSensor
 */
public class TemplateAttribute extends BaseEntity implements Deletable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * This {@code Comparator} compares {@link TemplateAttribute}s by the sort
	 * index of their corresponding {@link CatalogAttribute} in ascending order.
	 */
	public static final Comparator<TemplateAttribute> COMPARATOR = Comparator
			.comparing(ta -> ta.getCatalogAttribute().getSortIndex());

	/**
	 * The 'DefaultValue' attribute name.
	 */
	public static final String ATTR_DEFAULT_VALUE = "DefaultValue";

	/**
	 * The 'ValueReadOnly' attribute name.
	 */
	public static final String ATTR_VALUE_READONLY = "ValueReadonly";

	/**
	 * The 'Optional' attribute name.
	 */
	public static final String ATTR_OPTIONAL = "Obligatory";

	private static final Map<ValueType<?>, Function<String, Object>> VALUETYPE_FUNCTION_MAP = new HashMap<>();

	static {
		VALUETYPE_FUNCTION_MAP.put(ValueType.STRING, v -> v);
		VALUETYPE_FUNCTION_MAP.put(ValueType.DATE, v -> LocalDateTime.parse(v, Value.LOCAL_DATE_TIME_FORMATTER));
		VALUETYPE_FUNCTION_MAP.put(ValueType.BOOLEAN, Boolean::valueOf);
		VALUETYPE_FUNCTION_MAP.put(ValueType.BYTE, Byte::valueOf);
		VALUETYPE_FUNCTION_MAP.put(ValueType.SHORT, Short::valueOf);
		VALUETYPE_FUNCTION_MAP.put(ValueType.INTEGER, Integer::valueOf);
		VALUETYPE_FUNCTION_MAP.put(ValueType.LONG, Long::valueOf);
		VALUETYPE_FUNCTION_MAP.put(ValueType.FLOAT, Float::valueOf);
		VALUETYPE_FUNCTION_MAP.put(ValueType.DOUBLE, Double::valueOf);
		VALUETYPE_FUNCTION_MAP.put(ValueType.FLOAT_COMPLEX, FloatComplex::valueOf);
		VALUETYPE_FUNCTION_MAP.put(ValueType.DOUBLE_COMPLEX, DoubleComplex::valueOf);
	}

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	TemplateAttribute(Core core) {
		super(core);
	}

	// ======================================================================
	// Public methods
	// ======================================================================

	/**
	 * Returns the default {@link Value} of this template attribute.
	 *
	 * @return The default {@code Value} is returned.
	 */
	@SuppressWarnings({ "rawtypes" })
	public Value getDefaultValue() {
		ValueType valueType = getCatalogAttribute().getValueType();
		Value defaultValue = getValue(ATTR_DEFAULT_VALUE);
		boolean isValid = defaultValue.isValid();
		String value = defaultValue.extract();
		if (valueType.isEnumerationType()) {
			Enumeration enumObject = getCatalogAttribute().getEnumerationObject();
			return valueType.create(getName(), "", isValid, isValid ? enumObject.valueOf(value) : null,
					enumObject.getName());
		} else {
			return valueType.create(getName(), isValid ? parse(value, valueType) : null);
		}
	}

	/**
	 * Sets a new default value for this template attribute. Given input will be
	 * stored in its {@link String} representation.
	 *
	 * @param input
	 *            The new default value.
	 */
	public void setDefaultValue(Object input) {
		if (input == null) {
			getValue(ATTR_DEFAULT_VALUE).set(null);
			return;
		}

		ValueType<?> valueType = getCatalogAttribute().getValueType();
		boolean sequence = valueType.isSequence();

		// if this passes -> input is valid
		Value value = valueType.create("notRelevant", input);

		String stringValue;
		if (valueType.isFileLinkType()) {
			stringValue = getStringValueForFileLinkType(value, sequence);
		} else if (valueType.isDateType()) {
			LocalDateTime[] values = sequence ? value.extract() : new LocalDateTime[] { value.extract() };
			stringValue = Stream.of(values).map(ldt -> ldt.format(Value.LOCAL_DATE_TIME_FORMATTER))
					.collect(Collectors.joining(","));
		} else {
			if (input.getClass().isArray()) {
				stringValue = IntStream.range(0, Array.getLength(input)).mapToObj(i -> Array.get(input, i).toString())
						.collect(Collectors.joining(","));
			} else {
				stringValue = value.extract().toString();
			}
		}

		getValue(ATTR_DEFAULT_VALUE).set(stringValue);
	}

	/**
	 * Returns the value read only flag of this template attribute.
	 *
	 * @return Returns {@code true} if it is not allowed to modify
	 *         {@link Value}s derived from this template attribute.
	 */
	public Boolean isValueReadOnly() {
		return getValue(ATTR_VALUE_READONLY).extract();
	}

	/**
	 * Sets a new value read only flag for this template attribute.
	 *
	 * @param valueReadOnly
	 *            The new value read only flag.
	 */
	public void setValueReadOnly(Boolean valueReadOnly) {
		getValue(ATTR_VALUE_READONLY).set(valueReadOnly);
	}

	/**
	 * Returns the optional flag of this template attribute.
	 *
	 * @return Returns {@code true} if it is allowed to omit a {@link Value}
	 *         derived from this template attribute.
	 */
	public Boolean isOptional() {
		boolean mandatory = getValue(ATTR_OPTIONAL).extract();
		return mandatory ? Boolean.FALSE : Boolean.TRUE;
	}

	/**
	 * Sets a new optional flag for this template attribute.
	 *
	 * @param optional
	 *            The new optional flag.
	 */
	public void setOptional(Boolean optional) {
		getValue(ATTR_OPTIONAL).set(optional ? Boolean.FALSE : Boolean.TRUE);
	}

	/**
	 * Returns the {@link CatalogAttribute} this template attribute is
	 * associated with.
	 *
	 * @return The associated {@code CatalogAttribute} is returned.
	 */
	public CatalogAttribute getCatalogAttribute() {
		return getCore().getMutableStore().get(CatalogAttribute.class);
	}

	/**
	 * Returns the {@link TemplateRoot} this template attribute belongs to.
	 *
	 * @return The {@code TemplateRoot} is returned.
	 */
	public TemplateRoot getTemplateRoot() {
		Optional<TemplateComponent> templateComponent = getTemplateComponent();
		Optional<TemplateSensor> templateSensor = getTemplateSensor();
		if (templateComponent.isPresent()) {
			return templateComponent.get().getTemplateRoot();
		} else if (templateSensor.isPresent()) {
			return templateSensor.get().getTemplateRoot();
		} else {
			throw new IllegalStateException("Parent entity is unknown.");
		}
	}

	/**
	 * Returns the parent {@link TemplateComponent} of this template attribute.
	 *
	 * @return {@code Optional} is empty if a {@link TemplateSensor} is parent
	 *         of this template attribute.
	 * @see #getTemplateSensor()
	 */
	public Optional<TemplateComponent> getTemplateComponent() {
		return Optional.ofNullable(getCore().getPermanentStore().get(TemplateComponent.class));
	}

	/**
	 * Returns the parent {@link TemplateSensor} of this template attribute.
	 *
	 * @return {@code Optional} is empty if a {@link TemplateComponent} is
	 *         parent of this template attribute.
	 * @see #getTemplateComponent()
	 */
	public Optional<TemplateSensor> getTemplateSensor() {
		return Optional.ofNullable(getCore().getPermanentStore().get(TemplateSensor.class));
	}

	// ======================================================================
	// Private methods
	// ======================================================================

	/**
	 * Parses given {@code String} to the corresponding type of given
	 * {@link ValueType}.
	 *
	 * @param value
	 *            The {@code String} value.
	 * @param valueType
	 *            Used to resolve the corresponding converter.
	 * @return The parsed object is returned.
	 */
	private static Object parse(String value, ValueType<?> valueType) {
		if (valueType.isFileLinkType()) {
			Pattern pattern = Pattern.compile("([^,].*?)\\[(.*?),(.*?)\\]");
			Matcher matcher = pattern.matcher(value);
			List<FileLink> fileLinks = new ArrayList<>();
			while (matcher.find()) {
				fileLinks.add(FileLinkParser.parse(matcher.group()));
			}

			return valueType.isSequence() ? fileLinks.toArray(new FileLink[fileLinks.size()]) : fileLinks.get(0);
		} else {
			Function<String, Object> converter = getParser(valueType);
			if (valueType.isSequence()) {
				List<Object> values = Stream.of(value.split(",")).map(converter).collect(Collectors.toList());
				Object array = Array.newInstance(valueType.getValueClass().getComponentType(), values.size());

				if (valueType.getValueClass().getComponentType().isPrimitive()) {
					IntStream.range(0, values.size()).forEach(i -> Array.set(array, i, values.get(i)));
				} else {
					values.toArray((Object[]) array);
				}

				return array;
			} else {
				return converter.apply(value);
			}
		}
	}

	/**
	 * Returns the {@code String} conversion function for given
	 * {@link ValueType}.
	 *
	 * @param valueType
	 *            Used as identifier.
	 * @return The {@code String} conversion {@code Function} is returned.
	 * @throws IllegalArgumentException
	 *             Thrown if a corresponding {@code String} is not supported.
	 */
	private static Function<String, Object> getParser(ValueType<?> valueType) {
		Function<String, Object> converter = VALUETYPE_FUNCTION_MAP.get(valueType);
		if (converter == null) {
			throw new IllegalArgumentException(new StringBuilder().append("String conversion for value type '").append(valueType).append("' not supported.").toString());
		}
		return converter;
	}

	// ======================================================================
	// Inner classes
	// ======================================================================

	/**
	 * Utility class restore a {@link FileLink} from given {@code String}.
	 */
	private static final class FileLinkParser {

		// ======================================================================
		// Class variables
		// ======================================================================

		// markers
		private static final String NO_DESC_MARKER = "NO_DESC#";
		private static final String LOCAL_MARKER = "LOCALPATH#";

		// pattern group names
		private static final String DESCRIPTION = "description";
		private static final String MIMETYPE = "mimetype";
		private static final String PATH = "path";

		// pattern
		private static final Pattern FILE_LINK_PATTERN = Pattern
				.compile(new StringBuilder().append("(?<").append(DESCRIPTION).append(">.*?)\\[(?<").append(MIMETYPE).append(">.*?),(?<").append(PATH)
						.append(">.*?)\\]").toString());

		// ======================================================================
		// Public methods
		// ======================================================================

		/**
		 * Parses given {@code String} and returns it as {@link FileLink}.
		 *
		 * @param value
		 *            The {@code String} value which will be parsed.
		 * @return The corresponding {@code FileLink} is returned.
		 */
		public static FileLink parse(String value) {
			Matcher matcher = FILE_LINK_PATTERN.matcher(value);
			if (!matcher.find()) {
				throw new IllegalStateException("Unable to restore file link.");
			}
			String description = matcher.group(DESCRIPTION);
			String path = matcher.group(PATH);
			FileLink fileLink;
			if (path.startsWith(LOCAL_MARKER)) {
				try {
					fileLink = FileLink.newLocal(Paths.get(path.replaceFirst(LOCAL_MARKER, "")));
				} catch (IOException e) {
					throw new IllegalStateException("Unable to restore local file link.", e);
				}
			} else {
				fileLink = FileLink.newRemote(path, new MimeType(matcher.group(MIMETYPE)), description);
			}

			fileLink.setDescription(NO_DESC_MARKER.equals(description) ? null : description);
			return fileLink;
		}

	}

	/**
	 * Get string value for FileLinkType
	 *
	 * @param value
	 *            The {@code Value} value.
	 * @param sequence
	 *            The {@code boolean} sequence.
	 * @return Return {@code String} string value for FileLinkType
	 */
	private String getStringValueForFileLinkType(Value value, boolean sequence) {
		FileLink[] values = sequence ? value.extract() : new FileLink[] { value.extract() };
		return Stream.of(values).map(fl -> {
			StringBuilder sb = new StringBuilder();
			if (fl.getDescription().isEmpty()) {
				sb.append(FileLinkParser.NO_DESC_MARKER);
			} else {
				sb.append(fl.getDescription());
			}
			sb.append('[').append(fl.getMimeType()).append(',');
			if (fl.isRemote()) {
				sb.append(fl.getRemotePath());
			} else if (fl.isLocal()) {
				sb.append(FileLinkParser.LOCAL_MARKER).append(fl.getLocalPath());
			} else {
				throw new IllegalStateException("File link is neither in local nor remote state: " + fl);
			}

			return sb.append(']');
		}).collect(Collectors.joining(","));
	}
}
