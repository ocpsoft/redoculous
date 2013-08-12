package org.ocpsoft.redoculous.git;

import java.io.File;

import junit.framework.Assert;

import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ocpsoft.redoculous.util.Files;

@Ignore
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

      git.checkout()
               .setName("master")
               .setStartPoint("origin/master")
               .setCreateBranch(true)
               .setUpstreamMode(SetupUpstreamMode.TRACK)
               .setForce(true)
               .call();

      Assert.assertEquals("master", git.getRepository().getBranch());

      git.checkout()
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

      git.branchCreate()
               .setName("2.0.0.Beta2")
               .setUpstreamMode(SetupUpstreamMode.TRACK)
               .setStartPoint("refs/tags/2.0.0.Beta2")
               .setForce(false)
               .call();

      Assert.assertEquals(size + 1, git.branchList().setListMode(ListMode.ALL).call().size());

      git.reset()
               .setRef("2.0.0.Beta2")
               .setMode(ResetType.HARD)
               .call();

      git.reset()
               .setRef("HEAD")
               .setMode(ResetType.HARD)
               .call();

      git.reset()
               .setRef("2.0.0.Beta1")
               .setMode(ResetType.HARD)
               .call();

      git.checkout().setName("2.0.0.Beta1").setStartPoint("refs/tags/2.0.0.Beta1").setForce(true).call();

      Assert.assertEquals("f9c951a9036afedb1743fa5f5265044abfe8b675", git.getRepository().getBranch());

      git.reset()
               .setRef("2.0.0.Beta2")
               .setMode(ResetType.HARD)
               .call();

      Assert.assertEquals("6fd485dc94ed76fa25f1bbfcd57c382ce7fbe16e", git.getRepository().getBranch());

      git.checkout().setName("master").setForce(true).call();

      git.reset().setMode(ResetType.HARD).setRef("HEAD~10").call();

      String newFile = "redoculous-injected.txt";
      File created = new File(localPath, newFile);
      created.createNewFile();
      Status status = git.status().call();
      Assert.assertTrue(status.getUntracked().contains(newFile));
      Assert.assertFalse(status.getUntracked().isEmpty());

      git.add().addFilepattern(newFile).call();

      status = git.status().call();
      Assert.assertTrue(status.getAdded().contains(newFile));

      git.commit().setAuthor("lincoln", "lincoln@ocpsoft.org").setAmend(true)
               .setAll(true)
               .setCommitter("redoculous", "redoculous@ocpsoft.org")
               .setMessage("Committed by Redoculous - http://ocpsoft.org/redoculous")
               .call();

      Files.delete(new File(localPath, "transform-markup"), true);
      status = git.status().call();
      Assert.assertFalse(status.getMissing().isEmpty());
      Assert.assertTrue(status.getMissing().contains("transform-markup/pom.xml"));

      git.rm().addFilepattern("transform-markup/").call();

      status = git.status().call();
      Assert.assertFalse(status.getAdded().contains(newFile));
      Assert.assertTrue(status.getMissing().isEmpty());
      Assert.assertTrue(status.getUntracked().isEmpty());
      Assert.assertTrue(status.getRemoved().contains("transform-markup/pom.xml"));

      git.commit().setAuthor("lincoln", "lincoln@ocpsoft.org").setAmend(false)
               .setAll(true)
               .setCommitter("redoculous", "redoculous@ocpsoft.org")
               .setMessage("Committed by Redoculous 2 - http://ocpsoft.org/redoculous")
               .call();

      status = git.status().call();
      Assert.assertFalse(status.getAdded().contains(newFile));
      Assert.assertTrue(status.getUntracked().isEmpty());
      Assert.assertTrue(status.getMissing().isEmpty());
      Assert.assertFalse(status.getRemoved().contains("transform-markup/pom.xml"));

      PullResult pullResult = git.pull()
               .setTimeout(10)
               .setProgressMonitor(new TextProgressMonitor())
               .setRebase(false)
               .call();

      Assert.assertFalse(pullResult.isSuccessful());

      System.out.println("Done!");
   }
}