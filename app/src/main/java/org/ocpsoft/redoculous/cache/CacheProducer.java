/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.cache;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
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
   @Singleton
   @RepositoryCache
   public Cache<String, Repository> getRepositoryCache()
   {
      if (!cacheManager.cacheExists(REPOSITORY_CACHE))
      {
         cacheManager.defineConfiguration(REPOSITORY_CACHE, new ConfigurationBuilder()
                  .clustering().cacheMode(CacheMode.REPL_ASYNC).build());
      }

      Cache<String, Repository> result = cacheManager.getCache(REPOSITORY_CACHE, true);
      return result;
   }
}
