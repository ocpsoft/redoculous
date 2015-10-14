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
import org.ocpsoft.redoculous.render.RenderRequest;
import org.ocpsoft.redoculous.render.RenderResult;
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
   public RenderResult getRenderedContent(String namespace, String repo, String ref, String path)
   {
      if (path.startsWith("/"))
         path = path.substring(1);

      RenderResult result = render.resolveRendered(new RenderRequest(getGridRepository(namespace, repo), ref, path));

      if (result == null)
      {
         Lock lock = gridLock.getLock(tx, repo);
         lock.lock();
         try
         {
            Repository gridRepo = getGridRepository(namespace, repo);
            if (!gridRepo.getCachedRefDir(ref).exists())
            {
               _doPrimeGridRepository(namespace, repo);
               _doPrimeRef(namespace, repo, ref);
            }
         }
         finally
         {
            lock.unlock();
         }
         result = render.resolveRendered(new RenderRequest(getGridRepository(namespace, repo), ref, path));
      }

      return result;
   }

   private void _doPrimeRef(String namespace, String repo, String ref)
   {
      Repository gridRepo = getGridRepository(namespace, repo);
      if (!gridRepo.getRefDir(ref).exists())
      {
         purgeLocalRepository(namespace, repo);
         Repository localRepo = getLocalRepository(namespace, repo);
         try
         {
            setStatus(namespace, repo, new RepositoryStatus(State.CHECKOUT_REF, ref));
            localRepo.getBaseDir().mkdirs();
            io.copyFileFromGrid(gfs, gridRepo.getRepoArchive(), localRepo.getRepoArchive());
            localRepo.decompress();
            localRepo.initRef(ref);
            io.copyDirectoryToGrid(gfs, localRepo.getRefDir(ref), gridRepo.getRefDir(ref));
            setStatus(namespace, repo, new RepositoryStatus(State.INITIALIZED));
         }
         catch (RuntimeException e)
         {
            setStatus(namespace, repo, new RepositoryStatus(State.ERROR, e.getMessage()));
            throw e;
         }
         finally
         {
            purgeLocalRepository(namespace, repo);
         }
      }
   }

   @Override
   public void initRepository(String namespace, String repo)
   {
      primeGridRepository(namespace, repo);
   }

   @Override
   public void updateRepository(String namespace, String repo)
   {
      log.info("Update: [" + repo + "] [" + namespace + "] - Requested.");
      Repository localRepo = getLocalRepository(namespace, repo);
      Repository gridRepo = getGridRepository(namespace, repo);

      if (gridRepo != null && gridRepo.getRepoDir().exists())
      {
         Lock lock = gridLock.getLock(tx, repo);
         lock.lock();
         try
         {
            if (gridRepo != null && gridRepo.getRepoDir().exists())
            {
               purgeLocalRepository(namespace, repo);
               try
               {
                  setStatus(namespace, repo, new RepositoryStatus(State.UPDATING));
                  localRepo.getBaseDir().mkdirs();
                  io.copyFileFromGrid(gfs, gridRepo.getRepoArchive(), localRepo.getRepoArchive());
                  localRepo.decompress();
                  localRepo.update();
                  setRepositoryRefs(namespace, repo, localRepo.getRefs());
                  localRepo.compress();
                  purgeGridRepository(namespace, repo);
                  io.copyDirectoryToGrid(gfs, localRepo.getBaseDir(), gridRepo.getBaseDir());
                  log.info("Update: [" + repo + "] [" + namespace + "] - From grid. Success.");
                  setStatus(namespace, repo, new RepositoryStatus(State.INITIALIZED));
               }
               catch (RuntimeException e)
               {
                  setStatus(namespace, repo, new RepositoryStatus(State.ERROR, e.getMessage()));
                  throw e;
               }
               finally
               {
                  purgeLocalRepository(namespace, repo);
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
         primeGridRepository(namespace, repo);
         log.info("Update: [" + repo + "] [" + namespace
                  + "] - From source repository (Not previously cached. Init perfomed instead). Success.");
      }
   }

   @Override
   public void purgeRepository(String namespace, String repo)
   {
      log.info("Purge: [" + repo + "] [" + namespace + "] - Requested.");

      Lock lock = gridLock.getLock(tx, repo);
      lock.lock();
      try
      {
         setStatus(namespace, repo, new RepositoryStatus(State.PURGING, repo));
         purgeLocalRepository(namespace, repo);
         purgeGridRepository(namespace, repo);
         removeStatus(namespace, repo);
      }
      catch (RuntimeException e)
      {
         setStatus(namespace, repo, new RepositoryStatus(State.ERROR, e.getMessage()));
         throw e;
      }
      finally
      {
         lock.unlock();
      }
   }

   @Override
   public Repository getRepository(String namespace, String repo)
   {
      primeGridRepository(namespace, repo);
      return getGridRepository(namespace, repo);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Set<String> getRepositoryRefs(String namespace, String repo)
   {
      primeGridRepository(namespace, repo);
      return (Set<String>) defaultCache.get(Keys.from(namespace, repo, "versions"));
   }

   private void setRepositoryRefs(String namespace, String repo, Set<String> refs)
   {
      defaultCache.put(Keys.from(namespace, repo, "versions"), refs);
   }

   private void clearRepositoryRefs(String namespace, String repo)
   {
      defaultCache.remove(Keys.from(namespace, repo, "versions"));
   }

   private void purgeGridRepository(String namespace, String repo)
   {
      Repository cachedRepo = getGridRepository(namespace, repo);
      if (cachedRepo != null && cachedRepo.getBaseDir().exists())
      {
         clearRepositoryRefs(namespace, repo);

         // TODO This doesn't seem right - should probably find out why the files can't be deleted.
         int retriesLeft = 5;
         boolean deleted = Files.delete(cachedRepo.getBaseDir(), true);
         while (!deleted && retriesLeft > 0)
         {
            retriesLeft--;
            deleted = Files.delete(cachedRepo.getBaseDir(), true);
         }

         if (deleted)
            log.info("Purge: [" + repo + "] [" + namespace + "] - From grid. Success.");
         else
            throw new RuntimeException("Purge: [" + repo + "] [" + namespace + "] - From grid. Failed.");
      }
      else
      {
         log.info("Purge: [" + repo + "] [" + namespace + "] - From grid. Not required.");
      }
   }

   private void purgeLocalRepository(String namespace, String repo)
   {
      Repository localRepo = getLocalRepository(namespace, repo);
      if (localRepo != null && localRepo.getBaseDir().exists())
      {
         Files.delete(localRepo.getBaseDir(), true);
         log.info("Purge: [" + repo + "] [" + namespace + "] - From local filesystem. Success.");
      }
      else
      {
         log.info("Purge: [" + repo + "] [" + namespace + "] - From local filesystem. Not required.");
      }
   }

   private Repository getLocalRepository(String namespace, String repo)
   {
      return new GitRepository(new NativeFileAdapter(), Redoculous.getRoot(), namespace, repo);
   }

   private Repository getGridRepository(String namespace, String repo)
   {
      return new GitRepository(new GridFileAdapter(gfs), gfs.getFile("/"), namespace, repo);
   }

   private Repository primeGridRepository(String namespace, String repo)
   {
      Repository gridRepository = getGridRepository(namespace, repo);
      if (gridRepository == null || !gridRepository.getBaseDir().exists())
      {
         Lock lock = gridLock.getLock(tx, repo);
         lock.lock();
         try
         {
            _doPrimeGridRepository(namespace, repo);
         }
         finally
         {
            lock.unlock();
         }
      }
      return gridRepository;
   }

   private Repository _doPrimeGridRepository(String namespace, String repo)
   {
      Repository gridRepository = getGridRepository(namespace, repo);
      if (gridRepository == null || !gridRepository.getBaseDir().exists())
      {
         try
         {
            setStatus(namespace, repo, new RepositoryStatus(State.CLONING));
            Repository localRepo = getLocalRepository(namespace, repo);
            localRepo.init();
            localRepo.update();
            setRepositoryRefs(namespace, repo, localRepo.getRefs());
            localRepo.compress();
            io.copyDirectoryToGrid(gfs, localRepo.getBaseDir(), gridRepository.getBaseDir());
            setStatus(namespace, repo, new RepositoryStatus(State.INITIALIZED));
         }
         catch (RuntimeException e)
         {
            setStatus(namespace, repo, new RepositoryStatus(State.ERROR, e.getMessage()));
            throw e;
         }
         finally
         {
            purgeLocalRepository(namespace, repo);
         }
      }
      return gridRepository;
   }

   @Override
   public RepositoryStatus getStatus(String namespace, String repo)
   {
      RepositoryStatus status = (RepositoryStatus) defaultCache.get(Keys.from(namespace, repo, "status"));
      if (status == null)
         status = new RepositoryStatus(State.MISSING);
      return status;
   }

   private void setStatus(String namespace, String repo, RepositoryStatus status)
   {
      defaultCache.put(Keys.from(namespace, repo, "status"), status);
   }

   private void removeStatus(String namespace, String repo)
   {
      defaultCache.remove(Keys.from(namespace, repo, "status"));
   }
}
