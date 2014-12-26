package org.ocpsoft.redoculous.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.ocpsoft.redoculous.rest.model.VersionResult;

@Path("/v1/serve")
public interface DocumentService
{
   @GET
   public Response serve(
            @QueryParam("ns") @DefaultValue("") String namespace,
            @QueryParam("repo") String repoName,
            @QueryParam("ref") String refName,
            @QueryParam("path") String path)
            throws Exception;
   
   @GET
   @Path("/notoc")
   public Response serveNoTableOfContents(
            @QueryParam("ns") @DefaultValue("") String namespace,
            @QueryParam("repo") String repoName,
            @QueryParam("ref") String refName,
            @QueryParam("path") String path)
            throws Exception;

   @GET
   @Path("/toc")
   @Produces({ "text/html" })
   public Response serveTableOfContents(
            @QueryParam("ns") @DefaultValue("") String namespace,
            @QueryParam("repo") String repoName,
            @QueryParam("ref") String refName,
            @QueryParam("path") String path)
            throws Exception;

   @GET
   @Path("/versions")
   @Produces({ "application/xml", "application/json" })
   public VersionResult getAvailableVersions(
            @QueryParam("ns") @DefaultValue("") String namespace,
            @QueryParam("repo") String repoName,
            @QueryParam("filter") @DefaultValue(".*") String filter)
            throws Exception;
}
