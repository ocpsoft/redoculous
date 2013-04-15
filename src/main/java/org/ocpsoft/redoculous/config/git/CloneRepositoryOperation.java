package org.ocpsoft.redoculous.config.git;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.ocpsoft.redoculous.config.util.DocumentFilter;
import org.ocpsoft.redoculous.config.util.Files;
import org.ocpsoft.redoculous.config.util.SafeFileNameTransposition;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.exception.RewriteException;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

public final class CloneRepositoryOperation extends HttpOperation implements Parameterized
{
   Transposition<String> safeFileName = new SafeFileNameTransposition();

   private final File root;

   private String repoParam;
   private String refParam;

   private ParameterStore store;

   public CloneRepositoryOperation(File root, String repoParam, String refParam)
   {
      this.root = root;
      this.repoParam = repoParam;
      this.refParam = refParam;
   }

   @Override
   public void performHttp(HttpServletRewrite event, EvaluationContext context)
   {
      ParameterValueStore values = (ParameterValueStore) context.get(ParameterValueStore.class);
      String ref = values.retrieve(store.get(refParam));
      String repo = event.getRequest().getParameter(repoParam);

      String safeRepoName = safeFileName.transpose(event, context, repo);
      File repoDir = new File(root, safeRepoName + "/repo");
      File refsDir = new File(root, safeRepoName + "/refs");
      File cacheDir = new File(root, safeRepoName + "/caches");
      File refDir = new File(refsDir, ref);

      if (!repoDir.exists())
      {
         System.out.println("Cloning [" + repo + "] [" + ref + "]");
         try {
            repoDir.mkdirs();
            refsDir.mkdirs();
            cacheDir.mkdirs();

            Git.cloneRepository().setURI(repo).setRemote("origin")
                     .setCloneAllBranches(true).setDirectory(repoDir)
                     .setProgressMonitor(new TextProgressMonitor()).call();

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

   @Override
   public Set<String> getRequiredParameterNames()
   {
      return new HashSet<String>(Arrays.asList(repoParam, refParam));
   }

   @Override
   public void setParameterStore(ParameterStore store)
   {
      this.store = store;
   }
}