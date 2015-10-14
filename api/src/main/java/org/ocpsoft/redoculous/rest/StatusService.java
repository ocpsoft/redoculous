package org.ocpsoft.redoculous.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/v1/status")
public interface StatusService
{
   @GET
   public Response status();
}