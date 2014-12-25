package org.ocpsoft.redoculous.render;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MediaType;

import org.ocpsoft.common.util.Streams;

@ApplicationScoped
public class ICORenderer implements Renderer
{
   public ICORenderer()
   {}

   @Override
   public Iterable<String> getSupportedExtensions()
   {
      return Arrays.asList("ico");
   }

   @Override
   public String getName()
   {
      return "ICO";
   }

   @Override
   public void render(RenderRequest request, InputStream inputStream, OutputStream outputStream)
   {
      Streams.copy(inputStream, outputStream);
   }

   @Override
   public int priority()
   {
      return Renderer.BINARY;
   }

   @Override
   public MediaType getOutputMediaType()
   {
      return new MediaType("image", "ico");
   }
}
