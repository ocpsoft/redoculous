package org.ocpsoft.redoculous.rest;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.ocpsoft.redoculous.config.git.GitUtils;
import org.ocpsoft.redoculous.config.util.DocumentFilter;
import org.ocpsoft.redoculous.repositories.RepositoryUtils;
import org.ocpsoft.redoculous.util.Files;
import org.ocpsoft.rewrite.exception.RewriteException;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

@Path("/manage")
@Produces({ "application/xml", "application/json" })
public class ManagementService
{
   @Inject
   private RepositoryUtils repositories;

   @POST
   @Path("/init")
   public Response init(@QueryParam("repo") String repo)
   {
      File repoDir = repositories.getRepoDir(repo);
      File refsDir = repositories.getRefsDir(repo);
      File cacheDir = repositories.getCacheDir(repo);

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

      File repoDir = repositories.getRepoDir(repo);

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

         repositories.invalidate(repo);
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

}
