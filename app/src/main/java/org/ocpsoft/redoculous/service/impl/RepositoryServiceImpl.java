/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.service.impl;

import java.util.Set;

import javax.inject.Inject;

import org.ocpsoft.redoculous.cache.CacheController;
import org.ocpsoft.redoculous.cache.Keys;
import org.ocpsoft.redoculous.model.Ref;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.model.impl.GitRepository;
import org.ocpsoft.redoculous.service.RepositoryService;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class RepositoryServiceImpl implements RepositoryService
{
   @Inject
   private CacheController cache;

   @Override
   public Repository getRepository(String repository)
   {
      Object key = Keys.repository(repository);
      Repository result = (Repository) cache.query(key);
      if (result == null)
      {
         Repository repo = new GitRepository(repository);
         repo.init();
         cache.add(key, repo);
      }
      return result;
   }

   @Override
   public Set<Ref> getRefs(Repository repository)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Ref getRef(Repository repository, String ref)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getRenderedPath(Ref reff, String path)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
