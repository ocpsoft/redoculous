package org.ocpsoft.redoculous.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ocpsoft.redoculous.config.util.Files;

@Ignore
public class GitOperationTest
{

   private File localPath;
   private String remoteUrl;
   private Repository localRepo;
   private Git git;

   @Before
   public void init() throws IOException
   {
      localPath = File.createTempFile("redoculous", "");
      localPath.delete();
      localPath.mkdirs();
      remoteUrl = "git://github.com/ocpsoft/rewrite.git";
      localRepo = new FileRepository(new File(localPath, ".git"));
      localRepo.create();
      StoredConfig config = localRepo.getConfig();
      config.setString("remote", "origin", "url", remoteUrl);
      git = new Git(localRepo);
   }

   @After
   public void teardown() throws Exception
   {
      Files.delete(localPath, true);
   }

   @Test
   public void testFetchIntoExisingRepo() throws Exception
   {
      git.branchCreate()
               .setName("master")
               .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
               .setStartPoint("origin/master")
               .setForce(true)
               .call();
   }
}
