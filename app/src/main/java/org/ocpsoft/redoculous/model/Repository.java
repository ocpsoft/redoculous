/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.model;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public interface Repository extends Serializable
{
   void init();

   String getKey();

   String getUrl();

   Set<String> getRefs();

   String getCurrentRef();

   File getBaseDir();

   File getRepoDir();

   File getCacheDir();

   File getCachedRefDir(String ref);

   File getRefsDir();

   File getRefDir(String ref);

   /**
    * Transform the given ref into its canonicalized form.
    */
   String resolveRef(String ref);

   void initRef(String ref);

}
