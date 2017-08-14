package org.eclipse.mdm.api.dflt.model;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;

/**
 * Implementation of the project entity type. The project is the top level of
 * stored measurement data. Its name may be freely chosen but has to to be
 * unique. A project contains {@link Pool} entities as its children.
 *
 * @since 1.0.0
 * @author Viktor Stoehr, Gigatronik Ingolstadt GmbH
 * @see Pool
 */
public class Project extends BaseEntity implements Deletable {

	// ======================================================================
	// Class variables
	// ======================================================================

	/**
	 * The {@link Pool} child type.
	 */
	public static final Class<Pool> CHILD_TYPE_POOL = Pool.class;

	// ======================================================================
	// Constructors
	// ======================================================================

	/**
	 * Constructor.
	 *
	 * @param core
	 *            The {@link Core}.
	 */
	Project(Core core) {
		super(core);
	}

}
