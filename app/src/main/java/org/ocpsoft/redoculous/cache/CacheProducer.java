/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.cache;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.ocpsoft.redoculous.model.Repository;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class CacheProducer
{
   private static final String REPOSITORY_CACHE = "repository.cache";

   @Inject
   private EmbeddedCacheManager cacheManager;

   @Produces
   @RepositoryCache
   public Cache<String, Repository> getRepositoryCache()
   {
      Cache<String, Repository> result = cacheManager.getCache(REPOSITORY_CACHE);
      return result;
   }
}
