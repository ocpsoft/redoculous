/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.model.impl;

import java.io.File;

import org.ocpsoft.redoculous.cache.Keys;
import org.ocpsoft.redoculous.model.Repository;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public abstract class AbstractRepository implements Repository
{
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

}