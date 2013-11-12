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
   public RepositoryStatus status(String namespace, String repo)
   {
      return rs.getStatus(namespace, repo);
   }

   @Override
   public Response init(String namespace, String repo)
   {
      rs.initRepository(namespace, repo);

      UriBuilder uri = UriBuilder.fromPath("/v1/serve").queryParam("repo", repo);
      if (namespace != null && !namespace.isEmpty())
         uri.queryParam("ns", namespace);

      return Response.created(uri.build()).build();
   }

   @Override
   public Response updateRepository(String namespace, String repo)
   {
      rs.updateRepository(namespace, repo);
      return Response.status(Status.OK).build();
   }

   @Override
   public Response purgeRepository(String namespace, String repo)
   {
      rs.purgeRepository(namespace, repo);
      return Response.status(Status.OK).build();
   }

}
