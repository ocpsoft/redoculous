package org.ocpsoft.redoculous.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.ocpsoft.redoculous.exception.NoSuchRepositoryException;

@Provider
public class NoSuchRepositoryMapper implements ExceptionMapper<NoSuchRepositoryException>
{
   @Override
   public Response toResponse(NoSuchRepositoryException e)
   {
      return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_HTML)
               .entity("No such repository: " + e.getRepositoryName()).build();
   }
}
