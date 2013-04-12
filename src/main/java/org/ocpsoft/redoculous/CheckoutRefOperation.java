package org.ocpsoft.redoculous;

import java.io.File;

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
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

public final class CheckoutRefOperation extends HttpOperation
{
   Transposition<String> safeFileName = new SafeFileNameTransposition();

   private final File root;

   private String repoParam;

   private String refParam;

   public CheckoutRefOperation(File root, String repoParam, String refParam)
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

            Files.copyDirectory(repoDir, refDir, new DocumentFilter());
         }
         catch (Exception e) {
            Files.delete(refDir, true);
            Files.delete(refCacheDir, true);
            throw new RewriteException("Could checkout ref [" + ref + "] repository.", e);
         }
      }
   }
}