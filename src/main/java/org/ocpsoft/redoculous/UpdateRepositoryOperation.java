package org.ocpsoft.redoculous;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.exception.RewriteException;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

public final class UpdateRepositoryOperation extends HttpOperation
{
   private final File root;
   Transposition<String> safeFileName = new SafeFileNameTransposition();

   public UpdateRepositoryOperation(File root)
   {
      this.root = root;
   }

   @Override
   @SuppressWarnings("rawtypes")
   public void performHttp(HttpServletRewrite event, EvaluationContext context)
   {
      Gson gson = new Gson();
      try {
         String jsonString = event.getRequest().getParameter("payload");
         System.out.println(jsonString);
         Map json = gson.fromJson(jsonString, Map.class);
         StringMap repository = (StringMap) json.get("repository");
         String repo = (String) repository.get("url");
         if (!repo.endsWith(".git"))
            repo = repo + ".git";

         File repoDir = new File(root, safeFileName.transpose(event, context, repo) + "/repo");
         File cacheDir = new File(root, safeFileName.transpose(event, context, repo) + "/caches");

         try {
            Git git = Git.open(repoDir);

            git.fetch().setTagOpt(TagOpt.FETCH_TAGS)
                     .setRemote("origin")
                     .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"))
                     .setProgressMonitor(new TextProgressMonitor())
                     .call();

            git.fetch().setTagOpt(TagOpt.FETCH_TAGS)
                     .setRemote("origin")
                     .setRefSpecs(new RefSpec("+refs/tags/*:refs/tags/*"))
                     .setProgressMonitor(new TextProgressMonitor())
                     .call();

            git.pull()
                     .setRebase(false)
                     .setProgressMonitor(new TextProgressMonitor())
                     .call();

            deleteRecursively(cacheDir);
            cacheDir.mkdirs();
         }
         catch (GitAPIException e) {
            throw new RewriteException("Could not pull from git repository.", e);
         }
      }
      catch (Exception e) {
         throw new RewriteException("Error parsing update hook", e);
      }
   }

   private void deleteRecursively(File f) throws IOException
   {
      if (f.isDirectory()) {
         for (File c : f.listFiles())
            deleteRecursively(c);
      }
      if (!f.delete())
         throw new FileNotFoundException("Failed to delete file: " + f);
   }
}
