package org.ocpsoft.redoculous.rest;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

public class HooksIntegrationServiceImpl implements HooksIntegrationService
{
   @Inject
   private ManagementService rs;

   @Override
   public Response githubUpdateRepository(String repo, String payload)
            throws Exception
   {
      if (payload != null)
      {
         Gson gson = new Gson();
         Map<?, ?> json = gson.fromJson(payload, Map.class);
         StringMap<?> repository = (StringMap<?>) json.get("repository");
         repo = (String) repository.get("url");
         if (repo.startsWith("http") && !repo.endsWith(".git"))
            repo = repo + ".git";
      }

      if (repo == null || repo.isEmpty())
         return Response.status(Status.NOT_FOUND).build();
      else
         rs.updateRepository(repo);
      return Response.status(Status.OK).build();
   }
}
