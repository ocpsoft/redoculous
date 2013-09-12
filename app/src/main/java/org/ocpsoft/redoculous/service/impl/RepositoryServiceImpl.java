/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import java.util.concurrent.locks.Lock;

import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.infinispan.io.GridFilesystem;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.redoculous.Redoculous;
import org.ocpsoft.redoculous.cache.GridLock;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.model.impl.GitRepository;
import org.ocpsoft.redoculous.service.RepositoryService;
import org.ocpsoft.redoculous.util.Files;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class RepositoryServiceImpl implements RepositoryService
{
   private static final Logger log = Logger.getLogger(RepositoryServiceImpl.class);

   @Inject
   private FileOperations io;

   @Inject
   private GridFilesystem gfs;

   @Inject
   private RenderService render;

   @Inject
   private GridLock gridLock;

   @Inject
   UserTransaction tx;

   @Override
   public String getRenderedContent(String repo, String ref, String path)
   {
      if (path.startsWith("/"))
         path = path.substring(1);

      Lock lock = gridLock.getLock(tx, repo);
      lock.lock();

      try
      {
         _doPrimeRepository(repo);

         Repository gridRepo = getGridRepository(repo);
         if (!gridRepo.getCachedRefDir(ref).exists())
         {
            if (!gridRepo.getRefDir(ref).exists())
            {
               purgeLocalRepository(repo);
               Repository localRepo = getLocalRepository(repo);
               try
               {
                  io.copyDirectoryFromGrid(gfs, gridRepo.getRepoDir(), localRepo.getRepoDir());
                  localRepo.initRef(ref);
                  io.copyDirectoryToGrid(gfs, localRepo.getRefDir(ref), gridRepo.getRefDir(ref));
               }
               finally
               {
                  purgeLocalRepository(repo);
               }
            }
         }
      }
      finally
      {
         lock.unlock();
      }

      String result = render.resolveRendered(getGridRepository(repo), ref, path);
      return result;
   }

   @Override
   public void initRepository(String repo)
   {
      primeRepository(repo);
   }

   @Override
   public void updateRepository(String repo)
   {
      log.info("Update: [" + repo + "] - Requested.");
      Repository localRepo = getLocalRepository(repo);
      Repository gridRepo = getGridRepository(repo);

      if (gridRepo != null && gridRepo.getRepoDir().exists())
      {
         Lock lock = gridLock.getLock(tx, repo);
         lock.lock();
         try
         {
            if (gridRepo != null && gridRepo.getRepoDir().exists())
            {
               purgeLocalRepository(repo);
               try
               {
                  io.copyDirectoryFromGrid(gfs, gridRepo.getRepoDir(), localRepo.getRepoDir());
                  localRepo.update();
                  purgeGridRepository(repo);
                  io.copyDirectoryToGrid(gfs, localRepo.getBaseDir(), gridRepo.getBaseDir());
                  log.info("Update: [" + repo + "] - From grid. Success.");
               }
               finally
               {
                  purgeLocalRepository(repo);
               }
            }
         }
         finally
         {
            lock.unlock();
         }
      }
      else
      {
         primeRepository(repo);
         log.info("Update: [" + repo
                  + "] - From source repository (Not previously cached. Init perfomed instead). Success.");
      }
   }

   @Override
   public void purgeRepository(String repo)
   {
      log.info("Purge: [" + repo + "] - Requested.");

      Lock lock = gridLock.getLock(tx, repo);
      lock.lock();
      try
      {
         purgeLocalRepository(repo);
         purgeGridRepository(repo);
      }
      finally
      {
         lock.unlock();
      }
   }

   @Override
   public Repository getRepository(String repo)
   {
      primeRepository(repo);
      return getGridRepository(repo);
   }

   private void purgeGridRepository(String repo)
   {
      Repository cachedRepo = getGridRepository(repo);
      if (cachedRepo != null && cachedRepo.getBaseDir().exists())
      {
         Files.delete(cachedRepo.getBaseDir(), true);
         log.info("Purge: [" + repo + "] - From grid. Success.");
      }
      else
      {
         log.info("Purge: [" + repo + "] - From grid. Not required.");
      }
   }

   private void purgeLocalRepository(String repo)
   {
      Repository localRepo = getLocalRepository(repo);
      if (localRepo != null && localRepo.getBaseDir().exists())
      {
         Files.delete(localRepo.getBaseDir(), true);
         log.info("Purge: [" + repo + "] - From local filesystem. Success.");
      }
      else
      {
         log.info("Purge: [" + repo + "] - From local filesystem. Not required.");
      }
   }

   private Repository getLocalRepository(String repo)
   {
      return new GitRepository(new NativeFileAdapter(), Redoculous.getRoot(), repo);
   }

   private Repository getGridRepository(String repo)
   {
      return new GitRepository(new GridFileAdapter(gfs), gfs.getFile("/"), repo);
   }

   private Repository primeRepository(String repo)
   {
      Repository gridRepository = getGridRepository(repo);
      if (gridRepository == null || !gridRepository.getBaseDir().exists())
      {
         Lock lock = gridLock.getLock(tx, repo);
         lock.lock();
         try
         {
            _doPrimeRepository(repo);
         }
         finally
         {
            lock.unlock();
         }
      }
      return gridRepository;
   }

   private void _doPrimeRepository(String repo)
   {
      Repository gridRepository = getGridRepository(repo);
      if (gridRepository == null || !gridRepository.getBaseDir().exists())
      {
         try
         {
            Repository localRepo = getLocalRepository(repo);
            localRepo.init();
            localRepo.update();
            io.copyDirectoryToGrid(gfs, localRepo.getBaseDir(), gridRepository.getBaseDir());
         }
         finally
         {
            purgeLocalRepository(repo);
         }
      }
   }
}
