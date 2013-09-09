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
   public Repository getCachedRepository(String url)
   {
      String key = Keys.repository(url);
      Repository result = (Repository) repositoryCache.get(key);
      if (result == null)
      {
         Repository localRepo = getLocalRepository(url);
         localRepo.init();

         result = new GitRepository(gfs.getFile("/"), url);
         io.copyDirectoryToGrid(gfs, localRepo.getBaseDir(), result.getBaseDir());
         repositoryCache.putIfAbsentAsync(key, result);
      }
      return result;
   }

   @Override
   public File getRenderedPath(String repoUrl, String ref, String path)
   {
      Repository cachedRepo = getCachedRepository(repoUrl);
      if (!cachedRepo.getCachedRefDir(ref).exists())
      {
         if (!cachedRepo.getRefDir(ref).exists())
         {
            Repository localRepo = getLocalRepository(repoUrl);
            if (!localRepo.getBaseDir().exists())
            {
               io.copyDirectoryFromGrid(gfs, cachedRepo.getRepoDir(), localRepo.getRepoDir());
            }
            if (!localRepo.getRefDir(ref).exists())
            {
               localRepo.initRef(ref);
               io.copyDirectoryFromGrid(gfs, localRepo.getRefDir(ref), cachedRepo.getRefDir(ref));
            }
         }
      }

      File result = render.resolveRendered(cachedRepo, ref, path);
      return result;
   }

   @Override
   public Repository getLocalRepository(String url)
   {
      return new GitRepository(Redoculous.getRoot(), url);
   }

}
