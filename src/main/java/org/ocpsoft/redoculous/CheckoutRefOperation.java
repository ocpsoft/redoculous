package org.ocpsoft.redoculous;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.TagOpt;
import org.ocpsoft.redoculous.util.DocumentFilter;
import org.ocpsoft.redoculous.util.Files;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.exception.RewriteException;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

public final class CheckoutRefOperation extends HttpOperation implements Parameterized
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
      ParameterValueStore values = (ParameterValueStore) context.get(ParameterValueStore.class);
      String ref = values.retrieve(store.get(refParam));
      String repo = event.getRequest().getParameter(repoParam);

      String safeRepoName = safeFileName.transpose(event, context, repo);
      File repoDir = new File(root, safeRepoName + "/repo");
      File refsDir = new File(root, safeRepoName + "/refs");
      File cacheDir = new File(root, safeRepoName + "/caches");
      File refDir = new File(refsDir, ref);
      File refCacheDir = new File(cacheDir, ref);

      if (!refDir.exists() || true)
      {
         refDir.mkdirs();
         refCacheDir.mkdirs();
         try {
            Git git = Git.open(repoDir);
            git.fetch()
                     .setRemote("origin").setTagOpt(TagOpt.FETCH_TAGS)
                     .setThin(false).setTimeout(10)
                     .setProgressMonitor(new TextProgressMonitor()).call();

            git.reset().setRef(ref).setMode(ResetType.HARD).call();
            git.pull().setRebase(false).setTimeout(10).setProgressMonitor(new TextProgressMonitor()).call();
            Files.delete(refCacheDir, true);
            refCacheDir.mkdirs();
            Files.copyDirectory(repoDir, refDir, new DocumentFilter());
         }
         catch (Exception e) {
            Files.delete(refDir, true);
            Files.delete(refCacheDir, true);
            throw new RewriteException("Could checkout ref [" + ref + "] from repository [" + repo + "].", e);
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