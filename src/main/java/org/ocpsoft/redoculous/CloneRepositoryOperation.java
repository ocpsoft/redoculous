package org.ocpsoft.redoculous;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.ocpsoft.redoculous.util.DocumentFilter;
import org.ocpsoft.redoculous.util.Files;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.exception.RewriteException;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

public final class CloneRepositoryOperation extends HttpOperation
{
   Transposition<String> safeFileName = new SafeFileNameTransposition();

   private final File root;

   private String repoParam;

   private String refParam;

   public CloneRepositoryOperation(File root, String repoParam, String refParam)
   {
      this.root = root;
      this.repoParam = repoParam;
      this.refParam = refParam;
   }

   @Override
   public void performHttp(HttpServletRewrite event, EvaluationContext context)
   {
      ParameterStore params = (ParameterStore) context.get(ParameterStore.class);
      ParameterValueStore values = (ParameterValueStore) context.get(ParameterValueStore.class);
      String ref = values.retrieve(params.get(refParam));
      String repo = values.retrieve(params.get(repoParam));

      File repoDir = new File(root, safeFileName.transpose(event, context, repo) + "/repo");
      File refsDir = new File(root, safeFileName.transpose(event, context, repo) + "/refs");
      File cacheDir = new File(root, safeFileName.transpose(event, context, repo) + "/caches");

      if (!repoDir.exists())
      {
         try {
            repoDir.mkdirs();
            refsDir.mkdirs();
            cacheDir.mkdirs();

            Git.cloneRepository().setURI(repo).setRemote("origin")
                     .setCloneAllBranches(true).setDirectory(repoDir).call();

            File refDir = new File(refsDir, ref);
            Files.copyDirectory(repoDir, refDir, new DocumentFilter());
         }
         catch (GitAPIException e) {
            throw new RewriteException("Could not clone git repository.", e);
         }
         catch (IOException e) {
            throw new RewriteException("Could not copy repository to ref dir.", e);
         }
      }
   }
}