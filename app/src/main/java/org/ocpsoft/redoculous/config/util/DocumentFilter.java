package org.ocpsoft.redoculous.config.util;

import java.io.File;
import java.io.FileFilter;

import javax.enterprise.inject.Instance;

import org.ocpsoft.redoculous.render.Renderer;

public class DocumentFilter implements FileFilter
{
   private Instance<Renderer> renderers;

   public DocumentFilter(Instance<Renderer> renderers)
   {
      this.renderers = renderers;
   }

   @Override
   public boolean accept(File file)
   {
      for (Renderer renderer : renderers)
      {
         for (String extension : renderer.getSupportedExtensions())
         {
            if (!file.getName().equals(".git") && (file.getName().endsWith("." + extension) || file.isDirectory()))
               return true;
         }
      }
      return false;
   }

}
