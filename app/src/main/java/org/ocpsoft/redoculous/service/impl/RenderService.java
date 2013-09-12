package org.ocpsoft.redoculous.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.infinispan.io.GridFile;
import org.infinispan.io.GridFilesystem;
import org.ocpsoft.common.util.Streams;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.redoculous.cache.GridLock;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.render.Renderer;

public class RenderService
{
   private static final Logger log = Logger.getLogger(RenderService.class);

   @Inject
   @Any
   private Instance<Renderer> renderers;

   @Inject
   private GridFilesystem gfs;

   @Inject
   private GridLock gridLock;

   @Inject
   private UserTransaction tx;

   public String resolveRendered(Repository repo, String ref, String path)
   {
      File source = resolvePath(repo.getRefDir(ref), path);
      if (source.exists())
      {
         File result = gfs.getFile(repo.getCachedRefDir(ref), getRelativePath(repo.getRefDir(ref), source));
         if (!result.exists())
         {
            Lock lock = gridLock.getLock(tx, source.getAbsolutePath());
            lock.lock();
            try
            {
               if (!result.exists())
               {
                  LOOP: for (Renderer renderer : renderers)
                  {
                     for (String extension : renderer.getSupportedExtensions())
                     {
                        if (extension.matches(source.getName().replaceAll("^.*\\.([^.]+)", "$1")))
                        {
                           render(renderer, source, (GridFile) result);
                           log.info("Render: [" + repo.getUrl() + "] [" + ref + "] [" + path + "] - Complete.");
                           break LOOP;
                        }
                     }
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
            log.info("Render: [" + repo.getUrl() + "] [" + ref + "] [" + path + "] - Not required (already cached).");
         }

         try
         {
            if (result.exists())
               return Streams.toString(gfs.getInput(result));
            else if (source.exists())
               return Streams.toString(gfs.getInput(source));
         }
         catch (FileNotFoundException e)
         {
            throw new RuntimeException(e);
         }
      }

      log.info("Render: [" + repo.getUrl() + "] [" + ref + "] [" + path + "] - Not found.");
      return null;
   }

   private String getRelativePath(File base, File source)
   {
      String relative = source.getAbsolutePath().substring(base.getAbsolutePath().length(),
               source.getAbsolutePath().length());

      if (relative.startsWith("/"))
         relative = relative.substring(1);

      return relative;
   }

   private void render(Renderer renderer, File source, GridFile result)
   {
      InputStream input = null;
      OutputStream output = null;
      try
      {
         if (!result.getParentFile().exists())
            result.getParentFile().mkdirs();

         result.createNewFile();
         input = new BufferedInputStream(gfs.getInput(source));
         output = new BufferedOutputStream(gfs.getOutput(result));
         renderer.render(input, output);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         if (input != null)
            try
            {
               input.close();
            }
            catch (IOException e)
            {
               throw new RuntimeException(e);
            }
         if (output != null)
            try
            {
               output.close();
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
      }
   }

   private File resolvePath(File refDir, String path)
   {
      File original = gfs.getFile(refDir, path);
      File result = original;
      if (result.isDirectory())
      {
         LOOP: for (Renderer renderer : renderers)
         {
            for (String extension : renderer.getSupportedExtensions())
            {
               result = gfs.getFile(original, "index." + extension);
               if (result.isFile())
                  break LOOP;
            }
         }
      }

      if (!result.exists())
      {
         LOOP: for (Renderer renderer : renderers)
         {
            for (String extension : renderer.getSupportedExtensions())
            {
               result = gfs.getFile(refDir, path + "." + extension);
               if (result.isFile())
                  break LOOP;
            }
         }
      }

      if (!result.exists())
         result = original;
      return result;
   }

}
