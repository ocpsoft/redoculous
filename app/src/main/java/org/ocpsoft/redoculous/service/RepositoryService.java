/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service;

import java.util.Set;

import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.rest.model.RepositoryStatus;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface RepositoryService
{
   String getRenderedContent(String namespace, String repo, String ref, String path);

   void updateRepository(String namespace, String repo);

   void purgeRepository(String namespace, String repo);

   void initRepository(String namespace, String repo);

   Repository getRepository(String namespace, String repo);

   Set<String> getRepositoryRefs(String namespace, String repo);

   RepositoryStatus getStatus(String namespace, String repo);
}
