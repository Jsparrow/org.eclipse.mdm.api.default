package org.eclipse.mdm.api.dflt.model;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;

public final class Project extends BaseEntity implements Deletable {

	public static final Class<Pool> CHILD_TYPE_POOL = Pool.class;

	Project(Core core) {
		super(core);
	}

}
