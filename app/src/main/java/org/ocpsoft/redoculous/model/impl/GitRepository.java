/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ocpsoft.redoculous.model.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.TagOpt;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.util.Files;
import org.ocpsoft.redoculous.util.GitUtils;
import org.ocpsoft.rewrite.exception.RewriteException;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class GitRepository extends AbstractRepository implements Repository
{
   private Set<String> refs;

   public GitRepository(File root, String url)
   {
      super(root, url);
   }

   @Override
   public void init()
   {
      cloneRepository();
   }

   @Override
   public String getCurrentRef()
   {
      Git git = null;
      try
      {
         git = GitUtils.git(getBaseDir());
         return GitUtils.getCurrentBranchName(git);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to initialize repository [" + getUrl() + "]", e);
      }
      finally
      {
         if (git != null)
            GitUtils.close(git);
      }
   }

   @Override
   public String resolveRef(String ref)
   {
      if (!ref.startsWith("refs/"))
      {
         ref = "refs/remotes/origin/" + ref;
      }
      return ref;
   }

   @Override
   public Set<String> getRefs()
   {
      if (refs == null)
      {
         try
         {
            refs = new LinkedHashSet<String>();

            Git git = null;
            try
            {
               git = Git.open(getBaseDir());
               List<Ref> branches = git.branchList().setListMode(ListMode.ALL).call();
               refs.addAll(processRefs(branches));
               List<Ref> tags = git.tagList().call();
               refs.addAll(processRefs(tags));
            }
            finally
            {
               if (git != null)
               {
                  GitUtils.close(git);
               }
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      return refs;
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

   private void cloneRepository()
   {
      File refsDir = getRefsDir();
      File cacheDir = getCacheDir();

      if (!getRepoDir().exists())
      {
         try
         {
            getRepoDir().mkdirs();
            refsDir.mkdirs();
            cacheDir.mkdirs();

            Git git = Git.cloneRepository().setURI(getUrl())
                     .setRemote("origin").setCloneAllBranches(true)
                     .setDirectory(getRepoDir())
                     .setProgressMonitor(new TextProgressMonitor()).call();

            git.fetch().setRemote("origin").setTagOpt(TagOpt.FETCH_TAGS)
                     .setThin(false).setTimeout(10)
                     .setProgressMonitor(new TextProgressMonitor()).call();

            GitUtils.close(git);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Could not clone repository [" + getUrl() + "]", e);
         }
      }
   }

   public void initRef(String repo, String ref)
   {
      File repoDir = getRepoDir();

      ref = resolveRef(ref);

      File refDir = getRefDir(ref);
      File refCacheDir = getCachedRefDir(ref);
      if (!refDir.exists())
      {
         System.out.println("Creating ref copy [" + repo + "] [" + ref + "]");
         refDir.mkdirs();
         refCacheDir.mkdirs();
         Git git = null;
         try
         {
            git = Git.open(repoDir);

            git.checkout().setName(ref).call();

            System.out.println("Deleting cache for [" + repo + "] [" + ref + "]");
            Files.delete(refDir, true);
            Files.delete(refCacheDir, true);
            refCacheDir.mkdirs();
            Files.copyDirectory(repoDir, refDir);
         }
         catch (Exception e)
         {
            if (git != null)
            {
               GitUtils.close(git);
               git = null;
            }
            Files.delete(refDir, true);
            Files.delete(refCacheDir, true);
            throw new RewriteException("Could checkout ref [" + ref
                     + "] from repository [" + repo + "].", e);
         }
         finally
         {
            if (git != null)
            {
               GitUtils.close(git);
            }
         }
      }
   }
}
