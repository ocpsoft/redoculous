package org.ocpsoft.redoculous.config.git;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.ocpsoft.redoculous.config.util.SafeFileNameTransposition;
import org.ocpsoft.redoculous.repositories.RepositoryUtils;
import org.ocpsoft.rewrite.context.EvaluationContext;
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

   private String repoParam;
   private String refParam;

   private ParameterStore store;

   public CheckoutRefOperation(String repoParam, String refParam)
   {
      this.repoParam = repoParam;
      this.refParam = refParam;
   }

   @Override
   public void performHttp(HttpServletRewrite event, EvaluationContext context)
   {
      ParameterValueStore values = (ParameterValueStore) context
               .get(ParameterValueStore.class);
      String ref = values.retrieve(store.get(refParam));
      String repo = event.getRequest().getParameter(repoParam);

      try {
         new RepositoryUtils().initRef(repo, ref);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
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