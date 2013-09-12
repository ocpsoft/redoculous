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
   /**
    * Initialize the repository
    */
   void init();

   /**
    * Transform the given ref into its canonicalized form.
    */
   String resolveRef(String ref);

   /**
    * Check out the given ref and populate corresponding ref dir with a copy of unrendered files.
    */
   void initRef(String ref);

   /**
    * Update from the origin repository and delete refs/caches dirs.
    */
   void update();

   /*
    * Getters/setters
    */

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

}
