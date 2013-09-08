package org.ocpsoft.redoculous.config.git;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.ocpsoft.redoculous.config.util.SafeFileNameTransposition;
import org.ocpsoft.redoculous.util.GitRepositoryUtils;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.Transposition;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;

public final class CloneRepositoryOperation extends HttpOperation implements
         Parameterized
{
   Transposition<String> safeFileName = new SafeFileNameTransposition();

   private String repoParam;
   private String refParam;

   @SuppressWarnings("unused")
   private ParameterStore store;

   public CloneRepositoryOperation(File root, String repoParam)
   {
      this.repoParam = repoParam;
   }

   @Override
   public void performHttp(HttpServletRewrite event, EvaluationContext context)
   {
      String repo = event.getRequest().getParameter(repoParam);

      new GitRepositoryUtils().clone(repo);
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