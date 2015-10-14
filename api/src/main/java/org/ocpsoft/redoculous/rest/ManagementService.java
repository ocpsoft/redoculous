package org.ocpsoft.redoculous.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.ocpsoft.redoculous.rest.model.RepositoryStatus;

@Path("/v1/manage")
@Produces({ "application/xml", "application/json" })
public interface ManagementService
{
   @GET
   public RepositoryStatus status(
            @QueryParam("ns") @DefaultValue("") String namespace,
            @QueryParam("repo") String repo);

   @POST
   public Response init(
            @QueryParam("ns") @DefaultValue("") String namespace,
            @QueryParam("repo") String repo);

   @PUT
   public Response updateRepository(
            @QueryParam("ns") @DefaultValue("") String namespace,
            @QueryParam("repo") String repo);

   @DELETE
   public Response purgeRepository(
            @QueryParam("ns") @DefaultValue("") String namespace,
            @QueryParam("repo") String repo);
}