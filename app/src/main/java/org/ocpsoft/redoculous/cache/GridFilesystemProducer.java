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
import org.infinispan.io.GridFile.Metadata;
import org.infinispan.io.GridFilesystem;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GridFilesystemProducer
{
   private static final String REPO_CACHE_FILESYSTEM = "repo.cache.filesystem";
   private static final String REPO_CACHE_METADATA = "repo.cache.metadata";

   @Inject
   private EmbeddedCacheManager cacheManager;

   @Produces
   @Singleton
   public GridFilesystem getGridFilesystem()
   {
      if (!cacheManager.cacheExists(REPO_CACHE_FILESYSTEM))
      {
         cacheManager.defineConfiguration(REPO_CACHE_FILESYSTEM, new ConfigurationBuilder()
                  .clustering().cacheMode(CacheMode.DIST_ASYNC).build());
      }
      if (!cacheManager.cacheExists(REPO_CACHE_METADATA))
      {
         cacheManager.defineConfiguration(REPO_CACHE_METADATA, new ConfigurationBuilder()
                  .clustering().cacheMode(CacheMode.REPL_SYNC).build());
      }

      Cache<String, byte[]> fsCacheData = cacheManager.getCache(REPO_CACHE_FILESYSTEM, true);
      Cache<String, Metadata> fsCacheMetadata = cacheManager.getCache(REPO_CACHE_METADATA, true);
      return new GridFilesystem(fsCacheData, fsCacheMetadata);
   }
}
