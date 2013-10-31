package org.ocpsoft.redoculous.render;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;

import org.ocpsoft.common.util.Streams;

@ApplicationScoped
public class HTMLRenderer implements Renderer
{
   public HTMLRenderer()
   {
   }

   @Override
   public Iterable<String> getSupportedExtensions()
   {
      return Arrays.asList("html", "xhtml", "html5", "htm");
   }

   @Override
   public String getName()
   {
      return "markdown";
   }

   @Override
   public void render(RenderRequest request, InputStream inputStream, OutputStream outputStream)
   {
      Streams.copy(inputStream, outputStream);
   }

}
