/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.ocpsoft.redoculous;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.ocpsoft.common.util.Strings;

/**
 * Convenience tools for interacting with the Git version control system.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:jevgeni.zelenkov@gmail.com">Jevgeni Zelenkov</a>
 *
 */
public abstract class GitUtils
{
   public static Git clone(final File dir, final String repoUri) throws GitAPIException
   {
      CloneCommand clone = Git.cloneRepository().setURI(repoUri)
               .setDirectory(dir);
      Git git = clone.call();
      return git;
   }

   public static Git git(final File dir) throws IOException
   {
      RepositoryBuilder db = new RepositoryBuilder().findGitDir(dir);
      return new Git(db.build());
   }

   public static Ref checkout(final Git git, final String remote, final boolean createBranch,
            final SetupUpstreamMode mode, final boolean force)
            throws GitAPIException
   {
      CheckoutCommand checkout = git.checkout();
      checkout.setCreateBranch(createBranch);
      checkout.setName(remote);
      checkout.setForce(force);
      checkout.setUpstreamMode(mode);
      return checkout.call();
   }

   public static Ref checkout(final Git git, final Ref localRef, final boolean createBranch,
            final SetupUpstreamMode mode, final boolean force)
            throws GitAPIException
   {
      CheckoutCommand checkout = git.checkout();
      checkout.setName(Repository.shortenRefName(localRef.getName()));
      checkout.setForce(force);
      checkout.setUpstreamMode(mode);
      return checkout.call();
   }

   public static FetchResult fetch(final Git git, final String remote, final String refSpec, final int timeout,
            final boolean fsck, final boolean dryRun,
            final boolean thin,
            final boolean prune) throws GitAPIException
   {
      FetchCommand fetch = git.fetch();
      fetch.setCheckFetchedObjects(fsck);
      fetch.setRemoveDeletedRefs(prune);
      if (refSpec != null)
         fetch.setRefSpecs(new RefSpec(refSpec));
      if (timeout >= 0)
         fetch.setTimeout(timeout);
      fetch.setDryRun(dryRun);
      fetch.setRemote(remote);
      fetch.setThin(thin);
      fetch.setProgressMonitor(new TextProgressMonitor());

      FetchResult result = fetch.call();
      return result;
   }

   /**
    * Initialize a new git repository.
    *
    * @param dir The directory in which to create a new .git/ folder and repository.
    */
   public static Git init(final File dir) throws IOException
   {
      File gitDir = new File(dir, ".git");
      gitDir.mkdirs();

      RepositoryBuilder db = new RepositoryBuilder().setGitDir(gitDir).setup();
      Git git = new Git(db.build());
      git.getRepository().create();
      return git;
   }

   public static PullResult pull(final Git git, final int timeout) throws GitAPIException
   {
      PullCommand pull = git.pull();
      if (timeout >= 0)
         pull.setTimeout(timeout);
      pull.setProgressMonitor(new TextProgressMonitor());

      PullResult result = pull.call();
      return result;
   }

   public static List<Ref> getRemoteBranches(final Git repo) throws GitAPIException
   {
      List<Ref> results = new ArrayList<Ref>();
      try
      {
         FetchResult fetch = repo.fetch().setRemote("origin").call();
         Collection<Ref> refs = fetch.getAdvertisedRefs();
         for (Ref ref : refs)
         {
            if (ref.getName().startsWith("refs/heads"))
            {
               results.add(ref);
            }
         }
      }
      catch (InvalidRemoteException e)
      {
         e.printStackTrace();
      }

      return results;
   }

   public static List<Ref> getLocalBranches(final Git repo) throws GitAPIException
   {
      // returns only local branches by default
      return repo.branchList().call();
   }

   public static String getCurrentBranchName(final Git repo) throws IOException
   {
      return repo.getRepository().getBranch();
   }

   public static Ref switchBranch(final Git repo, final String branchName)
   {
      Ref switchedBranch = null;
      try
      {
         switchedBranch = repo.checkout().setName(branchName).call();
         if (switchedBranch == null)
            throw new RuntimeException("Couldn't switch to branch " + branchName);
      }
      catch (GitAPIException e)
      {
         e.printStackTrace();
      }

      return switchedBranch;
   }

   public static List<String> getLogForCurrentBranch(final Git repo) throws GitAPIException
   {
      List<String> results = new ArrayList<String>();
      Iterable<RevCommit> commits = repo.log().call();

      for (RevCommit commit : commits)
         results.add(commit.getFullMessage());

      return results;
   }

   public static Iterable<RevCommit> getLogForBranch(final Git repo, String branchName) throws GitAPIException,
            IOException
   {
      String oldBranch = repo.getRepository().getBranch();
      repo.checkout().setName(branchName).call();

      Iterable<RevCommit> commits = repo.log().call();

      repo.checkout().setName(oldBranch).call();

      return commits;
   }

   public static void add(final Git repo, String filepattern) throws GitAPIException
   {
      repo.add().addFilepattern(filepattern).call();
   }

   public static void addAll(final Git repo) throws GitAPIException
   {
      repo.add().addFilepattern(".").call();
   }

   public static void commit(final Git repo, String message) throws GitAPIException
   {
      repo.commit().setMessage(message).call();
   }

   public static void commitAll(final Git repo, String message) throws GitAPIException
   {
      repo.commit().setMessage(message).setAll(true).call();
   }

   public static void stashCreate(final Git repo) throws GitAPIException
   {
      repo.stashCreate().call();
   }

   public static void stashApply(final Git repo, String... stashRef) throws GitAPIException
   {
      if (stashRef.length >= 1 && !Strings.isNullOrEmpty(stashRef[0]))
      {
         repo.stashApply().setStashRef(stashRef[0]).call();
      }
      else
      {
         repo.stashApply().call();
      }
   }

   public static void stashDrop(final Git repo) throws GitAPIException
   {
      repo.stashDrop().call();
   }

   public static void cherryPick(final Git repo, Ref commit) throws GitAPIException
   {
      repo.cherryPick().include(commit).call();
   }

   public static void resetHard(final Git repo, String newBase) throws GitAPIException
   {
      repo.reset().setMode(ResetCommand.ResetType.HARD).setRef(newBase).call();
   }

   public static Ref createBranch(Git git, String branchName) throws RefAlreadyExistsException, RefNotFoundException,
            InvalidRefNameException, GitAPIException
   {
      Ref newBranch = git.branchCreate().setName(branchName).call();

      if (newBranch == null)
         throw new RuntimeException("Couldn't create new branch " + branchName);

      return newBranch;
   }

   public static void close(final Git repo)
   {
      repo.getRepository().close();
   }
}
