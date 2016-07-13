package org.eclipse.mdm.api.dflt.model;

import org.eclipse.mdm.api.base.model.BaseEntity;
import org.eclipse.mdm.api.base.model.Core;
import org.eclipse.mdm.api.base.model.Deletable;
import org.eclipse.mdm.api.base.model.Test;

public class Pool extends BaseEntity implements Deletable {

	public static final Class<Project> PARENT_TYPE_PROJECT = Project.class;	
	public static final Class<Test> CHILD_TYPE_TEST= Test.class;
	
	protected Pool(Core core) {
		super(core);	
	}
	
	

}
