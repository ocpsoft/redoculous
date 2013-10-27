package org.ocpsoft.redoculous.rest;

import javax.ws.rs.core.Response;

public class StatusServiceImpl implements StatusService
{
   @Override
   public Response status()
   {
      return Response.ok().build();
   }

}
