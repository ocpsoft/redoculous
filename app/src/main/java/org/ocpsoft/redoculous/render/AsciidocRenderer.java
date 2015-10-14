package org.ocpsoft.redoculous.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.ocpsoft.rewrite.exception.RewriteException;

@RequestScoped
public class AsciidocRenderer implements Renderer, JRubyLoadPathProvider
{
   @Inject
   private AsciidocProvider provider;

   private final Stack<RenderRequest> requests = new Stack<RenderRequest>();

   @Override
   public Iterable<String> getSupportedExtensions()
   {
      return Arrays.asList("ad", "adoc", "asc", "asciidoc");
   }

   @Override
   public String getName()
   {
      return "asciidoc";
   }

   @Override
   public void render(RenderRequest request, InputStream inputStream, OutputStream outputStream)
   {
      try
      {
         requests.push(request);
         Options options = new Options();
         options.setSafe(SafeMode.SAFE);
         provider.getAsciidoctor().render(new InputStreamReader(inputStream), new OutputStreamWriter(outputStream),
                  options);
         requests.pop();
      }
      catch (IOException e)
      {
         throw new RewriteException("Failed to render " + request);
      }
   }

   public Stack<RenderRequest> getRequests()
   {
      return requests;
   }

   @Override
   public List<String> getLoadPaths()
   {
      return Arrays.asList("gems/asciidoctor-0.1.4/lib");
   }

   @Override
   public int priority()
   {
      return Renderer.TEXT;
   }

   @Override
   public MediaType getOutputMediaType()
   {
      return MediaType.TEXT_HTML_TYPE;
   }
}
