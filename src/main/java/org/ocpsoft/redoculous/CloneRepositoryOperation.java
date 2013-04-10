package org.ocpsoft.redoculous;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.ocpsoft.rewrite.bind.Evaluation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.exception.RewriteException;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

public final class CloneRepositoryOperation extends HttpOperation
{
   Transposition<String> safeFileName = new SafeFileNameTransposition();

   private final File root;

   public CloneRepositoryOperation(File root)
   {
      this.root = root;
   }

   @Override
   public void performHttp(HttpServletRewrite event, EvaluationContext context)
   {
      String repo = (String) Evaluation.property("repo").retrieve(event, context);
      File repoDir = new File(root, safeFileName.transpose(event, context, repo));
      File cacheDir = new File(root, safeFileName.transpose(event, context, repo) + "-cache");
      if (!repoDir.exists())
      {
         try {
            repoDir.mkdirs();
            cacheDir.mkdirs();

            Git.cloneRepository().setURI(repo)
                     .setCloneAllBranches(true).setDirectory(repoDir).call();
         }
         catch (GitAPIException e) {
            throw new RewriteException("Could not clone git repository.", e);
         }
      }
   }
}