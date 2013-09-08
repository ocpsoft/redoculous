/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.model.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.lib.Ref;
import org.ocpsoft.redoculous.config.git.GitUtils;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.rest.DocumentService.VersionResult;
import org.ocpsoft.redoculous.util.GitRepositoryUtils;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GitRepository implements Repository
{
   private GitRepositoryUtils utils = new GitRepositoryUtils();

   private String repository;
   private File repoDir;

   public GitRepository(String repository)
   {
      this.repository = repository;
      this.repoDir = utils.getRepoDir(repository);
   }

   @Override
   public void init()
   {
      utils.clone(repository);
   }

   public VersionResult getVersions() throws Exception
   {
      List<String> result = new ArrayList<String>();

      Git git = null;
      try
      {
         git = Git.open(repoDir);
         List<Ref> branches = git.branchList().setListMode(ListMode.ALL).call();
         result.addAll(processRefs(branches));
         List<Ref> tags = git.tagList().call();
         result.addAll(processRefs(tags));
      }
      finally
      {
         if (git != null)
         {
            GitUtils.close(git);
         }
      }

      VersionResult versions = new VersionResult(result);
      return versions;
   }

   private List<String> processRefs(List<Ref> refs)
   {
      List<String> result = new ArrayList<String>();
      for (Ref ref : refs)
      {
         String name = ref.getName();
         result.add(name);
      }
      return result;
   }

}
