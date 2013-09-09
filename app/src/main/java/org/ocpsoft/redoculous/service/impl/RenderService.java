package org.ocpsoft.redoculous.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.render.Renderer;

public class RenderService
{
   @Inject
   @Any
   private Instance<Renderer> renderers;

   public File resolvePath(Repository repo, String ref, String path)
   {
      File pathFile = resolvePath(repo.getRefDir(ref), path);
      return pathFile;
   }

   public File resolveCachePath(Repository repo, String ref, String path)
   {
      return resolvePath(repo.getCachedRefDir(ref), path);
   }

   public File resolveRendered(Repository repo, String ref, String path)
   {
      File result = resolveCachePath(repo, ref, path);
      if (!result.exists())
      {
         File source = resolvePath(repo, ref, path);
         if (source.exists())
         {
            result = new File(repo.getCachedRefDir(ref), source.getName());
            LOOP: for (Renderer renderer : renderers)
            {
               for (String extension : renderer.getSupportedExtensions())
               {
                  if (extension.matches(source.getName().replaceAll("^.*\\.([^.]+)", "$1")))
                  {
                     render(renderer, source, result);
                     break LOOP;
                  }
               }
            }
         }
      }
      return result;
   }

   private void render(Renderer renderer, File source, File result)
   {
      InputStream input = null;
      OutputStream output = null;
      try
      {
         result.createNewFile();
         input = new BufferedInputStream(new FileInputStream(source));
         output = new BufferedOutputStream(new FileOutputStream(result));
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
      File pathFile = new File(refDir, path);
      if (path.endsWith("/") && pathFile.isDirectory())
      {
         LOOP: for (Renderer renderer : renderers)
         {
            for (String extension : renderer.getSupportedExtensions())
            {
               pathFile = new File(refDir, path + "/index." + extension);
               if (pathFile.isFile())
                  break LOOP;
            }
         }
      }

      if (!pathFile.exists())
      {
         LOOP: for (Renderer renderer : renderers)
         {
            for (String extension : renderer.getSupportedExtensions())
            {
               pathFile = new File(refDir, path + "." + extension);
               if (pathFile.isFile())
                  break LOOP;
            }
         }
      }
      return pathFile;
   }

}
