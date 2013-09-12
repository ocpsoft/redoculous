/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service;

import org.ocpsoft.redoculous.model.Repository;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface RepositoryService
{
   Repository getLocalRepository(String url);

   Repository getGridRepository(String url);

   String getRenderedContent(String repository, String ref, String path);

   Repository updateRepository(String repo);
}
