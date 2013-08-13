package org.ocpsoft.redoculous.config.util;

import java.io.File;
import java.io.FileFilter;

public class DocumentFilter implements FileFilter
{

   @Override
   public boolean accept(File file)
   {
      // TODO support other document types
      return !file.getName().equals(".git") && (file.getName().endsWith(".asciidoc") || file.isDirectory());
   }

}
