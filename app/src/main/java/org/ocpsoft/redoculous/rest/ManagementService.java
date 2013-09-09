package org.ocpsoft.redoculous.rest;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.ocpsoft.redoculous.service.RepositoryService;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

@Path("/v1/manage")
@Produces({ "application/xml", "application/json" })
public class ManagementService
{
   @Inject
   private RepositoryService rs;

   @POST
   @Path("/init")
   public Response init(@QueryParam("repo") String repo)
   {
      rs.getCachedRepository(repo);
      return Response.created(UriBuilder.fromPath("/v1/serve").queryParam("repo", repo).build()).build();
   }

   @POST
   @Path("/update")
   public void updateRepository(@QueryParam("repo") String repo, @FormParam("payload") String payload) throws Exception
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

   }

}
