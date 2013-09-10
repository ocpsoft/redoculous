package org.ocpsoft.redoculous.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.infinispan.io.GridFile;
import org.infinispan.io.GridFilesystem;
import org.ocpsoft.common.util.Streams;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.render.Renderer;

public class RenderService
{
   @Inject
   @Any
   private Instance<Renderer> renderers;

   @Inject
   private GridFilesystem gfs;

   public String resolveRendered(Repository repo, String ref, String path)
   {
      File result = resolvePath(repo.getCachedRefDir(ref), path);
      if (!result.exists())
      {
         File pathFile = resolvePath(repo.getRefDir(ref), path);
         File source = pathFile;
         if (source.exists())
         {
            result = gfs.getFile(repo.getCachedRefDir(ref), source.getName());
            LOOP: for (Renderer renderer : renderers)
            {
               for (String extension : renderer.getSupportedExtensions())
               {
                  if (extension.matches(source.getName().replaceAll("^.*\\.([^.]+)", "$1")))
                  {
                     render(renderer, source, (GridFile) result);
                     break LOOP;
                  }
               }
            }
         }
      }
      try
      {
         if (result.exists())
            return Streams.toString(gfs.getInput(result));
         else
            return null;
      }
      catch (FileNotFoundException e)
      {
         throw new RuntimeException("Could not render file", e);
      }
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
      if (path.endsWith("/") && result.isDirectory())
      {
         LOOP: for (Renderer renderer : renderers)
         {
            for (String extension : renderer.getSupportedExtensions())
            {
               result = gfs.getFile(refDir, path + "/index." + extension);
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
