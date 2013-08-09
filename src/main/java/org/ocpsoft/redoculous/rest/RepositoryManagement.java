package org.ocpsoft.redoculous.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.ocpsoft.redoculous.Redoculous;
import org.ocpsoft.redoculous.config.git.GitUtils;
import org.ocpsoft.redoculous.config.util.DocumentFilter;
import org.ocpsoft.redoculous.config.util.Files;
import org.ocpsoft.redoculous.config.util.SafeFileNameTransposition;
import org.ocpsoft.redoculous.exception.NoSuchRepositoryException;
import org.ocpsoft.rewrite.exception.RewriteException;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

@Path("/manage")
@Produces({ "application/xml", "application/json" })
public class RepositoryManagement
{

   @GET
   @Path("/versions")
   public VersionResult getAvailableVersions(@QueryParam("repo") String repo,
            @QueryParam("filter") @DefaultValue(".*") String filter)
            throws Exception
   {
      List<String> result = new ArrayList<String>();

      final File root = Redoculous.getRoot();
      String safeRepoName = SafeFileNameTransposition.toSafeFilename(repo);
      File repoDir = new File(root, safeRepoName + "/repo");
      if (!repoDir.isDirectory())
         throw new NoSuchRepositoryException(repo);

      Git git = null;
      try {
         git = Git.open(repoDir);

         git.fetch().setRemote("origin").setTagOpt(TagOpt.FETCH_TAGS)
                  .setThin(false).setTimeout(10)
                  .setProgressMonitor(new TextProgressMonitor()).call();
         List<Ref> branches = git.branchList().setListMode(ListMode.ALL).call();
         result.addAll(processRefs(branches, filter));
         List<Ref> tags = git.tagList().call();
         result.addAll(processRefs(tags, filter));
      }
      finally {
         if (git != null) {
            GitUtils.close(git);
         }
      }

      VersionResult versions = new VersionResult(result);
      return versions;
   }

   @POST
   @Path("/init")
   public Response init(@QueryParam("repo") String repo)
   {
      String safeRepoName = SafeFileNameTransposition.toSafeFilename(repo);

      final File root = Redoculous.getRoot();
      File repoDir = new File(root, safeRepoName + "/repo");
      File refsDir = new File(root, safeRepoName + "/refs");
      File cacheDir = new File(root, safeRepoName + "/caches");

      if (!repoDir.exists()) {
         try {
            repoDir.mkdirs();
            refsDir.mkdirs();
            cacheDir.mkdirs();

            Git git = Git.cloneRepository().setURI(repo)
                     .setRemote("origin").setCloneAllBranches(true)
                     .setDirectory(repoDir)
                     .setProgressMonitor(new TextProgressMonitor()).call();

            git.fetch().setRemote("origin").setTagOpt(TagOpt.FETCH_TAGS)
                     .setThin(false).setTimeout(10)
                     .setProgressMonitor(new TextProgressMonitor()).call();

            String ref = git.getRepository().getBranch();

            GitUtils.close(git);

            File refDir = new File(refsDir, ref);
            System.out.println("Initialized [" + repo + "] [" + ref + "] at");
            Files.copyDirectory(repoDir, refDir, new DocumentFilter());
         }
         catch (Exception e) {
            throw new RuntimeException("Could not clone repository [" + repo + "]", e);
         }
      }

      return Response.created(UriBuilder.fromPath("/serve").queryParam("repo", repo).build()).build();
   }

   @POST
   @Path("/update")
   public void updateRepository(@QueryParam("repo") String repo, @FormParam("payload") String payload) throws Exception
   {
      if (payload != null)
      {
         Gson gson = new Gson();
         Map<?, ?> json = gson.fromJson(payload, Map.class);
         StringMap<?> repository = (StringMap<?>) json.get("repository");
         repo = (String) repository.get("url");
         if (repo.startsWith("http") && !repo.endsWith(".git"))
            repo = repo + ".git";
      }

      String safeRepoName = SafeFileNameTransposition.toSafeFilename(repo);

      final File root = Redoculous.getRoot();
      File repoDir = new File(root, safeRepoName + "/repo");
      if (!repoDir.isDirectory())
         throw new NoSuchRepositoryException(repo);

      File refsDir = new File(root, safeRepoName + "/refs");
      File cacheDir = new File(root, safeRepoName + "/caches");
      Git git = null;
      try {
         System.out.println("Handling update request for [" + repo + "]");
         git = Git.open(repoDir);

         git.fetch()
                  .setTagOpt(TagOpt.FETCH_TAGS)
                  .setRemote("origin")
                  .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"))
                  .setProgressMonitor(new TextProgressMonitor()).call();

         git.fetch()
                  .setTagOpt(TagOpt.FETCH_TAGS)
                  .setRemote("origin")
                  .setRefSpecs(new RefSpec("+refs/tags/*:refs/tags/*"))
                  .setProgressMonitor(new TextProgressMonitor()).call();

         git.reset().setMode(ResetType.HARD)
                  .setRef("refs/remotes/origin/" + git.getRepository().getBranch())
                  .call();

         git.clean().setCleanDirectories(true).call();

         Files.delete(refsDir, true);
         Files.delete(cacheDir, true);
         cacheDir.mkdirs();
      }
      catch (GitAPIException e) {
         throw new RewriteException(
                  "Could not pull from git repository.", e);
      }
      finally {
         if (git != null) {
            GitUtils.close(git);

         }
      }
   }

   @XmlRootElement(name = "versions")
   public static class VersionResult
   {
      private List<String> versions;

      public VersionResult()
      {}

      public VersionResult(List<String> versions)
      {
         this.versions = versions;
      }

      @XmlElement
      public List<String> getVersions()
      {
         return versions;
      }
   }

   private List<String> processRefs(List<Ref> refs, String filter)
   {
      List<String> result = new ArrayList<String>();
      for (Ref ref : refs) {
         String name = ref.getName();
         if (filter == null || filter.isEmpty() || name.matches(filter))
            result.add(name);
      }
      return result;
   }
}
