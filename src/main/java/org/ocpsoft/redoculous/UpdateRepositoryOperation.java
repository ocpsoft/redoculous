package org.ocpsoft.redoculous;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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
         // ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         // Streams.copy(event.getRequest().getInputStream(), buffer);
         // String jsonString = new String(buffer.toByteArray());

         String jsonString = event.getRequest().getParameter("payload");
         System.out.println(jsonString);
         Map json = gson.fromJson(jsonString, Map.class);
         StringMap repository = (StringMap) json.get("repository");
         String repo = (String) repository.get("url");
         if (!repo.endsWith(".git"))
            repo = repo + ".git";

         File repoDir = new File(root, safeFileName.transpose(event, context, repo));
         File cacheDir = new File(root, safeFileName.transpose(event, context, repo) + "-cache");

         if (!repoDir.exists())
         {
            repoDir.mkdirs();
            cacheDir.mkdirs();
            Git.cloneRepository().setURI(repo)
                     .setCloneAllBranches(true).setDirectory(repoDir).call();
         }
         else
         {
            try {
               Git git = GitUtils.git(repoDir);
               GitUtils.pull(git, 15);
               deleteRecursively(cacheDir);
               cacheDir.mkdirs();
            }
            catch (GitAPIException e) {
               throw new RewriteException("Could not pull from git repository.", e);
            }
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
