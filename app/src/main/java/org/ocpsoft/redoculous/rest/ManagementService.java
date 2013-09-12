package org.ocpsoft.redoculous.rest;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.ocpsoft.redoculous.service.RepositoryService;

@Path("/v1/manage")
@Produces({ "application/xml", "application/json" })
public class ManagementService
{
   @Inject
   private RepositoryService rs;

   @POST
   public Response init(@QueryParam("repo") String repo)
   {
      rs.initRepository(repo);
      return Response.created(UriBuilder.fromPath("/v1/serve").queryParam("repo", repo).build()).build();
   }

   @PUT
   public void updateRepository(@QueryParam("repo") String repo) throws Exception
   {
      rs.updateRepository(repo);
   }

   @DELETE
   public void purgeRepository(@QueryParam("repo") String repo) throws Exception
   {
      rs.purgeRepository(repo);
   }

}
