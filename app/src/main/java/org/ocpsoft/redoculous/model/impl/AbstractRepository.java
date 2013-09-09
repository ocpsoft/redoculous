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
   private String url;
   private String key;
   private File root;

   public AbstractRepository(File root, String url)
   {
      this.root = root;
      this.url = url;
      this.key = Keys.repository(url);

   }

   @Override
   public File getBaseDir()
   {
      return new File(root, getKey());
   }

   @Override
   public File getRepoDir()
   {
      return new File(getBaseDir(), "repo");
   }

   @Override
   public File getCacheDir()
   {
      return new File(getBaseDir(), "caches");
   }

   @Override
   public File getRefsDir()
   {
      return new File(getBaseDir(), "refs");
   }

   @Override
   public File getRefDir(String ref)
   {
      return new File(getRefsDir(), ref);
   }

   @Override
   public File getCachedRefDir(String ref)
   {
      return new File(getCacheDir(), ref);
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