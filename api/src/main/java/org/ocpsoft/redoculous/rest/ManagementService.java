package org.ocpsoft.redoculous.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/v1/manage")
@Produces({ "application/xml", "application/json" })
public interface ManagementService
{
   @POST
   public Response init(@QueryParam("repo") String repo);

   @PUT
   public Response updateRepository(@QueryParam("repo") String repo) throws Exception;

   @DELETE
   public Response purgeRepository(@QueryParam("repo") String repo) throws Exception;
}