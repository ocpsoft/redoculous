/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.model.impl;

import java.io.File;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;

import org.ocpsoft.logging.Logger;
import org.ocpsoft.redoculous.cache.Keys;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.util.Files;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class AbstractRepository implements Repository
{
   private static final Logger log = Logger.getLogger(AbstractRepository.class);
   private static final long serialVersionUID = -6024964348584778584L;

   private String url;
   private String key;
   private FileAdapter adapter;
   private File root;

   public AbstractRepository(FileAdapter adapter, File root, String url)
   {
      this.adapter = adapter;
      this.root = root;
      this.url = url;
      this.key = Keys.from(url);

   }

   @Override
   public File getBaseDir()
   {
      return adapter.newFile(root, getKey());
   }

   @Override
   public File getRepoDir()
   {
      return adapter.newFile(getBaseDir(), "repo");
   }

   @Override
   public File getRepoArchive()
   {
      return adapter.newFile(getBaseDir(), "repo.zip");
   }

   @Override
   public File getCacheDir()
   {
      return adapter.newFile(getBaseDir(), "caches");
   }

   @Override
   public File getRefsDir()
   {
      return adapter.newFile(getBaseDir(), "refs");
   }

   @Override
   public File getRefDir(String ref)
   {
      return adapter.newFile(getRefsDir(), Keys.from(resolveRef(ref)));
   }

   @Override
   public File getCachedRefDir(String ref)
   {
      return adapter.newFile(getCacheDir(), Keys.from(resolveRef(ref)));
   }

   @Override
   public String getKey()
   {
      return key;
   }

   @Override
   public String getUrl()
   {
      return url;
   }

   @Override
   public void compress()
   {
      log.info("Compressing repository [" + getUrl() + "].");
      try {
         if (getRepoArchive().isFile())
            getRepoArchive().delete();

         ZipFile zipFile = new ZipFile(getRepoArchive());
         ZipParameters parameters = new ZipParameters();
         parameters.setIncludeRootFolder(false);
         parameters.setReadHiddenFiles(true);
         zipFile.createZipFileFromFolder(getRepoDir(), parameters, false, 0);
      }
      catch (ZipException e) {
         throw new RuntimeException("Could not compress repository: " + getRepoDir(), e);
      }
   }

   @Override
   public void decompress()
   {
      log.info("Decompressing repository [" + getUrl() + "].");
      try {
         if (getRepoDir().isDirectory())
         {
            for (File file : getRepoDir().listFiles()) {
               Files.delete(file, true);
            }
         }
         else
            getRepoDir().mkdirs();

         ZipFile zipFile = new ZipFile(getRepoArchive());
         UnzipParameters parameters = new UnzipParameters();
         parameters.setIgnoreAllFileAttributes(false);
         parameters.setIgnoreArchiveFileAttribute(false);
         parameters.setIgnoreDateTimeAttributes(false);
         parameters.setIgnoreHiddenFileAttribute(false);
         parameters.setIgnoreReadOnlyFileAttribute(false);
         parameters.setIgnoreSystemFileAttribute(false);
         zipFile.extractAll(getRepoDir().toString(), parameters);
      }
      catch (ZipException e) {
         throw new RuntimeException("Could not decompress repository: " + getRepoDir(), e);
      }
   }
}