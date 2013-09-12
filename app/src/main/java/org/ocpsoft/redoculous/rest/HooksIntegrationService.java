package org.ocpsoft.redoculous.rest;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

@Path("/v1/hooks")
@Produces({ "application/xml", "application/json" })
public class HooksIntegrationService
{
   @Inject
   private ManagementService rs;

   @POST
   @Path("/github")
   public void githubUpdateRepository(@QueryParam("repo") String repo, @FormParam("payload") String payload)
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

      rs.updateRepository(repo);
   }
}
