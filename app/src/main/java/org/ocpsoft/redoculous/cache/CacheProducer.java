/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.cache;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.infinispan.Cache;
import org.infinispan.io.GridFile.Metadata;
import org.infinispan.io.GridFilesystem;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class CacheProducer
{
   @Inject
   private EmbeddedCacheManager cacheManager;

   public static final String DEFAULT = "default";
   private static final String REPO_CACHE_LOCK = "grid.lock";
   private static final String REPO_CACHE_FILESYSTEM = "filesystem.content";
   private static final String REPO_CACHE_METADATA = "filesystem.metadata";

   @Produces
   @Singleton
   public GridFilesystem getGridFilesystem()
   {
      Cache<String, byte[]> fsCacheData = cacheManager.<String, byte[]>
               getCache(REPO_CACHE_FILESYSTEM)
               .getAdvancedCache()
               .with(CacheProducer.class.getClassLoader());
      Cache<String, Metadata> fsCacheMetadata = cacheManager.<String, Metadata>
               getCache(REPO_CACHE_METADATA)
               .getAdvancedCache()
               .with(CacheProducer.class.getClassLoader());
      return new GridFilesystem(fsCacheData, fsCacheMetadata);
   }

   @Produces
   public GridLock getGridLock()
   {
      return new GridLock(cacheManager.<String, Object> getCache(REPO_CACHE_LOCK).getAdvancedCache());
   }

   @Produces
   @Named(DEFAULT)
   public Cache<Object, Object> getDefaultCache()
   {
      return cacheManager.<Object, Object>
               getCache(DEFAULT)
               .getAdvancedCache()
               .with(CacheProducer.class.getClassLoader());
   }
}
