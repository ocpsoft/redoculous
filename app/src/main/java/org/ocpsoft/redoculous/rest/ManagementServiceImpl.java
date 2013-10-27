package org.ocpsoft.redoculous.rest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.ocpsoft.redoculous.rest.model.RepositoryStatus;
import org.ocpsoft.redoculous.service.RepositoryService;

public class ManagementServiceImpl implements ManagementService
{
   @Inject
   private RepositoryService rs;

   @Override
   public RepositoryStatus status(String repo)
   {
      return rs.getStatus(repo);
   }

   @Override
   public Response init(String repo)
   {
      rs.initRepository(repo);
      return Response.created(UriBuilder.fromPath("/v1/serve").queryParam("repo", repo).build()).build();
   }

   @Override
   public Response updateRepository(String repo) throws Exception
   {
      rs.updateRepository(repo);
      return Response.status(Status.OK).build();
   }

   @Override
   public Response purgeRepository(String repo) throws Exception
   {
      rs.purgeRepository(repo);
      return Response.status(Status.OK).build();
   }

}
