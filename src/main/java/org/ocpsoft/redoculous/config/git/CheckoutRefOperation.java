package org.ocpsoft.redoculous.config.git;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.api.Git;
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

public final class CheckoutRefOperation extends HttpOperation implements
         Parameterized
{
   Transposition<String> safeFileName = new SafeFileNameTransposition();

   private final File root;

   private String repoParam;
   private String refParam;

   private ParameterStore store;

   public CheckoutRefOperation(File root, String repoParam, String refParam)
   {
      this.root = root;
      this.repoParam = repoParam;
      this.refParam = refParam;
   }

   @Override
   public void performHttp(HttpServletRewrite event, EvaluationContext context)
   {
      ParameterValueStore values = (ParameterValueStore) context
               .get(ParameterValueStore.class);
      String ref = values.retrieve(store.get(refParam));

      if (!ref.startsWith("refs/"))
      {
         ref = "refs/remotes/origin/" + ref;
      }

      String repo = event.getRequest().getParameter(repoParam);

      String safeRepoName = safeFileName.transpose(event, context, repo);
      File repoDir = new File(root, safeRepoName + "/repo");
      File refsDir = new File(root, safeRepoName + "/refs");
      File cacheDir = new File(root, safeRepoName + "/caches");
      File refDir = new File(refsDir, ref);
      File refCacheDir = new File(cacheDir, ref);

      if (!refDir.exists()) {
         System.out.println("Creating ref copy [" + repo + "] [" + ref + "]");
         refDir.mkdirs();
         refCacheDir.mkdirs();
         Git git = null;
         try {
            git = Git.open(repoDir);

            git.checkout().setName(ref).call();

            System.out.println("Deleting cache for [" + repo + "] [" + ref + "]");
            Files.delete(refDir, true);
            Files.delete(refCacheDir, true);
            refCacheDir.mkdirs();
            Files.copyDirectory(repoDir, refDir, new DocumentFilter());
         }
         catch (Exception e) {
            if (git != null) {
               GitUtils.close(git);
               git = null;
            }
            Files.delete(refDir, true);
            Files.delete(refCacheDir, true);
            throw new RewriteException("Could checkout ref [" + ref
                     + "] from repository [" + repo + "].", e);
         }
         finally {
            if (git != null) {
               GitUtils.close(git);
            }
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