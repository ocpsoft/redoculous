/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service;

import java.util.Set;

import org.ocpsoft.redoculous.model.Ref;
import org.ocpsoft.redoculous.model.Repository;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface RepositoryService
{
   Repository getRepository(String repository);

   Set<Ref> getRefs(Repository repository);

   Ref getRef(Repository repository, String ref);

   String getRenderedPath(org.ocpsoft.redoculous.model.Ref reff, String path);
}
