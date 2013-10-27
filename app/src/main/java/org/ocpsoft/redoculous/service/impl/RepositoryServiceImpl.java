/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;

import org.infinispan.Cache;
import org.infinispan.io.GridFilesystem;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.redoculous.Redoculous;
import org.ocpsoft.redoculous.cache.CacheProducer;
import org.ocpsoft.redoculous.cache.GridLock;
import org.ocpsoft.redoculous.cache.Keys;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.model.impl.GitRepository;
import org.ocpsoft.redoculous.rest.model.RepositoryStatus;
import org.ocpsoft.redoculous.rest.model.RepositoryStatus.State;
import org.ocpsoft.redoculous.service.RepositoryService;
import org.ocpsoft.redoculous.util.Files;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class RepositoryServiceImpl implements RepositoryService
{
   private static final Logger log = Logger.getLogger(RepositoryServiceImpl.class);

   @Inject
   @Named(CacheProducer.DEFAULT)
   private Cache<Object, Object> defaultCache;

   @Inject
   private FileOperations io;

   @Inject
   private GridFilesystem gfs;

   @Inject
   private RenderService render;

   @Inject
   private GridLock gridLock;

   @Inject
   private UserTransaction tx;

   @Override
   public String getRenderedContent(String repo, String ref, String path)
   {
      if (path.startsWith("/"))
         path = path.substring(1);

      String result = render.resolveRendered(getGridRepository(repo), ref, path);

      if (result == null)
      {
         Lock lock = gridLock.getLock(tx, repo);
         lock.lock();
         try
         {
            Repository gridRepo = getGridRepository(repo);
            if (!gridRepo.getCachedRefDir(ref).exists())
            {
               _doPrimeGridRepository(repo);
               _doPrimeRef(repo, ref);
            }
         }
         finally
         {
            lock.unlock();
         }
         result = render.resolveRendered(getGridRepository(repo), ref, path);
      }

      return result;
   }

   private void _doPrimeRef(String repo, String ref)
   {
      Repository gridRepo = getGridRepository(repo);
      if (!gridRepo.getRefDir(ref).exists())
      {
         purgeLocalRepository(repo);
         Repository localRepo = getLocalRepository(repo);
         try
         {
            setStatus(repo, new RepositoryStatus(State.CHECKOUT_REF, ref));
            localRepo.getBaseDir().mkdirs();
            io.copyFileFromGrid(gfs, gridRepo.getRepoArchive(), localRepo.getRepoArchive());
            localRepo.decompress();
            localRepo.initRef(ref);
            io.copyDirectoryToGrid(gfs, localRepo.getRefDir(ref), gridRepo.getRefDir(ref));
            setStatus(repo, new RepositoryStatus(State.INITIALIZED));
         }
         catch (RuntimeException e)
         {
            setStatus(repo, new RepositoryStatus(State.ERROR, e.getMessage()));
            throw e;
         }
         finally
         {
            purgeLocalRepository(repo);
         }
      }
   }

   @Override
   public void initRepository(String repo)
   {
      primeGridRepository(repo);
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
                  setStatus(repo, new RepositoryStatus(State.UPDATING));
                  localRepo.getBaseDir().mkdirs();
                  io.copyFileFromGrid(gfs, gridRepo.getRepoArchive(), localRepo.getRepoArchive());
                  localRepo.decompress();
                  localRepo.update();
                  setRepositoryRefs(repo, localRepo.getRefs());
                  localRepo.compress();
                  purgeGridRepository(repo);
                  io.copyDirectoryToGrid(gfs, localRepo.getBaseDir(), gridRepo.getBaseDir());
                  log.info("Update: [" + repo + "] - From grid. Success.");
                  setStatus(repo, new RepositoryStatus(State.INITIALIZED));
               }
               catch (RuntimeException e)
               {
                  setStatus(repo, new RepositoryStatus(State.ERROR, e.getMessage()));
                  throw e;
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
         primeGridRepository(repo);
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
         setStatus(repo, new RepositoryStatus(State.PURGING, repo));
         purgeLocalRepository(repo);
         purgeGridRepository(repo);
         removeStatus(repo);
      }
      catch (RuntimeException e)
      {
         setStatus(repo, new RepositoryStatus(State.ERROR, e.getMessage()));
         throw e;
      }
      finally
      {
         lock.unlock();
      }
   }

   @Override
   public Repository getRepository(String repo)
   {
      primeGridRepository(repo);
      return getGridRepository(repo);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Set<String> getRepositoryRefs(String repo)
   {
      primeGridRepository(repo);
      return (Set<String>) defaultCache.get(Keys.from("versions:" + repo));
   }

   private void setRepositoryRefs(String repo, Set<String> refs)
   {
      defaultCache.put(Keys.from("versions:" + repo), refs);
   }

   private void clearRepositoryRefs(String repo)
   {
      defaultCache.remove(Keys.from("versions:" + repo));
   }

   private void purgeGridRepository(String repo)
   {
      Repository cachedRepo = getGridRepository(repo);
      if (cachedRepo != null && cachedRepo.getBaseDir().exists())
      {
         clearRepositoryRefs(repo);

         // TODO This doesn't seem right - should probably find out why the files can't be deleted.
         int retriesLeft = 5;
         boolean deleted = Files.delete(cachedRepo.getBaseDir(), true);
         while (!deleted && retriesLeft > 0)
         {
            retriesLeft--;
            deleted = Files.delete(cachedRepo.getBaseDir(), true);
         }

         if (deleted)
            log.info("Purge: [" + repo + "] - From grid. Success.");
         else
            throw new RuntimeException("Purge: [" + repo + "] - From grid. Failed.");
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

   private Repository primeGridRepository(String repo)
   {
      Repository gridRepository = getGridRepository(repo);
      if (gridRepository == null || !gridRepository.getBaseDir().exists())
      {
         Lock lock = gridLock.getLock(tx, repo);
         lock.lock();
         try
         {
            _doPrimeGridRepository(repo);
         }
         finally
         {
            lock.unlock();
         }
      }
      return gridRepository;
   }

   private Repository _doPrimeGridRepository(String repo)
   {
      Repository gridRepository = getGridRepository(repo);
      if (gridRepository == null || !gridRepository.getBaseDir().exists())
      {
         try
         {
            setStatus(repo, new RepositoryStatus(State.CLONING));
            Repository localRepo = getLocalRepository(repo);
            localRepo.init();
            localRepo.update();
            setRepositoryRefs(repo, localRepo.getRefs());
            localRepo.compress();
            io.copyDirectoryToGrid(gfs, localRepo.getBaseDir(), gridRepository.getBaseDir());
            setStatus(repo, new RepositoryStatus(State.INITIALIZED));
         }
         catch (RuntimeException e)
         {
            setStatus(repo, new RepositoryStatus(State.ERROR, e.getMessage()));
            throw e;
         }
         finally
         {
            purgeLocalRepository(repo);
         }
      }
      return gridRepository;
   }

   @Override
   public RepositoryStatus getStatus(String repo)
   {
      RepositoryStatus status = (RepositoryStatus) defaultCache.get(Keys.from("status:" + repo));
      if (status == null)
         status = new RepositoryStatus(State.MISSING);
      return status;
   }

   private void setStatus(String repo, RepositoryStatus status)
   {
      defaultCache.put(Keys.from("status:" + repo), status);
   }

   private void removeStatus(String repo)
   {
      defaultCache.remove(Keys.from("status:" + repo));
   }
}
