package org.ocpsoft.redoculous.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/v1/hooks")
@Produces({ "application/xml", "application/json" })
public interface HooksIntegrationService
{
   @POST
   @Path("/github")
   public Response githubUpdateRepository(
            @QueryParam("repo") String repo, @FormParam("payload") String payload)
            throws Exception;
}
