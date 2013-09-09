/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import java.io.File;

import javax.inject.Inject;

import org.infinispan.Cache;
import org.infinispan.io.GridFilesystem;
import org.ocpsoft.redoculous.Redoculous;
import org.ocpsoft.redoculous.cache.Keys;
import org.ocpsoft.redoculous.cache.RepositoryCache;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.model.impl.GitRepository;
import org.ocpsoft.redoculous.service.RepositoryService;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class RepositoryServiceImpl implements RepositoryService
{
   @Inject
   @RepositoryCache
   private Cache<String, Repository> repositoryCache;

   @Inject
   private FileOperations io;

   @Inject
   private GridFilesystem gfs;

   @Inject
   private RenderService render;

   @Override
   public Repository getLocalRepository(String url)
   {
      String key = Keys.repository(url);
      Repository result = (Repository) repositoryCache.get(key);
      if (result == null)
      {
         Repository localRepo = new GitRepository(Redoculous.getRoot(), url);
         localRepo.init();

         io.copyDirectoryAsync(localRepo.getBaseDir(), gfs.getFile(localRepo.getBaseDir().getAbsolutePath()));

         repositoryCache.putIfAbsentAsync(key, localRepo);
      }
      return result;
   }

   @Override
   public File getRenderedPath(String repoUrl, String ref, String path)
   {
      Repository cachedRepo = getCachedRepository(repoUrl);
      File result = render.resolveRendered(cachedRepo, ref, path);
      if (result == null)
      {
         Repository localRepo = getLocalRepository(repoUrl);
         File file = gfs.getFile(localRepo.getBaseDir().getAbsolutePath());
      }
      return result;
   }

   private Repository getCachedRepository(String url)
   {
      return new GitRepository(gfs.getFile(Redoculous.getRoot().getAbsolutePath()), url);
   }

}
