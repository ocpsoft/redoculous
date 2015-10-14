/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.render;

import org.ocpsoft.redoculous.model.Repository;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class RenderRequest
{
   @Override
   public String toString()
   {
      return "[repository=" + repository + ", ref=" + ref + ", path=" + path + "]";
   }

   private final Repository repository;
   private final String ref;
   private final String path;

   public RenderRequest(Repository repository, String ref, String path)
   {
      this.repository = repository;
      this.ref = ref;
      this.path = path;
   }

   public Repository getRepository()
   {
      return repository;
   }

   public String getRef()
   {
      return ref;
   }

   public String getPath()
   {
      return path;
   }

}
