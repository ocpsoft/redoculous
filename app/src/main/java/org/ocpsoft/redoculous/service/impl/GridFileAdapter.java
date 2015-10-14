/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.infinispan.io.GridFile;
import org.infinispan.io.GridFilesystem;
import org.ocpsoft.redoculous.model.impl.FileAdapter;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GridFileAdapter implements FileAdapter
{
   private static final long serialVersionUID = 1943469809609195419L;

   private GridFilesystem gfs;

   public GridFileAdapter(GridFilesystem gfs)
   {
      this.gfs = gfs;
   }

   @Override
   public File newFile(File parent, String child)
   {
      return gfs.getFile(parent, child);
   }

   @Override
   public InputStream getInputStream(File file) throws IOException
   {
      try
      {
         return gfs.getInput(file);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public OutputStream getOutputStream(File file) throws IOException
   {
      try
      {
         return gfs.getOutput((GridFile) file);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

}
