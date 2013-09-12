/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import org.infinispan.io.GridFilesystem;
import org.ocpsoft.common.util.Streams;
import org.ocpsoft.redoculous.model.impl.FileAdapter;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
@Stateless
public class FileOperations
{

   public void copyDirectoryFromGrid(GridFilesystem gfs, File source, File destination)
   {
      try
      {
         copyDirectory(new GridFileAdapter(gfs), new NativeFileAdapter(), source, destination, null);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not replicate directory from GridFilesystem", e);
      }
   }

   @Asynchronous
   public Future<Void> copyDirectoryToGridAsync(GridFilesystem gfs, File source, File destination)
   {
      copyDirectoryToGrid(gfs, source, destination);
      return new AsyncResult<Void>(null);
   }

   public void copyDirectoryToGrid(GridFilesystem gfs, File source, File destination)
   {
      try
      {
         copyDirectory(new NativeFileAdapter(), new GridFileAdapter(gfs), source,
                  gfs.getFile(destination.getAbsolutePath()), null);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not replicate directory to GridFilesystem", e);
      }
   }

   /*
    * Helpers
    */
   private void copyDirectory(FileAdapter sourceAdapter, FileAdapter destAdapter, File srcDir,
            File destDir, FileFilter filter) throws IOException
   {
      if (srcDir == null)
      {
         throw new NullPointerException("Source must not be null");
      }
      if (destDir == null)
      {
         throw new NullPointerException("Destination must not be null");
      }
      if (srcDir.exists() == false)
      {
         throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
      }
      if (srcDir.isDirectory() == false)
      {
         throw new IOException("Source '" + srcDir + "' exists but is not a directory");
      }

      // Cater for destination being directory within the source directory (see IO-141)
      List<String> exclusionList = null;
      if (destDir.getAbsolutePath().startsWith(srcDir.getAbsolutePath()))
      {
         File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
         if (srcFiles != null && srcFiles.length > 0)
         {
            exclusionList = new ArrayList<String>(srcFiles.length);
            for (File srcFile : srcFiles)
            {
               File exclusion = sourceAdapter.newFile(destDir, srcFile.getName());
               exclusionList.add(exclusion.getAbsolutePath());
            }
         }
      }
      doCopyDirectory(sourceAdapter, destAdapter, srcDir, destDir, filter, exclusionList);
   }

   private static void doCopyDirectory(FileAdapter sourceAdapter, FileAdapter destAdapter, File srcDir, File destDir,
            FileFilter filter, List<String> exclusionList) throws IOException
   {
      // recurse
      File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
      if (srcFiles == null)
      { // null if abstract pathname does not denote a directory, or if an I/O error occurs
         throw new IOException("Failed to list contents of " + srcDir);
      }
      if (destDir.exists())
      {
         if (destDir.isDirectory() == false)
         {
            throw new IOException("Destination '" + destDir + "' exists but is not a directory");
         }
      }
      else
      {
         if (!destDir.mkdirs() && !destDir.isDirectory())
         {
            throw new IOException("Destination '" + destDir + "' directory cannot be created");
         }
      }
      if (destDir.canWrite() == false)
      {
         // throw new IOException("Destination '" + destDir + "' cannot be written to");
      }
      for (File srcFile : srcFiles)
      {
         File dstFile = destAdapter.newFile(destDir, srcFile.getName());
         if (exclusionList == null || !exclusionList.contains(srcFile.getAbsolutePath()))
         {
            if (srcFile.isDirectory())
            {
               doCopyDirectory(sourceAdapter, destAdapter, srcFile, dstFile, filter, exclusionList);
            }
            else
            {
               doCopyFile(sourceAdapter, destAdapter, srcFile, dstFile);
            }
         }
      }
   }

   private static void doCopyFile(FileAdapter sourceAdapter, FileAdapter destAdapter, File srcFile, File destFile)
            throws IOException
   {
      if (destFile.exists() && destFile.isDirectory())
      {
         throw new IOException("Destination '" + destFile + "' exists but is a directory");
      }

      InputStream fis = null;
      OutputStream fos = null;
      try
      {
         fis = sourceAdapter.getInputStream(srcFile);
         fos = destAdapter.getOutputStream(destFile);
         Streams.copy(fis, fos);
      }
      finally
      {
         Streams.closeQuietly(fos);
         Streams.closeQuietly(fis);
      }

      if (srcFile.length() != destFile.length())
      {
         throw new IOException("Failed to copy full contents from '" +
                  srcFile + "' to '" + destFile + "'");
      }
   }
}
