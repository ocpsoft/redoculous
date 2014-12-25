package org.ocpsoft.redoculous.render;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MediaType;

import org.ocpsoft.common.util.Streams;

@ApplicationScoped
public class PNGRenderer implements Renderer
{
   public PNGRenderer()
   {}

   @Override
   public Iterable<String> getSupportedExtensions()
   {
      return Arrays.asList("png");
   }

   @Override
   public String getName()
   {
      return "PNG";
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
      return new MediaType("image", "png");
   }
}
