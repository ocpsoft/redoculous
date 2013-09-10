/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import javax.inject.Inject;

import org.infinispan.Cache;
import org.infinispan.io.GridFilesystem;
import org.ocpsoft.redoculous.Redoculous;
import org.ocpsoft.redoculous.cache.Keys;
import org.ocpsoft.redoculous.cache.RepositoryCache;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.model.impl.GitRepository;
import org.ocpsoft.redoculous.service.RepositoryService;
import org.ocpsoft.redoculous.util.Files;

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
   public String getRenderedContent(String url, String ref, String path)
   {
      if (path.startsWith("/"))
         path = path.substring(1);

      Repository cachedRepo = getCachedRepository(url);
      if (cachedRepo == null)
      {
         cachedRepo = primeRepository(url);
      }

      if (!cachedRepo.getCachedRefDir(ref).exists())
      {
         if (!cachedRepo.getRefDir(ref).exists())
         {
            Repository localRepo = getLocalRepository(url);
            if (!localRepo.getBaseDir().exists())
            {
               io.copyDirectoryFromGrid(gfs, cachedRepo.getRepoDir(), localRepo.getRepoDir());
            }
            if (!localRepo.getRefDir(ref).exists())
            {
               localRepo.initRef(ref);
               io.copyDirectoryToGrid(gfs, localRepo.getRefDir(ref), cachedRepo.getRefDir(ref));
            }
         }
      }

      String result = render.resolveRendered(cachedRepo, ref, path);
      return result;
   }

   @Override
   public Repository updateRepository(String repo)
   {
      Repository localRepo = getLocalRepository(repo);
      if (localRepo != null && localRepo.getBaseDir().exists())
      {
         if (localRepo.getRefsDir().exists())
            Files.delete(localRepo.getRefsDir(), true);
         if (localRepo.getCacheDir().exists())
            Files.delete(localRepo.getCacheDir(), true);
      }

      Repository cachedRepo = getCachedRepository(repo);
      if (cachedRepo != null && cachedRepo.getBaseDir().exists())
      {
         if (cachedRepo.getRefsDir().exists())
            Files.delete(cachedRepo.getRefsDir(), true);
         if (cachedRepo.getCacheDir().exists())
            Files.delete(cachedRepo.getCacheDir(), true);
      }

      return primeRepository(repo);
   }

   @Override
   public Repository getLocalRepository(String url)
   {
      return new GitRepository(new NativeFileAdapter(), Redoculous.getRoot(), url);
   }

   @Override
   public Repository getCachedRepository(String url)
   {
      String key = Keys.from(url);
      Repository result = (Repository) repositoryCache.get(key);
      return result;
   }

   private Repository primeRepository(String url)
   {
      Repository localRepo = getLocalRepository(url);
      localRepo.init();

      Repository result = new GitRepository(new GridFileAdapter(gfs), gfs.getFile("/"), url);
      io.copyDirectoryToGrid(gfs, localRepo.getBaseDir(), result.getBaseDir());
      repositoryCache.putIfAbsentAsync(Keys.from(url), result);
      return result;
   }

}
