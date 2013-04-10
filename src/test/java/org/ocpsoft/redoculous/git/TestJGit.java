package org.ocpsoft.redoculous.git;

import java.io.File;

import junit.framework.Assert;

import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.junit.Before;
import org.junit.Test;

public class TestJGit
{

   File localPath;
   private String remoteUrl;
   private Git git;

   @Before
   public void init() throws Exception
   {
      localPath = File.createTempFile("redoculous", "");
      localPath.delete();
      localPath.mkdirs();
      remoteUrl = "git://github.com/ocpsoft/rewrite.git";

      git = Git.cloneRepository()
               .setURI(remoteUrl)
               .setRemote("origin")
               .setNoCheckout(true)
               .setDirectory(localPath)
               .setTimeout(10)
               // .setProgressMonitor(new TextProgressMonitor())
               .setCloneAllBranches(true)
               .call();
   }

   @Test
   public void testTrackMaster() throws Exception
   {
      git.branchCreate()
               .setName("test")
               .setUpstreamMode(SetupUpstreamMode.TRACK)
               .setStartPoint("origin/master")
               .setForce(true)
               .call();

      Ref result = git.checkout()
               .setName("master")
               .setStartPoint("origin/master")
               .setCreateBranch(true)
               .setUpstreamMode(SetupUpstreamMode.TRACK)
               .setForce(true)
               .call();

      Assert.assertEquals("master", git.getRepository().getBranch());

      result = git.checkout()
               .setName("test")
               .call();

      Assert.assertEquals("test", git.getRepository().getBranch());

      git.fetch().setTagOpt(TagOpt.FETCH_TAGS)
               .setRemote("origin")
               .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"))
               .call();

      git.fetch().setTagOpt(TagOpt.FETCH_TAGS)
               .setRemote("origin")
               .setRefSpecs(new RefSpec("+refs/tags/*:refs/tags/*"))
               .call();

      int size = git.branchList().setListMode(ListMode.ALL).call().size();

      result = git.branchCreate()
               .setName("2.0.0.Beta2")
               .setUpstreamMode(SetupUpstreamMode.TRACK)
               .setStartPoint("refs/tags/2.0.0.Beta2")
               .setForce(false)
               .call();

      Assert.assertEquals(size + 1, git.branchList().setListMode(ListMode.ALL).call().size());

      result = git.reset()
               .setRef("2.0.0.Beta2")
               .setMode(ResetType.HARD)
               .call();

      result = git.reset()
               .setRef("HEAD")
               .setMode(ResetType.HARD)
               .call();

      result = git.reset()
               .setRef("2.0.0.Beta1")
               .setMode(ResetType.HARD)
               .call();

      git.checkout().setName("2.0.0.Beta1").setStartPoint("refs/tags/2.0.0.Beta1").setForce(true).call();

      Assert.assertEquals("f9c951a9036afedb1743fa5f5265044abfe8b675", git.getRepository().getBranch());

      System.out.println(result);
   }

}